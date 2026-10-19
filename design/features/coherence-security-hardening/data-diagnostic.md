# Coherence Spring Data: frozen 26.10 hardening diagnostic

Completed 14 October 2026, Europe/Belgrade.

## Decision

Five exact **test-resource** class entries resolve the assessed Data failure set.
All 335 existing Data cases passed, with zero failures, errors, or skips and
`maven.test.failure.ignore=false`. The case inventory matches the historical
baseline exactly; all 267 previously errored cases now pass.

For this frozen embedded profile, implement fixture metadata. No production Data
Java, POM, query, projection, or runtime-resource change is indicated by these
failures. At diagnostic completion the candidate existed only in a disposable
source copy; that investigation did not install the metadata in the repository.
The permanent implementation and
its separate verification are recorded below.

This is bounded evidence. The generated classes remain `executable=false`.
The existing Data tests use an embedded distributed cache, and the frozen
snapshot independently permits dynamic code by default. Exact owner permission
does not qualify stricter dynamic-code denial or remote installation through
Extend/gRPC. Keep those qualifications separate.

## Source, runtime, and artifact identity

- Branch `5.x`, HEAD `37cf05ea59bd60c03b21b52acc1eb4c1045859a3`.
  It matches the assessed commit. There were no tracked changes. Pre-existing
  untracked `AGENTS.md`, `JOURNAL.md`, and `design/` content were preserved.
- Experimental source came from `git archive HEAD`. No existing checkout build
  output, tests, production source, POMs, or runtime resources were changed.
- Maven **3.9.9** through Maven Wrapper **3.3.2**; execution JDK Temurin **21.0.12.1** at
  `/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home`.
- Every Maven experiment used **`-Pjdk17` / `javac --release 17`**. Inspected
  `CoherenceServer`, `CoherenceRepositoryQuery`, and test `Book` classes have
  class-file major **61**. The separate probe also compiled with `--release 17`.
- `coherence.security.mode` stayed unset. The probe reports `hardening=true`,
  `allowlistEnforced=true`, and `executableEnforced=true`. No serializer allowlist,
  package wildcard, bytecode exception, or explicit dynamic-code allowance was
  added. The sole dynamic property experiment set the stricter value `deny`.
- Resolution used `-o` and a task-owned clone of the cached Maven repository,
  with the retained origin-mapping `settings.xml`. All ten artifact checksums in
  [the original inventory](test-results.json) matched before and after the runs.

Actual Data test classpaths contained these seven Coherence artifacts:

| Artifact | Resolved snapshot | SHA-256 |
| --- | --- | --- |
| coherence | 26.10-20261013.200824-3 | 9c48703a5112025dac8afd315338eff914a00fd9c7de1e4316badc755b2dadc7 |
| coherence-java-client | 26.10-20261013.201141-3 | e838cea1368d10c4f64c3045f0e316203ba808a517281300e6e68cfab4523c1c |
| coherence-grpc-client-common | 26.10-20261013.201141-3 | 302a05af039727f43d4d26ea1720ecb499207f1a33f02b6e0a872b0b2e062278 |
| coherence-grpc | 26.10-20261013.201141-3 | 20c2833787e5cf0640aa185b181c79b48e48d992abcc8cf8677a47e5e4a2e462 |
| coherence-protobuf | 26.10-20261013.201141-3 | 55e80942715c3d1b157c28e6c0e90e6cc2ad1ba524f2cb0f7467f5d69a1eff16 |
| coherence-bedrock-testing-support | 26.10-20261013.201141-3 | 1b73e1795c1747707ca07afd14aaf4c478b76ded30f13cb2eb67cc63757a4431 |
| coherence-bedrock | 26.10-20261013.201141-3 | dd53be42e41750358dd66566b7e431c89f6ca21b4071ce36e1947d6ab845222d |

