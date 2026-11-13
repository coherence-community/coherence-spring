"""Capture bounded, periodic Surefire thread dumps until the caller writes stop."""

import os
from pathlib import Path
import re
import subprocess
import sys
import time


def collect(jcmd, arguments, destination):
    """Keep partial output and attach errors without failing the Maven build."""
    with destination.open('w', encoding='utf-8') as output:
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


def main():
    output = Path(sys.argv[1])
    output.mkdir(parents=True, exist_ok=True)
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
