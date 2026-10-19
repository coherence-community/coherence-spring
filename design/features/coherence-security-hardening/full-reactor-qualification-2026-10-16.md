# Frozen Coherence 26.10 full-reactor qualification — 16 October 2026

## Result

**Full-reactor acceptance failed.** The requested ordinary, fatal-test `clean verify`
ends with exit **1** in Core Surefire: **167 tests, 159 passed, eight errors,
zero failures/skips**. Its 28-module reactor has **10 SUCCESS, one FAILURE and
17 dependency-SKIPPED** results. The first failing suite is
`CoherenceNamedCacheConfigurationViewTests`: its three continuous-query view
cases hit hardened serialization-filter rejection of `data.Person`.

The separate bounded supplemental run executes all remaining modules with normal
checks and fatal tests, using exact current-source Core prerequisites in a private
Maven repository. It also exits **1**: **526 tests, 522 passed, four errors,
zero failures/skips**; **25 SUCCESS, one FAILURE (Docs), one SKIPPED (Website)**.
It is independent gap inventory and does not repair or replace the failed full run.

Across both invocations there are **692 unique test cases: 680 passed, 12 errors,
zero failures/skips**, in 78 unique suites across 13 test-bearing modules. The Cloud
Config server case passes in both invocations; the 693 executed results / 79 XML
suites therefore contain one duplicate. Twenty-five of the 28 modules complete
ordinary `verify` in at least one invocation. Core and Docs fail tests; Website
never executes its lifecycle. **CacheStoreDemoIT is excluded, not passed or skipped.**

## Exact input and environment

- Branch `5.x`, HEAD `37cf05ea59bd60c03b21b52acc1eb4c1045859a3`, with all current
  staged, unstaged and untracked work. All 689 initial tracked/untracked files were
  copied and fingerprinted, including `SessionRejectionEvidence.java`,
  `SessionRejectionEvidenceTests.java`, all new security resources and current author
  tags. `qualified-inputs.tar.gz` retains those exact input bytes without Git or builds.
- Maven **3.9.9** (cached distribution matching the wrapper), Temurin
  **21.0.12.1+1-LTS**, macOS aarch64. `JAVA_HOME` is
  `/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home`.
- Every invocation uses `-Pjdk17`, `-Dcoherence.version=26.10-SNAPSHOT`,
  `-Dmaven.test.failure.ignore=false`, offline resolution and `--fail-at-end`.
  The repository default remains **26.04**, and the product compiler release is **17**.
- `coherence.security.mode` and `coherence.remote.dynamic.unauthenticated` remain
  unset. `JAVA_TOOL_OPTIONS`, `JDK_JAVA_OPTIONS` and `_JAVA_OPTIONS` are unset.
  `MAVEN_OPTS` and the Surefire `argLine` set only the task temporary directory.
- No `-Dtest` selector, build/test skip flag, Checkstyle suppression, Javadoc bypass,
  hardening disablement, serializer/policy change or product/test-source edit was used.
  The primary run has no `-pl` selection. Supplemental selection is explicit below.
- The task invocations ran sequentially, without Maven parallelism; inherited
  Surefire has `forkCount=1`, `reuseForks=false`. Local TCP/UDP access was granted
  through normal tool permissions. No unrelated process was stopped. An unrelated
  Hibernate build was visible at initial inspection; port 1408 had no listener.
  These results contain no observed socket-permission or shared-port failure.

## Commands and timing

Task root: `/private/tmp/codex-work/full-reactor-26.10-zi6lfcei`. Evidence: `/private/tmp/codex-work/full-reactor-26.10-zi6lfcei/evidence/`.

The retained `run.sh`, settings, per-run command files and start/end/exit files
are authoritative. The primary command is:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
unset JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS _JAVA_OPTIONS
export MAVEN_OPTS="-Djava.io.tmpdir=/private/tmp/codex-work/full-reactor-26.10-zi6lfcei/jvm-tmp"
cd /private/tmp/codex-work/full-reactor-26.10-zi6lfcei/source
/Users/vasac/.m2/wrapper/dists/apache-maven-3.9.9-bin/33b4b2b4/apache-maven-3.9.9/bin/mvn \
  -o -B -ntp -s /private/tmp/codex-work/full-reactor-26.10-zi6lfcei/evidence/settings.xml -Pjdk17 \
  -Dmaven.repo.local=/private/tmp/codex-work/full-reactor-26.10-zi6lfcei/repository \
  -Dcoherence.version=26.10-SNAPSHOT -Dmaven.test.failure.ignore=false \
  -DargLine=-Djava.io.tmpdir=/private/tmp/codex-work/full-reactor-26.10-zi6lfcei/jvm-tmp \
  --fail-at-end clean verify