The historical 335-case baseline compiled with `-Pjdk21`. This run intentionally
changes the compiler target to 17 while retaining its source, execution JDK, and
artifact identities. Eight selected baseline failures reproduced with release 17
before adding metadata. A new complete unmodified release-17 baseline was not
run. The default product dependency remains 26.04; the Java 21-only frozen 26.10
artifact cannot execute on Java 17.

## Focused failures and responsible gates

The selected methods were `ensureMaxBigDecimal`, `ensureUpdateWithEntityFactory`,
and `ensureUpdateWithIdAndRemoteFunction` in both repository suites,
`QueryFinderTests.ensureBasicFinderQuery`, and
`ProjectionTests.ensureInterfaceProjection`.

All eight failed without metadata. Full JUnit causes and stderr telemetry are
retained separately from the passing candidate and intentional negative runs.

| Operation | Actual denied class | Synthetic / owner | Boundary and fatal gate |
| --- | --- | --- | --- |
| Sync/async `Book::getPagesAsBigDecimal` | `Book$getPagesAsBigDecimal$DB4C11B671CB61FEB3E5E7DC329D73EB` | Observed true; owner `model.Book` | Generated extractor in the aggregator graph; deserialization filter rejects the realized class |
| Sync/async `Book::new` | `Book$<init>$5DC269E5D399F756637A041566198A48` | Observed true; owner `model.Book` | Entity factory in the processor graph; deserialization filter rejects the realized class |
| Sync update lambda | `RepositoryTests$lambda$ensureUpdateWithIdAndRemoteFunction$6985ebc9$1$0:87FC9DB2AD1F769C9105E5754B68C89B` | Observed true; owner `repository.RepositoryTests` | Processor graph; deserialization filter rejects the realized test lambda |
| Async update lambda | `AsyncRepositoryTests$lambda$ensureUpdateWithIdAndRemoteFunction$6985ebc9$1$0:7477E80ACE795D4C4506407EB8F84ED7` | Observed true; owner `repository.AsyncRepositoryTests` | Processor graph; same filter rejection wrapped in `ExecutionException` |
| Derived `findByAuthor` | `model.Book` | Ordinary domain payload | Storage value deserialization during filtering; Java object input filter rejection |
| Closed interface projection | `model.Book` | Ordinary domain payload | Storage value deserialization during extraction; Java object input filter rejection |

All short class names above have prefix `com.oracle.coherence.spring.data.`;
generated Book names additionally use `model.` and generated test names use
`repository.`. The probe observed the full names and their synthetic flags in
the `RemotableSupport` class definitions after invoking the original methods.

Every selected suite uses `SessionNamedCache`, service type `DistributedCache`,
and **`com.tangosol.io.DefaultSerializer`, format `java`**. These are single-member
embedded clusters in the test JVM. Storage-worker and fork-join threads still
deserialize values and executable graphs. The recorded route is
`UNCLASSIFIED`; it must not be relabeled as an Extend or gRPC route.

Decisive baseline telemetry, shortened only by removing timestamp/thread prefixes:

```text
route=UNCLASSIFIED|gate=filter|principal=-|denied-class=com.oracle.coherence.spring.data.model.Book$getPagesAsBigDecimal$DB4C11B671CB61FEB3E5E7DC329D73EB|reason=serialization-allowlist-rejected
route=UNCLASSIFIED|gate=filter|principal=-|denied-class=com.oracle.coherence.spring.data.model.Book$<init>$5DC269E5D399F756637A041566198A48|reason=serialization-allowlist-rejected
route=UNCLASSIFIED|gate=filter|principal=-|denied-class=com.oracle.coherence.spring.data.model.Book|reason=serialization-allowlist-rejected
```

The generated-class causes end at `InvalidClassException: Deserialization of
class ... was rejected`, through `ExternalizableHelper.safeRealize()` and
`validateLoadClass()`. The derived-query and projection chains end at
`InvalidClassException: filter status: REJECTED`, through Java object input and
storage-value deserialization. Their generic JUnit messages do not identify the
payload; the telemetry does. The focused baseline did not fail on executable
classification or bytecode validation.

