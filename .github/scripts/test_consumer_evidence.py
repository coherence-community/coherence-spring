"""Fail-closed collector controls using disposable packaged evidence, not mocks of outcomes."""

import contextlib
import io
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as ET
import zipfile

from coherence_artifacts import cache_files, digest
from consumer_evidence import audit_consumers, load_properties
import full_reactor


class ConsumerEvidenceTests(unittest.TestCase):
    def setUp(self):
        tmp = tempfile.TemporaryDirectory(prefix='consumer-ci-')
        self.addCleanup(tmp.cleanup)
        self.source = Path(tmp.name)
        self.module = self.source / 'coherence-spring-session-boot-starter'
        self.target = self.module / 'target'
        self.build = self.target / 'it/general-starter/target'
        self.home = Path('/test/jdk')
        self.repository = self.source / 'repository'
        self.inventory = {'module': self.module.name,
                          'inventory': self.module.name + '/src/it/inventory.properties'}
        self.write(self.source / 'pom.xml', '''<project xmlns="http://maven.apache.org/POM/4.0.0">
          <groupId>com.oracle.coherence.spring</groupId><artifactId>parent</artifactId><version>5-SNAPSHOT</version>
          <name>Parent</name><properties><spring-boot.version>4.0.8</spring-boot.version></properties>
          <modules><module>coherence-spring-session-boot-starter</module></modules></project>''')
        self.write(self.module / 'pom.xml', '''<project xmlns="http://maven.apache.org/POM/4.0.0">
          <artifactId>coherence-spring-session-boot-starter</artifactId><name>Session</name></project>''')
        canonical = 'general-starter=generalCache\n'
        self.write(self.module / 'src/it/inventory.properties', canonical)
        self.write(self.module / 'src/it/general-starter/pom.xml', '<project/>')
        self.write(self.target / 'consumer-evidence/inventory.properties', canonical)
        self.props(self.target / 'consumer-evidence/summary.properties',
                   dict(result='PASS', fixtures='1', tests='1', failures='0', errors='0', skips='0'))
        self.jar = self.repository / 'com/oracle/coherence/ce/coherence/26.10-SNAPSHOT/coherence-26.10-SNAPSHOT.jar'
        self.archive(self.jar)
        self.write(self.jar.with_suffix('.pom'), '<project/>')
        product = self.source / 'product.jar'
        self.archive(product)
        self.write(product.with_suffix('.pom'), '<project/>')
        outer = {}
        self.keys = ['com.oracle.coherence.ce:coherence:26.10-SNAPSHOT',
                     'com.oracle.coherence.spring:coherence-spring-boot-starter:5-SNAPSHOT']
        all_keys = self.keys + ['com.oracle.coherence.spring:coherence-spring-session-boot-starter:5-SNAPSHOT']
        installed = []
        for key in all_keys:
            group, artifact, version = key.split(':')
            original = self.jar if group == 'com.oracle.coherence.ce' else product
            path = self.target / 'it-repository' / group.replace('.', '/') / artifact / version / f'{artifact}-{version}.jar'
            for ext in ('jar', 'pom'):
                actual = original.with_suffix('.' + ext)
                self.write(path.with_suffix('.' + ext), actual.read_bytes())
                outer[key + '.' + ext + '.source'] = str(actual)
                outer[key + '.' + ext + '.sha256'] = digest(actual)
            if key in self.keys:
                installed.append(path)
        self.installed = installed
        self.props(self.target / 'consumer-evidence/outer-artifacts.properties', outer)
        self.write(self.target / 'consumer-evidence/outer-dependencies.txt',
                   '\n'.join(k.replace(':26.', ':jar:26.').replace(':5-', ':jar:5-') + ':compile:' + str(p)
                             for k, p in zip(self.keys, [self.jar, product])))
        self.write(self.build / 'test-dependencies.txt',
                   '\n'.join(k.replace(':26.', ':jar:26.').replace(':5-', ':jar:5-') + ':compile:' + str(p)
                             for k, p in zip(self.keys, installed)))
        self.write(self.build / 'runtime-dependencies.txt', '\n'.join(
            k.replace(':26.', ':jar:26.').replace(':5-', ':jar:5-') + ':compile' for k in self.keys))
        self.write(self.build / 'runtime-classpath.txt', os.pathsep.join(map(str, installed)))
        self.write(self.build.parent / 'build.log', '[INFO] BUILD SUCCESS\n')
        self.write(self.target / 'invoker-reports/BUILD-general-starter.xml', '<build-job result="success" project="general-starter/pom.xml"/>')
        self.report(self.target / 'invoker-reports/TEST-general-starter.xml', 'maven.invoker.it.general-starter', 'general-starter', {})
        self.report_path = self.build / 'failsafe-reports/TEST-consumer.ConsumerIT.xml'
        self.jvm = {'java.version': '21.0.1', 'java.specification.version': '21', 'java.home': str(self.home),
                    'java.class.path': os.pathsep.join(map(str, installed)), 'coherence.security.mode': 'hardened',
                    'maven.test.failure.ignore': 'false'}
        self.report(self.report_path, 'consumer.ConsumerIT', 'generalCache', self.jvm)
        self.write(self.build / 'failsafe-reports/failsafe-summary.xml',
                   '<failsafe-summary timeout="false"><completed>1</completed><errors>0</errors><failures>0</failures><skipped>0</skipped></failsafe-summary>')
        self.evidence_path = self.build / 'consumer-evidence/generalCache.properties'
        evidence = dict(self.jvm, result='PASS', scenario='generalCache')
        evidence.update({'consumer.fixture': 'general-starter', 'consumer.release': '17',
                         'consumer.coherence.group': 'com.oracle.coherence.ce',
                         'consumer.coherence.version': '26.10-SNAPSHOT', 'consumer.boot.version': '4.0.8',
                         'consumer.security.mode': 'hardened', 'loaded.coherence.jar': str(installed[0])})
        evidence.update({k: v for k, v in outer.items() if k.endswith('sha256') and any(k.startswith(key + '.') for key in self.keys)})
        self.props(self.evidence_path, evidence)

    def write(self, path, value):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(value if isinstance(value, bytes) else value.encode())

    def archive(self, path):
        path.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(path, 'w') as jar:
            jar.writestr('example/Class.class', b'\xca\xfe\xba\xbe\0\0\0=')

    def props(self, path, values):
        self.write(path, '\n'.join(k.replace(':', '\\:') + '=' + str(v).replace('\\', '\\\\') for k, v in values.items()))

    def report(self, path, classname, name, props):
        suite = ET.Element('testsuite', name=classname, tests='1', failures='0', errors='0', skipped='0')
        ps = ET.SubElement(suite, 'properties')
        for k, v in props.items():
            ET.SubElement(ps, 'property', name=k, value=v)
        ET.SubElement(suite, 'testcase', classname=classname, name=name)
        self.write(path, ET.tostring(suite))

    def collect(self):
        expected = {r['artifact']: r for r in cache_files(self.repository, '26.10-SNAPSHOT')}
        return audit_consumers(self.source, self.inventory, expected, '21', self.home, 'hardened', lambda *_: None)

    def test_complete_independent_contract_passes(self):
        self.assertEqual(self.collect()['issues'], [])

    def test_classified_native_dependency_identities_are_distinct(self):
        path = self.build / 'test-dependencies.txt'
        for classifier in ('linux-x86_64', 'osx-aarch_64'):
            jar = self.target / 'it-repository/io/netty/native/1' / f'native-1-{classifier}.jar'
            self.archive(jar)
            with path.open('a') as out:
                out.write(f'\nio.netty:native:jar:{classifier}:1:runtime:{jar} -- module example\n')
        self.assertEqual(self.collect()['issues'], [])

    def test_missing_report_and_evidence_are_fatal(self):
        for path in (self.report_path, self.evidence_path, self.target / 'consumer-evidence/outer-artifacts.properties'):
            with self.subTest(path=path):
                content = path.read_bytes(); path.unlink()
                self.assertTrue(self.collect()['issues'])
                path.write_bytes(content)

    def test_failed_skipped_and_duplicate_consumers_are_fatal(self):
        original = self.report_path.read_bytes()
        for kind in ('failure', 'error', 'skipped', 'duplicate'):
            with self.subTest(kind=kind):
                suite = ET.fromstring(original)
                if kind == 'duplicate':
                    ET.SubElement(suite, 'testcase', classname='consumer.ConsumerIT', name='generalCache')
                    suite.set('tests', '2')
                else:
                    ET.SubElement(suite.find('testcase'), kind)
                    suite.set({'failure': 'failures', 'error': 'errors', 'skipped': 'skipped'}[kind], '1')
                self.report_path.write_bytes(ET.tostring(suite))
                self.assertTrue(self.collect()['issues'])
        self.report_path.write_bytes(original)

    def test_extra_case_or_report_is_fatal(self):
        suite = ET.parse(self.report_path).getroot()
        suite.find('testcase').set('name', 'unexpected')
        self.report_path.write_bytes(ET.tostring(suite))
        self.assertTrue(any('exact consumer cases' in i for i in self.collect()['issues']))

    def test_wrong_jdk_mode_release_and_versions_are_fatal(self):
        original = load_properties(self.evidence_path)
        for key, value in [('java.version', '17'), ('java.specification.version', '17'),
                           ('java.home', '/other/jdk'), ('coherence.security.mode', '<absent>'),
                           ('consumer.security.mode', 'unset'), ('consumer.release', '21'),
                           ('consumer.coherence.version', '26.04'), ('consumer.boot.version', '4.0.0')]:
            with self.subTest(key=key):
                self.props(self.evidence_path, dict(original, **{key: value}))
                self.assertTrue(self.collect()['issues'])
        self.props(self.evidence_path, original)

    def test_unexpected_dependencies_and_missing_boundary_are_fatal(self):
        path = self.build / 'runtime-dependencies.txt'
        original = path.read_text()
        for dependency in ('mockito', 'bedrock', 'org.springframework.session:spring-session-core:jar:4',
                           'spring-boot-session:jar:4'):
            path.write_text(original + '\n' + dependency)
            self.assertTrue(self.collect()['issues'])
        path.write_text(original)

    def test_snapshot_jar_pom_loaded_and_outer_drift_are_fatal(self):
        for path in [self.installed[0], self.installed[0].with_suffix('.pom'), self.jar, self.jar.with_suffix('.pom')]:
            with self.subTest(path=path):
                original = path.read_bytes(); path.write_bytes(b'drift')
                self.assertTrue(self.collect()['issues'])
                path.write_bytes(original)

    def test_non_release17_product_bytecode_is_fatal(self):
        path = self.installed[1]
        with zipfile.ZipFile(path, 'w') as jar:
            jar.writestr('Class.class', b'\xca\xfe\xba\xbe\0\0\0A')
        self.assertTrue(any('release-17' in i for i in self.collect()['issues']))

    def test_nested_fatal_failure_evidence_is_required(self):
        suite = ET.parse(self.report_path).getroot()
        for p in suite.findall('properties/property'):
            if p.get('name') == 'maven.test.failure.ignore':
                p.set('value', 'true')
        self.report_path.write_bytes(ET.tostring(suite))
        self.assertTrue(any('fatal failures' in i for i in self.collect()['issues']))

    def test_skipped_summary_and_failed_invoker_are_fatal(self):
        self.props(self.target / 'consumer-evidence/summary.properties', {'result': 'SKIPPED'})
        self.write(self.target / 'invoker-reports/BUILD-general-starter.xml', '<build-job result="failure"/>')
        self.assertTrue(any('summary' in i for i in self.collect()['issues']))
        self.assertTrue(any('Invoker failed' in i for i in self.collect()['issues']))

    def test_inventory_and_duplicate_report_hazards(self):
        inv = {'modules': [{'path': '.', 'artifact': 'parent', 'name': 'Parent', 'suites': [
            {'report': 'surefire-reports', 'name': 'unit.Example', 'cases': ['unit.Example#works']} ]},
            {'path': self.module.name, 'artifact': self.module.name, 'name': 'Session', 'suites': []}],
            'consumers': self.inventory, 'child_jvms': [], 'permitted_baseline_skips': [], 'required_cachestore_cases': []}
        inventory = self.source / 'inventory.json'; inventory.write_text(json.dumps(inv))
        report = self.source / 'target/surefire-reports/TEST-unit.Example.xml'
        self.report(report, 'unit.Example', 'works', self.jvm)
        self.write(self.source / 'target/classes/Class.class', b'\xca\xfe\xba\xbe\0\0\0=')
        output = self.source / 'output'; output.mkdir()
        self.write(output / 'maven.log', '[INFO] Reactor Summary\n[INFO] Parent .... SUCCESS\n[INFO] Session .... SUCCESS\n')
        (output / 'invocation.json').write_text(json.dumps({'java_home': str(self.home)}))
        (output / 'jvms.json').write_text(json.dumps([{'pid': 1, 'properties': dict(self.jvm,
            **{'sun.java.command': 'org.codehaus.plexus.classworlds.launcher.Launcher verify', 'user.dir': str(self.source)})}]))

        def audit(lane='snapshot'):
            with patch.object(full_reactor, 'INVENTORY', inventory), contextlib.redirect_stdout(io.StringIO()):
                full_reactor.audit(self.source, output, lane, '26.10-SNAPSHOT', '21', 'hardened', self.repository, 0)
            return json.loads((output / 'qualification.json').read_text())

        result = audit()
        self.assertEqual(result['issues'], [])
        self.assertEqual(result['totals']['tests'], 1)  # excludes Invoker wrapper AND real consumer
        self.assertEqual(len(result['consumers']['fixtures']['general-starter']), 1)
        # Both default lanes require exactly the reviewed skips, including their reasons.
        inv['permitted_baseline_skips'] = [{'module': '.', 'case': 'unit.Example#works',
                                          'message_contains': 'security API unavailable'}]
        inventory.write_text(json.dumps(inv))
        for lane in ('baseline', 'baseline21'):
            self.assertIn('Unexpected security API skips', audit(lane)['issues'])
            original = report.read_bytes()
            suite = ET.fromstring(original)
            suite.set('skipped', '1')
            skipped = ET.SubElement(suite.find('testcase'), 'skipped', message='security API unavailable')
            report.write_bytes(ET.tostring(suite))
            self.assertEqual(audit(lane)['issues'], [])
            skipped.set('message', 'unrelated skip')
            report.write_bytes(ET.tostring(suite))
            self.assertIn('Unexpected security API skips', audit(lane)['issues'])
            report.write_bytes(original)
        inv['permitted_baseline_skips'] = []
        inventory.write_text(json.dumps(inv))
        self.write(self.module / 'target/surefire-reports/TEST-duplicate.xml', report.read_bytes())
        self.assertTrue(any('Unclassified test report' in i for i in audit()['issues']))
        report.unlink()
        self.assertTrue(any('Missing/invalid reactor report' in i for i in audit()['issues']))


