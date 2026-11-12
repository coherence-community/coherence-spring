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
from consumer_evidence import audit_consumers, junit

INVENTORY = Path(__file__).with_name('full-reactor-inventory.json')

JAVA_VERSION = 'java.version'
JAVA_COMMAND = 'sun.java.command'
CLUSTER = 'coherence.cluster'
CONSUMER_FIXTURE = 'consumer.fixture'
INVOCATION_FILE = 'invocation.json'
MAVEN_LOG = 'maven.log'

PROPERTIES = {JAVA_VERSION, 'java.runtime.version', 'java.specification.version', 'java.home',
              'coherence.security.mode', CLUSTER, 'java.class.path', JAVA_COMMAND,
              'maven.test.failure.ignore', 'coherence.remote.dynamic.unauthenticated',
              'spring.application.admin.enabled', 'server.port', 'user.dir', CONSUMER_FIXTURE}


def trusted_maven(value, env):
    """CLI data selects only the wrapper or a separately operator-approved launcher."""
    trusted = {(ROOT / 'mvnw').resolve()}
    configured = env.get('FULL_REACTOR_TRUSTED_MAVEN')
    if configured:
        cached = Path(configured)
        if not cached.is_absolute():
            raise ValueError('FULL_REACTOR_TRUSTED_MAVEN must be an absolute operator-trusted path')
        trusted.add(cached.resolve())
    candidate = Path(value)
    candidate = (candidate if candidate.is_absolute() else ROOT / candidate).resolve()
    if candidate not in trusted:
        raise ValueError('--maven must select the repository wrapper or FULL_REACTOR_TRUSTED_MAVEN')
    if not candidate.is_file() or not os.access(candidate, os.X_OK):
        raise ValueError('The trusted Maven launcher must be an executable regular file')
    return str(candidate)


def prepare_output(output):
    """Require fresh evidence in an operator-owned directory; retain CI's source SHA."""
    if output.is_symlink():
        raise ValueError('The output directory must not be a symlink')
    output.mkdir(parents=True, exist_ok=True)
    for entry in output.iterdir():
        if entry.name != 'source-sha.txt' or entry.is_symlink() or not entry.is_file():
            raise ValueError('Use a fresh output directory (only a regular source-sha.txt may exist)')
    return output.resolve()


def manifest_launcher(command):
    """Capture ephemeral Surefire launcher identity while its JVM is alive."""
    match = re.match(r'^(.*[/\\]target[/\\]surefire[/\\]surefirebooter-[^/\\ ]+\.jar)(?: |$)', command)
    if not match:
        return None
    path = Path(match[1])
    try:
        with zipfile.ZipFile(path) as archive:
            manifest = archive.read('META-INF/MANIFEST.MF').decode('utf-8')
        main = re.search(r'^Main-Class: (.+)\r?$', manifest, re.MULTILINE)
        return {'path': str(path), 'sha256': digest(path),
                'main_class': main[1].strip() if main else None}
    except (OSError, KeyError, ValueError, zipfile.BadZipFile):
        return None


def process_snapshot():
    """Read parent/command identities before attaching to any Java process."""
    listing = subprocess.check_output(['ps', '-axo', 'pid=,ppid=,command='], text=True)
    processes = {}
    for line in listing.splitlines():
        parts = line.strip().split(None, 2)
        if len(parts) == 3:
            processes[int(parts[0])] = (int(parts[1]), parts[2])
    return processes


def observe_descendants(parent, jcmd, env, seen, rows, output):
    """Append fresh property evidence; retry transient attach failures next poll."""
    processes = process_snapshot()
    descendants = {parent}
    for _ in range(8):
        descendants |= {pid for pid, (ppid, _) in processes.items() if ppid in descendants}
    for pid in sorted(descendants - seen):
        if pid not in processes or not processes[pid][1].split()[0].endswith('/bin/java'):
            continue
        try:
            text = subprocess.check_output([jcmd, str(pid), 'VM.system_properties'],
                                           text=True, stderr=subprocess.STDOUT, timeout=5, env=env)
            props = {key: value for line in text.splitlines() if '=' in line
                     for key, value in [line.split('=', 1)] if key in PROPERTIES}
            if JAVA_VERSION in props:
                rows.append({'pid': pid, 'ppid': processes[pid][0], 'properties': props,
                             'manifest_launcher': manifest_launcher(props.get(JAVA_COMMAND, ''))})
                seen.add(pid)
                output.write_text(json.dumps(rows, indent=2))
        except (OSError, subprocess.SubprocessError):
            pass  # XML covers test forks only; required children must be observed.


