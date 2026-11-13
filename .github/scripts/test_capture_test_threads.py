"""Check diagnostic fork selection, attach failures and collector shutdown."""

from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

import capture_test_threads as collector


class CaptureTestThreadsTests(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory(prefix='test-thread-dumps-')
        self.addCleanup(temporary.cleanup)
        self.output = Path(temporary.name)
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
        for failure in (OSError('attach unavailable'), subprocess.TimeoutExpired('jcmd', 15), 1):
            with self.subTest(failure=failure):
                def run(command, **kwargs):
                    kwargs['stdout'].write('partial diagnostic\n')
                    self.assertEqual(kwargs['timeout'], 15)
                    self.assertFalse(kwargs['check'])
                    if isinstance(failure, Exception):
                        raise failure
                    return subprocess.CompletedProcess(command, failure)

                destination = self.output / 'failed-command.txt'
                with patch.object(collector.subprocess, 'run', side_effect=run):
                    collector.collect(self.jcmd, ['-l'], destination)
                text = destination.read_text()
                self.assertIn('partial diagnostic', text)
                self.assertIn('Diagnostic command', text)

    def test_stop_marker_prevents_further_samples_without_sleep(self):
        def capture(output, _jcmd):
            (output / 'stop').touch()

        with patch.object(collector.sys, 'argv', ['collector', str(self.output)]), \
                patch.dict(collector.os.environ, {'JAVA_HOME': '/selected jdk'}), \
                patch.object(collector, 'capture', side_effect=capture) as sample, \
                patch.object(collector.time, 'sleep') as sleep:
            collector.main()
            collector.main()
        sample.assert_called_once_with(self.output, Path('/selected jdk/bin/jcmd'))
        sleep.assert_not_called()


if __name__ == '__main__':
    unittest.main()
