# Coherence Spring 5.0: Coherence 26.10 security-hardening assessment and plan

**19 October 2026 latest-published snapshot local qualification — current status**
(recorded October 19, 2026 at 12:40:48 CEST (UTC+0200; Europe/Belgrade)): the unchanged updated snapshot runner
passes a fresh empty-repository online `-U` JDK21/release17/explicit-hardened
ordinary clean verify: **all 28 modules, 706/706 cases, 83 suites**, zero
failures/errors/skips, including all four CacheStore Failsafe integration methods.
Actual properties prove hardening in all nine Coherence children, Maven, HSQLDB
and all 77 observed test forks. All 688 compiled classes have major61; all ten
published Coherence JAR/POM identities, 298 runtime dependencies and 14 embedded
Coherence JAR entries pass run-relative consistency checks. Core resolved
`26.10-20261014.235911-4`; companions including Micrometer resolved
`26.10-20261015.000224-4` from the configured public snapshot endpoint. No local
artifact substitution or historical lock inputs. Both runner and independent
actual XML/reactor/bytecode/hash audits pass. See the
[dated report](latest-snapshot-local-2026-10-19.md) for commands, full hashes,
timestamps, evidence and scope. Prior frozen evidence remains historical.

**Next action and limits:** review the fresh local qualification and run hosted
snapshot CI separately when authorized. Hosted CI, remote Data installation and
production deployment/security/failover/rolling upgrades remain unverified by this
run. No production/policy/build-runner changes, stage/commit/push/publication or
hosted execution. Historical statuses below are preserved as chronological stages
and superseded where this new local result supplies the missing evidence.

**16 October 2026 CI policy correction — current status:** snapshot compatibility
CI tests the latest published `26.10-SNAPSHOT`, not the historical frozen build.
The snapshot lane runs online with Maven `-U`, after discarding cached Coherence
snapshots (including local-copy metadata). It records the resolved JAR/POM hashes
and Maven timestamp metadata as evidence for that run, without requiring old
checksums. Mixed Coherence release versions and package/classpath inconsistencies
remain audit failures. Java 17/26.04 remains the baseline; the snapshot lane uses
JDK 21, release 17 and explicit hardened mode. Once 26.10 is published as a release,
its release version can be pinned in CI.

The old lock is retained only as
[historical qualification evidence](historical-coherence-lock-2026-10-16.json).
Earlier frozen results below remain valid for their recorded inputs, but do not
qualify the latest snapshot. Seven runner regression tests, Python syntax and
workflow actionlint pass for this correction. No latest-snapshot reactor or hosted
workflow has been run in this correction. Next: execute the updated snapshot lane
and assess the exact artifacts it resolves. No production behavior changed.


**16 October 2026 full matrix and CI — current status:** both requested fresh
ordinary full reactors pass, all 28 modules and 83 suites each: Java 17/26.04/unset
has **697 passes plus nine expected absent-security-API skips**; JDK 21/exact frozen
26.10/explicit hardened has **706/706 passes and no skips**. All four CacheStore ITs
run under normal Failsafe. Actual Maven/fork/all-nine-Coherence-child mode/runtime,
688 major-61 output classes and all applicable resolved/packaged Coherence JAR/POM
hashes pass. No further production, policy, resource, test, POM or default changes.

CI now encodes the explicit supported matrix, release 17, fatal ordinary clean
verify, inherited hardening before JVM initialization, actual runtime/bytecode/
dependency audits, exact frozen JAR/POM pinning and always-retained failure reports.
Both full reactors also pass through the actual new CI runner locally with exactly
the same case identities/outcomes; workflow lint and all eight helper checks pass.
Existing main/PR JDK21 builds retain release17; deployment/Sonar/credential/owner
boundaries remain. Optional requested-version testing is explicitly non-frozen.
See [the complete matrix/CI report](full-matrix-ci-2026-10-16.md).

**Current next action and limits:** review the bounded CI/documentation changes.
Hosted CI and public frozen retrieval were not run/established: Sonatype lookup
timed out, and the original frozen Micrometer is an untimestamped local development
build with no recorded public identity. The frozen lane requires all exact locked
bytes and fails clearly if any artifact is absent/different; no publication or
substitution is included. Actual remote Data code installation, production
deployment/TLS/authentication, failover and persistence/rolling upgrades remain
separate. Earlier status/next-action paragraphs below are preserved verbatim as
historical stages, superseded by this current full-matrix result.


**16 October 2026 exact CacheStore permission — current status:** the shared sample
core now contains exactly the manual, non-executable Person permission. Real
persistence/Extend/server-load graphs require no further application types. Both
endpoints verify the Java serializer and policy boundaries, including unrelated
payload rejection before callbacks. The complete current frozen-26.10 / JDK-21 /
release-17 ordinary reactor passes **706/706 cases and all 28 modules**, zero
failures/errors/skips, mode unset, normal checks and fatal tests. All four CacheStore
IT methods are included. Focused frozen unset/hardened reactors each pass 680/680;
Java-17/26.04 unset passes 672 plus eight expected API skips, and its fresh hardened-
property app row passes nine plus one expected API skip. The default dependency and
Java-17 target are unchanged. See [the implementation/qualification report](cachestore-person-permission-2026-10-16.md)
for actual counts, graph/package evidence, the excluded mixed-companion diagnostic
and the baseline post-build wrapper incident.

**Current next action:** review these bounded changes; separately qualify the full
Java-17/explicit-hardened reactor matrices, hosted CI and deployment/security,
actual remote Data installation and rolling upgrades as required. The Person
permission and CacheStore lifecycle/readiness blockers are resolved for the tested
snapshot and sample paths. Earlier status/next-action statements below are retained
verbatim as historical stages, superseded by this current result.


