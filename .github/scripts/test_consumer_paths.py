"""Portable path regressions using the real consumer and outer guard helpers."""

import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest


class ConsumerPathTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temporary = tempfile.TemporaryDirectory(prefix='consumer-path-test-')
        cls.addClassCleanup(cls.temporary.cleanup)
        cls.work = Path(cls.temporary.name)
        cls.classes = cls.work / 'classes'
        cls.classes.mkdir()
        home = os.environ.get('JAVA_HOME')
        cls.java = str(Path(home) / 'bin/java') if home else shutil.which('java')
        javac = str(Path(home) / 'bin/javac') if home else shutil.which('javac')
        source = Path(__file__).resolve().parents[2] / 'coherence-spring-session-boot-starter/src/it'
        subprocess.run([javac, '--release', '17', '-d', str(cls.classes),
                        str(source / 'support/PathContract.java'),
                        str(source / 'harness/ConsumerHarness.java'),
                        str(source / 'harness/PathContractTests.java')], check=True, timeout=60)

    def exercise(self, scenario):
        work = self.work / scenario
        work.mkdir()
        result = subprocess.run([self.java, '-cp', str(self.classes), 'PathContractTests', scenario, str(work)],
                                text=True, capture_output=True, timeout=60)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_exact_real_repository_and_coordinates(self):
        self.exercise('repository')

    def test_exact_actual_war_deployment(self):
        self.exercise('war')

    def test_posix_windows_classpath_syntax_and_isolation(self):
        self.exercise('classpaths')

    def test_dependency_drive_letters_and_outer_freeze(self):
        self.exercise('dependencies')

    def test_colored_dependency_reports_and_outer_freeze(self):
        self.exercise('colored-dependencies')

    def test_missing_outer_artifact_is_fatal(self):
        self.exercise('missing-dependencies')

    def test_missing_colored_outer_artifact_is_fatal(self):
        self.exercise('colored-missing-dependencies')


if __name__ == '__main__':
    unittest.main()