```

| Invocation | Start (CEST, Europe/Belgrade) | End (CEST) | Exit | Purpose |
| --- | --- | --- | ---: | --- |
| `primary` | 2026-10-16T00:32:02+0200 | 2026-10-16T00:35:24+0200 | 1 | Requested full reactor |
| `supplemental-prerequisites` | 2026-10-16T00:35:57+0200 | 2026-10-16T00:35:59+0200 | 1 | Offline setup attempt; plugin-version resolution failed |
| `supplemental-prerequisites-pinned` | 2026-10-16T00:36:18+0200 | 2026-10-16T00:36:21+0200 | 0 | Current-source prerequisite setup only |
| `supplemental` | 2026-10-16T00:36:38+0200 | 2026-10-16T00:44:06+0200 | 1 | Remaining ordinary suites and checks |

The initial setup attempt used `-pl coherence-spring-core -am test-compile
jar:jar install:install`; offline prefix-version resolution failed before any
module ran. The successful retry pins the cached lifecycle JAR plugin version:

```text
-pl coherence-spring-core -am test-compile
org.apache.maven.plugins:maven-jar-plugin:3.4.1:jar
org.apache.maven.plugins:maven-install-plugin:3.1.4:install
```

This runs normal validation/compilation and creates private parent/test-support/Core
artifacts from the current source. **Its SUCCESS is setup only:** it does not run
Core tests, Javadoc or `verify` and is not an accepted Core build. Artifact hashes
are in `supplemental-prerequisite-identities.json`. No shared project artifact was
installed, no source was patched, and no skip flag or failure ignoring was used.
The subsequent supplemental command uses the primary arguments with:

```text
-pl '!coherence-spring-core' clean verify
```

## Frozen dependency identity

The private Maven repository was cloned from the shared cache. The retained offline
origin mirror maps `snapshots-repo,sonatype-snapshots` to `coherence-snapshots` at
`https://central.sonatype.com/repository/maven-snapshots`. Newer timestamped variants
and their conflicting origin metadata were removed **only from the private copy**;
the nine timestamped inventory artifacts use their exact JAR/POM aliases. The
cached locally installed Micrometer artifact has no recorded timestamp; its exact
hash, rather than an invented snapshot identity, governs. Cached project-group
artifacts were removed before the primary run to prevent stale source substitution.
No snapshot update, network artifact resolution or shared-cache mutation occurred.

All ten inventory checksums match before and after every invocation. Primary XML
classpath verification covers nine Coherence artifacts; supplemental XML covers
all ten. Every observed Bedrock child classpath is also checked. There is no
unknown or mismatching Coherence artifact in the resolved test/observed JVM paths.

| Artifact (`com.oracle.coherence.ce`, version `26.10-SNAPSHOT`) | Resolved snapshot | SHA-256 |
| --- | --- | --- |
| `coherence` | `26.10-20261013.200824-3` | `9c48703a5112025dac8afd315338eff914a00fd9c7de1e4316badc755b2dadc7` |
| `coherence-bedrock` | `26.10-20261013.201141-3` | `dd53be42e41750358dd66566b7e431c89f6ca21b4071ce36e1947d6ab845222d` |
| `coherence-bedrock-testing-support` | `26.10-20261013.201141-3` | `1b73e1795c1747707ca07afd14aaf4c478b76ded30f13cb2eb67cc63757a4431` |
| `coherence-grpc` | `26.10-20261013.201141-3` | `20c2833787e5cf0640aa185b181c79b48e48d992abcc8cf8677a47e5e4a2e462` |
| `coherence-grpc-client-common` | `26.10-20261013.201141-3` | `302a05af039727f43d4d26ea1720ecb499207f1a33f02b6e0a872b0b2e062278` |
| `coherence-grpc-proxy` | `26.10-20261013.201141-3` | `8e04e3c5909c2e995c5be7258f83f3f2dbc1477b3d459dc1612a3da9b107a899` |
| `coherence-grpc-proxy-common` | `26.10-20261013.201141-3` | `06914d29739ffe1cf9e082514bf130b500ea09731268320a9d1c50279e2fd5e6` |
| `coherence-java-client` | `26.10-20261013.201141-3` | `e838cea1368d10c4f64c3045f0e316203ba808a517281300e6e68cfab4523c1c` |
| `coherence-micrometer` | `not recorded (local cached artifact)` | `43f9079c664c42f51973163977a501e308f4c38ac214708cea2b08781f97b83f` |
| `coherence-protobuf` | `26.10-20261013.201141-3` | `55e80942715c3d1b157c28e6c0e90e6cc2ad1ba524f2cb0f7467f5d69a1eff16` |

