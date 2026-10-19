# Complete reactor matrix and repeatable CI

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


Qualification and CI work on branch `5.x`, HEAD `37cf05ea59bd60c03b21b52acc1eb4c1045859a3`.
Completed October 16, 2026 at 16:05:22 CEST (UTC+0200; Europe/Belgrade). Both actual CI runner exercises also pass.

## Requested full rows: accepted

| Fresh complete ordinary reactor | Passed | Expected skips | Suites | Modules | Maven exit | Maven time |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| Temurin 17.0.20.1+1 / 26.04 / mode unset / release 17 | **697** | **9** | 83 | 28 SUCCESS | 0 | 13:27 |
| Temurin 21.0.12.1+1-LTS / exact frozen 26.10 / explicit hardened / release 17 | **706** | **0** | 83 | 28 SUCCESS | 0 | 11:13 |

Baseline ran **2026-10-16 15:04:47–15:18:15 CEST**; frozen hardened ran
**15:18:55–15:30:09 CEST** (Europe/Belgrade, UTC+0200). Each full row has
706 case identities, including all four original CacheStore IT methods and their
normal Failsafe integration-test/verify executions. The baseline skips are the
Core and Docs unlisted-payload controls (one each), six Session deny controls
(two each over Extend, gRPC and POF), and the CacheStore Person policy/deny control.
All use `securityPolicyAvailable`; 26.04 lacks the newer security API. The full
reactor adds the Docs skip to the earlier focused reactor's eight skips.

These are fresh full invocations, distinct from the earlier 706/706 **unset**
frozen full reactor and the earlier focused 680-case matrix. No module/test
selectors, skip flags, ignored failures, style suppressions or Javadoc bypasses.
All 706 earlier case identities and all 582 runtime/build input fingerprints match.
No production Java, policy, resource, POM, dependency default or test source changed.

| Test-bearing module | Java 17 / 26.04 | Frozen 26.10 hardened |
| --- | ---: | ---: |
| `coherence-spring-boot-starter` | 50 + 0 skipped | 50 |
| `coherence-spring-core` | 167 + 1 skipped | 168 |
| `coherence-spring-data` | 335 + 0 skipped | 335 |
| `coherence-spring-docs` | 10 + 1 skipped | 11 |
| `coherence-spring-session` | 111 + 6 skipped | 117 |
| `samples/cachestore-demo/coherence-spring-cachestore-demo-app` | 9 + 1 skipped | 10 |
| `samples/circuit-breaker-cache-demo/circuit-breaker-cache-demo-app` | 2 + 0 skipped | 2 |
| `samples/circuit-breaker-cache-demo/circuit-breaker-cache-demo-server` | 1 + 0 skipped | 1 |
| `samples/coherence-spring-cloud-config-demo/coherence-spring-cloud-config-demo-app` | 2 + 0 skipped | 2 |
| `samples/coherence-spring-cloud-config-demo/coherence-spring-cloud-config-demo-server` | 1 + 0 skipped | 1 |
| `samples/coherence-spring-demo/coherence-spring-demo-boot` | 1 + 0 skipped | 1 |
| `samples/hibernate-cache-demo` | 1 + 0 skipped | 1 |
| `samples/spring-session-demo/spring-session-demo-app` | 6 + 0 skipped | 6 |
| `samples/spring-session-demo/spring-session-demo-server` | 1 + 0 skipped | 1 |

All 11 normal Checkstyle configurations, ordinary Javadoc, Docs HTML/PDF/ZIP,
Website and normal/repackaged JAR/WAR packaging complete. Each row produces
688 inspected project main/test classes, all **major 61**. No compiler target 21.
Both normal and repackaged shared CacheStore core permissions remain unchanged;
14 embedded Coherence JAR entries per row match the selected inventory. Each row
records hashes for 298 observed dependency JAR paths and all applicable Coherence
JAR/POM files. Normal baseline and frozen checksums match before and after.

## Runtime and child propagation

The hardened invocation sets `JAVA_TOOL_OPTIONS=-Dcoherence.security.mode=hardened`
before starting Maven; the JDK inherits it in every fork and Bedrock launch.
Maven also receives the explicit user property. Actual `jcmd VM.system_properties`
evidence, every suite's XML properties and inherited-JDK startup output verify it.
Baseline clears Java injection variables and leaves the mode property absent.
Spring Environment defaults and parent-only flags are not used as child evidence.