def observe(parent, stop, output, env):
    seen, rows = set(), []
    jcmd = str(Path(env['JAVA_HOME']) / 'bin/jcmd')
    while not stop.is_set():
        try:
            observe_descendants(parent, jcmd, env, seen, rows, output)
        except (OSError, subprocess.SubprocessError, ValueError) as error:
            (output.parent / 'observer-error.txt').write_text(str(error))
        stop.wait(0.5)


def reactor_modules(source):
    ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
    identities = set()

    def visit(path):
        pom = ET.parse(source / path / 'pom.xml').getroot()
        identity = (str(path), pom.findtext('m:artifactId', namespaces=ns), pom.findtext('m:name', namespaces=ns))
        if identity in identities:
            raise ValueError(f'Duplicate reactor module {path}')
        identities.add(identity)
        for child in pom.findall('m:modules/m:module', ns):
            visit(path / child.text)
    visit(Path('.'))
    return identities


def matches_child_role(command, props, role):
    if command != role['command'] and not command.startswith(role['command'] + ' '):
        return False
    if role.get('cluster_prefix'):
        return (props.get(CLUSTER) or '').startswith(role['cluster_prefix'])
    return props.get(CLUSTER) == role['cluster']


def classify_runtime(row, source, inventory, fixtures, booter_directories):
    """Classify infrastructure JVMs after the reviewed child-role match fails."""
    props = row['properties']
    command = props.get(JAVA_COMMAND, '')
    if command.startswith('org.codehaus.plexus.classworlds.launcher.Launcher '):
        cwd = Path(props.get('user.dir', '')).resolve()
        if cwd == source:
            return 'maven'
        if cwd.parent == fixtures:
            return 'consumer-maven'
        return 'unknown'
    elif command.startswith('org.apache.maven.surefire.booter.ForkedBooter '):
        return 'consumer-fork' if props.get(CONSUMER_FIXTURE) else 'fork'
    elif (row.get('manifest_launcher')
          and row['manifest_launcher'].get('main_class') == 'org.apache.maven.surefire.booter.ForkedBooter'
          and re.fullmatch(r'[0-9a-f]{64}', row['manifest_launcher'].get('sha256', ''))
          and Path(row['manifest_launcher']['path']).parent in booter_directories
          and command.startswith(row['manifest_launcher']['path'] + ' ')):
        return 'consumer-fork' if props.get(CONSUMER_FIXTURE) else 'fork'
    elif command.startswith('jdk.compiler/com.sun.tools.javac.launcher.Main '
                            + str(source / inventory['consumers']['module'] / 'src/it/harness/ConsumerHarness.java') + ' '):
        return 'consumer-harness'
    else:
        return 'unknown'


def audit_runtime(rows, inventory, source, properties, issues):
    runtime, children = [], collections.Counter()
    roles = inventory['child_jvms']
    fixtures = source / inventory['consumers']['module'] / 'target/it'
    booter_directories = {source / m['path'] / 'target/surefire' for m in inventory.get('modules', [])}
    booter_directories |= {fixtures / p.parent.name / 'target/surefire'
                          for p in (source / inventory['consumers']['module'] / 'src/it').glob('*/pom.xml')}
    pids = set()
    for row in rows:
        props = row['properties']
        command = props.get(JAVA_COMMAND, '')
        matches = [r for r in roles if matches_child_role(command, props, r)]
        if len(matches) == 1:
            matched = matches[0]
            role = matched['role']
            children[(role, matched['command'], matched['cluster'])] += 1
        else:
            role = classify_runtime(row, source, inventory, fixtures, booter_directories)
        if row['pid'] in pids:
            issues.append(f'Duplicate JVM property observation: {row["pid"]}')
        pids.add(row['pid'])
        if role == 'unknown':
            issues.append(f'Unclassified Java process: {command}')
        properties(props, f'{role}:{row["pid"]}')
        runtime.append(dict(row, role=role))
    wanted = collections.Counter({(r['role'], r['command'], r['cluster']): r['count'] for r in roles})
    if children != wanted:
        issues.append('Actual property evidence for every inventoried child JVM is required: '
                      f'missing={list((wanted - children).elements())}, extra={list((children - wanted).elements())}')
    if not any(r['role'] == 'maven' for r in runtime):
        issues.append('Maven JVM evidence is missing')
    return runtime