## Observed JVM state and compiler output

Actual Maven properties are retained from `jcmd VM.system_properties`, rather than
inferred from command flags. Every one of the 79 Surefire XML suite records reports
Java 21.0.12.1, fatal-test property `false` for `maven.test.failure.ignore`, and an
absent security-mode property. The JVM observer retains sampled parent/test/Bedrock
properties (5 distinct Maven JVMs, 53 distinct sampled Surefire JVMs,
8 distinct Bedrock child JVMs); all sampled runtime versions are
21.0.12.1+1-LTS and all have security mode unset. Sampling is supplementary to the
complete test XML inventory; it does not claim every short-lived JVM was observed.

Core Bedrock children retain the platform default temporary directory; Session
children actually receive the task temporary directory. Session endpoint tests
assert client/server runtime, policy-resource visibility and null mode/dynamic
properties. They pass for Extend/Java and protocol-2 gRPC with Java and POF.
Only the Java gRPC fixture has the existing exact `coherence.grpc.serializer.allowlist=java`.
`jvm-audit.json`, endpoint log lines and the XML retain the actual distinctions.

A separate fresh JVM loads `CoherenceMode` from the verified frozen core and reports:

```text
coherence.security.mode=null
coherence.remote.dynamic.unauthenticated=null
effectiveHardened=true
```

The source/command and `frozen-unset-mode-probe.txt` are retained. Runtime logs also
explicitly report hardened behavior (for example, the disabled invocation proxy).
No explicit-hardened full-reactor rerun was justified while these default-mode
blockers remain; previous scoped explicit-hardened Session/Data results remain
historical evidence, not a new full-reactor pass.

Every compiled project output has class-file major **61**: **389 main classes**
and **290 test classes**, including Core output retained from the primary run.
The per-module counts are in `supplemental-results.json`. The frozen Coherence core
itself is Java-21-only; release-17 project bytecode does not make that dependency
runnable on Java 17. No Java 17 runtime matrix was rerun in this task.

## Module coverage

Test counts below describe the latest ordinary suite execution in this task.
`SKIPPED` denotes a Maven dependency ban, not a JUnit skip. A dash means no selected
normal-lifecycle test cases; it does not imply deployment or application behavior
was tested. Core prerequisite setup is excluded from the supplemental status.

| Module | Primary | Supplemental | Tests / passed / errors |
| --- | --- | --- | --- |
| `coherence-spring-boot-starter` | SKIPPED | SUCCESS | 50 / 50 / 0 |
| `coherence-spring-core` | FAILURE | Not selected; prerequisite only | 166 / 158 / 8 |
| `coherence-spring-data` | SKIPPED | SUCCESS | 335 / 335 / 0 |
| `coherence-spring-docs` | SKIPPED | FAILURE | 9 / 5 / 4 |
| `coherence-spring-session` | SKIPPED | SUCCESS | 117 / 117 / 0 |
| `coherence-spring-site` | SKIPPED | SKIPPED | — |
| `coherence-spring-tests` | SUCCESS | SUCCESS | — |
| `coherence-spring-parent` | SUCCESS | SUCCESS | — |
| `coherence-spring-cachestore-demo-app` | SKIPPED | SUCCESS | 0 selected; IT excluded |
| `coherence-spring-cachestore-demo-core` | SKIPPED | SUCCESS | — |
| `coherence-spring-cachestore-demo-server` | SKIPPED | SUCCESS | — |
| `coherence-spring-cachestore-demo` | SUCCESS | SUCCESS | — |
| `circuit-breaker-cache-demo-app` | SKIPPED | SUCCESS | 2 / 2 / 0 |
| `circuit-breaker-cache-demo-server` | SKIPPED | SUCCESS | 1 / 1 / 0 |
| `circuit-breaker-cache-demo-parent` | SUCCESS | SUCCESS | — |
| `coherence-spring-cloud-config-demo-parent-app` | SKIPPED | SUCCESS | 2 / 2 / 0 |
| `coherence-spring-cloud-config-demo-server` | SUCCESS | SUCCESS | 1 / 1 / 0 |
| `coherence-spring-cloud-config-demo-parent` | SUCCESS | SUCCESS | — |
| `coherence-spring-demo-boot` | SKIPPED | SUCCESS | 1 / 1 / 0 |
| `coherence-spring-demo-classic` | SKIPPED | SUCCESS | — |
| `coherence-spring-demo-core` | SUCCESS | SUCCESS | — |
| `coherence-spring-demo-parent` | SUCCESS | SUCCESS | — |
| `hibernate-cache-demo` | SKIPPED | SUCCESS | 1 / 1 / 0 |
| `samples` | SUCCESS | SUCCESS | — |
| `spring-session-demo-parent` | SUCCESS | SUCCESS | — |
| `spring-session-demo-app` | SKIPPED | SUCCESS | 6 / 6 / 0 |
| `spring-session-demo-server` | SKIPPED | SUCCESS | 1 / 1 / 0 |
| `spring-session-demo-war` | SKIPPED | SUCCESS | — |