class RuntimeEvidenceTests(unittest.TestCase):
    def test_source_harness_uses_actual_jdk17_and_21_launcher_command(self):
        inv = json.loads(full_reactor.INVENTORY.read_text())
        source = Path('/source')
        rows = [{'pid': i, 'properties': {'sun.java.command': child['command'], 'coherence.cluster': child['cluster']}}
                for i, child in enumerate(inv['child_jvms'])]
        rows += [{'pid': 100, 'properties': {'sun.java.command': 'org.codehaus.plexus.classworlds.launcher.Launcher verify',
                                           'user.dir': str(source)}},
                 {'pid': 101, 'properties': {'sun.java.command': 'jdk.compiler/com.sun.tools.javac.launcher.Main '
                                           + str(source / inv['consumers']['module'] / 'src/it/harness/ConsumerHarness.java') + ' audit /source'}}]
        issues = []
        runtime = full_reactor.audit_runtime(rows, inv, source, lambda *_: None, issues)
        self.assertEqual(issues, [])
        self.assertEqual(runtime[-1]['role'], 'consumer-harness')
        rows[-1]['properties']['sun.java.command'] = 'jdk.compiler/com.sun.tools.javac.launcher.Main /unknown/Program.java'
        issues = []
        full_reactor.audit_runtime(rows, inv, source, lambda *_: None, issues)
        self.assertTrue(any('Unclassified Java process' in i for i in issues))

    def test_xml_cannot_substitute_for_missing_child_or_unknown_jvm(self):
        inv = json.loads(full_reactor.INVENTORY.read_text())
        source = Path('/source')
        rows = [{'pid': i, 'properties': {'sun.java.command': child['command'], 'coherence.cluster': child['cluster']}}
                for i, child in enumerate(inv['child_jvms'])]
        rows.append({'pid': 100, 'properties': {'sun.java.command': 'org.codehaus.plexus.classworlds.launcher.Launcher verify',
                                              'user.dir': str(source)}})
        issues = []
        full_reactor.audit_runtime(rows, inv, source, lambda *_: None, issues)
        self.assertEqual(issues, [])
        issues = []
        full_reactor.audit_runtime(rows[1:], inv, source, lambda *_: None, issues)
        self.assertTrue(any('all nine' in i for i in issues))
        rows.append({'pid': 101, 'properties': {'sun.java.command': 'unknown.Application'}})
        issues = []
        full_reactor.audit_runtime(rows, inv, source, lambda *_: None, issues)
        self.assertTrue(any('Unclassified Java process' in i for i in issues))


if __name__ == '__main__':
    unittest.main()
