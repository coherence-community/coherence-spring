# Latest published Coherence snapshot: local qualification

Recorded October 19, 2026 at 12:40:48 CEST (UTC+0200; Europe/Belgrade).

The actual updated snapshot CI runner **passes** the current source on branch
`5.x`, HEAD `37cf05ea59bd60c03b21b52acc1eb4c1045859a3`, against newly downloaded
published `26.10-SNAPSHOT` dependencies. All **28 modules succeed; 706/706 cases
pass across 83 suites**, zero failures, errors or skips. All four CacheStore
integration methods pass through ordinary Failsafe. Runner and Maven both exit 0;
the runner audit and separate independent audit report no issues.

This result qualifies this bounded local invocation and its actual inputs. It
supersedes the lack of latest-published-snapshot local evidence in the assessment;
historical frozen passes were neither inputs nor acceptance evidence.

## Invocation and isolation

Run: **19 October 2026, 12:19:50–12:38:07 CEST (UTC+0200;
Europe/Belgrade)**. Maven reports **18:15 min**. Runtime: Temurin
`21.0.12.1+1-LTS`; Maven `3.9.9`, matching the wrapper distribution.
The unchanged actual runner was invoked in `/private/tmp/codex-work/spring-latest-snapshot-p3e4za53/source`, a byte-identical copy
of every task-start tracked/untracked input, so normal `clean verify` could not
delete existing checkout build output. The independent audit checks that every
copied input still matches the initial manifest. Existing staged/unstaged/untracked
work, index, normal Maven cache and previous tasks' evidence are preserved.

```sh
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
python3 .github/scripts/full_reactor.py --lane snapshot \
  --maven /Users/vasac/.m2/wrapper/dists/apache-maven-3.9.9-bin/33b4b2b4/apache-maven-3.9.9/bin/mvn \
  --repository /private/tmp/codex-work/spring-latest-snapshot-p3e4za53/repository \
  --jvm-tmpdir /private/tmp/codex-work/spring-latest-snapshot-p3e4za53/jvm \
  --output /private/tmp/codex-work/spring-latest-snapshot-p3e4za53/evidence/run
```

The exact generated Maven command (also in `run/invocation.json`) is:

```sh
/Users/vasac/.m2/wrapper/dists/apache-maven-3.9.9-bin/33b4b2b4/apache-maven-3.9.9/bin/mvn -B -ntp -Pjdk17 -Dmaven.test.failure.ignore=false -Dcoherence.version=26.10-SNAPSHOT --settings .mvn/settings.xml --fail-at-end -U -Dcoherence.security.mode=hardened -Dmaven.repo.local=/private/tmp/codex-work/spring-latest-snapshot-p3e4za53/repository clean verify
```

The runner strips inherited Java/Maven option variables and sets
`JAVA_TOOL_OPTIONS=-Dcoherence.security.mode=hardened -Djava.io.tmpdir=/private/tmp/codex-work/spring-latest-snapshot-p3e4za53/jvm`.
It also explicitly passes the hardening property to Maven. No test selectors,
skips, ignored failures, style suppressions, offline flag, historical timestamp
inputs, historical lock, or production/security-policy corrections were used.
The historical lock exists only as an inert documentation file in the copied
source; no runner or Maven resolution reads it. The selected repository was
created empty and populated by online Maven resolution, including Micrometer;
no artifacts were copied from the user's cache or historical builds.

## Actual results and independent verification

Parsed the actual reactor summary and all 83 Surefire/Failsafe XML files, including
every testcase element, rather than relying on the process exit. All expected
counts match current source; no count change or weakened check was needed.

| Test-bearing module | Passed | Suites |
| --- | ---: | ---: |
| `coherence-spring-boot-starter` | 50 | 12 |
| `coherence-spring-core` | 168 | 32 |
| `coherence-spring-data` | 335 | 5 |
| `coherence-spring-docs` | 11 | 8 |
| `coherence-spring-session` | 117 | 11 |
| `samples/cachestore-demo/coherence-spring-cachestore-demo-app` | 10 | 3 |
| `samples/circuit-breaker-cache-demo/circuit-breaker-cache-demo-app` | 2 | 2 |
| `samples/circuit-breaker-cache-demo/circuit-breaker-cache-demo-server` | 1 | 1 |
| `samples/coherence-spring-cloud-config-demo/coherence-spring-cloud-config-demo-app` | 2 | 2 |
| `samples/coherence-spring-cloud-config-demo/coherence-spring-cloud-config-demo-server` | 1 | 1 |
| `samples/coherence-spring-demo/coherence-spring-demo-boot` | 1 | 1 |
| `samples/hibernate-cache-demo` | 1 | 1 |
| `samples/spring-session-demo/spring-session-demo-app` | 6 | 3 |
| `samples/spring-session-demo/spring-session-demo-server` | 1 | 1 |
| **Total** | **706** | **83** |