All 335 Data cases, all 117 Session cases and all 50 Boot starter cases pass in the
supplemental run. All 15 ordinary sample cases pass, including seven Session sample
cases (server context 1, app context 1, REST 4, timeout 1). These results do not
qualify remote Data installation, arbitrary application payloads, WAR deployment,
production TLS/authentication, failover or rolling upgrades.

Normal unsuppressed Checkstyle reports zero violations wherever configured.
The clean Session pass retains 41 Java-file entries in its Checkstyle cache. Some
final Checkstyle XML files are empty because the ordinary source-JAR lifecycle
fork invokes validation again against the same cache; the exact logs, cache files
and checker XML are retained. No prior-task target/cache was copied into the build.
Javadoc ran normally in modules that reached package, including Data, Session,
Boot starter and all sample modules; it emits ordinary warnings but no Javadoc
error diagnostics. The existing `failOnError=false` setting was not changed.
Core stops before its normal package/Javadoc; Docs stops before rendered HTML,
HTMLsingle, PDF, aggregate Javadoc, ZIP and aggregate verification; Website remains
dependency-skipped. Those packaging paths remain unverified.

## Failure diagnosis

### Core: eight test errors, six concrete denied fixture classes

The first failing Maven phase is `test`, goal
`maven-surefire-plugin:3.5.4:test (default-test)`. Compilation and normal Checkstyle
complete before it. No compiler or style failure caused this build result.

| Suite | Errors | Direct evidence |
| --- | ---: | --- |
| `CoherenceNamedCacheConfigurationViewTests` | 3 | `shouldInjectContinuousQueryCacheWithFilters`, `...WithTransformerAndFilter`, `...WithTransformer`; `InvalidClassException: filter status: REJECTED`, telemetry names `data.Person` |
| `EventHandlerTests` | 1 | Filter denial of `com.oracle.coherence.spring.event.EventHandlerTests$Uppercase` |
| `MapEventHandlerTests` | 1 | Filter denial of `com.oracle.coherence.spring.event.MapEventHandlerTests$Uppercase` |
| `InterceptorsTests` | 1 | Filter denials of `data.Person` and `com.oracle.coherence.spring.event.InterceptorsTests$Uppercase` |
| `MapListenerTests` | 1 | Filter denials of `data.Person` and `com.oracle.coherence.spring.event.mapevent.UppercaseEntryProcessor` |
| `CoherenceInjectorTests` | 1 | Explicit class rejection of `CoherenceInjectorTests$BeanOne` while `shouldInjectAfterDeserialization` restores the `ExternalizableLite` object |

The relevant telemetry has `route=UNCLASSIFIED|gate=filter` and
`reason=serialization-allowlist-rejected` (also `class-rejected` for `BeanOne`).
Nested XML causes include `InvalidClassException`, rather than merely a generic
failed request wrapper. `primary-failure-denials.json` associates exact classes
with suites, and `primary-results.json` retains all eight case names and traces.
No permission experiment was performed, so the observed six classes are a bounded
review input, not proof that six entries alone would fix all later gates.

### Docs: four example-view errors

The supplemental run first fails its own `test` phase in Docs. Each of these
one-case suites errors during continuous-query view creation:

| Suite | Denied class |
| --- | --- |
| `ViewTransformerTests` | `com.oracle.coherence.spring.doc.di.viewtransformer.Person` |
| `WhereFilterTests` | `com.oracle.coherence.spring.doc.di.wherefilter.Person` |
| `ExtractorBindingTests` | `com.oracle.coherence.spring.doc.extractorbinding.Plant` |
| `FilterBindingTests` | `com.oracle.coherence.spring.doc.filterbinding.Plant` |

The reported `UndeclaredThrowableException` / bean-factory wrappers end in
`IllegalStateException: This ContinuousQueryCache is disconnected. Retry the operation again.`
The same suite logs directly name each payload with `gate=filter` and
`reason=serialization-allowlist-rejected`. This is concrete serialization-boundary
evidence, not an inferred network outage from the disconnected wrapper alone.
The other five documentation cases pass. See
`supplemental-docs-failure-denials.json` and the four complete XML/log records.

