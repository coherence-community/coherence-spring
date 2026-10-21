"""Independently audit packaged-consumer evidence contract v1.

The fixture/method list belongs to src/it/inventory.properties. Invoker JUnit
wrappers are build results, never reactor or consumer test cases.
"""

import collections
import os
from pathlib import Path
import re
import xml.etree.ElementTree as ET
import zipfile

from coherence_artifacts import digest


def load_properties(path):
    """Read Java Properties.store output (including escaped coordinate keys)."""
    result = {}
    text = path.read_text(encoding='iso-8859-1')
    text = re.sub(r'(?<!\\)\\\r?\n\s*', '', text)

    def unescape(value):
        return re.sub(r'\\(u[0-9a-fA-F]{4}|.)', lambda m: (
            chr(int(m[1][1:], 16)) if m[1].startswith('u') else
            {'t': '\t', 'n': '\n', 'r': '\r', 'f': '\f'}.get(m[1], m[1])), value)

    for line in text.splitlines():
        line = line.lstrip()
        if not line or line.startswith(('#', '!')):
            continue
        match = re.match(r'((?:\\.|[^:=\s])*)\s*[:=]?\s*(.*)', line)
        key, value = (unescape(v) for v in match.groups())
        if key in result:
            raise ValueError(f'Duplicate property {key}: {path}')
        result[key] = value
    return result


def junit(path):
    """Validate counters against actual cases, including duplicate identities."""
    suite = ET.parse(path).getroot()
    if suite.tag != 'testsuite':
        raise ValueError(f'Expected testsuite: {path}')
    cases = []
    for case in suite.findall('testcase'):
        outcomes = [k for k in ('failure', 'error', 'skipped') if case.find(k) is not None]
        if len(outcomes) > 1:
            raise ValueError(f'Multiple outcomes: {path}')
        kind = outcomes[0] if outcomes else 'passed'
        element = case.find(kind)
        cases.append({'class': case.get('classname'), 'name': case.get('name'),
                      'result': kind, 'message': element.get('message') if element is not None else None})
    identities = [(c['class'], c['name']) for c in cases]
    if len(set(identities)) != len(identities):
        raise ValueError(f'Duplicate test cases: {path}')
    counts = collections.Counter(c['result'] for c in cases)
    for attr, count in [('tests', len(cases)), ('failures', counts['failure']),
                        ('errors', counts['error']), ('skipped', counts['skipped'])]:
        if int(suite.get(attr, '0')) != count:
            raise ValueError(f'JUnit {attr} counter disagrees with cases: {path}')
    return suite, cases


def dependencies(path):
    rows = []
    for line in path.read_text().splitlines():
        match = re.fullmatch(r'\s*([^:\s]+):([^:]+):jar:(?:([^:]+):)?([^:]+):'
                             r'(compile|test|runtime|provided|system):(.+?)(?: --.*)?\s*', line)
        if match:
            group, artifact, classifier, version, scope, filename = match.groups()
            key = f'{group}:{artifact}:{version}' + (':' + classifier if classifier else '')
            rows.append((key, scope, Path(filename.strip())))
    if not rows:
        raise ValueError(f'No dependency identities: {path}')
    if len({key for key, _, _ in rows}) != len(rows):
        raise ValueError(f'Duplicate dependency identities: {path}')
    return rows


def check_classes(path, issues, origin):
    with zipfile.ZipFile(path) as archive:
        for name in archive.namelist():
            if name.endswith('.class') and int.from_bytes(archive.read(name)[6:8], 'big') != 61:
                issues.append(f'{origin}: expected release-17 bytecode: {path}!{name}')