**16 October 2026 CacheStore lifecycle/readiness follow-up:** the sample app now
binds Failsafe integration-test/verify, uses bounded Boot application-admin Ready
polling and closes its client/server/database. Java 17 / 26.04 ordinary app verify
passes all four existing IT methods plus four new readiness regressions. The
fresh frozen 26.10 / JDK 21 release-17 explicit-hardened ten-module reactor executes
all four IT methods: one passes and three error on exact sample Person payload
rejections; Failsafe verify exits 1. Its other 674 cases pass, including all four
readiness regressions. This is not a green newer-Coherence sample or full reactor.
See [the lifecycle/readiness report](cachestore-lifecycle-readiness-2026-10-16.md)
for exact scope, split baseline qualification, commands and evidence.

**Current next action:** qualify exact sample-owned Person metadata separately.
Lifecycle coverage and readiness are implemented; older next-action statements
below record prior stages. The prior 696-case full-reactor pass used the earlier
selection rules and does not qualify the restored IT. Other deployment, full
matrix, actual remote Data and rolling-upgrade work remains separate.

Assessment: 14 October 2026, Europe/Belgrade. Revised after review on the same date.

## Decision and current status

**16 October 2026 fixture-fix follow-up (current):** Core and documentation test
fixtures now have exact test-owned serialization metadata and four new graph/deny
regressions. A fresh ordinary full frozen reactor on JDK 21 / release 17 passes
**696/696 tests and all 28 modules**, with security mode unset, normal checks and
Javadoc, fatal tests and no selectors/skips. All 692 prior unique cases remain and
pass. Core is 168/168; Docs is 11/11 and its normal HTML/PDF/ZIP plus Website
packaging now execute successfully. Separately, the affected explicit-hardened
row passes 31/31; Java 17/26.04 passes 29 with two expected new API-capability skips.
Production behavior/policy, POMs/default 26.04, test selection rules and existing
index entries are unchanged. See [the fixture-fix report](core-docs-fixture-fix-2026-10-16.md)
for scope, exact inputs/commands, graph permissions, JVM/artifact evidence and limits.

**Current next actions:** restore CacheStoreDemoIT’s ordinary lifecycle coverage
and repair readiness, then qualify its four methods. Full explicit-hardened / Java
17 reactor matrices, actual remote Data installation and deployment/security /
rolling-upgrade coverage remain separate. The earlier failed full-reactor run
and all historical results below are preserved; the ordinary reactor now passes
within its existing selection rules, which still exclude CacheStoreDemoIT.

**16 October 2026 full-reactor follow-up:** the current source on JDK 21 with
`-Pjdk17`, the exact frozen 26.10 artifacts, unset security mode, normal checks and
fatal test failures does **not** pass. The ordinary 28-module `clean verify` exits
1: 167 tests / 159 passed / eight Core errors; 10 modules succeed, one fails and
17 are dependency-skipped. A separate ordinary supplemental run using exact
current-source Core prerequisites reports 522/526 passing, with four Docs errors
and Website still skipped. Across both invocations, 680/692 unique cases pass,
with 12 errors and no failures/skips; this is not a full-reactor acceptance pass.
Data 335/335, Session 117/117, Boot starter 50/50 and all 15 ordinary sample cases
pass in the supplemental run. All ten frozen checksums and compiled release-17
outputs match. CacheStoreDemoIT's four methods remain excluded from its ordinary
lifecycle. See [the full-reactor qualification report](full-reactor-qualification-2026-10-16.md)
for exact commands, module coverage, artifacts, actual JVM properties and evidence.

**Next actions:** first review exact Core fixture metadata for the six observed
denied classes; separately review the four Docs payload graphs. Then restore
CacheStore IT lifecycle/readiness coverage and repeat ordinary full-reactor
qualification before a separate explicit-hardened run. These remain proposed
implementation tasks; this diagnostic changes no product code, policy or build
selection rules. The historical results and test-results.json below are retained.

The unmodified integration did not pass the suite with the tested Coherence
`26.10-SNAPSHOT` hardened defaults. The investigation identifies concrete Session
metadata and serializer problems, application/test payload rejections, a Spring
configuration mismatch, and an excluded sample integration test.

The Session implementation now preserves Java 17 product compatibility, uses a
named reset processor, ships exact executable/payload metadata and selects
remote serializers explicitly. Its bounded matrix and real negative controls
pass. The proven Data fixture metadata is retained. Core and documentation
mechanisms and actual remote Data installation paths still need focused
qualification. See [Session implementation and verification](session-implementation.md).

**The diagnostic policy is not the proposed production policy.** Its passing
Session cases still emit executable-classification rejections for a generated
lambda and rely on this snapshot's separate dynamic-code allowance. Those are
historical diagnostic results. The named-processor Session implementation and
its scoped qualification are now complete; broader qualification remains
outstanding.

