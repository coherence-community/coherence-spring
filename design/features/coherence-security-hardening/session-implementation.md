# Session production hardening

Implemented and qualified 14 October 2026, Europe/Belgrade, on `5.x` at
`37cf05ea59bd60c03b21b52acc1eb4c1045859a3`. This
report supplements [the assessment](assessment.md); it does not replace its
historical results or `test-results.json`.

## Implementation

`CoherenceIndexedSessionRepository.resetMaxInactiveIntervalForActiveSessions()`
now submits `SessionResetEntryProcessor`, a top-level named class with no
captured enclosing instance. Its sole instance field is a long millisecond
interval. Java serialization has serial version 1; POF index 0 is a long.
POF ID 4001 is unused in the prior Session registries and both tested Coherence
built-in registries. Existing IDs are unchanged. Applications with an existing
4001 assignment must coordinate another unused ID across clients and servers.

The processor skips expired and absent sessions, changes the active session's
interval, calls `BinaryEntry.expire(interval)` and then `entry.setValue(session)`,
and returns null. It retains the old expiry-sentinel semantics. It carries no
Spring repository, cache, publisher or session. The new absent-entry guard
avoids reviving or dereferencing a missing record.

The normal Session JAR contains authored
`META-INF/coherence/security-config.xml`, version 1.0, with these exact names:

| Type | Executable |
| --- | --- |
| `com.oracle.coherence.spring.session.SessionResetEntryProcessor` | true |
| `com.oracle.coherence.spring.session.SessionUpdateEntryProcessor` | true |
| `com.oracle.coherence.spring.session.support.PrincipalNameExtractor` | true |
| `org.springframework.session.MapSession` | false |
| `org.springframework.security.authentication.AbstractAuthenticationToken` | false |
| `org.springframework.security.authentication.UsernamePasswordAuthenticationToken` | false |
| `org.springframework.security.core.context.SecurityContextImpl` | false |
| `org.springframework.security.core.authority.SimpleGrantedAuthority` | false |
| `org.springframework.security.core.authority.FactorGrantedAuthority` | false |

There is no repository-owner entry, package wildcard, generated class name,
annotation requiring a newer Coherence API, or global security-mode change.
The default dependency remains 26.04 and the product target remains Java 17.

## Serializer review and fixture contract

`MapSessionPofSerializer` writes attributes as objects. The update processor
also transports attribute deltas. `AuthenticationPofSerializer` writes principal
and details objects, mixed registered authorities and string/null credentials.
`SecurityContextPofSerializer` transports the authentication object; the authority
serializer transports the authority string. The serializable
`AbstractAuthenticationToken` superclass needs permission for Java serialization.
Application-defined principals, details and attributes require application-owned
exact metadata and, for POF, registrations and serializers. Choosing a Spring
Security `User` principal does not implicitly permit it through this policy.

The authentication POF serializer remains specific to
`UsernamePasswordAuthenticationToken`; non-null credentials retain the existing
string representation. It now supports erased/null credentials and mixed
`SimpleGrantedAuthority`/`FactorGrantedAuthority` collections. The new factor
serializer preserves authority at index 0 and issuance Instant at index 1,
registered at unused fixture/example ID 3003. The actual sample authentication
flow exposed this Spring Security 7 payload, so its exact permission belongs in
the library's supported graph.

Optional token index 5 stores a nonzero integer: 1 authenticated, -1
unauthenticated. An absent/0 field retains historical authenticated=true when
reading old data. A plain false boolean was unsuitable because POF omits default
values. Regression tests read an actual old uniform-SimpleGrantedAuthority,
no-state-field wire fixture and verify null credentials, mixed authority types,
issuance time and explicit unauthenticated state. This does not recover the
original authentication state of old data that never encoded it, or qualify
rolling upgrades with older readers.

The sample app and receiver server each own a separate three-entry policy for
`org.springframework.security.core.userdetails.User`, `User$AuthorityComparator`
and `org.springframework.security.web.authentication.WebAuthenticationDetails`.
The sample configuration selects these concrete types. They have no executable
classification and do not appear in the library policy. The POF sample configs
retain their existing payload scope and now register the reset processor and
principal extractor consistently.