Ordinary Failsafe methods in `CacheStoreDemoIT`:

- `shouldGetPersonInDB`
- `shouldCreatePerson`
- `shouldUpdatePerson`
- `shouldNotGetPersonNotInDB`

The independent audit rechecks each suite's actual JDK 21, explicit hardened mode,
fatal test-failure property and absence of dynamic unauthenticated-code override.
Eleven Checkstyle XML reports contain zero violations. Normal Javadoc, Docs
HTML/PDF/ZIP, Website and JAR/WAR packaging complete in the normal reactor.
Seven Python runner regression tests also pass. No hosted workflow was dispatched.

## Child JVM and Java 17 output evidence

The runner used JDK `jcmd VM.system_properties` on actual descendants, retaining
**88 JVM observations**: Maven, 77 test forks, eight library Coherence children,
one CacheStore child and HSQLDB. **Every observation has JDK 21 and explicit
`coherence.security.mode=hardened`**; none has the forbidden dynamic-code override.
The complete expected eight cluster names plus CacheStore child are present.

| Child role | PID | Cluster property | Actual mode |
| --- | ---: | --- | --- |
| coherence | 37045 | `CoherenceExtendTestsCluster` | hardened |
| coherence | 37064 | `GrpcSessionBeanTestsCluster` | hardened |
| coherence | 37092 | `GrpcSessionTestsCluster` | hardened |
| coherence | 37352 | `ExtendSessionTestsCluster` | hardened |
| coherence | 37553 | `GrpcSessionCoherenceIndexedSessionRepositoryTestsCluster` | hardened |
| coherence | 37792 | `PofCoherenceIndexedSessionRepositoryTestsCluster` | hardened |
| coherence | 38291 | `CoherenceConfigDataLoaderTests` | hardened |
| coherence | 38325 | `CoherenceGrpcClientTestsCluster` | hardened |
| database | 40584 | `(not configured)` | hardened |
| cachestore | 40585 | `(not configured)` | hardened |

`run/jvms.json` and `run/qualification.json` preserve actual properties, roles,
commands and classpaths. Every suite's XML independently records the runtime
properties. All **688 compiled production/test classes** have major **61**
(Java 17). The independent audit also inspects packaged application class entries
and confirms major 61; bundled dependency and Boot launcher class versions are
outside the application's compiler-output claim. No JDK 17 runtime row was rerun
here; this task explicitly qualifies the JDK 21 snapshot lane targeting Java 17.

## Resolved published artifact identity

Configured published endpoint:
`https://central.sonatype.com/repository/maven-snapshots`.
Settings ID `sonatype-snapshots` and POM IDs `snapshots-repo` /
`coherence-snapshots` all map to this same endpoint. The independent audit checks
raw `_remote.repositories` records for the downloaded timestamped JARs (Micrometer's
JAR records `coherence-snapshots`, its POM `sonatype-snapshots`). No local-copy
snapshot metadata exists. Each alias matches exactly one downloaded timestamped
JAR by SHA-256; each repository's ordinary-JAR metadata matches that identity.

All ten selected Coherence dependencies have Maven version `26.10-SNAPSHOT`.
Core's `20261014.235911-4` timestamp and the companion artifacts'
`20261015.000224-4` timestamp are their actual published identities; consistent
Maven versions do not imply identical publication timestamps across artifacts.

