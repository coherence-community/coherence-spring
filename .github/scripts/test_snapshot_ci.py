"""Regression checks for latest-snapshot resolution and per-run evidence."""

import contextlib
import io
import json
import os
from pathlib import Path
import subprocess
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
        with patch.dict(os.environ, {'JAVA_HOME': '/test/jdk', 'JAVA_TOOL_OPTIONS': '-Dcoherence.security.mode=compatibility',
                                    'FULL_REACTOR_TRUSTED_MAVEN': ''}), \
                patch('sys.argv', ['full_reactor.py', '--lane', lane, '--output', str(output), *extra]), \
                patch.object(full_reactor.subprocess, 'check_output', return_value=f'java.specification.version = {java}\n'), \
                patch.object(full_reactor.subprocess, 'Popen', return_value=process) as launch, \
                patch.object(full_reactor.threading, 'Thread'), \
                patch.object(full_reactor, 'audit', return_value=False) as audit:
            self.assertEqual(full_reactor.main(), 0)
            self.invocation = json.loads((output / 'invocation.json').read_text())
            self.assertEqual(audit.call_args.kwargs['expected_home'], Path('/test/jdk').resolve())
            self.assertEqual(launch.call_args.args[0][0], str((artifacts.ROOT / 'mvnw').resolve()))
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
                           '--repository', '/task/repository', '--hibernate-version', '4.1.0'))
        command, env = call.args[0], call.kwargs['env']
        self.assertIn('-o', command)
        self.assertNotIn('-U', command)
        self.assertIn('-Dcoherence-hibernate.version=4.1.0', command)
        self.assertIn('-Dspring.cloud.config.enabled=false', command)
        self.assertEqual(env['MAVEN_ARGS'], '-o -Dcoherence-hibernate.version=4.1.0')
        self.assertEqual(self.invocation['qualification_scope'], 'local-frozen-offline')
        self.assertEqual(self.invocation['hibernate_version'], '4.1.0')
        self.assertEqual(env['JAVA_TOOL_OPTIONS'], '-Dcoherence.security.mode=hardened')
        self.assertIn('-Pjdk17', command)

    def test_local_frozen_requires_isolation_and_offline_and_safe_provider(self):
        for extra in [('--local-frozen',), ('--local-frozen', '--offline'),
                      ('--hibernate-version', '4.1.0'),
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
                                                   '21', 'hardened', repository, 0,
                                                   expected_home=Path('/test/jdk')))
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
                                       '21', 'hardened', repository, 0, expected_home=Path('/test/jdk'))
                issues = json.loads((output / 'qualification.json').read_text())['issues']
                self.assertEqual(any('Packaged Coherence differs' in issue for issue in issues), should_reject)