def audit_jvm_properties(props, origin, java, home, mode, issues):
    if props.get('coherence.security.mode') != mode:
        issues.append(f'{origin}: security mode {props.get("coherence.security.mode")} != {mode}')
    if props.get(JAVA_VERSION, '').split('.')[0] != java:
        issues.append(f'{origin}: unexpected JVM {props.get(JAVA_VERSION)}')
    if Path(props.get('java.home', '')).resolve() != home:
        issues.append(f'{origin}: unexpected JDK home {props.get("java.home")}')
    if props.get('coherence.remote.dynamic.unauthenticated') is not None:
        issues.append(f'{origin}: dynamic-code override is forbidden')


def classpath_record(path, expected, version, origin, issues):
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
    return row


def audit_classpath(props, origin, expected, version, dependencies, issues):
    for key in ['java.class.path', 'surefire.test.class.path']:
        for value in props.get(key, '').split(os.pathsep):
            path = Path(value.rstrip(chr(92)))
            if path.suffix != '.jar' or not path.is_file() or str(path) in dependencies:
                continue
            dependencies[str(path)] = classpath_record(path, expected, version, origin, issues)


def audit_reactor_report(path, report, name, properties, cases, counts, issues):
    try:
        suite, tests = junit(path)
        actual = [c['class'] + '#' + c['name'] for c in tests]
        if suite.get('name') != report['name'] or collections.Counter(actual) != collections.Counter(report['cases']):
            issues.append(f'{path}: suite/case inventory differs')
        props = {p.get('name'): p.get('value') for p in suite.findall('properties/property')}
        properties(props, report['name'])
        if props.get('maven.test.failure.ignore') != 'false':
            issues.append(report['name'] + ': fatal failures were not enabled')
        for case in tests:
            cases.append(dict(case, module=name))
            counts[{'passed': 'passed', 'failure': 'failures', 'error': 'errors', 'skipped': 'skipped'}[case['result']]] += 1
        counts['tests'] += len(tests)
        counts['suites'] += 1
    except (OSError, ValueError, ET.ParseError) as error:
        issues.append(f'Missing/invalid reactor report {path}: {error}')


def audit_reactor_reports(source, inventory, properties, issues):
    modules, cases = {}, []
    expected_reports = set()
    expected_cases = set()
    for module in inventory['modules']:
        name = module['path']
        counts = collections.Counter()
        for report in module['suites']:
            path = source / name / 'target' / report['report'] / ('TEST-' + report['name'] + '.xml')
            expected_reports.add(path)
            expected_cases.update((name, c) for c in report['cases'])
            audit_reactor_report(path, report, name, properties, cases, counts, issues)
        if counts:
            modules[name] = counts
    return modules, cases, expected_reports, expected_cases


def audit_report_discovery(source, inventory, expected_reports, issues):
    # Discovery detects unclassified reports; it never supplies acceptance criteria.
    consumer_target = source / inventory['consumers']['module'] / 'target'
    for path in source.glob('**/target/*-reports/TEST-*.xml'):
        if path in expected_reports:
            continue
        relative = path.relative_to(consumer_target) if path.is_relative_to(consumer_target) else None
        if relative and (relative.parts[0] == 'invoker-reports' or
                         (len(relative.parts) == 5 and relative.parts[0] == 'it'
                          and relative.parts[2:4] == ('target', 'failsafe-reports'))):
            continue  # Independently audited below, never included in reactor totals.
        issues.append(f'Unclassified test report: {path}')


