# Core and documentation fixture hardening fixes

Completed October 16, 2026 at 01:24:03 CEST (UTC+0200; Europe/Belgrade). Branch `5.x`, HEAD
`37cf05ea59bd60c03b21b52acc1eb4c1045859a3`, preserving all prior uncommitted work.

## Result

**The repaired ordinary full reactor passes: 696/696 tests, all 28 modules
SUCCESS, exit 0, zero failures/errors/skips.** This is one fresh, clean, complete
`verify` invocation against the exact frozen Coherence 26.10 snapshot on JDK 21,
with `-Pjdk17`, security mode unset, fatal tests, normal Checkstyle/Javadoc and
no module/test selectors or skip flags. All 692 unique prior case identities are
still present and pass; four meaningful regression cases were added.

Core now passes 168/168 and Docs 11/11, including all twelve cases that previously
errored. Data 335/335, Session 117/117, Boot starter 50/50 and all 15 ordinary sample
cases also pass. Normal HTML, single-page HTML, PDF, aggregate Javadoc, Docs ZIP
and Website packaging complete. CacheStoreDemoIT remains excluded by the unchanged
lifecycle; this result does not claim its four methods pass.

The prior failed run remains historical in
[the full-reactor diagnostic](full-reactor-qualification-2026-10-16.md).
`test-results.json` is unchanged.

## Implementation and graph review

The runtime fix consists of **two test-owned resources**:

- `coherence-spring-core/src/test/resources/META-INF/coherence/security-config.xml`:
  seven exact manual entries. `data.Person`, its nested `data.PhoneNumber` and
  `CoherenceInjectorTests$BeanOne` are payloads. Exactly four named processor fixtures
  have `executable="true"`: `EventHandlerTests$Uppercase`,
  `MapEventHandlerTests$Uppercase`, `InterceptorsTests$Uppercase` and
  `event.mapevent.UppercaseEntryProcessor`, all under
  `com.oracle.coherence.spring`.
- `coherence-spring-docs/src/test/resources/META-INF/coherence/security-config.xml`:
  six exact non-executable names under `com.oracle.coherence.spring.doc`: the two
  `Person` types in `di.viewtransformer` / `di.wherefilter`, and `Plant` plus its
  `PlantType` enum in each of `extractorbinding` / `filterbinding`.

`Person` serializes its `PhoneNumber` field, so the Core graph needs both types.
Its strings and LocalDate use supported built-ins. Each documentation Plant
serializes its own enum. The first positive graph check failed with exact filter
rejection of `extractorbinding.PlantType` when only the four outer types were
permitted; both reviewed enum types are therefore included. No approval for
`java.lang.Enum`, owner/test-container classes, generated lambda names or packages
was added. The processor fixtures are named static/no-capture operations that
change the existing Person last-name field; no production processor changed.

Separate fresh-JVM probes against the actual test classpaths assert allowlisting
and classifications for all 13 entries: the three Core payloads and six Docs
payload/enum types remain non-executable; only the four processors are classified.
The normal Core and Docs JARs exclude the test security resources and helpers.
Production policy and behavior, serializer settings, all dependency/POM/CI rules,
Java 17 target and the default Coherence 26.04 dependency remain unchanged.

## Permanent regression coverage

Two package-private `FixtureSerializationTests` classes were added:

- Core: `coherence-spring-core/src/test/java/com/oracle/coherence/spring/FixtureSerializationTests.java`.
  The positive case round-trips a Person with a real PhoneNumber and LocalDate and
  asserts the restored graph.
- Docs: `coherence-spring-docs/src/test/java/com/oracle/coherence/spring/doc/security/FixtureSerializationTests.java`.
  The positive case round-trips all four example payloads, including both PlantType
  enums, and asserts all populated values.

Each class also includes an intentionally unlisted Serializable payload whose
private `readObject` increments a counter. The negative case first requires that
its exact class is not allowlisted, then performs actual serialization and requires
an `InvalidClassException` root cause on deserialization and zero read callbacks.
This detects a disabled filter or an accidental permission for arbitrary test types;
it cannot pass on a generic network exception. No old test was weakened, removed,
renamed or skipped. The controls compile without new Coherence API dependencies:
capability detection uses reflection. Only the two new deny cases are skipped
on 26.04, where that security API is absent; positive graph cases execute there.