The bounded Data fixture resource is now implemented: its five exact manual
test entries passed all 335 existing Data cases on Java 17/Coherence 26.04 and on
JDK 21/frozen 26.10 with release 17, both unset and explicit hardened mode in
separate JVM runs. The policy stays out of production output and the normal
Data JAR. See [the separate implementation verification](data-diagnostic.md#permanent-fixture-implementation-and-verification);
the historical assessment results and `test-results.json` remain unchanged.

## Scope and compatibility constraints

- Assess Coherence Spring `5.0.0-SNAPSHOT`, branch `5.x`, source commit
  `37cf05ea59bd60c03b21b52acc1eb4c1045859a3`.
- The source's default Coherence dependency is `26.04`; its Java baseline is 17.
- Coherence Spring must continue to work on Java 17 with a compatible Coherence
  version. A selected Coherence version that requires Java 21 must run on Java 21.
- Exercise the Coherence security default with `coherence.security.mode` unset;
  qualify explicit `hardened` mode separately during implementation.
- The assessment and subsequent review did not change production Java, POMs, or
  runtime configuration. Experimental changes were confined to disposable copies.

## Reproducibility and baseline results

The tested core artifact is `26.10-20261013.200824-3`, published on 13 October 2026.
Its JAR SHA-256 is:

```text
9c48703a5112025dac8afd315338eff914a00fd9c7de1e4316badc755b2dadc7
```

[test-results.json](test-results.json) records the unmodified-source failures and
the identities and checksums of all ten Coherence artifacts observed in test
classpaths. Individual modules have different timestamped snapshot versions.

Tests used Maven Wrapper 3.9.9 and Temurin 17.0.20.1 / 21.0.12.1. Offline resolution
held the cached artifacts fixed; it does not establish that they were the newest
available snapshots. The baseline left security mode unset and added no manual
serialization allowlist or remote-execution relaxation.

### Historical commands

The original full-reactor commands used the matching runtime profile:

```bash
JAVA_HOME=<JDK_HOME> ./mvnw -o -B -s .mvn/settings.xml -Pjdk<17_or_21> \
  -Dcoherence.version=26.10-SNAPSHOT \
  -Dmaven.test.failure.ignore=true -fae clean verify
```

These are records of the investigation. In particular, `-Pjdk21` compiles the
integration for Java 21; use the release-17 combinations in the implementation
plan for product acceptance. `maven.test.failure.ignore=true` collected failures
from later modules. Maven `SUCCESS` markers under that option are not passing
test-suite evidence.

The first reactor had a separate offline repository-origin resolution failure in
the classic demo's core module. A temporary settings mirror mapped
`snapshots-repo` and `sonatype-snapshots` to the cached `coherence-snapshots` origin
at the same Sonatype URL. The supplemental invocation built the classic sample,
which has no tests, and executed the missing Boot sample and the excluded IT:

```bash
JAVA_HOME=<JDK_21_HOME> ./mvnw -o -B -s <temporary-settings.xml> -Pjdk21 \
  -Dcoherence.version=26.10-SNAPSHOT \
  -pl :coherence-spring-demo-classic,:coherence-spring-demo-boot,:coherence-spring-cachestore-demo-app -am \
  -Dtest=SpringDemoApplicationTests,CacheStoreDemoIT \
  -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.test.failure.ignore=true \
  -Dmaven.javadoc.skip=true -fae verify
```

### Unmodified-source results

| Area | Tests | Passed | Failures | Errors |
| --- | ---: | ---: | ---: | ---: |
| Core | 166 | 158 | 0 | 8 |
| Data | 335 | 68 | 0 | 267 |
| Session | 93 | 4 | 14 | 75 |
| Boot starter | 50 | 50 | 0 | 0 |
| Executable documentation | 9 | 5 | 0 | 4 |
| Ordinary sample tests | 15 | 12 | 0 | 3 |
| **Ordinary total** | **668** | **297** | **14** | **357** |
| CacheStoreDemoIT setup result | 1 | 0 | 1 | 0 |

There are zero reported skips. The IT row is a class-level setup failure: its
four methods did not execute through the original readiness helper. The JUnit
inventory therefore contains 669 reported results and 372 failure/error records,
including that setup failure. Many failures cascade from a shared context or
deserialization rejection; they are not 372 independently diagnosed defects.

Every concrete suite is accounted for. The similarly named
`CoherenceNamespaceHandlerIntegrationTests` is a fixture holder without JUnit
test methods.

## Findings and their limits

### Java execution version and product bytecode target are separate

The tested core classes have class-file major 65 and require Java 21. The Java
17 attempt fails in `coherence-spring-tests` before security tests execute:

```text
class file has wrong version 65.0, should be 61.0
```

All 28 projects subsequently compiled on JDK 21 with `-Pjdk17`, retaining
`--release 17`. Inspected core, data, session, starter, and test-support main
classes have major 61. This is compile/target evidence; the Java 21-only
Coherence dependency still cannot run on Java 17.

Both `jdk17` and `jdk21` profiles overwrite `java.version`, which the compiler
uses as its `release`. The existing `maven-simple.yml` also sends one selected
Coherence version to both runtime rows. The newer-Coherence job must choose
JDK 21 independently of the release-17 compilation target.

### Payload permission, executable classification, and serializer selection are distinct

Hardened serialization rejects application and fixture classes even in an
embedded single-member cluster. That topology still crosses serialization
boundaries. Core telemetry names `data.Person`, `CoherenceInjectorTests$BeanOne`,
and test entry processors. Documentation telemetry also names its example
`Person` and `Plant` types.

Application metadata belongs in application artifacts; fixture metadata belongs
in test resources. Do not ship library-wide package wildcards or test types.
Named processors/extractors also require executable classification at the
relevant remote boundary. POF type registration does not replace security
metadata, and class metadata does not approve a gRPC serializer format.

### Historical Session diagnosis and completed production implementation

At the assessed unmodified HEAD, the Session module shipped no main-resource
security configuration. The historical fresh-JVM probe reported:

| Class | Deserialization allowed | Classified executable |
| --- | --- | --- |
| `org.springframework.session.MapSession` | false | false |
| `com.oracle.coherence.spring.session.SessionUpdateEntryProcessor` | false | false |
| `com.oracle.coherence.spring.session.support.PrincipalNameExtractor` | false | false |

The entry processor and principal extractor need executable classification.
`MapSession` is a payload. The Spring Security graph also needs explicit review:
Java serialization checks serializable superclasses, and
`AuthenticationPofSerializer` writes principal/details objects with `writeObject`.
Application-defined principals and attributes remain application-owned.

The historical eight-entry experiment permitted these exact types:

| Type | Historical diagnostic role |
| --- | --- |
| `org.springframework.session.MapSession` | Payload |
| `com.oracle.coherence.spring.session.SessionUpdateEntryProcessor` | Executable |
| `com.oracle.coherence.spring.session.support.PrincipalNameExtractor` | Executable |
| `com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository` | Owner of the generated reset-interval lambda |
| `org.springframework.security.authentication.AbstractAuthenticationToken` | Serializable superclass |
| `org.springframework.security.authentication.UsernamePasswordAuthenticationToken` | Authentication payload |
| `org.springframework.security.core.context.SecurityContextImpl` | Context payload |
| `org.springframework.security.core.authority.SimpleGrantedAuthority` | Authority payload |

This policy made 77 of 93 Session cases pass. The remaining 16 POF cases passed
after explicitly selecting POF on the client. These are disjoint diagnostic
passes, not a complete passing reactor or proof of fully classified executables.

In the unmodified source, `resetMaxInactiveIntervalForActiveSessions()` submitted
a generated lambda. The passing Extend and gRPC cases still log its executable-policy
rejection. Adding its owner to the payload allowlist did not classify the
generated lambda as executable.

The matching `RemoteInstallGate.enforceInstall()` source explains the successful
operation: it catches classification rejection for a dynamic class and applies
the separate dynamic-install policy. That policy currently defaults to allow in
both security modes. A policy rejection log can therefore coexist with a
successful operation on this snapshot.

**Production direction:** replace this integration-owned lambda with a named
static processor, classify that processor executable, and omit the repository
bean from the production policy. This makes permissions explicit and avoids
depending on the dynamic-code allowance for this operation. The historical owner
entry remains evidence of the experiment, not an implementation recommendation.

### Session implementation verification, 14 October 2026

The named top-level `SessionResetEntryProcessor` now carries only one long
interval, with Java serial version 1 and POF index 0/type ID 4001. The Session
JAR ships nine exact manual entries: three executables and six payload types,
including the serializable token superclass and Spring Security 7's
`FactorGrantedAuthority`. The repository owner is omitted. Application-owned
principal/details/attribute types remain outside this policy; the two Java
security samples own their exact User/comparator/web-details entries.

The actual sample login flow exposed the factor authority. POF token handling
now preserves mixed registered authorities and their issuance time, null/erased
credentials and explicit authentication state at optional index 5. Reading an
actual legacy uniform/no-state wire fixture is covered; absent state retains
the old authenticated=true behavior. This is not rolling-upgrade qualification.

| Session acceptance | Passed | Failures / errors / skips |
| --- | ---: | --- |
| Java 17 / Coherence 26.04 | 107 of 113 | 0 / 0 / 6 new-runtime controls |
| JDK 21 / frozen 26.10, unset | 113 of 113 | 0 / 0 / 0 |
| JDK 21 / frozen 26.10, hardened | 113 of 113 | 0 / 0 / 0 |
| JDK 21 / frozen 26.10, hardened + dynamic deny, named-reset subset | 19 of 19 | 0 / 0 / 0 |

All 93 historical Session method identities execute and pass in each complete
row. All builds use release 17; the normal JAR contains 22 major-61 product
classes, the exact production policy and named processor, without fixtures or
test policy. Both directly affected Java sample combinations pass 7/7 cases;
the WAR is built and its POF registry loaded, not deployed. Schemas and normal
Checkstyle/package checks pass. Javadoc is explicitly skipped.

The ordinary gRPC Java fixture approves only the exact `java` format on its
server; POF uses explicit matching endpoints. Effective gRPC protocol is 1 on
26.04 and 2 on frozen 26.10, with no unsafe serializer rejection/fallback.
Actual child JVM properties and production-resource visibility are asserted.
The named reset produces no classification rejection under dynamic deny.

Real Java request-boundary controls reject unlisted attribute/principal/details
graphs before payload callbacks or session mutation. Extend, Java gRPC and POF
gRPC reject an allowlisted, registered but unclassified named processor before
its process method runs. Exact class/route denial telemetry is checked alongside
JUnit results. The unlisted POF graph is unregistered and rejected before
transmission. A separate probe demonstrates that explicitly registered
PortableObjectSerializer callbacks can run for XML-unlisted types: registration
and serializer review remain an independent application-owned trust boundary.
Do not interpret the XML policy as blanket enforcement over registered POF
serializers or binary cache puts as proof of deserialization.

**Session next action:** review the completed source/policy/guide and retained
[implementation evidence](session-implementation.md). No further fix is indicated
by this scoped matrix. Coordinate application POF registrations and matching
endpoint versions; qualify old-reader/rolling upgrades, production Coherence
endpoint authentication/TLS and deployment separately. Continue the remaining
core/docs diagnostics, actual remote Data qualification and CacheStore work as
separate assessment steps. No CI architecture, default dependency or global
Java-target change is part of this implementation.

### Data's assessed errors resolve with exact test metadata in the embedded profile

Data contributed **267 of the 357 historical ordinary errors**. The focused
[Data diagnostic](data-diagnostic.md) reproduced eight representative failures on
JDK 21 with `-Pjdk17`, using the same frozen artifacts and source commit. There
was no tracked-source or artifact drift; the compiler target intentionally
changed from the original baseline's 21 to 17.

Three exact domain entries (`Book`, `Author`, `Address`) made six focused cases
pass. Adding exact entries for `RepositoryTests` and `AsyncRepositoryTests`
resolved their generated test lambdas. The resulting five-entry test-resource
policy passed **all 335 Data cases**, zero failures/errors/skips, with failure
ignoring disabled. The complete suite/method inventory matches the historical
baseline, and all 267 former errors now pass. No Data production Java/POM/runtime
resource change was needed for this diagnostic or made in the checkout.

Failure messages include generated names such as:

```text
com.oracle.coherence.spring.data.model.Book$getPagesAsBigDecimal$DB4C11B671CB61FEB3E5E7DC329D73EB
Book$<init>$<hash>
RepositoryTests$lambda$ensureUpdate...<hash>
```

The getter and constructor names arise from explicit test inputs
`Book::getPagesAsBigDecimal` and `Book::new`. The runtime probe observed their
synthetic flags and mapped them to `Book`; the test lambdas mapped to their exact
test owners. Derived-query and projection cases instead rejected the ordinary
`Book` payload while storage threads deserialized values. All four selected
suites use an embedded `DistributedCache` and `DefaultSerializer`, format `java`;
telemetry reports route `UNCLASSIFIED`. These are distinct failure mechanisms.

The matching `SerializationAllowlist.isAllowlistedName()` explicitly maps
recognized synthetic lambda/method-reference names to their owning class before
checking permission. The diagnostic proves that an exact `Book` entry covers
these recognized synthetic `Book$...` classes. Each generated class nevertheless
remains `executable=false`: executable classification checks its own name rather
than applying that owner mapping. The embedded positive probe did not record
executable-policy checks before its explicit direct gate control. Passing the
Data suite therefore does not qualify remote install classification.

Removing Author or Address permission rejected the removed payload in both
query/projection controls. Domain-only metadata left the two test lambdas
rejected. An unlisted payload, an allowlisted but unclassified named-class gate
input, and generated bytecode referencing a denied Runtime class were also
rejected. With the candidate policy and explicit dynamic-code deny, six selected
method-reference/lambda cases failed at local lambda creation while the derived
query and projection passed. These intentional controls preserve independent
policy enforcement; they are separate from the zero-error default-profile run.

**Implemented:** the five exact entries under Data test resources, including
complete embedded-suite verification in unset and explicit hardened mode.
Permanent negative Java coverage remains separate work. No Data production
query/projection change is indicated for the assessed embedded profile.
Separately qualify real Extend/gRPC installation paths, including any supported
strict dynamic-code policy. Do not ship test owners or mark domain owners
executable as a substitute for that qualification.

The four documentation errors also need focused validation against their full
causes and telemetry; their empty/indirect JUnit messages alone are insufficient.

### gRPC serializer choices need explicit qualification in every remote fixture

The snapshot's `GrpcSerializerPolicy` accepts empty/server-default format, POF,
JSON, Genson, and the named Coherence JSON serializer by default. Explicit
`java` and `DefaultSerializer` names require server approval through
`coherence.grpc.serializer.allowlist`.

The ordinary gRPC Session suite passes while logging `client-serializer-unsafe`.
Its effective serializer and negotiation/fallback path need to be made explicit,
as do those of the core gRPC fixtures. A final operation assertion does not
establish which negotiation path was used.

The POF Session fixture enables POF only in the child server and uses the general
gRPC client cache configuration without an explicit POF serializer. The resulting
Java-format rejection is fatal during Session initialization. A dedicated client
configuration with `<serializer>pof</serializer>` fixes that diagnostic mismatch.
The element belongs inside `remote-grpc-cache-scheme`, after the service/cluster
elements according to the schema. Matching POF registrations are also required.

### Compatibility mode changes enforcement, not classification

With `-Dcoherence.security.mode=compatibility`, the probe reports hardening false,
deserialization allowed, and executable classification still false for the
three probed types. The false classification does **not** demonstrate execution
denial: `DefaultRemoteExecutablePolicy.enforce()` records `would_reject` and
returns when executable enforcement is disabled.

Compatibility mode is a temporary migration posture. It does not remove every
hard-floor deny rule or replace hardened-mode qualification. The independent
dynamic-code default is allow on the tested snapshot in both modes; an explicit
`coherence.remote.dynamic.unauthenticated=deny` is a separate policy combination.

### Spring environment and JVM property resolution differ

The matching `CoherenceMode` reads `coherence.security.mode` directly from
`System.getProperty` and caches it. `SpringSystemPropertyResolver` and the Boot
`coherence.properties` map feed Coherence's `Config` resolver; they do not set JVM
system properties.

A fresh-JVM probe with a Spring environment property reports:

```text
system.security.mode=null
spring.resolved.security.mode=compatibility
hardening=true
```

A fresh JVM with `-Dcoherence.security.mode=compatibility` reports hardening
false. Document the JVM switch and initialization timing for this snapshot.
Coordinate any resolver change with Coherence; do not mutate global JVM system
properties from a late Spring application context.

### CacheStore IT coverage and readiness are separate from payload permission

The parent Surefire includes only `*Test` and `*Tests`; `CacheStoreDemoIT` is not
bound to Failsafe in its own module. Explicit execution fails because `IsSpringUp`
polls `org.springframework.boot:type=Endpoint,name=Health`, which is absent in
the started Boot application. The application and Coherence proxy do start.

A disposable correction enabling Boot's application-admin Ready attribute lets
all four methods run: one passes and three fail on the sample's
`com.oracle.coherence.spring.example.model.Person` payload. Restore automatic IT
execution and readiness first, then qualify sample payload metadata separately.

## Implementation plan

The exact Data fixture resource is implemented and verified as noted above.
Session step 3 and the Session portion of step 4 are also implemented and
qualified as recorded above. The remaining broader changes and acceptance
checks below are proposed work.

### 1. Establish the runtime/target matrix and reproducible policy propagation

| Purpose | Execution JDK | Coherence | Integration compiler target |
| --- | --- | --- | --- |
| Existing Java 17 product baseline | 17 | Supported Java 17-compatible baseline, currently 26.04 | 17 |
| Newer Coherence qualification | 21 | Frozen 26.10-SNAPSHOT artifacts | 17 |

- Add an explicit CI combination rather than applying 26.10 to both rows of
  `maven-simple.yml`. Retain the existing Java 17 combination.
- Run the 26.10 combination with JDK 21 and `-Pjdk17`, or an equally explicit
  release-17 override. Do not use `-Pjdk21` for release-17 artifact acceptance.
- Record resolved snapshot versions/checksums. Verify runtime version and output
  bytecode in the reports/artifacts, rather than inferring them from job labels.
- Qualify unset mode and explicit `hardened` in fresh JVMs. Check properties and
  policy-resource visibility in child Bedrock JVMs as well as the test JVM.
- Keep the default Coherence dependency until the supported matrix and release
  policy are settled. Use failure ignoring only for diagnostic collection.

### 2. Implement proven Data test metadata; finish core and documentation diagnosis

The Data diagnostic is complete for the frozen embedded default profile:
335/335 cases passed with five exact test entries. The policy is now in repository
test resources and its separate implementation runs also pass the bounded
Java 17/26.04 and JDK 21/frozen 26.10 matrix. Its full commands, suite counts, source interpretation,
negative controls, and limits are in [data-diagnostic.md](data-diagnostic.md).

- Implemented exact Data test-resource entries for Book, Author, Address,
  RepositoryTests, and AsyncRepositoryTests, with production artifacts unchanged.
  Permanent negative coverage for missing payload/owner permission remains
  separate work.
- Finish the core payload/processor and documentation-example diagnostics whose
  JUnit wrappers obscure their causes. Qualify actual remote Data installation
  separately from the now-passing embedded suite.
- Record the actual denied class, synthetic flag, owner-name mapping, serializer,
  boundary, and deepest cause. Distinguish payload permission, bytecode
  validation, executable classification, and query/projection behavior.
- Test minimal exact owner/domain metadata as a diagnostic hypothesis. Do not
  register hash-named generated classes, apply package wildcards, or assume
  `lambda-target="true"` solves owner/payload permission.
- Retain complete 335-case Data coverage when implementing the proven metadata.
  The diagnostic has zero remaining errors in its default profile. Its six
  strict dynamic-deny control errors are a separate policy profile, not evidence
  that default-profile production query/repository changes are needed.
- Add only the proven per-module test metadata and example/application metadata.
  Keep fixture types out of published production artifacts. If an integration
  component owns remote executable types, classify those types in its artifact.

### 3. Replace the Session reset lambda and ship a reviewed narrow policy

**Implemented and scoped qualification complete.** The requirements below are
retained for review; exact source, serializer changes, commands and limits are
in [session-implementation.md](session-implementation.md).

- Introduce a named static reset-interval entry processor, parallel to
  `SessionUpdateEntryProcessor`. Its wire state should contain the required
  interval, without capturing the Spring repository bean, cache, or publisher.
- Preserve existing expired-session handling, interval update, and entry-expiry
  behavior. Define and test its serialization contract and POF registration;
  review the type ID and compatibility implications before assigning it.
- In `coherence-spring-session/src/main/resources/META-INF/coherence/security-config.xml`,
  classify the new processor, `SessionUpdateEntryProcessor`, and
  `PrincipalNameExtractor` with `executable="true"`.
- Allow the required payloads. Audit the supported authentication graph,
  including `AbstractAuthenticationToken`, before freezing external Spring
  Security entries. Keep application-defined principal/details/attribute classes
  application-owned. Omit `CoherenceIndexedSessionRepository` from the production
  policy after replacing its lambda.
- An authored XML policy avoids requiring new annotation APIs or a Java 21-only
  generator in the Java 17 baseline. Validate `version="1.0"`, permitted `source`
  values such as `manual`, exact class names, and packaged resource contents.
- Test reset behavior through embedded, Extend, and gRPC boundaries, including
  explicit dynamic-code deny for this named operation. Confirm that the reset
  path produces no executable-classification rejection. Rerun all Session cases
  with the production candidate; the historical eight-entry experiment is not
  its acceptance run.

### 4. Make all remote serializer configurations explicit

**Session fixtures complete; core fixtures remain separate work.** Session
selects explicit Java/POF endpoints, verifies active protocols and has no unsafe
negotiation fallback in its acceptance runs.

- Cover the ordinary gRPC Session fixture, the POF Session fixture, and core
  gRPC fixtures. Select and verify a supported POF/JSON or server-owned default
  configuration at both endpoints; assert the effective serializer/protocol.
- Remove reliance on a rejected negotiation followed by an unspecified fallback.
  If compatibility fallback is intentionally supported, isolate and test it as
  its own scenario with documented expected behavior.
- Use a dedicated POF client configuration and matching type registrations for
  the POF fixture. Preserve serializers for supported Spring Security payloads.
- Qualify an intentional Java-format configuration separately only when supported
  and narrowly approved on the server. Do not enable every client-selected format.
- Include negative serializer-selection tests and successful real operations.

### 5. Restore automatic CacheStore IT coverage

- Bind `CacheStoreDemoIT` to Failsafe in its own module.
- Replace the obsolete Health-MBean assumption with a supported bounded readiness
  check that reports the cause when startup/readiness fails. The diagnostic
  application-admin Ready check is one candidate, not a production endpoint change.
- Package exact sample-model metadata in the shared sample core artifact so
  client and server receive it.
- Execute all four methods through the normal reactor and verify database
  read-through/write-through behavior, not merely application startup.

### 6. Define and test the Spring configuration contract

- Document the JVM-only hardening switch and memoization timing in this snapshot.
- Add fresh-process coverage for JVM properties, Spring environment properties,
  Boot property maps, and early config-data loading. Verify child JVM behavior.
- Coordinate any Coherence resolver change before promising `application.yaml`
  parity. Consider actionable validation of a configured-but-ineffective setting;
  avoid late global property mutation.
- Explain compatibility-mode shadow enforcement accurately. Do not equate
  `isExecutable() == false` with a rejected operation when enforcement is disabled.

### 7. Qualify policy boundaries and the complete reactor

- Add positive embedded/Extend/gRPC cases for Session lifecycle, expiry, principal
  lookup and security attributes; repository CRUD/query/projection/sort/aggregation,
  async operations and lambdas; listeners/transformations/topics; and config-data
  loading where supported.
- Prove that an unlisted payload and an unclassified **named** executable are
  rejected before user code runs in hardened mode. Test dynamic-code allow/deny
  separately according to the intended support contract.
- Cover bounded container limits, authenticated Coherence endpoints, TLS hostname
  mismatch, and durable topic restart where applicable. Passing startup/sample
  tests alone does not exercise these gates.
- Run the full supported matrix with test failure ignoring disabled. Preserve
  negative-case expectations and security telemetry alongside ordinary pass counts.

### 8. Update guidance and retain reviewable evidence

- Align the compatibility table, 5.0 migration guide, core/data/session/Boot guides,
  sample resources, and startup commands with the qualified configurations.
- Explain payload permission, executable classification, serializer selection,
  per-JAR policy merging, shaded-JAR merging, receiver deployment, application-owned
  security attributes, and independent HTTP authentication.
- Keep compatibility mode a temporary migration option rather than an integration
  default. Describe the snapshot's dynamic-code allowance separately.
- Keep decisive excerpts, source identities, commands, diagnostic inputs, and
  limitations with the assessment. Name logs by experiment; duplicate copies do
  not constitute independent evidence.

## Broader hardening inventory and remaining coverage

The following comes from matching core/gRPC sources. Rows without a failing
integration test describe coverage requirements, not demonstrated regressions.

| Area | Tested snapshot behavior | Required integration coverage |
| --- | --- | --- |
| Serialization | Java/ExternalizableLite class gates, JEP 290 integration, bytecode deny rules and strict POF registration/container checks; configured POF serializers remain a separate trust boundary | Exact metadata and each supported serializer |
| Remote execution | Classification of processors, aggregators, filters, extractors, comparators and other remote work at applicable boundaries | Named integration-owned executables and real negative controls; ordinary client listeners do not automatically require executable classification |
| Dynamic code | Default allow in both security modes; explicit deny adds a separate gate | Documented allow/deny combinations; no blanket static-lambda conversion |
| Container limits | 64 MiB container bytes, 262144 elements, 131072 map entries; hardened mode rejects unlimited settings | Large supported payloads/results and bounded overrides |
| Metrics/management HTTP | Basic authentication is the hardened default | Authentication for Coherence endpoints; distinguish in-process Micrometer binding and Spring Actuator endpoints |
| TLS/identity | Stricter hostname verification; default identity asserter accepts no token but rejects a raw Subject under hardening; applicable proof/signature checks tighten | Configured secure connections and Spring Security use; hardening alone does not enable TLS or authenticate all anonymous cache clients |
| Invocation/concurrent proxies | Hardening-aware disabled defaults for remote exposure | Actual usage and intentional endpoint configuration with executable checks retained |
| gRPC diagnostics | Automatic Channelz disabled; automatic error disclosure safe | Public status/behavior assertions and server-side diagnostics |
| Management/Reporter/NameService/XML | Constrained management/Reporter inputs and passive discovery values; required XML protections fail closed | Discovery, management, report and custom XML scenarios |
| Persisted topic policy | Hardened default rejects subscriber-policy drift | Durable groups across restart/deployment and metadata changes |

## Completion criteria

- Java 17 with the supported compatible Coherence baseline passes. JDK 21 with
  the frozen 26.10 artifacts passes both unset and explicit hardened mode while
  producing release-17 integration bytecode.
- All existing suites and the four CacheStore IT methods run through their normal
  bindings, without ignored failures or unexplained coverage exclusions.
- Data's 267 errors have a qualified resolution; fixture changes and production
  behavior changes are distinguished by evidence.
- The named Session reset processor and packaged production metadata pass real
  boundary tests without relying on a dynamic classification override for reset.
- Ordinary remote fixtures have verified serializer choices. Expected fallback
  and negative-policy cases are identified separately.
- Unlisted payload/unclassified named-executable controls retain rejection before
  user execution. Published policies contain reviewed production types only.
- Guidance, samples, artifact contents, and retained evidence match the accepted
  configurations. Production TLS/authentication or other untested features are
  not declared qualified by unrelated green samples.

## Evidence excerpts and provenance

These excerpts preserve the decisive observations beside the plan. No additional
test run was performed for this document revision. Raw debugging evidence remains
under `/private/tmp/codex-work/coherence-spring-hardening.0qJBOK/evidence/`.

### Session experiments and log identity

`session-metadata-six-types.log.gz` and `session-metadata-control.log.gz` contain
the **same decompressed six-entry experiment**. They must not be treated as
separate six- and eight-entry runs. Decompressed SHA-256:

```text
2092a2531bb3d3c3796766c2b2dd02439d406126a476134fd90cd6b2c05f5e9d
```

Its telemetry names the denied superclass:

```text
route=UNCLASSIFIED|gate=filter|principal=-|denied-class=org.springframework.security.authentication.AbstractAuthenticationToken|reason=serialization-allowlist-rejected
Tests run: 16, Failures: 0, Errors: 3, Skipped: 0
```

It also names the generated reset lambda, rather than the repository bean itself:

```text
com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository$lambda$resetMaxInactiveIntervalForActiveSessions$7f4f5f2c$1$0:881E111AE1FACBBB885C658A754969A4
```

The separate eight-entry run is `session-all-metadata-control.log.gz`.
Decompressed SHA-256:

```text
442ef4a440a0df3763b6a61f34645f8e85cb424114ca612063ed68576f289623
```

That run reports 93 tests with 16 errors, all in the POF suite, and includes
classification warnings on successful Extend/gRPC reset operations. In the
following normalized excerpts, `<generated-reset-lambda>` replaces the long
generated name above; logger/process prefixes are omitted:

```text
route=EXTEND_PROXY|gate=remote-executable-policy|principal=-|denied-class=<generated-reset-lambda>|reason=PROCESS_ENTRY
route=GRPC|gate=remote-executable-policy|principal=-|denied-class=<generated-reset-lambda>|reason=PROCESS_ENTRY
Tests run: 93, Failures: 0, Errors: 16, Skipped: 0
```

`pof-serializer-control.log.gz` then records:

```text
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
```

Decompressed SHA-256:
`0206d15d66d0564c7ee4de1e03f4a82bc5e3d5e37b7200c6e4d826884b289b25`.
That run also retains a generated reset-lambda classification warning. Its green
result establishes the bounded behavior, not completed executable classification.

### Source checks that constrain interpretation

The matching core source establishes:

- `DefaultRemoteExecutablePolicy.java`, lines 28–35: when executable enforcement
  is disabled, an unclassified class records `would_reject` and the method returns.
- `CoherenceMode.java`, lines 115–117: executable enforcement follows the
  hardening setting. Its dynamic-default-deny method returns false.
- `SerializationAllowlist.java`, lines 407–415 and 472–542: recognized synthetic
  lambda/method-reference names are mapped to their owner before allowlist lookup.
- `RemoteInstallGate.java`, lines 1224–1257: dynamic-class classification rejection
  is caught and the independent dynamic-install gate determines admission.

These source checks correct the review's conclusions that compatibility mode
still necessarily rejects the probed executables and that an owner entry cannot
cover a generated method-reference class. They do not eliminate the need for
Data diagnostics or a better production design for the Session reset operation.

### Retained artifacts

- [Unmodified-source test inventory](test-results.json): original results remain
  unchanged by this plan revision.
- `test-reports.tar.gz`: baseline XML and distinct `session-eight-entry-policy`,
  `pof-client-matched`, and `cachestore-readiness-corrected` report groups.
- `diagnostic-session-security-config.xml`: historical eight-entry policy, not
  the proposed shipping policy.
- `diagnostic-pof-client-cache-config.xml`, `diagnostic-IsSpringUp.java`,
  `PolicyProbe.java`, probe output, `diagnostic-commands.txt`, and compressed Maven
  logs: exact bounded experiment inputs/results.
- `cachestore-readiness-control.log.gz`: four methods, zero assertion failures,
  three errors. Decompressed SHA-256:
  `27d7fa10f81146180901db66f0a954d0c1722370b23c58eedaf0e5bdedf7b488`.
- `security-source-excerpts.tar.gz`: matching security source entry points.
  Invalid diagnostic XML attempts are retained separately and are not baseline
  integration defects. Disposable build trees and dependency source archives
  were removed after evidence capture.

### Primary sources

- [Exact core snapshot source JAR](https://central.sonatype.com/repository/maven-snapshots/com/oracle/coherence/ce/coherence/26.10-SNAPSHOT/coherence-26.10-20261013.200824-3-sources.jar).
- [Exact gRPC snapshot source JAR](https://central.sonatype.com/repository/maven-snapshots/com/oracle/coherence/ce/coherence-grpc/26.10-SNAPSHOT/coherence-grpc-26.10-20261013.201141-3-sources.jar).
- Source entry points include `CoherenceMode`, `SerializationAllowlist`,
  `SecurityConfig`, `RemoteExecutablePolicy`, `DefaultRemoteExecutablePolicy`,
  `RemoteInstallGate`, `RemoteExecutionMode`, `SerializationLimitPolicy`,
  `HttpAuthDefaults`, `GrpcSerializerPolicy`, `GrpcDiagnosticsPolicy`,
  `DefaultIdentityAsserter`, `SSLHostnameVerifierPreprocessor`,
  `ManagementInvocationPolicy`, `ReporterSecurity`, `NameServiceValuePolicy`, and
  `TopicsPersistedPolicyDrift`.
- The local Coherence development guide, `prj/docs/core/04_serialization_hardening.adoc`,
  at checkout commit `6834a4e0a44a5779ff1d25fc6e9573632729d19f`, provides migration
  context. Matching tested binaries/sources govern the snapshot findings.
- [Published 26.07 release notes](https://github.com/oracle/coherence/releases)
  describe an older opt-in posture; they do not establish this snapshot's defaults.
