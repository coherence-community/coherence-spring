"""Regression checks for latest-snapshot resolution and per-run evidence."""

import contextlib
import io
import json
import os
from pathlib import Path
import tempfile
import unittest
import zipfile
from unittest.mock import MagicMock, patch

import coherence_artifacts as artifacts
import full_reactor


class SnapshotCiTests(unittest.TestCase):
    def test_manifest_forks_require_observed_main_class_and_reviewed_module_path(self):
        with tempfile.TemporaryDirectory(prefix='manifest-fork-') as temporary:
            source = Path(temporary).resolve() / 'source with spaces'
            jar = source / 'sample/target/surefire/surefirebooter-test.jar'
            jar.parent.mkdir(parents=True)
            command = str(jar) + ' arguments'
            inventory = {'modules': [{'path': 'sample'}], 'child_jvms': [], 'consumers': {'module': 'consumer'}}
            maven = {'pid': 1, 'properties': {'sun.java.command': 'org.codehaus.plexus.classworlds.launcher.Launcher verify',
                                            'user.dir': str(source)}}
            for main, valid in [('org.apache.maven.surefire.booter.ForkedBooter', True), ('unrelated.Main', False)]:
                with zipfile.ZipFile(jar, 'w') as archive:
                    archive.writestr('META-INF/MANIFEST.MF', 'Manifest-Version: 1.0\r\nMain-Class: ' + main + '\r\n')
                evidence = full_reactor.manifest_launcher(command)
                self.assertEqual(evidence['sha256'], artifacts.digest(jar))
                row = {'pid': 2, 'properties': {'sun.java.command': command}, 'manifest_launcher': evidence}
                issues = []
                runtime = full_reactor.audit_runtime([maven, row], inventory, source, lambda *args: None, issues)
                self.assertEqual(runtime[1]['role'], 'fork' if valid else 'unknown')
                self.assertEqual(bool(issues), not valid)
            row['manifest_launcher']['main_class'] = 'org.apache.maven.surefire.booter.ForkedBooter'
            for change in [dict(path=str(source / 'unreviewed/target/surefire/surefirebooter-test.jar')),
                           dict(sha256=''), dict(main_class=None)]:
                with self.subTest(change=change):
                    bad = dict(row, manifest_launcher=dict(row['manifest_launcher'], **change))
                    if 'path' in change:
                        bad['properties'] = {'sun.java.command': change['path'] + ' arguments'}
                    issues = []
                    full_reactor.audit_runtime([maven, bad], inventory, source, lambda *args: None, issues)
                    self.assertTrue(issues)
            jar.unlink()
            self.assertIsNone(full_reactor.manifest_launcher(command))

    def invoke(self, lane, java, extra=()):
        temporary = tempfile.TemporaryDirectory(prefix='snapshot-ci-test-')
        self.addCleanup(temporary.cleanup)
        output = Path(temporary.name) / 'output'
        process = MagicMock(pid=123, stdout=iter(()))
        process.wait.return_value = 0
        with patch.dict(os.environ, {'JAVA_HOME': '/test/jdk', 'JAVA_TOOL_OPTIONS': '-Dcoherence.security.mode=compatibility'}), \
                patch('sys.argv', ['full_reactor.py', '--lane', lane, '--output', str(output), *extra]), \
                patch.object(full_reactor.subprocess, 'check_output', return_value=f'java.specification.version = {java}\n'), \
                patch.object(full_reactor.subprocess, 'Popen', return_value=process) as launch, \
                patch.object(full_reactor.threading, 'Thread'), \
                patch.object(full_reactor, 'audit', return_value=False):
            self.assertEqual(full_reactor.main(), 0)
            self.invocation = json.loads((output / 'invocation.json').read_text())
            return launch.call_args


    def test_sample_children_require_exact_command_cluster_prefix_and_counts(self):
        role = {'role': 'sample-cache-app', 'command': 'sample.Probe app', 'cluster': None,
                'cluster_prefix': 'SampleCacheIT-', 'count': 1}
        inventory = {'child_jvms': [role], 'consumers': {'module': 'consumer'}}
        maven = {'pid': 1, 'properties': {'sun.java.command': 'org.codehaus.plexus.classworlds.launcher.Launcher verify',
                                        'user.dir': '/source'}}
        child = {'pid': 2, 'properties': {'sun.java.command': 'sample.Probe app production',
                                        'coherence.cluster': 'SampleCacheIT-unique'}}
        issues = []
        observed = []
        full_reactor.audit_runtime([maven, child], inventory, Path('/source'),
                                   lambda props, origin: observed.append(origin), issues)
        self.assertEqual(issues, [])
        self.assertEqual(observed, ['maven:1', 'sample-cache-app:2'])
        for command, cluster in [('sample.Probe appOther', 'SampleCacheIT-unique'),
                                 ('sample.Probe app', 'SampleCacheITOther-unique'),
                                 ('sample.Probe app', ''), ('sample.Probe app', None)]:
            with self.subTest(command=command, cluster=cluster):
                bad = {'pid': 2, 'properties': {'sun.java.command': command, 'coherence.cluster': cluster}}
                issues = []
                full_reactor.audit_runtime([maven, bad], inventory, Path('/source'), lambda *args: None, issues)
                self.assertTrue(any('Unclassified Java process' in issue for issue in issues))
                self.assertTrue(any('every inventoried child JVM' in issue for issue in issues))
        issues = []
        full_reactor.audit_runtime([maven], inventory, Path('/source'), lambda *args: None, issues)
        self.assertTrue(any('every inventoried child JVM' in issue for issue in issues))

    def test_snapshot_refreshes_and_propagates_hardening(self):
        call = self.invoke('snapshot', '21')
        command, env = call.args[0], call.kwargs['env']
        self.assertIn('-U', command)
        self.assertNotIn('-nsu', command)
        self.assertIn('-Pjdk17', command)
        self.assertIn('-Dcoherence.version=26.10-SNAPSHOT', command)
        self.assertIn('-Dmaven.test.failure.ignore=false', command)
        self.assertEqual(env['JAVA_TOOL_OPTIONS'], '-Dcoherence.security.mode=hardened')
        self.assertEqual(command[-2:], ['clean', 'verify'])
        self.assertEqual(self.invocation['qualification_scope'], 'standard')

    def test_baseline_retains_release_and_unset_mode(self):
        call = self.invoke('baseline', '17')
        self.assertIn('-Dcoherence.version=26.04', call.args[0])
        self.assertIn('-Pjdk17', call.args[0])
        self.assertNotIn('-U', call.args[0])
        self.assertNotIn('JAVA_TOOL_OPTIONS', call.kwargs['env'])

    def test_requested_snapshot_also_refreshes(self):
        call = self.invoke('requested', '21', ('--version', '26.11-SNAPSHOT'))
        self.assertIn('-U', call.args[0])
        self.assertIn('-Dcoherence.version=26.11-SNAPSHOT', call.args[0])

    def test_jdk21_default_baseline_retains_release_unset_mode_and_fatal_tests(self):
        call = self.invoke('baseline21', '21')
        command, env = call.args[0], call.kwargs['env']
        self.assertIn('-Dcoherence.version=26.04', command)
        self.assertIn('-Pjdk17', command)
        self.assertIn('-Dmaven.test.failure.ignore=false', command)
        self.assertNotIn('-U', command)
        self.assertNotIn('-Dcoherence.security.mode=hardened', command)
        self.assertNotIn('JAVA_TOOL_OPTIONS', env)
        self.assertEqual(command[-2:], ['clean', 'verify'])

    def test_default_baselines_reject_wrong_jdk_and_version_override(self):
        for lane, java, extra in [('baseline', '21', ()), ('baseline21', '17', ()),
                                  ('baseline21', '21', ('--version', '26.10-SNAPSHOT'))]:
            with self.subTest(lane=lane, java=java, extra=extra), \
                    contextlib.redirect_stderr(io.StringIO()), self.assertRaises(SystemExit) as failure:
                self.invoke(lane, java, extra)
            self.assertEqual(failure.exception.code, 2)

    def test_snapshot_cannot_claim_latest_offline(self):
        with contextlib.redirect_stderr(io.StringIO()), self.assertRaises(SystemExit) as failure:
            self.invoke('snapshot', '21', ('--offline',))
        self.assertEqual(failure.exception.code, 2)

    def test_frozen_local_snapshot_is_explicit_offline_and_propagates_provider(self):
        call = self.invoke('snapshot', '21', ('--local-frozen', '--offline',
                           '--repository', '/task/repository', '--hibernate-version', '4.1.0-SNAPSHOT'))
        command, env = call.args[0], call.kwargs['env']
        self.assertIn('-o', command)
        self.assertNotIn('-U', command)
        self.assertIn('-Dcoherence-hibernate.version=4.1.0-SNAPSHOT', command)
        self.assertIn('-Dspring.cloud.config.enabled=false', command)
        self.assertEqual(env['MAVEN_ARGS'], '-o -Dcoherence-hibernate.version=4.1.0-SNAPSHOT')
        self.assertEqual(self.invocation['qualification_scope'], 'local-frozen-offline')
        self.assertEqual(self.invocation['hibernate_version'], '4.1.0-SNAPSHOT')
        self.assertEqual(env['JAVA_TOOL_OPTIONS'], '-Dcoherence.security.mode=hardened')
        self.assertIn('-Pjdk17', command)

    def test_local_frozen_requires_isolation_and_offline_and_safe_provider(self):
        for extra in [('--local-frozen',), ('--local-frozen', '--offline'),
                      ('--hibernate-version', '4.1.0-SNAPSHOT'),
                      ('--local-frozen', '--offline', '--repository', '/task/repo',
                       '--hibernate-version', '../other')]:
            with self.subTest(extra=extra), contextlib.redirect_stderr(io.StringIO()), self.assertRaises(SystemExit):
                self.invoke('snapshot', '21', extra)

    def test_offline_baseline_propagates_to_independent_maven_launchers(self):
        call = self.invoke('baseline', '17', ('--offline',))
        self.assertEqual(call.kwargs['env']['MAVEN_ARGS'], '-o')
        self.assertNotIn('JAVA_TOOL_OPTIONS', call.kwargs['env'])

    def test_records_new_snapshot_bytes_without_historical_pin(self):
        with tempfile.TemporaryDirectory(prefix='snapshot-ci-test-') as temporary:
            repository = Path(temporary)
            folder = repository / 'com/oracle/coherence/ce/coherence/26.10-SNAPSHOT'
            folder.mkdir(parents=True)
            jar = folder / 'coherence-26.10-SNAPSHOT.jar'
            jar.write_bytes(b'newly published snapshot')
            jar.with_suffix('.pom').write_text('<project/>')
            timestamped = folder / 'coherence-26.10-20261016.170000-8.jar'
            timestamped.write_bytes(jar.read_bytes())
            row, = artifacts.cache_files(repository, '26.10-SNAPSHOT')
            self.assertEqual(row['sha256'], artifacts.digest(jar))
            self.assertEqual(row['matching_timestamped_files'], [timestamped.name])
            jar.write_bytes(b'next published snapshot')
            newer, = artifacts.cache_files(repository, '26.10-SNAPSHOT')
            self.assertNotEqual(newer['sha256'], row['sha256'])
            self.assertEqual(newer['matching_timestamped_files'], [])

    def test_mixed_release_dependency_is_rejected(self):
        with tempfile.TemporaryDirectory(prefix='snapshot-ci-test-') as temporary:
            source = Path(temporary)
            output = source / 'evidence'
            output.mkdir()
            (output / 'maven.log').write_text('')
            repository = source / 'repository'
            jar = repository / 'com/oracle/coherence/ce/coherence/26.04/coherence-26.04.jar'
            jar.parent.mkdir(parents=True)
            jar.write_bytes(b'older Coherence release')
            jar.with_suffix('.pom').write_text('<project/>')
            (output / 'jvms.json').write_text(json.dumps([{'pid': 123, 'properties': {
                'java.version': '21', 'coherence.security.mode': 'hardened', 'java.class.path': str(jar)}}]))
            # Other whole-reactor requirements are deliberately absent in this unit fixture.
            with contextlib.redirect_stdout(io.StringIO()):
                self.assertTrue(full_reactor.audit(source, output, 'snapshot', '26.10-SNAPSHOT',
                                                   '21', 'hardened', repository, 0))
            issues = json.loads((output / 'qualification.json').read_text())['issues']
            self.assertTrue(any('mixed Coherence version:' in issue for issue in issues), issues)

    def test_packaged_bytes_must_match_current_run(self):
        with tempfile.TemporaryDirectory(prefix='snapshot-ci-test-') as temporary:
            source = Path(temporary)
            output = source / 'evidence'
            output.mkdir()
            (output / 'maven.log').write_text('')
            repository = source / 'repository'
            jar = repository / 'com/oracle/coherence/ce/coherence/26.10-SNAPSHOT/coherence-26.10-SNAPSHOT.jar'
            jar.parent.mkdir(parents=True)
            jar.write_bytes(b'new published artifact')
            jar.with_suffix('.pom').write_text('<project/>')
            package = source / 'app/target/app.jar'
            package.parent.mkdir(parents=True)
            for payload, should_reject in [(jar.read_bytes(), False), (b'different older artifact', True)]:
                with zipfile.ZipFile(package, 'w') as archive:
                    archive.writestr('BOOT-INF/lib/coherence-26.10-SNAPSHOT.jar', payload)
                with contextlib.redirect_stdout(io.StringIO()):
                    full_reactor.audit(source, output, 'snapshot', '26.10-SNAPSHOT',
                                       '21', 'hardened', repository, 0)
                issues = json.loads((output / 'qualification.json').read_text())['issues']
                self.assertEqual(any('Packaged Coherence differs' in issue for issue in issues), should_reject)


if __name__ == '__main__':
    unittest.main()