The ordinary gRPC fixture explicitly selects Java at client and server. Its
server approves only the exact `java` format through
`coherence.grpc.serializer.allowlist=java`. This is a bounded Java interoperability
fixture, not shipping Session policy or a production approval recommendation.
The POF fixture has a dedicated explicit POF client config, explicit server POF
selection and matching registrations. Extend selects Java explicitly. The
endpoint tests require gRPC protocol greater than zero
and matching client/server serializers; legacy protocol fallback is excluded.
The observed protocol is 1 on 26.04 and 2 on frozen 26.10.

The frozen POF registration probe is a material limit: an XML-unlisted type
explicitly registered with `PortableObjectSerializer` is instantiated and its
`readExternal` callback runs. The assertion-bearing probe observed
`xmlAllowlisted=false`, constructorCalls=1 and readCalls=1. The exact frozen
serializer bytecode directly constructs and initializes the registered type.
POF registration/custom serializer review is therefore a separate application
trust decision. The final unlisted POF control has no registration and is
rejected before transmission. It does not prove rejection of registered types
by the XML policy. Java controls submit an unlisted graph inside the permitted
update processor and prove receiver rejection before mutation or payload
callbacks, using an independent server inspection. Binary cache puts alone
do not prove deserialization because a server can retain opaque binary values.

Bedrock launch options explicitly forward only defined security-mode,
dynamic-policy, log and temporary-directory properties. Endpoint assertions
compare their actual child values and runtime with the parent and verify that
the production Session resource is visible. Unset properties remain unset.

## Verification

All acceptance commands exit 0 with test failure ignoring disabled. Maven is
3.9.9 through Wrapper 3.3.2. Every build uses `-Pjdk17` / javac release 17,
including JDK 21 execution. Clusters run sequentially; Surefire uses one fork
and `reuseForks=false`. Each matrix row starts new Maven/Surefire JVMs.

| Run | Actual test JVM | Coherence | Security mode / dynamic policy | Reported / passed | Failures / errors / skips |
| --- | --- | --- | --- | --- | --- |
| Java 17 baseline | Temurin 17.0.20.1+1 | 26.04 Build 120796 | unset / default | 113 / 107 | 0 / 0 / 6 |
| Frozen default | Temurin 21.0.12.1+1-LTS | 26.10 Build 0 | unset / default | 113 / 113 | 0 / 0 / 0 |
| Frozen hardened | Temurin 21.0.12.1+1-LTS | 26.10 Build 0 | hardened / default | 113 / 113 | 0 / 0 / 0 |
| Named reset subset | Temurin 21.0.12.1+1-LTS | 26.10 Build 0 | hardened / deny | 19 / 19 | 0 / 0 / 0 |

All 93 historical suite/method identities are present and pass in each complete
row. No historical test is deleted, renamed or skipped. The six baseline skips
are new runtime-isolated negative controls whose Coherence APIs do not exist in
26.04. All six execute and pass on frozen 26.10.

The 20 additional cases are five cache-expiry/expired-session regressions,
three endpoint assertions, three unlisted-graph controls (each checks attribute,
principal and details), three named-executable controls, three reset processor
behavior/wire tests and three token POF regressions. The existing principal
lookup case now verifies simple and factor authorities, factor issuance time,
credentials and authentication state through every repository fixture.

The deny subset executes both reset scenarios in all five repository fixtures,
all three remote endpoint assertions and all six wire/behavior unit tests.
Its actual parent and child JVM properties are hardened/deny. It emits no
reset-classification rejection, generated reset-lambda name, unsafe serializer
rejection or dynamic-code-denial warning. This proves the named operation does
not depend on the old generated-lambda allowance; it does not qualify arbitrary
application lambda APIs under deny.

