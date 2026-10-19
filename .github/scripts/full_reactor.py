#!/usr/bin/env python3
"""Run and audit the complete release-17 qualification reactor."""

import argparse
import collections
import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import threading
import xml.etree.ElementTree as ET
import zipfile

from coherence_artifacts import ROOT, cache_files, dependency_record, digest

PROPERTIES = {'java.version', 'java.runtime.version', 'java.specification.version', 'java.home',
              'coherence.security.mode', 'coherence.cluster', 'java.class.path', 'sun.java.command',
              'maven.test.failure.ignore', 'coherence.remote.dynamic.unauthenticated',
              'spring.application.admin.enabled', 'server.port'}
CLUSTERS = {'CoherenceExtendTestsCluster', 'GrpcSessionBeanTestsCluster', 'GrpcSessionTestsCluster',
            'ExtendSessionTestsCluster', 'GrpcSessionCoherenceIndexedSessionRepositoryTestsCluster',
            'PofCoherenceIndexedSessionRepositoryTestsCluster', 'CoherenceConfigDataLoaderTests',
            'CoherenceGrpcClientTestsCluster'}


def observe(parent, stop, output, env):
    seen, rows = set(), []
    jcmd = str(Path(env['JAVA_HOME']) / 'bin/jcmd')
    while not stop.is_set():
        try:
            listing = subprocess.check_output(['ps', '-axo', 'pid=,ppid=,command='], text=True)
            processes = {}
            for line in listing.splitlines():
                parts = line.strip().split(None, 2)
                if len(parts) == 3:
                    processes[int(parts[0])] = (int(parts[1]), parts[2])
            descendants = {parent}
            for _ in range(8):
                descendants |= {pid for pid, (ppid, _) in processes.items() if ppid in descendants}
            for pid in sorted(descendants - seen):
                if pid not in processes or not processes[pid][1].split()[0].endswith('/bin/java'):
                    continue
                try:
                    text = subprocess.check_output([jcmd, str(pid), 'VM.system_properties'],
                                                   text=True, stderr=subprocess.STDOUT, timeout=5, env=env)
                    props = dict(line.split('=', 1) for line in text.splitlines()
                                 if '=' in line and line.split('=', 1)[0] in PROPERTIES)
                    if 'java.version' in props:
                        rows.append({'pid': pid, 'ppid': processes[pid][0], 'properties': props})
                        seen.add(pid)
                        output.write_text(json.dumps(rows, indent=2))
                except (OSError, subprocess.SubprocessError):
                    pass  # Short forks are also verified through every suite's XML properties.
        except (OSError, subprocess.SubprocessError, ValueError) as error:
            (output.parent / 'observer-error.txt').write_text(str(error))
        stop.wait(0.5)