class SecurityBoundaryTests(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory(prefix='reactor-security-')
        self.addCleanup(temporary.cleanup)
        self.source = Path(temporary.name).resolve() / 'source with spaces'
        self.source.mkdir()
        self.wrapper = self.executable(self.source / 'mvnw')
        self.home = self.source / 'jdk with spaces'
        self.counter = 0

    def executable(self, path):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text('#!/bin/sh\nexit 0\n')
        path.chmod(0o700)
        return path

    def invoke(self, maven=None, trusted='', output=None, extra=()):
        self.counter += 1
        self.output = output if output is not None else self.source / f'evidence {self.counter}'
        argv = ['full_reactor.py', '--lane', 'baseline', '--output', str(self.output), *extra]
        if maven is not None:
            argv += ['--maven', str(maven)]
        process = MagicMock(pid=123, stdout=iter(()))
        process.wait.return_value = 0

        def launch(*args, **kwargs):
            self.initial_manifest = json.loads((self.output / 'invocation.json').read_text())
            return process

        with patch.object(full_reactor, 'ROOT', self.source), \
                patch.dict(os.environ, {'JAVA_HOME': str(self.home), 'FULL_REACTOR_TRUSTED_MAVEN': trusted}), \
                patch('sys.argv', argv), \
                patch.object(full_reactor.subprocess, 'check_output',
                             return_value='java.specification.version = 17\n') as probe, \
                patch.object(full_reactor.subprocess, 'Popen', side_effect=launch) as popen, \
                patch.object(full_reactor.threading, 'Thread'), \
                patch.object(full_reactor, 'audit', return_value=False) as audit:
            self.probe, self.launch, self.audit = probe, popen, audit
            result = full_reactor.main()
        self.final_manifest = json.loads((self.output / 'invocation.json').read_text())
        return result

    def assert_rejected_before_subprocess(self, **kwargs):
        with contextlib.redirect_stderr(io.StringIO()), self.assertRaises(SystemExit) as failure:
            self.invoke(**kwargs)
        self.assertEqual(failure.exception.code, 2)
        self.probe.assert_not_called()
        self.launch.assert_not_called()
        self.audit.assert_not_called()

    def test_wrapper_is_anchored_to_checkout_and_passes_live_home(self):
        # The test process is in the real checkout, not this disposable source.
        self.assertEqual(self.invoke(), 0)
        self.assertEqual(self.launch.call_args.args[0][0], str(self.wrapper))
        self.assertEqual(self.launch.call_args.kwargs['cwd'], self.source)
        self.assertEqual(self.audit.call_args.kwargs['expected_home'], self.home)
        self.assertEqual(self.initial_manifest['java_home'], str(self.home))
        self.assertEqual(self.final_manifest['java_home'], str(self.home))
        self.assertNotIn('maven_exit', self.initial_manifest)
        self.assertEqual(self.final_manifest['maven_exit'], 0)

    def test_only_operator_configured_cached_launcher_is_allowed_and_recorded_exactly(self):
        cached = self.executable(self.source / 'cached Maven;$(echo evidence) "quoted"/bin/mvn')
        self.assert_rejected_before_subprocess(maven=cached)
        self.assertEqual(self.invoke(maven=cached, trusted=str(cached),
                                    extra=('--offline', '--repository', str(self.source / 'local cache'))), 0)
        command = self.launch.call_args.args[0]
        self.assertEqual(command[0], str(cached))
        self.assertIn('-o', command)
        self.assertIn('-Pjdk17', command)
        self.assertEqual(self.launch.call_args.kwargs['env']['MAVEN_ARGS'], '-o')
        self.assertEqual(self.initial_manifest['command'], command)
        self.assertEqual(self.final_manifest['command'], command)
        self.assertEqual({p.name for p in self.output.iterdir()},
                         {'invocation.json', 'reviewed-inventory.json', 'maven.log'})

    def test_arbitrary_basename_option_and_shell_looking_launchers_are_rejected(self):
        attacker = self.executable(self.source / 'untrusted/bin/mvn')
        for maven in (attacker, '/bin/sh', 'mvn', '-version', './mvnw; echo injected',
                      '../outside/mvnw', './mvnw --offline'):
            with self.subTest(maven=maven):
                self.assert_rejected_before_subprocess(maven=maven)
        self.assertFalse(any(self.source.glob('evidence *')))

    def test_canonical_alias_is_allowed_but_symlink_to_other_executable_is_not(self):
        alias = self.source / 'wrapper alias'
        alias.symlink_to(self.wrapper)
        self.assertEqual(self.invoke(maven=alias), 0)
        self.assertEqual(self.launch.call_args.args[0][0], str(self.wrapper))
        alias.unlink()
        alias.symlink_to(self.executable(self.source / 'other/bin/mvnw'))
        self.assert_rejected_before_subprocess(maven=alias)

    def test_trusted_launcher_must_be_absolute_configured_and_executable(self):
        not_executable = self.source / 'cached/bin/mvn'
        not_executable.parent.mkdir(parents=True)
        not_executable.write_text('not executable')
        directory = self.source / 'directory/mvn'
        directory.mkdir(parents=True)
        for maven, trusted in ((not_executable, str(not_executable)), (directory, str(directory)),
                               (self.source / 'missing/mvn', str(self.source / 'missing/mvn')),
                               (not_executable, 'cached/bin/mvn')):
            with self.subTest(maven=maven, trusted=trusted):
                self.assert_rejected_before_subprocess(maven=maven, trusted=trusted)

    def test_output_accepts_empty_directory_or_preserves_prewritten_ci_sha(self):
        for prewritten in (False, True):
            with self.subTest(prewritten=prewritten):
                output = self.source / ('CI evidence' if prewritten else 'empty evidence')
                output.mkdir()
                sha = output / 'source-sha.txt'
                content = b'0123456789abcdef\n'
                if prewritten:
                    sha.write_bytes(content)
                self.assertEqual(self.invoke(output=output), 0)
                if prewritten:
                    self.assertEqual(sha.read_bytes(), content)

    def test_output_rejects_existing_files_directories_and_symlinks_without_overwriting(self):
        outside = self.source / 'outside.txt'
        outside.write_text('keep this evidence')
        for name in ('invocation.json', 'qualification.json', 'reactor-summary.txt', 'maven.log',
                     'jvms.json', 'reviewed-inventory.json', 'resolved-coherence.json',
                     'observer-error.txt', 'unexpected.txt', 'source-sha.txt'):
            for kind in ('file', 'directory', 'symlink', 'dangling'):
                if name == 'source-sha.txt' and kind == 'file':
                    continue
                with self.subTest(name=name, kind=kind):
                    output = self.source / f'unsafe {name} {kind}'
                    output.mkdir()
                    entry = output / name
                    if kind == 'file':
                        entry.write_text('keep original')
                    elif kind == 'directory':
                        entry.mkdir()
                    else:
                        entry.symlink_to(outside if kind == 'symlink' else self.source / 'absent.txt')
                    self.assert_rejected_before_subprocess(output=output)
                    self.assertEqual(outside.read_text(), 'keep this evidence')
                    self.assertEqual(list(output.iterdir()), [entry])
                    if kind == 'file':
                        self.assertEqual(entry.read_text(), 'keep original')
                    elif kind in ('symlink', 'dangling'):
                        self.assertTrue(entry.is_symlink())
        self.assertFalse((self.source / 'absent.txt').exists())

    def test_output_root_symlink_rejected_but_trusted_parent_alias_supported(self):
        target = self.source / 'actual output'
        target.mkdir()
        linked = self.source / 'linked output'
        linked.symlink_to(target, target_is_directory=True)
        self.assert_rejected_before_subprocess(output=linked)
        self.assertEqual(list(target.iterdir()), [])
        parent = self.source / 'parent alias'
        parent.symlink_to(self.source, target_is_directory=True)
        output = parent / 'fresh evidence'
        self.assertEqual(self.invoke(output=output), 0)
        self.assertEqual(self.audit.call_args.args[1], output.resolve())


class ObserverTests(unittest.TestCase):
    def test_descendants_retry_attach_and_preserve_exact_properties(self):
        with tempfile.TemporaryDirectory(prefix='observer-test-') as temporary:
            output = Path(temporary) / 'jvms.json'
            listing = ('100 1 /jdk/bin/java Maven\n101 100 /jdk/bin/java child\n'
                       '102 100 /usr/bin/python helper\n103 102 /jdk/bin/java grandchild\n'
                       '104 1 /jdk/bin/java unrelated\n')
            stop = MagicMock()
            stop.is_set.side_effect = [False, False, True]
            env = {'JAVA_HOME': '/jdk'}
            responses = [listing, 'java.version=21\nsun.java.command=first=exact\nignored=secret\n',
                         subprocess.CalledProcessError(1, 'jcmd'), 'not.ready=true\n',
                         listing, 'java.version=old\njava.version=21\nconsumer.fixture=sample\n',
                         'java.version=21\ncoherence.cluster=cluster with spaces\n']
            with patch.object(full_reactor.subprocess, 'check_output', side_effect=responses) as probe:
                full_reactor.observe(100, stop, output, env)
            rows = json.loads(output.read_text())
            self.assertEqual(rows, [
                {'pid': 100, 'ppid': 1, 'properties': {'java.version': '21', 'sun.java.command': 'first=exact'},
                 'manifest_launcher': None},
                {'pid': 101, 'ppid': 100, 'properties': {'java.version': '21', 'consumer.fixture': 'sample'},
                 'manifest_launcher': None},
                {'pid': 103, 'ppid': 102, 'properties': {'java.version': '21', 'coherence.cluster': 'cluster with spaces'},
                 'manifest_launcher': None}])
            attached = [call.args[0][1] for call in probe.call_args_list if call.args[0][0] == '/jdk/bin/jcmd']
            self.assertEqual(attached, ['100', '101', '103', '101', '103'])
            self.assertEqual(stop.wait.call_count, 2)
            self.assertFalse((output.parent / 'observer-error.txt').exists())

    def test_listing_failures_record_exact_diagnostic_and_keep_polling(self):
        with tempfile.TemporaryDirectory(prefix='observer-test-') as temporary:
            output = Path(temporary) / 'jvms.json'
            stop = MagicMock()
            stop.is_set.side_effect = [False, False, False, True]
            with patch.object(full_reactor.subprocess, 'check_output',
                              side_effect=['invalid 1 /jdk/bin/java\n', OSError('ps unavailable'), '']):
                full_reactor.observe(100, stop, output, {'JAVA_HOME': '/jdk'})
            self.assertEqual((output.parent / 'observer-error.txt').read_text(), 'ps unavailable')
            self.assertFalse(output.exists())
            self.assertEqual(stop.wait.call_count, 3)


class RunnerFailureTests(unittest.TestCase):
    def test_failed_maven_stream_is_retained_and_audit_failure_propagates(self):
        with tempfile.TemporaryDirectory(prefix='runner-test-') as temporary:
            source = Path(temporary)
            wrapper = source / 'mvnw'
            wrapper.write_text('#!/bin/sh\nexit 7\n')
            wrapper.chmod(0o700)
            output = source / 'evidence'
            process = MagicMock(pid=123, stdout=iter(['first line\n', 'failure details\n']))
            process.wait.return_value = 7
            argv = ['full_reactor.py', '--lane', 'baseline', '--output', str(output)]
            with patch.object(full_reactor, 'ROOT', source), \
                    patch.dict(os.environ, {'JAVA_HOME': '/test/jdk'}), patch('sys.argv', argv), \
                    patch.object(full_reactor.subprocess, 'check_output', return_value='java.specification.version = 17\n'), \
                    patch.object(full_reactor.subprocess, 'Popen', return_value=process), \
                    patch.object(full_reactor.threading, 'Thread') as watcher, \
                    patch.object(full_reactor, 'audit', return_value=True) as audit, \
                    contextlib.redirect_stdout(io.StringIO()) as stdout:
                self.assertEqual(full_reactor.main(), 1)
            self.assertEqual(stdout.getvalue(), 'first line\nfailure details\n')
            self.assertEqual((output / 'maven.log').read_text(), stdout.getvalue())
            self.assertEqual(json.loads((output / 'invocation.json').read_text())['maven_exit'], 7)
            self.assertEqual(audit.call_args.args[-1], 7)
            self.assertEqual(audit.call_args.kwargs['expected_home'], Path('/test/jdk').resolve())
            watcher.return_value.start.assert_called_once()
            watcher.return_value.join.assert_called_once_with(timeout=15)


if __name__ == '__main__':
    unittest.main()