### Coverage and environment distinctions

- **Actual test errors:** eight Core errors plus four Docs errors, as above.
- **Compilation/style/docs tool failure:** none observed in executed phases.
  Rendered documentation/Website packaging does not run because tests/dependencies
  stop it, so there is no full docs-rendering acceptance claim.
- **Environment/setup failure:** the first supplemental prerequisite attempt cannot
  resolve an unversioned plugin offline. Explicit cached plugin versions resolve it.
  This is separate from both ordinary run failures; there is no cluster socket
  permission, artifact drift or observed port-conflict failure in either test run.
- **Dependency-skipped:** 17 modules in the primary; Website alone in the supplemental.
  The table records every module; aggregate SUCCESS does not imply tests exist.
- **Excluded integration test:** CacheStore app inherits Surefire includes
  `**/*Test.java` and `**/*Tests.java` and has no own/inherited Failsafe binding.
  `CacheStoreDemoIT` matches neither include; all four methods are unexecuted and
  produce no XML result or JUnit skip. The app verifies and its IT source compiles,
  which does not test CacheStore behavior. Classic demo Failsafe goals run in that
  separate module and select no tests; they do not discover the CacheStore IT.
  `cachestore-lifecycle-selection.json` retains the POM hashes and selection evidence.
  The old readiness/payload failures in the assessment were not rerun or repaired.

## Smallest recommended next implementation tasks

1. Review the six observed Core fixture graphs and add exact **test-owned** metadata
   in `coherence-spring-core/src/test/resources/META-INF/coherence/security-config.xml`.
   Review the four named processor classes for executable classification where
   required, rather than approving package wildcards or library-wide payloads.
   Add meaningful deny controls and rerun the unchanged 166-case Core suite with
   fatal tests and normal checks. No claim is made about additional gates hidden
   behind these first rejections.
2. Review the four Docs example payload graphs under
   `coherence-spring-docs/src/test/java/com/oracle/coherence/spring/doc/` and supply
   narrowly owned fixture metadata under its test resources. Rerun all nine cases,
   then the ordinary Docs/Website packaging path including Javadoc and PDF.
3. Separately restore CacheStore IT lifecycle coverage in
   `samples/cachestore-demo/coherence-spring-cachestore-demo-app/pom.xml` and repair
   its `src/test/java/com/oracle/coherence/spring/example/IsSpringUp.java` readiness
   contract using current Boot behavior. Retest all four methods before any claim
   about CacheStore payload policy; previous bounded readiness diagnostics remain
   historical. Do not count a normal no-selected-tests build as an IT pass.
4. After those scoped changes, rerun the same frozen clean full reactor with unset
   mode and fatal tests, then qualify explicit hardened mode separately. Maintain
   the Java 17/26.04 baseline and keep remote Data, deployment/security and rolling
   upgrade qualification distinct. None of these implementation changes is
   authorized or implemented by this run/diagnostic task.

## Retained evidence and preservation

`/private/tmp/codex-work/full-reactor-26.10-zi6lfcei/evidence/` retains:

- Exact command, environment, start/end/exit records and compressed logs for each
  invocation, plus distinct reactor summaries.
- `test-reports.tar.gz`: primary and supplemental Surefire/Failsafe XML, available
  dump diagnostics and configured Checkstyle XML/cache/checker evidence.
- `primary-results.json`, `supplemental-results.json`, `combined-results.json`:
  complete cases, failure traces, module counts, actual test properties, resolved
  artifact checksums and compiled class-file versions.
- Frozen artifact/POM/origin metadata, pre/post checksums, JVM samples/audit, mode
  probe, packaged JAR hashes, lifecycle selection and direct denial extracts.
- `qualified-inputs.tar.gz`, initial 689-file manifest, complete staged index-entry
  baseline, Git state and final preservation/cleanup verification. `SHA256SUMS`
  fingerprints the retained audit artifacts.

The only checkout changes for this task are this report, a current-status addition
in `assessment.md`, and timestamped `JOURNAL.md` / branch resume-journal updates.
Historical assessment results, `test-results.json`, all initial implementation/test
inputs, staged index entries and Git branch/HEAD are preserved. No staging, commit,
push, publication, hosted workflow or cross-chat message occurred. Disposable
source/build/repository/probe/JVM directories are removed after evidence capture;
other tasks’ retained evidence is untouched. This is completed diagnostic work,
with full-reactor acceptance still failing and the listed coverage gaps open.