An initial normal Checkstyle run caught that the private payload helper needed to
be final; the helper was corrected in both new classes. A subsequent run exposed
and recorded the missing Docs enum permission described above. These intermediate
failures remain separate from the accepted verification, with input hashes/logs.
No temporary Checkstyle suppression or Javadoc bypass was used.

## Verification

| Run | Actual JVM / dependency | Scope | Tests / passed / skipped | Failures / errors | Exit |
| --- | --- | --- | --- | --- | ---: |
| `full-frozen-default` | Temurin 21.0.12.1+1-LTS / frozen 26.10, mode unset | Clean full reactor, normal verify | 696 / 696 / 0 | 0 / 0 | 0 |
| `frozen-hardened` | Temurin 21.0.12.1+1-LTS / frozen 26.10, explicit hardened | Affected Core/Docs suites, clean test | 31 / 31 / 0 | 0 / 0 | 0 |
| `baseline17` | Temurin 17.0.20.1+1 / 26.04, mode unset | Same affected suites, clean test | 31 / 29 / 2 expected API skips | 0 / 0 | 0 |
| `focused-frozen-r3` | Temurin 21.0.12.1+1-LTS / frozen 26.10, mode unset | Four new graph/deny cases | 4 / 4 / 0 | 0 / 0 | 0 |

The affected selection includes the six historically failing Core suite classes,
all four failing Docs example classes and both new regression classes. Prerequisite
modules compile with normal checks; modules without matching selected tests have
no runtime-test claim in those scoped rows. The explicit-hardened and Java 17 rows
are not full-reactor runs. All runs are sequential, have fatal test failures and
compile for release 17; no unrelated process is terminated.

| Invocation | Start (Europe/Belgrade, CEST) | End (CEST) |
| --- | --- | --- |
| `full-frozen-default` | 2026-10-16T01:08:26+0200 | 2026-10-16T01:19:26+0200 |
| `frozen-hardened` | 2026-10-16T01:20:05+0200 | 2026-10-16T01:21:22+0200 |
| `baseline17` | 2026-10-16T01:21:24+0200 | 2026-10-16T01:22:39+0200 |

All 28 reactor modules report SUCCESS in the full run. Test-bearing coverage is:

| Module/group | Tests passed |
| --- | ---: |
| Core | 168 |
| Data | 335 |
| Session | 117 |
| Boot starter | 50 |
| Executable documentation | 11 |
| Ordinary sample cases | 15 |
| **Total** | **696** |

Normal configured Checkstyle passes without changed rules or suppressions. Javadoc
runs and emits ordinary warnings but no error diagnostics; the existing
`failOnError=false` setting was not changed. The 17 generated HTML/PDF files and
packaged artifacts are fingerprinted before scoped clean runs remove outputs.
This verifies generation and packaging, not a visual review of every PDF page or
a deployed Website/WAR. Every compiled project main/test class inspected has major
61. The frozen Coherence core itself requires Java 21; the Java 17 row uses 26.04.

Actual runtime/mode/fatal-test properties are checked from all accepted XML suites
and sampled parent/child JVMs. The hardened row explicitly sets mode in Maven and
Surefire JVMs. The full-run mode is absent; existing Session endpoint tests verify
its actual child properties and serializer/protocol configuration. The JVM audit
validates every observed Coherence classpath against the applicable artifact inventory.

## Reproduction and artifact identity

Evidence root: `/private/tmp/codex-work/fixture-hardening-5401ypr8/evidence/`.

Maven is the cached **3.9.9** distribution matching the wrapper. JDK homes are
`/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home` and
`/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home`.
JAVA_TOOL_OPTIONS, JDK_JAVA_OPTIONS and _JAVA_OPTIONS are cleared. Temporary-directory
properties isolate task files; no security-policy relaxation is set. Full command:

```bash
/private/tmp/codex-work/fixture-hardening-5401ypr8/evidence/run.sh full-frozen-default 21 26.10-SNAPSHOT unset clean verify
```

That runner invokes the cached Maven binary in the complete current-source copy with:

```text
-o -B -ntp -s /private/tmp/codex-work/fixture-hardening-5401ypr8/evidence/settings.xml -Pjdk17
-Dmaven.repo.local=/private/tmp/codex-work/fixture-hardening-5401ypr8/repository
-Dcoherence.version=26.10-SNAPSHOT -Dmaven.test.failure.ignore=false
-DargLine=-Djava.io.tmpdir=/private/tmp/codex-work/fixture-hardening-5401ypr8/jvm-tmp
--fail-at-end clean verify
```

There is no `-pl`, `-Dtest`, skip flag, style suppression or Javadoc bypass in that
full invocation. Exact exports, expanded commands, versions, start/end/status and
logs are in each `*-command.txt` and companion files. `affected-matrix.sh` and
`matrix-runner.sh` record the bounded 31-case selector and fresh JVM mode settings.
The full source was a complete working-tree copy, including staged/unstaged and
untracked work; existing index entries were preserved.

Offline resolution uses a task-owned Maven repository and the previously audited
origin mapping. `freeze.py` selects the frozen timestamped JAR/POM aliases and
removes newer variants only in that private copy. No snapshot update or shared
repository installation is performed. Core is **26.10-20261013.200824-3**, SHA-256
`9c48703a5112025dac8afd315338eff914a00fd9c7de1e4316badc755b2dadc7`.
Companions retain their separately recorded **26.10-20261013.201141-3** identities;
Micrometer retains its exact locally cached hash without an invented timestamp.
All ten frozen inventory hashes match before/after each invocation and in resolved
full/scoped test classpaths. `baseline-artifacts.json` separately fingerprints the
26.04 classpaths; its core SHA-256 is
`8f233e9a85c35d192127f16203b1b420a50fb308a5435a37f4d5c7acd635142b`.
The full ten-entry frozen inventory is unchanged in `test-results.json` and the
[earlier qualification report](full-reactor-qualification-2026-10-16.md#frozen-dependency-identity).

## Remaining work and limits

The Core/Docs fixture defects are fixed within the tested matrix. **Next is the
separate CacheStore IT lifecycle/readiness task:** bind its four methods to the
ordinary lifecycle and repair `IsSpringUp` before claiming CacheStore behavior.
The current full reactor still excludes that IT, and no build rule or readiness
helper was changed here. Classic demo Failsafe does not discover another module’s IT.

This work does not qualify a full explicit-hardened reactor, a full Java 17 reactor,
actual remote Data installation, application-owned arbitrary payload graphs, TLS /
endpoint authentication, failover, rolling upgrades, deployment, hosted CI or a later
released Coherence artifact. Keep those separate from the successful ordinary
frozen full-reactor verification.

## Retained evidence and preservation

`test-reports.tar.gz` separates every invocation’s XML/style diagnostics. Accepted
rows contain 80 full-run suites plus 12 hardened and 12 baseline suites. Compressed
logs and JSON retain all cases, classpaths/checksums, JVM observations, bytecode
versions, reactor summaries, intermediate failures and exact inputs. Input archives
and fingerprints preserve the complete baseline and qualified source; no generated
build repository is retained. `legacy-case-preservation.json` proves all 692 prior
cases remain and pass; `fixture-policy-review.json` and probes record exact permissions.
`preservation.json`, `cleanup.json`, `task-changes.diff` and `SHA256SUMS` audit the
final scope and cleanup.

Checkout changes are exactly the two fixture policies, two new regression classes,
updated assessment status, this report and the required two timestamped journals.
Every pre-existing implementation file, historical qualification, test-results.json,
branch/HEAD and staged index entry is preserved. Disposable source/dependency/build/
probe/JVM directories are removed after evidence capture. Other tasks’ evidence is
untouched. No staging, commit, push, publication, hosted workflow or cross-chat
message occurred.