The directly affected samples pass seven cases on Java 17/26.04 (unset) and
seven on JDK 21/frozen 26.10 (hardened): server context 1, app context 1,
RestTests 4 and SessionTimeoutTests 1. App/server JARs carry their exact
sample-owned metadata; their own classes and the packaged WAR's own classes
have major 61. WAR deployment and POF HTTP sample behavior were not exercised.
Both sample POF registries were loaded and their four existing/new Session IDs
asserted in a separate probe.

### Negative boundary evidence

The final unset and hardened logs each contain three actual named-executable
policy rejections (Extend, Java gRPC, POF gRPC) and six actual remote Java
payload-filter rejections (three graph locations on Extend and Java gRPC).
`acceptance-boundaries.json` asserts these exact classes, routes and counts,
as well as the three effective endpoints and absence of reset/serializer
rejections. This excludes unrelated network exceptions as acceptance evidence.
The tests independently inspect server state and require unchanged sessions,
zero unlisted-payload read callbacks and zero unclassified processor invocations.
Extend closes the rejected request's cache channel, so each payload request uses
a disposable cache with server-side inspection/cleanup.

Decisive log fields, present in both final full frozen runs:

```text
route=EXTEND_PROXY|gate=filter|...|denied-class=com.oracle.coherence.spring.session.SessionTestSupport$UnlistedPayload|reason=serialization-allowlist-rejected
route=GRPC|gate=filter|...|denied-class=com.oracle.coherence.spring.session.SessionTestSupport$UnlistedPayload|reason=serialization-allowlist-rejected
route=EXTEND_PROXY|gate=remote-executable-policy|...|denied-class=com.oracle.coherence.spring.session.SessionTestSupport$UnclassifiedProcessor|reason=PROCESS_ENTRY
route=GRPC|gate=remote-executable-policy|...|denied-class=com.oracle.coherence.spring.session.SessionTestSupport$UnclassifiedProcessor|reason=PROCESS_ENTRY
```

The named control is explicitly allowlisted and registered for POF but remains
unclassified. Thus its rejection tests executable policy rather than missing
payload permission. The POF unlisted-payload control instead requires the
specific unknown-user-type error before transmission. The registered POF probe
and its limitation are separate evidence, as explained above.

`SessionPolicyProbe.java` runs in another hardened/deny JVM. It asserts that all
three production executables are permitted and classified, all six payloads are
permitted and unclassified, the repository owner is unlisted, and the reset
class is non-synthetic with no enclosing class, serial version 1 and exactly
one serialized long field.

### Artifact and packaging identity

Frozen core: `26.10-20261013.200824-3`, SHA-256
`9c48703a5112025dac8afd315338eff914a00fd9c7de1e4316badc755b2dadc7`.
All ten original inventory checksums match before and after offline verification.
The actual Session classpaths contain these frozen artifacts:

| Artifact | Resolved snapshot | SHA-256 |
| --- | --- | --- |
| `coherence-bedrock-testing-support` | `26.10-20261013.201141-3` | `1b73e1795c1747707ca07afd14aaf4c478b76ded30f13cb2eb67cc63757a4431` |
| `coherence-bedrock` | `26.10-20261013.201141-3` | `dd53be42e41750358dd66566b7e431c89f6ca21b4071ce36e1947d6ab845222d` |
| `coherence-grpc-client-common` | `26.10-20261013.201141-3` | `302a05af039727f43d4d26ea1720ecb499207f1a33f02b6e0a872b0b2e062278` |
| `coherence-grpc-proxy-common` | `26.10-20261013.201141-3` | `06914d29739ffe1cf9e082514bf130b500ea09731268320a9d1c50279e2fd5e6` |
| `coherence-grpc-proxy` | `26.10-20261013.201141-3` | `8e04e3c5909c2e995c5be7258f83f3f2dbc1477b3d459dc1612a3da9b107a899` |
| `coherence-grpc` | `26.10-20261013.201141-3` | `20c2833787e5cf0640aa185b181c79b48e48d992abcc8cf8677a47e5e4a2e462` |
| `coherence-java-client` | `26.10-20261013.201141-3` | `e838cea1368d10c4f64c3045f0e316203ba808a517281300e6e68cfab4523c1c` |
| `coherence-protobuf` | `26.10-20261013.201141-3` | `55e80942715c3d1b157c28e6c0e90e6cc2ad1ba524f2cb0f7467f5d69a1eff16` |
| `coherence` | `26.10-20261013.200824-3` | `9c48703a5112025dac8afd315338eff914a00fd9c7de1e4316badc755b2dadc7` |

