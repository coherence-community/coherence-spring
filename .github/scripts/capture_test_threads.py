"""Capture bounded, periodic Surefire thread dumps until the caller writes stop."""

import os
from pathlib import Path
import re
import subprocess
import sys
import time


def collect(jcmd, arguments, destination):
    """Keep partial output and attach errors without failing the Maven build."""
    with destination.open('x', encoding='utf-8') as output:
        try:
            result = subprocess.run(
                [str(jcmd), *arguments], stdout=output, stderr=subprocess.STDOUT,
                timeout=15, check=False)
            if result.returncode:
                output.write(f'\nDiagnostic command exited with status {result.returncode}\n')
        except (OSError, subprocess.TimeoutExpired) as exc:
            output.write(f'\nDiagnostic command failed: {exc}\n')


def capture(output, jcmd):
    """Take a JVM inventory and dump each live Surefire fork, not just Maven."""
    stamp = time.strftime('%Y%m%dT%H%M%SZ', time.gmtime())
    listing = output / f'{stamp}-jvms.txt'
    collect(jcmd, ['-l'], listing)
    for line in listing.read_text(encoding='utf-8', errors='replace').splitlines():
        pid, separator, command = line.partition(' ')
        if separator and pid.isdecimal() and re.search(r'ForkedBooter|surefirebooter', command):
            collect(jcmd, [pid, 'Thread.print', '-l'], output / f'{stamp}-{pid}-threads.txt')


def diagnostics_directory():
    """Use only the fixed directory beneath the trusted runner temporary root."""
    runner_temp = Path(os.environ['RUNNER_TEMP'])
    if not runner_temp.is_absolute() or not runner_temp.is_dir():
        raise ValueError('RUNNER_TEMP must be an existing absolute directory')
    output = runner_temp.resolve(strict=True) / 'startup-diagnostics'
    if output.is_symlink():
        raise ValueError('The diagnostics directory must not be a symlink')
    output.mkdir(exist_ok=True)
    return output


def main():
    if len(sys.argv) != 1:
        raise SystemExit('capture_test_threads.py accepts no arguments; use trusted RUNNER_TEMP')
    output = diagnostics_directory()
    jcmd = Path(os.environ['JAVA_HOME']) / 'bin' / 'jcmd'
    stop = output / 'stop'
    while not stop.exists():
        capture(output, jcmd)
        # Check the stop marker frequently so normal build completion is prompt.
        for _ in range(30):
            if stop.exists():
                return
            time.sleep(1)


if __name__ == '__main__':
    main()