## Exact candidate and incremental results

Add this resource only to
`coherence-spring-data/src/test/resources/META-INF/coherence/security-config.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<security-config version="1.0" xmlns="http://xmlns.oracle.com/coherence/coherence-security-config">
  <allowed-classes>
    <class name="com.oracle.coherence.spring.data.model.Book" source="manual"/>
    <class name="com.oracle.coherence.spring.data.model.Author" source="manual"/>
    <class name="com.oracle.coherence.spring.data.model.Address" source="manual"/>
    <class name="com.oracle.coherence.spring.data.repository.RepositoryTests" source="manual"/>
    <class name="com.oracle.coherence.spring.data.repository.AsyncRepositoryTests" source="manual"/>
  </allowed-classes>
</security-config>
```

`Book`, `Author`, and `Address` permit the fixture object graph. `Book` also covers
its recognized synthetic getter/constructor classes. Each repository test class
permits its own recognized generated test lambdas. Test instances themselves
are not serialized by the selected lambdas. No generated hash names, projection
interfaces/DTOs, Spring repository beans, production integration classes,
`executable` flags, or additional `lambda-target` flags were needed.

| Experiment | Tests | Passed | Failures | Errors | Skips | Maven exit |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Unmodified focused baseline | 8 | 0 | 0 | 8 | 0 | 0, failures ignored |
| Unmodified focused baseline, stderr telemetry | 8 | 0 | 0 | 8 | 0 | 0, failures ignored |
| Three domain entries only | 8 | 6 | 0 | 2 | 0 | 0, failures ignored |
| Five-entry candidate, focused | 8 | 8 | 0 | 0 | 0 | 0 |
| Five-entry candidate, all Data | 335 | 335 | 0 | 0 | 0 | 0 |
| Negative: remove only Author | 2 | 0 | 0 | 2 | 0 | 1 |
| Negative: remove only Address | 2 | 0 | 0 | 2 | 0 | 1 |
| Negative: candidate plus dynamic deny | 8 | 2 | 0 | 6 | 0 | 1 |

Only the first three collection runs enabled failure ignoring. All candidate
passes and intentional negative runs disabled it. Maven success in those first
three rows is not acceptance evidence.

## Complete Data accounting

| Suite | Historical tests | Historical passed / errors | Candidate passed / errors |
| --- | ---: | ---: | ---: |
| QueryCreatorTests | 45 | 45 / 0 | 45 / 0 |
| QueryFinderTests | 43 | 0 / 43 | 43 / 0 |
| AsyncRepositoryTests | 116 | 12 / 104 | 116 / 0 |
| ProjectionTests | 10 | 0 / 10 | 10 / 0 |
| RepositoryTests | 121 | 11 / 110 | 121 / 0 |
| **Total** | **335** | **68 / 267** | **335 / 0** |

Both inventories have zero failures and skips. `baseline-comparison.json` matches
every suite/method name against the original XML, lists all 267 former errors as
passing, and records an empty remaining-error list. No case was deleted,
excluded, renamed, or replaced to obtain this result.

## Source interpretation and negative controls

The matching frozen Coherence source, `SerializationAllowlist.isAllowlistedName()`
(lines 407–416 in the retained excerpt), first checks the exact class name, then:

```java
String sCapturingClass = fSynthetic ? generatedCapturingClassName(sName) : null;
return sCapturingClass != null && isDirectlyAllowlisted(sCapturingClass);
```

`generatedCapturingClassName()` recognizes `$lambda$` and method-reference names,
including `<init>` and their hexadecimal versions. The runtime probe confirms
that the exact `Book` and test-owner entries are used, rather than assuming that
synthetic names require separate registrations:

```text
PROBE mode=null hardening=true runtimeMode=DEV executableEnforced=true allowlistEnforced=true dynamicAllowed=true
PROBE generated=com.oracle.coherence.spring.data.model.Book$getPagesAsBigDecimal$DB4C11B671CB61FEB3E5E7DC329D73EB synthetic=true owner=com.oracle.coherence.spring.data.model.Book allowlisted=true executable=false
PROBE generated=com.oracle.coherence.spring.data.model.Book$<init>$5DC269E5D399F756637A041566198A48 synthetic=true owner=com.oracle.coherence.spring.data.model.Book allowlisted=true executable=false
```

`RemoteExecutablePolicy.isExecutable()` (lines 67–70) instead checks the actual
class's annotation or exact executable name. It does not perform this owner
mapping. All observed generated classes remain unclassified. The probe invokes
the named-class install gate on the allowed but unclassified `Book.class` and
requires its `PROCESS_ENTRY` policy rejection. This is a direct gate control,
not a claim that Book is an entry processor or that an Extend/gRPC request was
executed.

`RemoteInstallGate.enforceInstall()` (lines 1224–1264) catches executable-policy
rejection for dynamic classes and applies `RemoteExecutionMode` independently.
The latter defaults to allow on this snapshot. However, the embedded positive
probe's telemetry contains no executable-policy checks before the explicit gate
control. The full Data log also has no executable-policy rejection warnings.
Consequently, these Data passes establish deserialization permission and allowed
bytecode checks, not successful classification or remote install qualification.

The controls retain independent enforcement:

- Removing Author or Address alone makes both existing query/projection methods
  fail. Telemetry names the removed type with
  `reason=serialization-allowlist-rejected`. Each graph entry is needed.
- The domain-only experiment leaves exactly the sync/async test lambdas rejected,
  proving that domain entries do not cover unrelated test owners.
- An unlisted Serializable payload is rejected using each suite's actual cache
  serializer. The probe requires `InvalidClassException`; it does not accept an
  unrelated exception as success.
- The direct named-class executable control rejects an allowlisted class that
  lacks executable classification. `DefaultRemoteExecutablePolicy.enforce()`
  remains active under hardened defaults.
- A generated lambda whose bytecode references `java.lang.Runtime` is rejected
  with `site=LAMBDA, reason=bytecode-references-gadget`. The lambda is never
  invoked. A separate class-name check also returns `class-name-on-denylist`.
- `coherence.remote.dynamic.unauthenticated=deny` rejects all six selected
  getter/constructor/test-lambda operations at local dynamic-lambda creation,
  before their cache calls. It reports `dynamic-remote-denied-by-mode`. The
  derived query and projection still pass. This is an intentional stricter-policy
  result, not six unresolved default-profile errors.

The telemetry tag `mode=dev` describes `coherence.mode`, whose default is DEV.
`SerializationTelemetry.modeTag()` uses that runtime mode. It does not describe
`coherence.security.mode`; the probe separately proves hardening/enforcement true.

The selected derived-query path creates ordinary Coherence property filters in
[CoherenceQueryCreator](../../../coherence-spring-data/src/main/java/com/oracle/coherence/spring/data/repository/query/CoherenceQueryCreator.java).
The selected closed projection builds `ReflectionExtractor`/fragment extractors
and invokes a built-in extraction processor in
[CoherenceRepositoryQuery](../../../coherence-spring-data/src/main/java/com/oracle/coherence/spring/data/repository/query/CoherenceRepositoryQuery.java).
DTO construction and Spring projection processing occur after results return.
The `Book::new` failure comes from explicit repository-test input, not that DTO
constructor path. Passing all 43 finder, 10 projection, and 45 query-creator cases
with unchanged production code supplies the bounded basis for the metadata-only
decision.

## Reproduction and retained evidence

This task's retained evidence is at:

```text
/private/tmp/codex-work/coherence-spring-data-diagnostic.tD00Ta/evidence/
```

It contains compressed logs, distinct JUnit reports, per-run JSON/exit status,
the exact XML variants, source excerpts, the assertion-bearing probe, artifact
checksums, compiler-target evidence, and a file manifest. The earlier assessment
evidence remains untouched at
`/private/tmp/codex-work/coherence-spring-hardening.0qJBOK/evidence/`.