Observed baseline: Maven, 70 test forks, nine Coherence children and HSQLDB.
Observed frozen: Maven, 72 test forks, nine Coherence children and HSQLDB.
All 83 suite reports per row verify actual runtime/mode and fatal failures.
The nine Coherence launch sites are:

- Core: `CoherenceExtendTests`, `GrpcSessionBeanTests`, `GrpcSessionTests`.
- Session: Extend, gRPC and POF `CoherenceIndexedSessionRepositoryTests` variants.
- Boot: `CoherenceConfigDataLoaderTests`, `CoherenceGrpcClientTests`.
- CacheStore: `CacheStoreDemoIT`; its HSQLDB launch is observed separately.

Every child is verified on the actual requested JDK and mode. Existing Session
and CacheStore option forwarding remains; the inherited JVM option also covers
Core/Boot launches without source changes. Existing explicit serializer contracts,
production policies and genuine default/unset paths are preserved. No dynamic
unauthenticated-code allowance is introduced.

## Exact frozen inputs and availability boundary

Core is `26.10-20261013.200824-3`, SHA-256
`9c48703a5112025dac8afd315338eff914a00fd9c7de1e4316badc755b2dadc7`.
The eight timestamped companion artifacts keep their own
`26.10-20261013.201141-3` identities. The tenth artifact, Micrometer, is the original
untimestamped locally installed development build, SHA-256
`43f9079c664c42f51973163977a501e308f4c38ac214708cea2b08781f97b83f`.
Its manifest says `(dev-vasac)` and its local resolver marker indicates installation;
no published timestamp was recorded. This limitation is retained explicitly.

[The historical lock](historical-coherence-lock-2026-10-16.json) copies every original
JAR identity/hash from unchanged [historical test-results.json](test-results.json)
and adds hashes of the exact original flattened POMs. No moving snapshot or
26.04 companion substitution. Fresh full-reactor builds prevent stale/raw seeded
project POMs from selecting mixed companions.

The frozen bootstrap fetches exact timestamped URLs from the existing public
Sonatype snapshot repository. Micrometer uses its only recorded, untimestamped
coordinate under the same repository and must match the original checksum.
**If any file is missing or differs, CI fails with `FROZEN INPUT UNAVAILABLE` before
mutating its Maven cache.** A public lookup from this environment timed out
(curl exit 28, HTTP 000); public availability is unverified. In particular, the
local development Micrometer may not exist publicly. The lane cannot become a
hosted green frozen qualification unless all exact locked bytes are available;
no repository publication, new secret or replacement artifact is included.

Downloaded bytes are installed into the ordinary cached `~/.m2/repository` with
standard local SNAPSHOT resolver metadata. Maven `-nsu` prevents timestamp updates;
pre/post hashes and all actual test/child/packaged classpaths enforce the lock.
CI has no workstation path, temporary evidence dependency or private repository.

## CI changes

- `.github/workflows/maven-simple.yml`: explicit two-row qualification matrix,
  independent failure results, release 17 on both JDKs, fatal full `clean verify`,
  normal repository settings and automatic CacheStore Failsafe coverage.
- `.github/scripts/coherence_artifacts.py` and the lock: fetch/check all ten frozen
  JARs and ten original POMs; reject unavailable/drifted inputs before cache changes.
- `.github/scripts/full_reactor.py`: runtime/mode guard, inherited hardening,
  actual Maven/fork/all-nine-child property evidence, complete XML/module inventory,
  fatal failures, exact dependencies/POMs and embedded JAR hashes, major-61 audit,
  commands/source fingerprints/timings/statuses and diagnostics.
- Always upload Surefire/Failsafe/check reports, Maven log, runtime/classpath/hash
  evidence and sample logs even on failure. Snapshot-cache cleanup remains.
- Preserve optional manual Coherence-version testing as a clearly **non-frozen**
  extra JDK-21/release-17/hardened lane. It never places 26.10 on JDK 17.
- Existing `maven.yml` and `ci-pr.yml` use `-Pjdk17` on JDK 21 to preserve the
  product target. Their existing whitespace-splitting snapshot cleanup is replaced
  by `find -exec`; owner/triggers, Sonar, publication and credential boundaries stay.

