# CacheStore IT lifecycle and readiness — 16 October 2026

## Outcome and scope

`CacheStoreDemoIT` now runs automatically during the sample app's ordinary Maven
`verify` lifecycle. Its startup check uses Boot 4's application-admin `Ready`
attribute instead of an absent Health endpoint MBean. The Java 17 / Coherence
26.04 app verification passes all four existing IT methods and all four new
readiness regressions, without selectors or ignored failures.

The frozen 26.10 / Java 21 run reaches all four methods, but **fails**: one IT
passes and three error because the sample `Person` payload is not permitted.
Failsafe's `verify` goal correctly returns exit 1. Lifecycle/readiness is repaired;
newer-Coherence sample payload qualification remains a separate task. No sample
or library production permission was added or relaxed.

Branch `5.x`, HEAD `37cf05ea59bd60c03b21b52acc1eb4c1045859a3`; all prior source,
resources and staged index entries are preserved outside the changes below.

## Implementation

- App `pom.xml`: bind Failsafe 3.5.4 to `integration-test` and `verify`, fail on
  an empty IT selection, and use one fresh fork with the existing loopback / TTL /
  IPv4 / class-loader conventions. Explicit `classesDirectory=target/classes`
  avoids Failsafe selecting the repackaged Boot JAR with nested `BOOT-INF` classes.
  Parent Surefire selection and every other module's bindings are unchanged.
- `IsSpringUp.java`: read `Ready` from
  `org.springframework.boot:type=Admin,name=SpringApplication` on the platform
  MBean server. An unregistered MBean means not ready; other management exceptions
  and fatal errors propagate rather than becoming a misleading false result.
- `CacheStoreDemoIT.java`: enable application-admin/JMX only in the launched test
  server, use an ephemeral HTTP port, and wait separately for Spring readiness
  and the Coherence `Proxy` service. Each poll has a ten-second remote-future
  timeout inside a one-minute Bedrock wait, with descriptive assertion messages
  and retained exception causes. Defined security-mode, dynamic-code and tmpdir
  properties are forwarded to the Coherence child; unset values stay unset.
  Retain and close the Extend client before the server and database, using finally
  blocks so partial startup or cleanup failure still closes remaining resources.
- `IsSpringUpTests.java`: four regressions cover Boot's actual admin registration /
  `ApplicationReadyEvent` transition, missing registration, retained management
  exceptions and retained `LinkageError`. No existing IT test body was changed.

Boot 4.0.8's cached source confirms that its admin registrar registers with the
platform MBean server and changes Ready on ApplicationReadyEvent. The cached
Failsafe 3.5.4 plugin descriptor documents its packaged-JAR classpath default.
These exact local artifact hashes and sources/descriptors are retained in evidence.
No production management endpoint configuration or dependency was changed.

## Verification

All commands used cached Maven 3.9.9, offline resolution, `-Pjdk17` (release 17),
normal lifecycle checks/Javadoc/packaging, and `-Dmaven.test.failure.ignore=false`.
No test selectors or test/check skip flags, compatibility-mode fallback or dynamic-code
allowance override was used. Clusters ran sequentially in a disposable source tree
and private Maven repository under the evidence root's parent.

| Invocation | Tests | Passed | Failures | Errors | Skipped | Exit |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Java 17 / 26.04 prerequisite attempt | 670 | 663 | 0 | 0 | 7 | 1 |
| Corrected Java 17 / 26.04 app `clean verify` | 8 | 8 | 0 | 0 | 0 | 0 |
| Java 21 / frozen 26.10, explicit hardened, ten-module reactor | 678 | 675 | 0 | 3 | 0 | 1 |

The first row has nine successful prerequisite modules, but the app's test
compilation failed because the initial bounded-future lambda used checked
`Future.get`. It is **not a successful reactor invocation**. After correcting the
poll to `orTimeout(...).join()`, the second row used the exact nine successfully
built prerequisite POMs/JARs copied into the private repository (15 checksummed
files). No prerequisite source changed. The seven existing 26.04 skips are one
Core deny test and six Session deny tests whose newer security API is unavailable.
A still earlier interrupted run was superseded while correcting the Boot-JAR
classpath; its status and JVM-exit check are retained, not counted as acceptance.