Prepare a fresh task-owned source/dependency copy. Refuse checksum drift:

```bash
evidence=/private/tmp/codex-work/coherence-spring-data-diagnostic.tD00Ta/evidence
export DATA_DIAGNOSTIC_ROOT=$(mktemp -d /private/tmp/codex-work/coherence-data-repro.XXXXXX)
mkdir -p "$DATA_DIAGNOSTIC_ROOT/source" "$DATA_DIAGNOSTIC_ROOT/evidence" "$DATA_DIAGNOSTIC_ROOT/jvm-tmp"
git archive 37cf05ea59bd60c03b21b52acc1eb4c1045859a3 | tar -x -C "$DATA_DIAGNOSTIC_ROOT/source"
cp -cR /Users/vasac/.m2/repository "$DATA_DIAGNOSTIC_ROOT/repository"
cp "$evidence"/{settings.xml,artifact-identities.json,run.sh,summarize.py,probe.sh,DataPolicyProbe.java,verify-artifacts.py,*security-config.xml} "$DATA_DIAGNOSTIC_ROOT/evidence/"
python3 "$evidence/verify-artifacts.py" "$evidence/artifact-identities.json" "$DATA_DIAGNOSTIC_ROOT/repository"
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
focused='RepositoryTests#ensureMaxBigDecimal+ensureUpdateWithEntityFactory+ensureUpdateWithIdAndRemoteFunction,AsyncRepositoryTests#ensureMaxBigDecimal+ensureUpdateWithEntityFactory+ensureUpdateWithIdAndRemoteFunction,QueryFinderTests#ensureBasicFinderQuery,ProjectionTests#ensureInterfaceProjection'
runner="$DATA_DIAGNOSTIC_ROOT/evidence/run.sh"
```

The runner executes this Maven command, captures exit status, and copies Data XML
before another run can overwrite it:

```bash
./mvnw -o -B -s "$DATA_DIAGNOSTIC_ROOT/evidence/settings.xml" -Pjdk17 \
  -Dmaven.repo.local="$DATA_DIAGNOSTIC_ROOT/repository" \
  -Dcoherence.version=26.10-SNAPSHOT -pl coherence-spring-data -am \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -DargLine="-Djava.io.tmpdir=$DATA_DIAGNOSTIC_ROOT/jvm-tmp" \
  -Dtest="$focused" -Dcoherence.log=stderr \
  -Dmaven.test.failure.ignore=false test
```

Run the unmodified focused baseline before copying any policy. For collection,
use `true` only on the baseline/domain-only invocations. For each policy variant,
copy its retained XML to the test-resource path; replace the file rather than
merging policies. When testing no policy after one was used, also remove its
copied `target/test-classes/META-INF/coherence/security-config.xml`.

```bash
bash "$runner" baseline-telemetry "-Dtest=$focused" -Dcoherence.log=stderr -Dmaven.test.failure.ignore=true
policy="$DATA_DIAGNOSTIC_ROOT/source/coherence-spring-data/src/test/resources/META-INF/coherence/security-config.xml"
mkdir -p "$(dirname "$policy")"
cp "$evidence/domain-only-security-config.xml" "$policy"
bash "$runner" domain-only-focused "-Dtest=$focused" -Dmaven.test.failure.ignore=true
cp "$evidence/candidate-security-config.xml" "$policy"
bash "$runner" candidate-focused "-Dtest=$focused" -Dcoherence.log=stderr -Dmaven.test.failure.ignore=false
bash "$runner" candidate-all -Dtest=RepositoryTests,AsyncRepositoryTests,ProjectionTests,QueryFinderTests,QueryCreatorTests -Dcoherence.log=stderr -Dmaven.test.failure.ignore=false
bash "$DATA_DIAGNOSTIC_ROOT/evidence/probe.sh"
```