The Java 17 baseline core SHA-256 is
`8f233e9a85c35d192127f16203b1b420a50fb308a5435a37f4d5c7acd635142b`.
Full baseline and frozen classpath records are in each run JSON. No snapshot
update or shared snapshot installation occurred; resolution used a task-owned
copy of the Maven repository in offline mode and the retained origin-mapping
settings from the Data verification.

Every accepted normal Session JAR contains exactly the authored production
policy, the named reset processor and factor-authority serializer, with 22
product classes of major 61. It excludes test classes and test policy. Compiled
main/test output in Session, core and test support also has only major 61.
The policy SHA-256 is `dc629ec3d3a12787b43853be7ca6613087fd133f4c33d8ec99cbb8528e35d15b`.

| Session package run | Normal JAR SHA-256 |
| --- | --- |
| `java17-final` | `6d69c58172609680a5c9cb77d6044406e52180fe44aaa2d260fa5696fc572fe3` |
| `java21-final-unset` | `aea532cf80e48d1f62e801b768216b1dec98836c7227293c041bf66c05f4450b` |
| `java21-final-hardened` | `9cc60ecb2338a00e29434d2fc7b1ec771c318b5c455598675b41746f31ebcf01` |
| `java21-final-dynamic-deny` | `d99f65e2676da63cf82f0d663147a6fcdee66512c5348c6cae2572c903f31ab9` |

All final cache/POF XML and both sample POF files validate against the schemas
extracted from both tested core JARs. All four main/test/sample security XML
files validate against the frozen JAR's security schema, SHA-256
`24d5b38a631527bf39b3682692db5f20411422d94f7e94f3bff2d48d2209257a`.
The production XML has exactly nine manual entries and three executable flags.
All relevant normal Maven Checkstyle/build checks pass with zero violations.
Javadoc generation was skipped explicitly.

### Commands and retained evidence

Evidence root:
`/private/tmp/codex-work/coherence-spring-session.eSfo5D/evidence/`.
Exact Maven commands, environment, runtime and exit statuses are preserved in
`java17-final-command.txt`, `java21-final-unset-command.txt`,
`java21-final-hardened-command.txt`, `java21-final-dynamic-deny-command.txt`,
`samples17-final-command.txt` and `samples21-final-command.txt`.
The runner command shape is:

```bash
./mvnw -o -B -s "$evidence/settings.xml" -Pjdk17 \
  -Dmaven.repo.local="$task_root/repository" -Dcoherence.version="$version" \
  -pl coherence-spring-session -am -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest="$selection" -Dcoherence.log=stderr \
  -Dmaven.test.failure.ignore=false -Dmaven.javadoc.skip=true \
  -DargLine="-Djava.io.tmpdir=$task_root/jvm-tmp $mode_and_dynamic_flags" \
  $mode_and_dynamic_flags clean package
```

`run.sh` records the exact full-suite or deny-subset selection and sets the
requested Java home. `final-runs.sh` executes the frozen combinations and
baseline samples sequentially. `run-samples.sh` selects only the three Session
sample modules and their build prerequisites. To reproduce after cleanup,
create a fresh disposable current-source copy including untracked resources,
clone dependencies, apply the retained origin mapping and refuse artifact or
`candidate-inputs-final.json` checksum drift before running these commands.