def audit_reactor_identity(source, output, inventory, status, issues):
    log = (output / MAVEN_LOG).read_text(errors='replace')
    summary = log[log.rfind('[INFO] Reactor Summary'):]
    reactor = re.findall(r'^\[INFO\] (.*?) \.{2,} (SUCCESS|FAILURE|SKIPPED)', summary, re.MULTILINE)
    # S2083 reviewed: log text is report content; output and the basename select the destination.
    (output / 'reactor-summary.txt').write_text(summary)  # NOSONAR(S2083)
    if (status
            or collections.Counter(name for name, _ in reactor) != collections.Counter(m['name'] for m in inventory['modules'])
            or any(state != 'SUCCESS' for _, state in reactor)):
        issues.append(f'Full reactor failed or module identities differ: Maven exit {status}, {len(reactor)} modules')
    # Verify source POM identities too: a renamed/replaced project cannot hide behind
    # the same display name in the log.
    try:
        actual_modules = reactor_modules(source)
        wanted_modules = {(m['path'], m['artifact'], m['name']) for m in inventory['modules']}
        if actual_modules != wanted_modules:
            issues.append('Source reactor module identities differ from reviewed inventory')
    except (OSError, ValueError, ET.ParseError) as error:
        issues.append(f'Invalid source reactor: {error}')
    return reactor


def audit_skips(cases, inventory, lane, issues):
    skipped = [c for c in cases if c['result'] == 'skipped']
    permitted = {(c['module'], c['case']): c['message_contains'] for c in inventory['permitted_baseline_skips']}
    actual_skips = {(c['module'], c['class'] + '#' + c['name']) for c in skipped}
    if lane in ('baseline', 'baseline21'):
        valid_skips = actual_skips == set(permitted)
    elif lane == 'snapshot':
        valid_skips = not skipped
    else:
        valid_skips = actual_skips <= set(permitted)
    if not valid_skips or any(
            permitted.get((c['module'], c['class'] + '#' + c['name']), '__unexpected__') not in (c['message'] or '')
            for c in skipped):
        issues.append('Unexpected security API skips')


def audit_cachestore(cases, inventory, issues):
    its = [c for c in cases if c['class'].endswith('.CacheStoreDemoIT')]
    if (collections.Counter(c['class'] + '#' + c['name'] for c in its) != collections.Counter(inventory['required_cachestore_cases'])
            or any(c['result'] != 'passed' for c in its)):
        issues.append('All four ordinary CacheStore Failsafe methods must pass')


def audit_bytecode(source, issues):
    bytecode = collections.Counter()
    for target in source.glob('**/target'):
        for directory in ['classes', 'test-classes']:
            for path in (target / directory).rglob('*.class'):
                bytecode[int.from_bytes(path.read_bytes()[6:8], 'big')] += 1
    if set(bytecode) != {61}:
        issues.append(f'Expected release-17 class major 61: {bytecode}')
    return bytecode


def audit_package(path, expected, version, packaged, issues):
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


def audit_packages(source, expected, version, issues):
    packaged = []
    for path in list(source.glob('**/target/*.jar')) + list(source.glob('**/target/*.war')):
        audit_package(path, expected, version, packaged, issues)
    return packaged