To reproduce each removal control, copy `negative-no-author-security-config.xml`
or `negative-no-address-security-config.xml` to `$policy` and run the runner with
`-Dtest=QueryFinderTests#ensureBasicFinderQuery,ProjectionTests#ensureInterfaceProjection`,
stderr logging, and failure ignoring false. Expect exit 1 and two errors.
Restore the candidate XML, then add
`-Dcoherence.remote.dynamic.unauthenticated=deny` to the eight-case invocation.
Expect exit 1 and six errors. Run these clusters sequentially.

## Next action and limits

The five-entry test resource is now implemented and verified as recorded below.
Permanent negative Java coverage remains separate work. Keep
application domain permissions owned by the application. Do not ship fixture
classes in a library policy or mark domain/test owners executable to chase a
classification warning.

Explicit hardened mode is now verified for the existing embedded Data suite.
Qualify actual remote Data boundaries separately, including any supported strict
dynamic-code-deny or static-lambda profile. The current tests and direct gate
probe do not cover Extend/gRPC negotiation,
authenticated production callers, POF, arbitrary domain graphs, multiple-member
clusters, or every named/dynamic executable installation path. Core and
documentation failures from assessment step 2 also remain to be diagnosed.

The original diagnostic performed no complete reactor, Java 17/26.04 runtime
suite, release acceptance, hosted CI, commit, push, or publication.
Disposable source/build trees,
dependency copies, extracted sources, probe classes, and task JVM temporary files
were removed after evidence capture; only the identified evidence was retained.

## Permanent fixture implementation and verification

Implemented and verified 14 October 2026, Europe/Belgrade. The repository now
contains [the five-entry test policy](../../../coherence-spring-data/src/test/resources/META-INF/coherence/security-config.xml)
with the same exact names and `source="manual"` attributes as the diagnostic.
No wildcard, generated hash name, `executable`, or `lambda-target` attribute was
added. Production Java, POMs, main resources, default Coherence 26.04, and the
Java 17 baseline remain unchanged.

These are new runs of the actual repository resource, copied explicitly into a
task-owned source tree while still untracked; they do not replace the diagnostic
results above or the original `test-results.json`.

| Execution JDK | Coherence | JVM security mode | Passed | Failures / errors / skips |
| --- | --- | --- | ---: | --- |
| Temurin 17.0.20.1 | 26.04, Build 120796 | unset | 335 | 0 / 0 / 0 |
| Temurin 21.0.12.1 | frozen 26.10-SNAPSHOT | unset | 335 | 0 / 0 / 0 |
| Temurin 21.0.12.1 | frozen 26.10-SNAPSHOT | hardened | 335 | 0 / 0 / 0 |

Each sequential `clean package` run used Maven 3.9.9, `-Pjdk17`/release 17,
offline resolution, the retained settings-origin mapping, fresh Surefire JVMs,
and failure ignoring disabled. All suite/method inventories match the preceding
335-case diagnostic. All ten frozen artifact hashes matched before and after;
the seven actual Data classpath identities match the table above.

Every run copied the policy byte-for-byte to Data `target/test-classes`; it was
absent from `target/classes` and the normal Data JAR, which also omitted all five
fixture classes. All 29 packaged product classes have major 61, as do the
compiled Data/core/test-support outputs inspected separately.

The first sandboxed Java 17 attempt was blocked at UDP socket initialization:
45 cases passed and 290 reported startup errors, Maven exit 1. The unchanged
command passed all 335 with local socket access. That attempt is retained
separately and is not passing acceptance evidence. Javadoc generation was
skipped; this was the Data suite plus its build dependencies, not the full
reactor, remote Data, strict dynamic-code-deny, or hosted CI qualification.

Exact commands, XML reports, per-run artifact/packaging/bytecode records, and the
blocked-attempt evidence are retained under
`/private/tmp/codex-work/coherence-spring-data-fixture.QaqVsy/evidence/` and recorded
in `JOURNAL.md`. Task-owned source/build/dependency copies and JVM temporary files
were removed after capture. Earlier evidence remains untouched. No staging,
commit, push, publication, or messages to other chats occurred.
