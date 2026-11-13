"""Check diagnostic fork selection, attach failures and collector shutdown."""

from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

import capture_test_threads as collector


class CaptureTestThreadsTests(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory(prefix='test thread dumps ')
        self.addCleanup(temporary.cleanup)
        self.runner_temp = Path(temporary.name)
        self.output = self.runner_temp / 'startup-diagnostics'
        self.output.mkdir()
        self.jcmd = Path('/selected jdk/bin/jcmd')

    def test_only_numeric_surefire_pids_are_dumped(self):
        inventory = ('100 org.codehaus.plexus.classworlds.launcher.Launcher\n'
                     '101 org.apache.maven.surefire.booter.ForkedBooter\n'
                     '102 /build with spaces/surefirebooter-123.jar\n'
                     '103 jdk.jcmd/sun.tools.jcmd.JCmd\n'
                     'bad-pid org.apache.maven.surefire.booter.ForkedBooter\n')

        def run(command, **kwargs):
            kwargs['stdout'].write(inventory if command[-1] == '-l' and len(command) == 2 else 'thread stacks\n')
            return subprocess.CompletedProcess(command, 0)

        with patch.object(collector.subprocess, 'run', side_effect=run) as command:
            collector.capture(self.output, self.jcmd)
        self.assertEqual([call.args[0] for call in command.call_args_list], [
            [str(self.jcmd), '-l'], [str(self.jcmd), '101', 'Thread.print', '-l'],
            [str(self.jcmd), '102', 'Thread.print', '-l']])
        dumps = list(self.output.glob('*-threads.txt'))
        self.assertEqual(len(dumps), 2)
        self.assertTrue(all(path.read_text() == 'thread stacks\n' for path in dumps))

    def test_failed_commands_keep_partial_output_and_do_not_raise(self):
        for index, failure in enumerate((OSError('attach unavailable'), subprocess.TimeoutExpired('jcmd', 15), 1)):
            with self.subTest(failure=failure):
                def run(command, **kwargs):
                    kwargs['stdout'].write('partial diagnostic\n')
                    self.assertEqual(kwargs['timeout'], 15)
                    self.assertFalse(kwargs['check'])
                    if isinstance(failure, Exception):
                        raise failure
                    return subprocess.CompletedProcess(command, failure)

                destination = self.output / f'failed-command-{index}.txt'
                with patch.object(collector.subprocess, 'run', side_effect=run):
                    collector.collect(self.jcmd, ['-l'], destination)
                text = destination.read_text()
                self.assertIn('partial diagnostic', text)
                self.assertIn('Diagnostic command', text)

    def test_stop_marker_prevents_further_samples_without_sleep(self):
        def capture(output, _jcmd):
            (output / 'stop').touch()

        with patch.object(collector.sys, 'argv', ['collector']), \
                patch.dict(collector.os.environ, {'JAVA_HOME': '/selected jdk',
                                                'RUNNER_TEMP': str(self.runner_temp)}), \
                patch.object(collector, 'capture', side_effect=capture) as sample, \
                patch.object(collector.time, 'sleep') as sleep:
            collector.main()
            collector.main()
        sample.assert_called_once_with(self.output, Path('/selected jdk/bin/jcmd'))
        sleep.assert_not_called()

    def test_fixed_output_under_canonical_runner_temp(self):
        self.output.rmdir()
        alias = self.runner_temp / 'runner-alias'
        alias.symlink_to(self.runner_temp, target_is_directory=True)
        with patch.dict(collector.os.environ, {'RUNNER_TEMP': str(alias)}):
            output = collector.diagnostics_directory()
        self.assertEqual(output, self.output.resolve())
        self.assertTrue(output.is_dir())

    def test_missing_or_relative_runner_temp_is_rejected(self):
        for environment in ({}, {'RUNNER_TEMP': ''}, {'RUNNER_TEMP': '../outside'},
                            {'RUNNER_TEMP': str(self.runner_temp / 'missing')}):
            with self.subTest(environment=environment), \
                    patch.dict(collector.os.environ, environment, clear=True), \
                    self.assertRaises((KeyError, ValueError)):
                collector.diagnostics_directory()

    def test_output_directory_symlinks_are_rejected(self):
        self.output.rmdir()
        outside = self.runner_temp / 'outside'
        outside.mkdir()
        for target in (outside, self.runner_temp / 'missing'):
            with self.subTest(target=target):
                self.output.symlink_to(target, target_is_directory=True)
                with patch.dict(collector.os.environ, {'RUNNER_TEMP': str(self.runner_temp)}), \
                        self.assertRaisesRegex(ValueError, 'symlink'):
                    collector.diagnostics_directory()
                self.output.unlink()
        self.assertEqual(list(outside.iterdir()), [])

    def test_extra_arguments_cannot_select_output(self):
        outside = self.runner_temp / 'outside'
        alias = self.runner_temp / 'outside-link'
        alias.symlink_to(outside, target_is_directory=True)
        for argument in ('../outside', str(outside), str(self.output) + '-sibling', str(alias)):
            with self.subTest(argument=argument), \
                    patch.object(collector.sys, 'argv', ['collector', argument]), \
                    patch.object(collector, 'diagnostics_directory') as directory, \
                    self.assertRaisesRegex(SystemExit, 'accepts no arguments'):
                collector.main()
            directory.assert_not_called()
        self.assertFalse(outside.exists())

    def test_existing_report_files_are_not_overwritten(self):
        outside = self.runner_temp / 'outside.txt'
        outside.write_text('preserve this evidence')
        destination = self.output / 'existing-report.txt'
        for kind in ('regular', 'symlink', 'dangling'):
            with self.subTest(kind=kind):
                if kind == 'regular':
                    destination.write_text('existing report')
                else:
                    destination.symlink_to(outside if kind == 'symlink' else self.runner_temp / 'missing')
                with patch.object(collector.subprocess, 'run') as command, \
                        self.assertRaises(FileExistsError):
                    collector.collect(self.jcmd, ['-l'], destination)
                command.assert_not_called()
                self.assertEqual(outside.read_text(), 'preserve this evidence')
                if kind == 'regular':
                    self.assertEqual(destination.read_text(), 'existing report')
                else:
                    self.assertTrue(destination.is_symlink())
                destination.unlink()


if __name__ == '__main__':
    unittest.main()