The retained evidence includes compressed logs, distinct JUnit XML, per-run
results, exact policy/configuration fingerprints, schemas/probes, classpath and
packaging records, denial excerpts, inspection scripts and SHA256SUMS.
Intermediate failures are kept separately: protocol-wrapper assertion repair,
property propagation repair, payload-boundary diagnostics, the initial sample
factor rejection and the default-value omission caught by the token regression.
They are not acceptance passes. Task-owned source/build/dependency trees,
probe classes, unretained extracted schemas/sources and JVM temporary files were removed
after compact evidence capture. Other tasks' evidence was preserved.

## Remaining limits

The scoped Session implementation is complete. Registered POF serializers are
an independent trust boundary; arbitrary application graphs remain application
owned. The old token wire format cannot reveal authentication state it never
stored, and older-reader/rolling-upgrade behavior is not qualified. Existing
non-string credentials still use the serializer's string representation.

This work does not qualify the full reactor, remote Data, CacheStore, new CI
architecture, production Coherence endpoint authentication/TLS, containers,
multi-member failover, WAR deployment or release readiness. No stage, commit,
push, publication or message to another chat occurred. The five-entry Data test
resource, unrelated changes, original test-results.json, branch/HEAD and index
are preserved.

## Regression-test correction — 15 October 2026

The permanent remote controls accepted any `RuntimeException`, so client
serialization or transport failures could pass. Earlier acceptance checked exact
receiver telemetry only in a temporary script. Unchanged data and zero callbacks
remain independent checks; they cannot establish the rejection reason.

`AbstractRemoteSessionTests` now checks Extend's preserved named `SecurityException`
and payload `InvalidClassException` causes/messages. Java payload requests and all
named-executable requests require one complete receiver event with the exact
route, gate, class and reason shown above. `SessionRejectionEvidence` awaits fresh
UUID log markers around each synchronous request. Four permanent regressions
exclude stale/subsequent events, mismatched fields, embedded text and missing or
duplicate evidence. Payload caches have unique names.

Frozen gRPC sanitizes remote causes, making receiver evidence necessary. POF
payload controls instead require the exact unregistered class's
`IllegalArgumentException` before transmission. The named processor must round-trip
through its serializer, retain POF registration 4999 and reach executable policy;
it has no executable permission. Registered POF serializers remain a separate
trust boundary. Both gRPC fixtures now clean their actual server ports: an
unrelated listener on 1408 exposed the old fixed-port assumption and was preserved.

Fresh offline clean-package runs use failure ignoring disabled, fresh JVMs and
`-Pjdk17` / release 17:

| Runtime / dependency | Mode | Passed | Skipped | Failures / errors |
| --- | --- | --- | --- | --- |
| JDK 21 / exact frozen 26.10 | unset | 117 | 0 | 0 / 0 |
| JDK 21 / exact frozen 26.10 | hardened | 117 | 0 | 0 / 0 |
| Java 17 / default 26.04 | unset | 111 | 6 | 0 / 0 |

All 93 historical cases pass. Baseline skips cover absent security APIs. Each
frozen row asserts nine receiver windows: three executable and six Java payload
denials. Resolved Coherence JARs match the frozen hashes above; all ten inventory
checksums match. Serializer/property checks and release-17 packaging pass.

Two disposable hardened mutations each produce exactly one failure, without
errors/skips: removing only registration 4999 fails the serialization precondition;
a simulated gRPC transport exception fails receiver evidence (expected one event,
found zero). Restoring both inputs passes both named controls (2/2). The checkout
was never intentionally broken.

Evidence: `/private/tmp/codex-work/session-rejection-8d4gycl7/evidence/` retains
commands/statuses, compressed logs, JUnit XML, `verification.json`, fingerprints,
mutation diagnostics and scoped diff. `final-matrix.sh` and `mutations.sh` identify
the commands. Three pre-existing production missing-`@author` Checkstyle errors
were suppressed only in the disposable build for exact files/check/message;
repository rules/Javadocs are unchanged. All other style checks passed.

Production behavior/policy, Data's five-entry policy, Java 17 target and 26.04
default are unchanged. Javadoc, full-reactor, sample reruns, deployment,
TLS/authentication, failover and rolling upgrades remain outside this follow-up.
Historical results above are preserved.
