"""Selected-release consumer checks and the independent strict qualification boundary."""

import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest
import zipfile

from coherence_artifacts import digest
from consumer_evidence import load_properties
import test_consumer_evidence as strict


class ConsumerReleaseTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temporary = tempfile.TemporaryDirectory(prefix='consumer-release-test-')
        cls.addClassCleanup(cls.temporary.cleanup)
        cls.work = Path(cls.temporary.name)
        cls.classes = cls.work / 'classes'
        cls.classes.mkdir()
        home = os.environ.get('JAVA_HOME')
        cls.java = str(Path(home) / 'bin/java') if home else shutil.which('java')
        javac = str(Path(home) / 'bin/javac') if home else shutil.which('javac')
        source = Path(__file__).resolve().parents[2] / 'coherence-spring-session-boot-starter/src/it'
        subprocess.run([javac, '--release', '17', '-d', str(cls.classes),
                        str(source / 'support/ReleaseContract.java'),
                        str(source / 'harness/ConsumerHarness.java'),
                        str(source / 'harness/ReleaseContractTests.java')], check=True, timeout=60)

    def exercise(self, scenario):
        work = self.work / scenario
        work.mkdir()
        result = subprocess.run([self.java, '-cp', str(self.classes), 'ReleaseContractTests', scenario, str(work)],
                                text=True, capture_output=True, timeout=60)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_release_declarations_and_actual_runtime_compatibility(self):
        self.exercise('declarations')

    def test_real_compiler_fixture_and_packaged_bytecode(self):
        self.exercise('bytecode')

    def test_outer_guard_requires_consistent_release_evidence(self):
        self.exercise('guard')

    def test_consistent_release21_bytes_and_hashes_cannot_qualify_release17(self):
        fixture = strict.ConsumerEvidenceTests('test_complete_independent_contract_passes')
        self.addCleanup(fixture.doCleanups)
        fixture.setUp()
        self.assertEqual(fixture.collect()['issues'], [])
        # Make a coherent release21 result, including fresh matching artifact hashes.
        # The strict collector must reject the target itself, independently of drift.
        outer_path = fixture.target / 'consumer-evidence/outer-artifacts.properties'
        outer = load_properties(outer_path)
        for field, original in list(outer.items()):
            if field.startswith('com.oracle.coherence.spring:') and field.endswith('.jar.source'):
                with zipfile.ZipFile(original, 'w') as jar:
                    jar.writestr('example/Class.class', b'\xca\xfe\xba\xbe\0\0\0A')
        for field, original in list(outer.items()):
            if field.endswith('.jar.source'):
                outer[field.replace('.source', '.sha256')] = digest(Path(original))
        for jar in (fixture.target / 'it-repository/com/oracle/coherence/spring').rglob('*.jar'):
            key = next(k[:-len('.jar.source')] for k in outer if k.endswith('.jar.source')
                       and k.split(':')[1] == jar.parent.parent.name)
            shutil.copyfile(outer[key + '.jar.source'], jar)
        fixture.props(outer_path, outer)
        evidence = load_properties(fixture.evidence_path)
        evidence.update({k: v for k, v in outer.items() if k in evidence and k.endswith('.sha256')})
        evidence['consumer.release'] = '21'
        fixture.props(fixture.evidence_path, evidence)
        issues = fixture.collect()['issues']
        self.assertTrue(any('consumer.release != 17' in issue for issue in issues), issues)
        self.assertTrue(any('expected release-17 bytecode' in issue for issue in issues), issues)
        self.assertFalse(any('drift' in issue for issue in issues), issues)


if __name__ == '__main__':
    unittest.main()