def audit(source, output, lane, version, java, mode, repository, status):
    issues, dependencies, modules, cases, runtime = [], {}, {}, [], []
    resolved = cache_files(repository, version)
    expected = {a['artifact']: a for a in resolved}

    def properties(props, origin):
        if props.get('coherence.security.mode') != mode:
            issues.append(f'{origin}: security mode {props.get("coherence.security.mode")} != {mode}')
        if props.get('java.version', '').split('.')[0] != java:
            issues.append(f'{origin}: unexpected JVM {props.get("java.version")}')
        if props.get('coherence.remote.dynamic.unauthenticated') is not None:
            issues.append(f'{origin}: dynamic-code override is forbidden')
        for key in ['java.class.path', 'surefire.test.class.path']:
            for value in props.get(key, '').split(os.pathsep):
                path = Path(value.rstrip(chr(92)))
                if path.suffix != '.jar' or not path.is_file() or str(path) in dependencies:
                    continue
                row = {'path': str(path), 'sha256': digest(path)}
                if '/com/oracle/coherence/ce/' in str(path):
                    row = dependency_record(path)
                    artifact = expected.get(row['artifact'])
                    if row['version'] != version:
                        issues.append(f'{origin}: mixed Coherence version: {path}; expected {version}')
                    if (not artifact or row['sha256'] != artifact['sha256']
                            or row['pom_sha256'] != artifact['pom_sha256']):
                        issues.append(f'{origin}: Coherence dependency differs within this run: {path}')
                    if row['pom_sha256'] is None:
                        issues.append(f'{origin}: Coherence POM evidence is missing: {path}')
                dependencies[str(path)] = row

    observations = output / 'jvms.json'
    for row in json.loads(observations.read_text()) if observations.is_file() else []:
        props = row['properties']
        command = props.get('sun.java.command', '')
        role = ('database' if 'org.hsqldb.Server' in command else 'cachestore' if 'CacheStoreDemo' in command
                else 'coherence') if 'JavaApplicationRunner' in command else 'fork' if 'ForkedBooter' in command else 'maven'
        properties(props, f'{role}:{row["pid"]}')
        runtime.append(dict(row, role=role))
    children = [r for r in runtime if r['role'] in ['coherence', 'cachestore']]
    if len(children) != 9 or {r['properties'].get('coherence.cluster') for r in children if r['role'] == 'coherence'} != CLUSTERS:
        issues.append('Actual property evidence for all nine Coherence children is required')
    if not any(r['role'] == 'maven' for r in runtime) or not any(r['role'] == 'database' for r in runtime):
        issues.append('Maven/database JVM evidence is missing')
    for path in sorted(source.glob('**/target/*-reports/TEST-*.xml')):
        suite = ET.parse(path).getroot()
        module = str(path.parent.parent.parent.relative_to(source))
        counts = modules.setdefault(module, collections.Counter())
        for key in ['tests', 'failures', 'errors', 'skipped']:
            counts[key] += int(suite.get(key, '0'))
        counts['suites'] += 1
        props = {p.get('name'): p.get('value') for p in suite.findall('properties/property')}
        properties(props, suite.get('name'))
        if props.get('maven.test.failure.ignore') != 'false':
            issues.append(suite.get('name') + ': fatal failures were not enabled')
        for case in suite.findall('testcase'):
            kind = next((k for k in ['failure', 'error', 'skipped'] if case.find(k) is not None), 'passed')
            element = case.find(kind)
            cases.append({'module': module, 'class': case.get('classname'), 'name': case.get('name'),
                          'result': kind, 'message': element.get('message') if element is not None else None})
    totals = dict(sum(modules.values(), collections.Counter()))
    log = (output / 'maven.log').read_text(errors='replace')
    summary = log[log.rfind('[INFO] Reactor Summary'):]
    reactor = re.findall(r'^\[INFO\] (.*?) \.{2,} (SUCCESS|FAILURE|SKIPPED)', summary, re.MULTILINE)
    (output / 'reactor-summary.txt').write_text(summary)
    if status or len(reactor) != 28 or any(state != 'SUCCESS' for _, state in reactor):
        issues.append(f'Full reactor failed or incomplete: Maven exit {status}, {len(reactor)} modules')
    if totals.get('tests') != 706 or totals.get('suites') != 83 or totals.get('failures') or totals.get('errors'):
        issues.append('Complete passing 706-case / 83-suite inventory is required; update qualification counts when adding tests')
    skipped = [c for c in cases if c['result'] == 'skipped']
    if lane != 'requested' and (len(skipped) != (9 if lane == 'baseline' else 0)
                               or any('securityPolicyAvailable' not in (c['message'] or '') for c in skipped)):
        issues.append('Unexpected security API skips')
    its = [c for c in cases if c['class'].endswith('.CacheStoreDemoIT')]
    if len(its) != 4 or any(c['result'] != 'passed' for c in its):
        issues.append('All four ordinary CacheStore Failsafe methods must pass')
    bytecode = collections.Counter()
    for target in source.glob('**/target'):
        for directory in ['classes', 'test-classes']:
            for path in (target / directory).rglob('*.class'):
                bytecode[int.from_bytes(path.read_bytes()[6:8], 'big')] += 1
    if set(bytecode) != {61}:
        issues.append(f'Expected release-17 class major 61: {bytecode}')
    packaged = []
    for path in list(source.glob('**/target/*.jar')) + list(source.glob('**/target/*.war')):
        with zipfile.ZipFile(path) as archive:
            for name in archive.namelist():
                if not name.startswith(('BOOT-INF/lib/coherence-', 'WEB-INF/lib/coherence-')) or not name.endswith('.jar'):
                    continue
                filename = Path(name).name
                if filename.startswith(('coherence-spring-', 'coherence-hibernate-')):
                    continue
                sha = hashlib.sha256(archive.read(name)).hexdigest()
                matches = [a for a, record in expected.items()
                           if filename in {Path(record['path']).name, f"{a}-{version}.jar"}]
                if len(matches) != 1 or sha != expected[matches[0]]['sha256']:
                    issues.append(f'Packaged Coherence differs from this run: {path}:{name}')
                packaged.append({'package': str(path), 'entry': name, 'sha256': sha})
    (output / 'resolved-coherence.json').write_text(json.dumps(resolved, indent=2))
    result = {'lane': lane, 'coherence_version': version, 'compiler_release': 17, 'maven_exit': status,
              'totals': totals, 'modules': modules, 'reactor': reactor, 'cases': cases,
              'jvms': runtime, 'dependencies': list(dependencies.values()), 'coherence_cache_after': resolved,
              'packaged_coherence': packaged, 'class_major_counts': bytecode, 'issues': issues}
    (output / 'qualification.json').write_text(json.dumps(result, indent=2))
    print(json.dumps({'totals': totals, 'class_major_counts': bytecode, 'issues': issues}, indent=2))
    return bool(issues)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--lane', choices=['baseline', 'snapshot', 'requested'], required=True)
    parser.add_argument('--version', help='Only for the optional requested lane')
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--maven', default='./mvnw', help='Matching cached Maven may be used for a local exercise')
    parser.add_argument('--repository', type=Path, help='Disposable repository for a local exercise; CI uses its ordinary Maven cache')
    parser.add_argument('--offline', action='store_true', help='Local cached exercise only')
    parser.add_argument('--jvm-tmpdir', type=Path, help='Task-owned JVM temporary directory for a local exercise')
    args = parser.parse_args()
    version = {'baseline': '26.04', 'snapshot': '26.10-SNAPSHOT'}.get(args.lane, args.version)
    if not version or not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9_.-]*', version) or (args.version and args.lane != 'requested'):
        parser.error('A safe explicit version is required only for the requested lane')
    if args.offline and version.endswith('-SNAPSHOT'):
        parser.error('Snapshot qualification must resolve the latest published artifacts online')
    java, mode = ('17', None) if args.lane == 'baseline' else ('21', 'hardened')
    env = os.environ.copy()
    for name in ['JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', '_JAVA_OPTIONS', 'MAVEN_ARGS', 'MAVEN_OPTS']:
        env.pop(name, None)
    if mode:
        env['JAVA_TOOL_OPTIONS'] = '-Dcoherence.security.mode=hardened'
    if args.jvm_tmpdir:
        args.jvm_tmpdir.mkdir(parents=True, exist_ok=True)
        env['JAVA_TOOL_OPTIONS'] = (env.get('JAVA_TOOL_OPTIONS', '')
                                   + f' -Djava.io.tmpdir={args.jvm_tmpdir.resolve()}').strip()
    home = Path(env['JAVA_HOME'])
    runtime = subprocess.check_output([str(home / 'bin/java'), '-XshowSettings:properties', '-version'],
                                      text=True, stderr=subprocess.STDOUT, env=env)
    if not re.search(r'java.specification.version = ' + java + r'\s', runtime):
        parser.error(f'{args.lane} requires JDK {java}')
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    if (output / 'maven.log').exists():
        parser.error('Use a fresh output directory for each invocation')
    repository = (args.repository or Path.home() / '.m2/repository').resolve()
    command = [args.maven, '-B', '-ntp', '-Pjdk17', '-Dmaven.test.failure.ignore=false',
               f'-Dcoherence.version={version}', '--settings', '.mvn/settings.xml', '--fail-at-end']
    if version.endswith('-SNAPSHOT'):
        command.append('-U')
    if mode:
        command.append('-Dcoherence.security.mode=hardened')
    if args.repository:
        command.append(f'-Dmaven.repo.local={repository}')
    if args.offline:
        command.append('-o')
    command += ['clean', 'verify']
    manifest = {'command': command, 'runtime': runtime, 'security_mode': mode,
                'JAVA_TOOL_OPTIONS': env.get('JAVA_TOOL_OPTIONS'), 'started': datetime.datetime.now(datetime.timezone.utc).isoformat(),
                'source_sha256': {str(p.relative_to(ROOT)): digest(p) for p in ROOT.rglob('*')
                                  if p.is_file() and '.git' not in p.parts and 'target' not in p.parts
                                  and (p.suffix in ['.java', '.xml'] or p.name == 'pom.xml')
                                  and p.name != '.flattened-pom.xml'}}
    (output / 'invocation.json').write_text(json.dumps(manifest, indent=2))
    stop = threading.Event()
    with (output / 'maven.log').open('w') as log:
        process = subprocess.Popen(command, cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, env=env)
        watcher = threading.Thread(target=observe, args=(process.pid, stop, output / 'jvms.json', env), daemon=True)
        watcher.start()
        for line in process.stdout:
            log.write(line)
            print(line, end='', flush=True)
        status = process.wait()
        stop.set()
        watcher.join(timeout=15)
    manifest.update(maven_exit=status, ended=datetime.datetime.now(datetime.timezone.utc).isoformat())
    (output / 'invocation.json').write_text(json.dumps(manifest, indent=2))
    return 1 if audit(ROOT, output, args.lane, version, java, mode, repository, status) else 0


if __name__ == '__main__':
    raise SystemExit(main())