def audit(source, output, lane, version, java, mode, repository, status, *, expected_home):
    """Audit evidence against a trusted JDK supplied independently of the manifest.

    Replay callers must supply their original, independently trusted expected home;
    neither invocation.json nor observed JVM properties may set that expectation.
    """
    home = Path(expected_home)
    if not home.is_absolute():
        raise ValueError('expected_home must be an absolute trusted JDK home')
    home = home.resolve()
    issues, dependencies = [], {}
    resolved = cache_files(repository, version)
    expected = {a['artifact']: a for a in resolved}

    def properties(props, origin):
        audit_jvm_properties(props, origin, java, home, mode, issues)
        audit_classpath(props, origin, expected, version, dependencies, issues)

    inventory = json.loads(INVENTORY.read_text())
    invocation = json.loads((output / INVOCATION_FILE).read_text()) if (output / INVOCATION_FILE).is_file() else {}
    # The manifest records the canonical live home. Compare it as data, without
    # resolving a filesystem path chosen by the evidence being audited.
    if invocation.get('java_home') != str(home):
        issues.append('Invocation JDK home differs from the trusted expected home')
    if invocation.get('inventory_sha256', digest(INVENTORY)) != digest(INVENTORY):
        issues.append('Reviewed inventory changed during the run')
    observations = output / 'jvms.json'
    rows = json.loads(observations.read_text()) if observations.is_file() else []
    runtime = audit_runtime(rows, inventory, source, properties, issues)
    modules, cases, expected_reports, expected_cases = audit_reactor_reports(source, inventory, properties, issues)
    audit_report_discovery(source, inventory, expected_reports, issues)
    totals = dict(sum(modules.values(), collections.Counter()))
    reactor = audit_reactor_identity(source, output, inventory, status, issues)
    if (totals.get('tests', 0) != len(expected_cases) or totals.get('suites', 0) != len(expected_reports)
            or totals.get('failures') or totals.get('errors')):
        issues.append('Complete passing reviewed reactor suite/case inventory is required')
    audit_skips(cases, inventory, lane, issues)
    audit_cachestore(cases, inventory, issues)
    consumers = audit_consumers(source, inventory['consumers'], expected, java, home, mode, properties)
    issues.extend(consumers['issues'])
    bytecode = audit_bytecode(source, issues)
    packaged = audit_packages(source, expected, version, issues)
    (output / 'resolved-coherence.json').write_text(json.dumps(resolved, indent=2))
    result = {'lane': lane, 'coherence_version': version, 'compiler_release': 17, 'maven_exit': status,
              'qualification_scope': invocation.get('qualification_scope', 'standard'),
              'hibernate_version': invocation.get('hibernate_version'),
              'inventory_sha256': digest(INVENTORY), 'totals': totals, 'modules': modules, 'reactor': reactor, 'cases': cases,
              'auditor_sha256': {p.name: digest(p) for p in [Path(__file__), Path(__file__).with_name('consumer_evidence.py')]},
              'consumers': consumers,
              'jvms': runtime, 'dependencies': list(dependencies.values()), 'coherence_cache_after': resolved,
              'packaged_coherence': packaged, 'class_major_counts': bytecode, 'issues': issues}
    # S2083 reviewed: evidence fields are JSON content; output and the basename select the destination.
    (output / 'qualification.json').write_text(json.dumps(result, indent=2))  # NOSONAR(S2083)
    print(json.dumps({'totals': totals, 'class_major_counts': bytecode, 'issues': issues}, indent=2))
    return bool(issues)


def parse_arguments():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--lane', choices=['baseline', 'baseline21', 'snapshot', 'requested'], required=True,
                        help='baseline/baseline21: default 26.04 on JDK 17/21, with identical strict skip policy')
    parser.add_argument('--version', help='Only for the optional requested lane')
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--maven', default='./mvnw',
                        help='Repository wrapper, or cached launcher approved by FULL_REACTOR_TRUSTED_MAVEN')
    parser.add_argument('--repository', type=Path, help='Disposable repository for a local exercise; CI uses its ordinary Maven cache')
    parser.add_argument('--offline', action='store_true', help='Local cached exercise only')
    parser.add_argument('--local-frozen', action='store_true',
                        help='Explicit offline frozen-input exercise, never fresh published-snapshot qualification')
    parser.add_argument('--hibernate-version', help='Explicit provider selection for a local frozen exercise')
    parser.add_argument('--jvm-tmpdir', type=Path, help='Task-owned JVM temporary directory for a local exercise')
    args = parser.parse_args()
    version = {'baseline': '26.04', 'baseline21': '26.04', 'snapshot': '26.10-SNAPSHOT'}.get(args.lane, args.version)
    if not version or not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9_.-]*', version) or (args.version and args.lane != 'requested'):
        parser.error('A safe explicit version is required only for the requested lane')
    if args.local_frozen and (not args.offline or not args.repository):
        parser.error('Local frozen exercises require --offline and a disposable --repository')
    if args.hibernate_version and (not args.local_frozen or not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9_.-]*', args.hibernate_version)):
        parser.error('A safe provider version may be selected only for a local frozen exercise')
    if args.offline and version.endswith('-SNAPSHOT') and not args.local_frozen:
        parser.error('Snapshot qualification must resolve the latest published artifacts online')
    return parser, args, version


def maven_environment(args, mode, env):
    for name in ['JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', '_JAVA_OPTIONS', 'MAVEN_ARGS', 'MAVEN_OPTS']:
        env.pop(name, None)
    if args.offline:
        # Maven 3.9+ consumes this in both the outer launcher and Invoker's
        # independent Maven launchers; the outer -o alone is not inherited.
        env['MAVEN_ARGS'] = '-o'
    if args.hibernate_version:
        env['MAVEN_ARGS'] += f' -Dcoherence-hibernate.version={args.hibernate_version}'
    if mode:
        env['JAVA_TOOL_OPTIONS'] = '-Dcoherence.security.mode=hardened'
    if args.jvm_tmpdir:
        args.jvm_tmpdir.mkdir(parents=True, exist_ok=True)
        env['JAVA_TOOL_OPTIONS'] = (env.get('JAVA_TOOL_OPTIONS', '')
                                   + f' -Djava.io.tmpdir={args.jvm_tmpdir.resolve()}').strip()
    return env