| Artifact | Actual timestamped version | JAR SHA-256 | POM SHA-256 |
| --- | --- | --- | --- |
| `coherence` | `26.10-20261014.235911-4` | `5fd52c564bb3b550c289564150e0ac30a5e704f2c5ad8c631da75323d5c70ef2` | `09bdcbe01a7266abacde4973f28d179bff41c7939c163cc05553292cf38a61bd` |
| `coherence-bedrock` | `26.10-20261015.000224-4` | `bc03696c9b7dfdf4180b576ea89e60b002bdab1162185b46bf263b631cc4ec09` | `6585a2a4b855b013c7387088b10a14b65f3c078a2dfeed608f36e8df80ea49d5` |
| `coherence-bedrock-testing-support` | `26.10-20261015.000224-4` | `b2635fb43bcde27536f302f2156ea15e25745ac1b7dcc59d176943959d3e6011` | `7d3febc194fcfeae1b047c23a9b836966ae1bbfa0bdbe31a133b4df1d1aef5ca` |
| `coherence-grpc` | `26.10-20261015.000224-4` | `db5b725b5ce3359a350f44dee6fbaa4bee6e358d88571b2bfece763fd86e601f` | `81ef5fcd318e83e4fbf18612aaad6e550b3e5b3bdca91a018611a4c82ec19ad1` |
| `coherence-grpc-client-common` | `26.10-20261015.000224-4` | `7d6381cc1cde5904e28f3a5db92856b7fb887423c7b9a9e1ffaac26449e9c5b3` | `831a56d1772301b675ab327f43bca5f768452d5621d5d906cdb30dcb49697bce` |
| `coherence-grpc-proxy` | `26.10-20261015.000224-4` | `e6d3c7e2512885f280afd56971d357708c04ead874d496d7845dd5ca20831640` | `427d743312f7f7fa4be16f7f8fbbc6cbee5b179c507da7d1eddc43451ec22605` |
| `coherence-grpc-proxy-common` | `26.10-20261015.000224-4` | `3a967a8cd2cbadc928e1828f4d21436fae3c0fc5144d48cd6a0e7ae3600e47e9` | `0b1168086847ecc1e3d9714a9c56997ebb167927072d13976a868f4243d6ca95` |
| `coherence-java-client` | `26.10-20261015.000224-4` | `e93c61e0efee22afd5576e574700665825f6fd0bf3e67b70f3ad2c14a48ddbb5` | `a3092667cff90f041bba950d5a0f613051d900fa5cda8ffa7b4f60b1961cbb91` |
| `coherence-micrometer` | `26.10-20261015.000224-4` | `a0fd591d37d4f9f3b1c516fd96eeeb2234a8bdfb1a7824b80af80bad1977de70` | `ffe9b511e7717eefd37e28e6bddce9f6bbfc5b48d880b9daf2db88ce6b3b7a50` |
| `coherence-protobuf` | `26.10-20261015.000224-4` | `23c747d0b61945ddcdfa0c74dce63c9cec1aec9caf11f9a7aeb8a59a237cc62e` | `610e3ef2ab229ef8b04ee512d27685abb2b412bb37bd73e9e09405c6777d7281` |

These hashes and timestamp metadata are **evidence of this run, not a lock or
future pass criterion**. Maven used `-U`; no release was substituted. A future
snapshot run must resolve the then-current publication. Pin a release only once
26.10 is released.

The runner records **298 dependency JAR fingerprints** from actual runtime
classpaths. All resolved Coherence classpath JAR/POM hashes match this run's cache;
no mixed release versions are present. The runner and independent audit compare
all **14 embedded Coherence JAR entries** in produced Boot JARs / WARs against
these resolved bytes and find exact matches. Full per-package paths, entry names
and hashes are in both audit manifests; built-artifact hashes are retained too.

## Evidence and limits

Retained compact review evidence: `/private/tmp/codex-work/spring-latest-snapshot-p3e4za53/evidence/`.

- `run/invocation.json`: actual command, runtime, source fingerprints, start/end and exit.
- `run/qualification.json`, `run/resolved-coherence.json`, `run/jvms.json`,
  `run/reactor-summary.txt`, compressed `run/maven.log.gz`: complete runner evidence.
- `independent_audit.py`, `independent-audit.json`: independent XML/reactor/property,
  bytecode, dependency/package, metadata and Checkstyle checks.
- `qualified-source.tar.gz`: exact task-start source, including runner/settings.
- `reports.tar.gz`: actual JUnit/Failsafe reports, Checkstyle and test/server logs.
- `coherence-resolution-metadata.tar.gz`: raw Coherence POMs, timestamp metadata,
  repository origin records and resolver status, without dependency JAR binaries.
- `built-artifacts.json`, initial file/index/status/HEAD manifests and final
  preservation/evidence hashes document artifact identity and worktree preservation.

Qualified runner/configuration fingerprints:

- `.github/scripts/full_reactor.py`: SHA-256 `37fe8cb03f93fd661bcbbfae5a0b4d061539f6188b156060ab519c281e31a6d3`.
- `.github/scripts/coherence_artifacts.py`: SHA-256 `f0ef70f6ae5c15b71b9eda617c105a2579ebb016efc5e2dcaf4ca320c5fcfade`.
- `.github/workflows/maven-simple.yml`: SHA-256 `ef3f4e6e9911fae99ce64304016c10a251505c65cb67177ecc97959d17f6eb70`.
- `.mvn/settings.xml`: SHA-256 `ff0b93a04978644849d998d360d863a838e7b46eee4c3a8d47c2095b5fff8f50`.

No qualification failure occurred. Nonfatal compiler/Javadoc/runtime warnings are
retained in the log; normal checks were not bypassed. This establishes local
latest-published-snapshot compatibility for the tested invocation. Hosted Linux CI,
actual remote Data code installation, production TLS/authentication/deployment,
failover, persisted-data and rolling upgrades remain unverified by this run.

Next recommended action: review the unchanged runner/workflow and this fresh local
evidence, then run hosted snapshot CI separately when authorized. Continue to
resolve latest snapshots with `-U`; pin the released version once available.
No stage, commit, push, publication, production change or hosted execution occurred.
Task-owned disposable source, Maven repository and JVM/build output are removed
after evidence archival; the compact evidence directory above remains for review.