def audit_consumers(source, inventory, expected, java, home, mode, properties):
    """Return independent fixture results and all failures; never trust PASS alone."""
    issues, results = [], {}
    module = source / inventory['module']
    target = module / 'target'

    def require(condition, message):
        if not condition:
            issues.append('Consumers: ' + message)

    try:
        canonical = load_properties(source / inventory['inventory'])
        fixture_dirs = {p.parent.name for p in (module / 'src/it').glob('*/pom.xml')}
        require(set(canonical) == fixture_dirs, 'source fixture inventory differs')
        copied = load_properties(target / 'consumer-evidence/inventory.properties')
        require(canonical == copied, 'copied fixture inventory differs')
        methods = {name: value.split(',') for name, value in canonical.items()}
        require(all(len(set(v)) == len(v) and all(v) for v in methods.values()), 'duplicate/empty methods')
        summary = load_properties(target / 'consumer-evidence/summary.properties')
        for key, value in {'result': 'PASS', 'fixtures': str(len(methods)),
                           'tests': str(sum(map(len, methods.values()))), 'failures': '0',
                           'errors': '0', 'skips': '0'}.items():
            require(summary.get(key) == value, f'summary {key} != {value}')
        outer = load_properties(target / 'consumer-evidence/outer-artifacts.properties')
        outer_rows = dependencies(target / 'consumer-evidence/outer-dependencies.txt')
        # Product coordinates are taken from the owning reactor POM, not fixture claims.
        ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
        pom = ET.parse(source / 'pom.xml').getroot()
        product_group = pom.findtext('m:groupId', namespaces=ns)
        product_version = pom.findtext('m:version', namespaces=ns)
        boot_version = pom.findtext('m:properties/m:spring-boot.version', namespaces=ns)
        required_outer = {k for k, _, _ in outer_rows}
        required_outer.add(f'{product_group}:{module.name}:{product_version}')
        require(set(outer) == {k + '.' + ext + '.' + field for k in required_outer
                              for ext in ('jar', 'pom') for field in ('sha256', 'source')},
                'outer manifest coordinate/field inventory differs')
        for key in required_outer:
            group, artifact, version = key.split(':')
            require(group in (product_group, 'com.oracle.coherence.ce'), f'unexpected outer group: {key}')
            if group == product_group:
                require(version == product_version, f'mixed outer product version: {key}')
            for ext in ('jar', 'pom'):
                path = Path(outer[key + '.' + ext + '.source'])
                require(path.is_file() and digest(path) == outer[key + '.' + ext + '.sha256'],
                        f'outer {ext} bytes drift: {key}')
            if group == 'com.oracle.coherence.ce':
                actual = expected.get(artifact)
                require(actual is not None and actual['version'] == version
                        and actual['sha256'] == outer[key + '.jar.sha256']
                        and actual['pom_sha256'] == outer[key + '.pom.sha256'],
                        f'outer Coherence differs from this run: {key}')
        for prefix in ('BUILD-', 'TEST-'):
            require({p.name for p in (target / 'invoker-reports').glob(prefix + '*.xml')}
                    == {prefix + f + '.xml' for f in methods}, f'Invoker {prefix} inventory differs')
        require({p.name for p in (target / 'it').iterdir() if p.is_dir()} == set(methods),
                'cloned fixture inventory differs')
    except (OSError, ValueError, KeyError, ET.ParseError) as error:
        return {'fixtures': results, 'issues': issues + [f'Consumers: missing/invalid outer evidence: {error}']}

    checked_jars = set()
    for fixture, names in sorted(methods.items()):
        origin = f'consumer:{fixture}'
        clone = target / 'it' / fixture
        build = clone / 'target'
        try:
            require((clone / 'build.log').stat().st_size > 0, f'{fixture}: empty build log')
            invoker = ET.parse(target / f'invoker-reports/BUILD-{fixture}.xml').getroot()
            require(invoker.get('result') == 'success', f'{fixture}: Invoker failed')
            require(invoker.get('project') == fixture + '/pom.xml', f'{fixture}: Invoker project identity differs')
            wrapper, wrapper_cases = junit(target / f'invoker-reports/TEST-{fixture}.xml')
            require(len(wrapper_cases) == 1 and wrapper_cases[0]['result'] == 'passed',
                    f'{fixture}: Invoker JUnit wrapper failed')
            require(wrapper.get('name') == 'maven.invoker.it.' + fixture
                    and [(c['class'], c['name']) for c in wrapper_cases] == [('maven.invoker.it.' + fixture, fixture)],
                    f'{fixture}: Invoker JUnit wrapper identity differs')
            reports = sorted((build / 'failsafe-reports').glob('TEST-*.xml'))
            require(len(reports) == 1, f'{fixture}: expected one Failsafe suite')
            require(not list((build / 'surefire-reports').glob('TEST-*.xml')),
                    f'{fixture}: unexpected Surefire consumer reports')
            cases = []
            for path in reports:
                suite, tests = junit(path)
                require(suite.get('name') == inventory.get('test_class', 'consumer.ConsumerIT'),
                        f'{fixture}: consumer suite identity differs')
                props = {p.get('name'): p.get('value') for p in suite.findall('properties/property')}
                properties(props, origin)
                require(props.get('maven.test.failure.ignore') == 'false', f'{fixture}: fatal failures evidence missing')
                require(Path(props.get('java.home', '')).resolve() == home, f'{fixture}: JDK home differs')
                cases += tests
            require(collections.Counter((c['class'], c['name']) for c in cases)
                    == collections.Counter((inventory.get('test_class', 'consumer.ConsumerIT'), n) for n in names),
                    f'{fixture}: exact consumer cases differ')
            require(all(c['result'] == 'passed' for c in cases), f'{fixture}: failed/skipped consumer')
            failsafe = ET.parse(build / 'failsafe-reports/failsafe-summary.xml').getroot()
            require(failsafe.get('timeout') == 'false', f'{fixture}: Failsafe timeout/missing outcome')
            for key, value in {'completed': str(len(names)), 'errors': '0', 'failures': '0', 'skipped': '0'}.items():
                require(failsafe.findtext(key) == value, f'{fixture}: Failsafe {key} differs')
            require(failsafe.find('failureMessage') is None or not failsafe.findtext('failureMessage'),
                    f'{fixture}: Failsafe failure message')
            records = dependencies(build / 'test-dependencies.txt')
            graph = (build / 'runtime-dependencies.txt').read_text()
            runtime = (build / 'runtime-classpath.txt').read_text().strip()
            require(bool(graph.strip()) and bool(runtime), f'{fixture}: empty dependency evidence')
            require({p.name for p in (build / 'consumer-evidence').glob('*.properties')}
                    == {n + '.properties' for n in names}, f'{fixture}: method evidence inventory differs')
            fixture_artifacts = {key for key, _, _ in records if key.split(':')[0]
                                 in (product_group, 'com.oracle.coherence.ce')}
            require(len(fixture_artifacts) >= 2, f'{fixture}: incomplete packaged artifacts')
            for key, _, path in records:
                require(path.is_file(), f'{fixture}: missing dependency {path}')
                if key in fixture_artifacts:
                    require(key in required_outer, f'{fixture}: unexpected product/Coherence dependency {key}')
                    require(path.is_relative_to(target / 'it-repository'), f'{fixture}: dependency outside isolated repository')
                    for ext in ('jar', 'pom'):
                        actual = path.with_suffix('.' + ext)
                        require(actual.is_file() and digest(actual) == outer.get(key + '.' + ext + '.sha256'),
                                f'{fixture}: {ext} byte drift: {key}')
                    if key.startswith(product_group + ':') and path not in checked_jars:
                        check_classes(path, issues, origin)
                        checked_jars.add(path)
                if key.startswith('org.springframework.boot:'):
                    require(key.split(':')[2] == boot_version, f'{fixture}: Boot dependency version differs')
            for name in names:
                evidence = load_properties(build / f'consumer-evidence/{name}.properties')
                for key, value in {'result': 'PASS', 'scenario': name, 'consumer.fixture': fixture,
                                   'consumer.release': '17', 'java.specification.version': java,
                                   'consumer.coherence.group': 'com.oracle.coherence.ce',
                                   'consumer.coherence.version': expected['coherence']['version'],
                                   'consumer.boot.version': boot_version,
                                   'consumer.security.mode': mode or 'unset',
                                   'coherence.security.mode': mode or '<absent>'}.items():
                    require(evidence.get(key) == value, f'{fixture}/{name}: {key} != {value}')
                require(evidence.get('java.version', '').split('.')[0] == java, f'{fixture}/{name}: incorrect JDK')
                require(Path(evidence.get('java.home', '')).resolve() == home, f'{fixture}/{name}: incorrect JDK home')
                require({k for k in evidence if k.endswith(('.jar.sha256', '.pom.sha256'))
                         and not k.startswith('war.loaded.')}
                        == {k + '.' + ext + '.sha256' for k in fixture_artifacts for ext in ('jar', 'pom')},
                        f'{fixture}/{name}: compared artifact inventory differs')
                for key in fixture_artifacts:
                    for ext in ('jar', 'pom'):
                        field = key + '.' + ext + '.sha256'
                        require(evidence.get(field) == outer.get(field), f'{fixture}/{name}: artifact evidence drift: {field}')
                actual_cp = evidence.get('java.class.path', '')
                require(all(str(p) in actual_cp.split(os.pathsep) for k, _, p in records if k in fixture_artifacts),
                        f'{fixture}/{name}: packaged dependency absent from actual classpath')
                all_dependencies = graph + runtime + actual_cp + (build / 'test-dependencies.txt').read_text()
                for forbidden in ('coherence-spring-tests', 'mockito', 'assertj', 'bedrock', 'coherence-grpc-proxy'):
                    require(forbidden not in all_dependencies, f'{fixture}: unexpected dependency {forbidden}')
                for forbidden in ('junit', 'spring-test', 'test-classes', 'target/classes'):
                    require(forbidden not in runtime, f'{fixture}: unexpected runtime dependency {forbidden}')
                require(not re.search(r'coherence-spring-[^/]+/target/(test-)?classes', actual_cp),
                        f'{fixture}: reactor classpath leakage')
                for entry in runtime.split(os.pathsep):
                    require(Path(entry).is_file() and entry.endswith('.jar'), f'{fixture}: runtime dependency is not a JAR: {entry}')
                if fixture.startswith('general-'):
                    require('org.springframework.session:' not in all_dependencies
                            and '/org/springframework/session/' not in all_dependencies
                            and 'spring-boot-session' not in all_dependencies,
                            f'{fixture}: Session dependency leaked')
                if fixture == 'general-starter':
                    require('jakarta.servlet-api' not in graph + actual_cp, f'{fixture}: servlet API leaked')
                if fixture == 'plain-spring-session':
                    require('org.springframework.boot:' not in graph and '/org/springframework/boot/' not in actual_cp,
                            f'{fixture}: Boot dependency leaked')
                if fixture == 'library-without-boot-session':
                    require('spring-boot-session' not in all_dependencies, f'{fixture}: Boot Session leaked')
                if fixture == 'boot-session-without-coherence-session':
                    require(':coherence-spring-session:' not in all_dependencies
                            and '/coherence-spring-session/' not in all_dependencies,
                            f'{fixture}: Coherence Session leaked')
                if fixture in ('session-starter', 'non-web', 'custom-repository', 'boot-war'):
                    for artifact in ('coherence-spring-session-boot-starter', 'coherence-spring-boot-starter',
                                     'coherence-spring-session', 'spring-boot-session'):
                        require(':' + artifact + ':jar:' in graph, f'{fixture}: missing integration dependency {artifact}')
                loaded = Path(evidence.get('loaded.coherence.jar', ''))
                require(loaded.is_file() and digest(loaded) == expected['coherence']['sha256'],
                        f'{fixture}/{name}: loaded Coherence bytes drift')
                require(loaded in {p for k, _, p in records if k == 'com.oracle.coherence.ce:coherence:' + expected['coherence']['version']},
                        f'{fixture}/{name}: loaded Coherence outside dependency inventory')
                if fixture == 'boot-war':
                    loaded = Path(evidence.get('war.loaded.coherence.jar', ''))
                    require('WEB-INF/lib/' in str(loaded) and loaded.is_file()
                            and digest(loaded) == expected['coherence']['sha256']
                            and evidence.get('war.loaded.coherence.sha256') == expected['coherence']['sha256'],
                            f'{fixture}/{name}: loaded WAR Coherence bytes drift')
            results[fixture] = cases
        except (OSError, ValueError, KeyError, ET.ParseError, zipfile.BadZipFile) as error:
            issues.append(f'{origin}: missing/invalid fixture evidence: {error}')
    return {'fixtures': results, 'issues': issues}