def maven_command(args, maven, repository, version, mode):
    command = [maven, '-B', '-ntp', '-Pjdk17', '-Dmaven.test.failure.ignore=false',
               f'-Dcoherence.version={version}', '--settings', '.mvn/settings.xml', '--fail-at-end']
    if version.endswith('-SNAPSHOT') and not args.local_frozen:
        command.append('-U')
    if args.hibernate_version:
        command.append(f'-Dcoherence-hibernate.version={args.hibernate_version}')
    if args.local_frozen:
        # Outer context tests must not consult the sample's optional Config
        # Server. The packaged integration starts its own native local server.
        command.append('-Dspring.cloud.config.enabled=false')
    if mode:
        command.append('-Dcoherence.security.mode=hardened')
    if args.repository:
        command.append(f'-Dmaven.repo.local={repository}')
    if args.offline:
        command.append('-o')
    command += ['clean', 'verify']
    return command


def invocation_manifest(args, command, runtime, home, mode, env):
    manifest = {'command': command, 'runtime': runtime, 'java_home': str(home),
                'qualification_scope': 'local-frozen-offline' if args.local_frozen else 'standard',
                'hibernate_version': args.hibernate_version, 'MAVEN_ARGS': env.get('MAVEN_ARGS'),
                'inventory_sha256': digest(INVENTORY), 'security_mode': mode,
                'JAVA_TOOL_OPTIONS': env.get('JAVA_TOOL_OPTIONS'), 'started': datetime.datetime.now(datetime.timezone.utc).isoformat(),
                'source_sha256': {str(p.relative_to(ROOT)): digest(p) for p in ROOT.rglob('*')
                                  if p.is_file() and '.git' not in p.parts and 'target' not in p.parts
                                  and (p.suffix in ['.java', '.xml'] or p.name == 'pom.xml')
                                  and p.name != '.flattened-pom.xml'}}
    return manifest


def run_maven(command, output, env):
    stop = threading.Event()
    with (output / MAVEN_LOG).open('w') as log:
        process = subprocess.Popen(command, cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, env=env)
        watcher = threading.Thread(target=observe, args=(process.pid, stop, output / 'jvms.json', env), daemon=True)
        watcher.start()
        for line in process.stdout:
            log.write(line)
            print(line, end='', flush=True)
        status = process.wait()
        stop.set()
        watcher.join(timeout=15)
    return status


def main():
    parser, args, version = parse_arguments()
    java, mode = {'baseline': ('17', None), 'baseline21': ('21', None)}.get(args.lane, ('21', 'hardened'))
    env = os.environ.copy()
    try:
        maven = trusted_maven(args.maven, env)
        output = prepare_output(args.output)
    except (OSError, ValueError) as error:
        parser.error(str(error))
    env = maven_environment(args, mode, env)
    home = Path(env['JAVA_HOME']).resolve()
    env['JAVA_HOME'] = str(home)
    runtime = subprocess.check_output([str(home / 'bin/java'), '-XshowSettings:properties', '-version'],
                                      text=True, stderr=subprocess.STDOUT, env=env)
    if not re.search(r'java.specification.version = ' + java + r'\s', runtime):
        parser.error(f'{args.lane} requires JDK {java}')
    repository = (args.repository or Path.home() / '.m2/repository').resolve()
    command = maven_command(args, maven, repository, version, mode)
    manifest = invocation_manifest(args, command, runtime, home, mode, env)
    (output / INVOCATION_FILE).write_text(json.dumps(manifest, indent=2))
    (output / 'reviewed-inventory.json').write_bytes(INVENTORY.read_bytes())
    status = run_maven(command, output, env)
    manifest.update(maven_exit=status, ended=datetime.datetime.now(datetime.timezone.utc).isoformat())
    (output / INVOCATION_FILE).write_text(json.dumps(manifest, indent=2))
    return 1 if audit(ROOT, output, args.lane, version, java, mode, repository, status,
                      expected_home=home) else 0


if __name__ == '__main__':
    raise SystemExit(main())