The simple workflow retains its existing dispatch trigger and owner restriction;
it is not renamed to the deployment-triggering workflow. No hosted workflow was
run or dispatched, and no Git staging/commit/push/publication occurred.

## Reproduction and local CI validation

Retained evidence: `/private/tmp/codex-work/spring-full-matrix-1oroafce/evidence/`.
Exact expanded commands, JDK homes, status/times, source/index manifests, process
properties/classpaths, XML inventories, packaging/check records and hashes are
retained there. Task-owned dependency/source/repro trees are removed after final evidence capture;
about 44.6 MiB of compact logs/XML/input/checksum records remain; all 332 suite XML
records were independently reparsed from the retained archive. Historical task
evidence is untouched.

Initial full rows use cached Maven 3.9.9 matching the wrapper, offline resolution,
normal repository settings with the same existing snapshot IDs mapped to the
frozen cached origin, `-Pjdk17 -Dmaven.test.failure.ignore=false`, task-owned local
cache/JVM directory, `--fail-at-end clean verify`; baseline adds
`-Dcoherence.version=26.04`, frozen adds `-Dcoherence.version=26.10-SNAPSHOT` and
`-Dcoherence.security.mode=hardened` plus inherited `JAVA_TOOL_OPTIONS`.
Full command transcripts are `full-*-command.txt`; no runtime source correction
was needed.

CI entry points (from the repository root):

```bash
# On JDK 17
python3 .github/scripts/full_reactor.py --lane baseline --output "$RUNNER_TEMP/qualification"
# On JDK 21; every input must be publicly obtainable and hash-identical
python3 .github/scripts/coherence_artifacts.py --output "$RUNNER_TEMP/qualification"
python3 .github/scripts/full_reactor.py --lane frozen --output "$RUNNER_TEMP/qualification"
```

Both use ordinary full `./mvnw -B -ntp -Pjdk17
-Dmaven.test.failure.ignore=false -Dcoherence.version=<lane>
--settings .mvn/settings.xml --fail-at-end clean verify`; frozen additionally
uses `-nsu` and explicit hardened mode. The helper clears inherited Maven/Java
injection options before selecting mode. The optional local-exercise arguments
select a disposable cache, matching cached Maven, offline resolution and JVM temp
directory; the hosted workflow does not depend on these local paths/flags.

Validation already passed: actionlint on all three changed workflows, Python AST
syntax, exact equality of the original frozen JAR inventory, the 20-file positive
bootstrap fixture, and eight local negative/positive helper checks (unavailable and
corrupt downloads, both wrong-JDK combinations, accepted evidence, missing child,
wrong mode and wrong compiler target). Fixture downloads use exact accepted local
bytes and do not establish public availability. Both actual CI runner full reactors also pass, without changing the qualified
implementation between runs:

| Actual CI runner local exercise | Europe/Belgrade start–end | Cases / skips | Maven time | Runner exit |
| --- | --- | --- | --- | ---: |
| Baseline, normal `.mvn/settings.xml` | 15:36:14–15:49:39 CEST | 697 passed + 9 expected skips | 13:23 | 0 |
| Frozen, bootstrapped exact bytes, normal settings, `-nsu` | 15:50:11–16:01:22 CEST | 706 passed / 0 skips | 11:10 | 0 |

Both repeat all 28 modules, 83 suites and every original case identity/outcome.
Their own runner audits and a separate reparse/packaging/classpath audit have
zero issues. All nine Coherence children and HSQLDB have correct actual modes,
all 688 output classes are major 61, all four CacheStore ITs pass, normal checks
and packaging execute, and resolved/embedded artifact and POM hashes match.
The CI implementation hash manifest is identical before/after these exercises.
Local-only cached/offline inputs exercise the real CI runner and exact bootstrap
bytes; online retrieval, Ubuntu-hosted execution and workflow dispatch remain
unverified. No source/test/POM or CI correction was needed during the full repeats.

## Remaining qualification

Hosted CI/public frozen artifact retrieval, actual remote Data code installation,
production deployment/TLS/authentication, failover, persistence/rolling upgrades
and broader platform coverage remain separate. Local green reactors and checksum
fixtures establish only the configurations and paths described here.