The final frozen reactor rebuilt every prerequisite from current source: Core
168/168, Data 335/335, Session 117/117, Boot starter 50/50. Nine modules succeed;
only the app fails, specifically at Failsafe `verify`. Its four readiness tests
pass, and the four integration methods have these results:

| Existing IT method | Java 17 / 26.04 | Java 21 / frozen 26.10 hardened |
| --- | --- | --- |
| `shouldNotGetPersonNotInDB` | Pass | Pass |
| `shouldGetPersonInDB` | Pass | Person filter rejection |
| `shouldCreatePerson` | Pass | Person filter rejection |
| `shouldUpdatePerson` | Pass | Person filter rejection |

Both JVMs reach application readiness and the Extend proxy; the baseline proves
actual read-through, create/write-through and update behavior. Frozen failures
are `InvalidClassException: filter status: REJECTED`, with two client-side Person
denials and one server storage-worker Person denial. Server startup completes
before the operations. These are actual method errors, not a class setup failure:

```text
denied-class=com.oracle.coherence.spring.example.model.Person|reason=serialization-allowlist-rejected
```

Normal lifecycle commands, following the common offline/private-repository flags:

```bash
# Java 17 / Coherence 26.04 prerequisite attempt (app test compilation failed)
mvn -Pjdk17 -Dcoherence.version=26.04 -Dmaven.test.failure.ignore=false \
  -pl :coherence-spring-cachestore-demo-app -am clean verify
# Corrected Java 17 app, exact built prerequisites in private repository
mvn -Pjdk17 -Dcoherence.version=26.04 -Dmaven.test.failure.ignore=false \
  -pl :coherence-spring-cachestore-demo-app clean verify
# Fresh Java 21 reactor, frozen ten-artifact inventory
mvn -Pjdk17 -Dcoherence.version=26.10-SNAPSHOT -Dcoherence.security.mode=hardened \
  -Dmaven.test.failure.ignore=false \
  -pl :coherence-spring-cachestore-demo-app -am clean verify
```

Exact commands include `-o -B -ntp`, the cached-origin settings, private repo,
`--fail-at-end`, task tmpdir and explicit test-fork argLine. Hardened mode is set
before startup in Maven, test forks and the CacheStore Coherence server. The
runner scripts and complete environment/command records are authoritative.

The corrected baseline app ran October 16, 2026 at 02:06:57–02:07:12 CEST;
the frozen reactor ran at 02:07:45–02:16:16 CEST (UTC+0200, Europe/Belgrade).
Observed runtimes are Temurin `17.0.20.1+1` and `21.0.12.1+1-LTS`. Both actual
CacheStore servers have `spring.application.admin.enabled=true` and HTTP port 0;
the frozen server has `coherence.security.mode=hardened`, with no dynamic-code
override. Existing prerequisite child JVM settings are recorded separately;
this is not a claim that every unrelated child fixture propagates explicit mode.

All ten frozen dependency checksums match the unchanged historical inventory
before and after the runs. Twenty distinct baseline/frozen Coherence JAR paths
observed on JVM classpaths match their inventories. All 535 compiled project
main/test class outputs in the completed source copy have class-file major 61.
Both normal and repackaged application JARs exclude the test helpers. The default
Coherence dependency remains 26.04; no Java target, production Java, application
configuration or security resource was changed.

## Evidence and remaining work

Evidence: `/private/tmp/codex-work/cachestore-lifecycle-5qknkhde/evidence/`.
It retains exact initial/qualified source archives and hashes, index/branch/HEAD,
commands/environments/times/statuses, JUnit/Failsafe XML and summaries, compressed
Maven and child-process logs, JVM/classpath audits, bytecode/packaging checks,
private prerequisite provenance and a scoped diff. Disposable source, dependency
and JVM temp trees are removed after capture; no unrelated task files are touched.

The original index-entry SHA-256 is preserved:
`c78a857058f4131741d56697afd9c21faab24b87df35a1df7146731e6549951e`.
`test-results.json` and all earlier qualification reports remain historical and
unchanged. The earlier 696-case green full reactor predates automatic CacheStore
coverage; it does not establish a green reactor after this change.

Next: separately review exact sample-owned Person graph metadata in the shared
sample core artifact and requalify these same read/write methods at both endpoints.
This task does not qualify the complete 28-module reactor on current source,
current-source unset 26.10 mode, production deployment/TLS/authentication,
failover or rolling upgrades. No staging, commit, push, publication or messages
to other chats occurred.
