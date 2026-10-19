# CacheStore sample Person permission and qualification

Completed October 16, 2026 at 09:03:39 CEST (UTC+0200; Europe/Belgrade). Branch `5.x`,
HEAD `37cf05ea59bd60c03b21b52acc1eb4c1045859a3`.

## Result and implementation

The current ordinary **28-module frozen-26.10 reactor passes 706/706 cases**
(83 suites, 14 test-bearing modules), exit 0, zero failures/errors/skips. All four
CacheStore IT methods now pass through normal Failsafe. All 696 earlier case
identities remain; the additional coverage is four restored ITs, four existing
readiness regressions and two new Person graph/policy cases.

Added `samples/cachestore-demo/coherence-spring-cachestore-demo-core/src/main/resources/META-INF/coherence/security-config.xml`.
The target did not previously exist. Its version-1.0 Coherence security namespace
contains exactly:

```xml
<class name="com.oracle.coherence.spring.example.model.Person" source="manual"/>
```

No executable flag, wildcard or additional permission is added. The shared sample
core supplies both endpoints; library production policies are unchanged. Default
Coherence remains 26.04 and the compiler target remains Java 17.

## Actual graph and endpoint boundaries

Person directly extends Object and declares Long, int and String fields, with no
replacement hook or relationships. `JpaRepositoryCacheLoader.load` returns
`findById(key).orElse(null)`; store uses `saveAndFlush`. New package-private
`PersonSerializationTests` and added IT assertions inspect actual saved entities,
Extend-returned values, create/update inputs, database results and the server's
real repository `load` results. Original read-through/write-through/update
assertions are preserved.

Recorded Java stream descriptors are exactly **Person, Long, Number**; actual
objects are **Person, Long, String**. Number is Long's superclass descriptor.
No Hibernate proxy, additional application superclass or replacement object is
serialized on these paths. Populated DefaultSerializer round trips preserve every
Person field. Existing built-ins suffice; no further entry is justified.

The real Failsafe client and launched CacheStoreDemo server assert DefaultSerializer,
expected child mode and runtime, and Person's shared normal-core-JAR origin.
On frozen 26.10 they also require hardened enforcement, exact merged `manual`
permission, allowlisting and non-executable classification in both security and
remote-executable policy APIs. An unrelated unlisted Serializable payload must
fail with InvalidClassException before its read callback. Server logs retain four
endpoint checks, three real loaded-graph checks and four exact unlisted-class
rejections per frozen run. Bedrock probes are bounded; no Coherence executable
permission or dynamic-code override is added.

The normal shared core JAR contains the exact policy. Both app and standalone-server
Boot JARs embed that byte-identical core JAR and exclude all test helpers.

## Fresh ordinary verification

All accepted rows use clean verify, fatal tests, normal lifecycle checks/Javadoc,
no failure suppression, and separate fresh Maven/test/server JVMs. Checkstyle runs
where normally activated; its 11 full-run configurations match repository rules
exactly. The sample app has no Checkstyle binding; none was added or suppressed.

| Runtime / mode | Scope | Cases | Passed | Skipped | Maven exit |
| --- | --- | ---: | ---: | ---: | ---: |
| JDK 21 / frozen 26.10 / unset | Ten-module focused reactor | 680 | 680 | 0 | 0 |
| JDK 21 / frozen 26.10 / hardened | Fresh ten-module focused reactor | 680 | 680 | 0 | 0 |
| JDK 17 / 26.04 / unset | Ten-module focused reactor | 680 | 672 | 8 | 0 |
| JDK 17 / 26.04 / hardened property | App, exact baseline prerequisites | 10 | 9 | 1 | 0 |
| JDK 21 / frozen 26.10 / unset | Complete current 28-module reactor | 706 | 706 | 0 | 0 |

Every row executes all four IT methods and four readiness cases successfully.
26.04 lacks the newer security API: only deny controls are capability-skipped
(Core one, Session six, sample one); setting hardened there does not establish
26.10 enforcement. The app-only baseline reuses 15 checksummed current baseline
POM/JAR prerequisites, including normal generated flattened dependency POMs.

Full coverage: Core 168, Data 335, Session 117, Boot starter 50, Docs 11,
other ordinary samples 15, CacheStore app 10. Normal Docs HTML/PDF/ZIP,
Website and other packaging complete. All inspected project output classes have
major 61. Actual runtimes are Temurin 17.0.20.1+1 and 21.0.12.1+1-LTS;
XML and observed JVMs verify runtime/mode/fatal-test properties. CacheStore children
have the expected mode, admin enabled and HTTP port 0. This does not assert explicit
property forwarding by every unrelated prerequisite child fixture.

Two execution qualifications are retained separately: an earlier app-only frozen
hardened diagnostic passed 10/10 but resolved two 26.04 Bedrock companion JARs from
raw seeded dependency POMs; it is **excluded**, replaced by the accepted fresh
680-case hardened reactor. The baseline reactor recorded Maven exit 0 and its end
time, then its wrapper exited 2 after this task edited that running script. The
post-build hash/report collection was completed separately before any next build;
no build input changed. Both incidents and commands are retained.

| Accepted invocation | Start (Europe/Belgrade) | End (Europe/Belgrade) |
| --- | --- | --- |
| focused-frozen-unset | 2026-10-16 08:17:39+0200 | 2026-10-16 08:26:24+0200 |
| focused-frozen-hardened | 2026-10-16 08:40:21+0200 | 2026-10-16 08:48:53+0200 |
| focused-baseline-unset | 2026-10-16 08:27:06+0200 | 2026-10-16 08:38:03+0200 |
| focused-baseline-hardened-app | 2026-10-16 08:40:05+0200 | 2026-10-16 08:40:20+0200 |
| full-frozen-unset | 2026-10-16 08:48:55+0200 | 2026-10-16 08:59:58+0200 |

## Reproduction, evidence and limits

Evidence: `/private/tmp/codex-work/cachestore-person-jbpi_nmp/evidence/`.
`run.sh` records expanded commands, cleared Java option variables, exact JDK homes,
times and Maven exit status. Common flags are:

```text
-o -B -ntp -s <evidence>/settings.xml -Pjdk17
-Dmaven.repo.local=<task>/repository -Dcoherence.version=<version>
-Dmaven.test.failure.ignore=false -DargLine=-Djava.io.tmpdir=<task>/jvm-tmp
--fail-at-end
```

Focused reactor rows add `-pl :coherence-spring-cachestore-demo-app -am`;
app-only adds the same `-pl` without `-am`. Hardened is set before initialization
in Maven, test forks and the CacheStore child. The full invocation adds only
`clean verify`: **no selectors, skips or suppressions**.

Offline resolution and before/after plus resolved-classpath hashes hold the ten
frozen artifacts to unchanged [test-results.json](test-results.json). Core is
`26.10-20261013.200824-3`, SHA-256
`9c48703a5112025dac8afd315338eff914a00fd9c7de1e4316badc755b2dadc7`;
companions retain their exact inventory identities. The baseline is fingerprinted
separately. Compact evidence retains source archives/fingerprints, XML, commands,
compressed logs, JVM/artifact audits, generated-artifact hashes, policy/packaging
proof, case-identity preservation and a diff limited to this task.

Prior source/index entries and historical reports/results are preserved. Task-owned
source, dependency and JVM/build trees are removed after capture; other tasks'
evidence remains untouched. No staging, commit, push, publication, hosted workflow
or cross-chat message occurred. Full Java-17/explicit-hardened reactor matrices,
hosted CI, production deployment/TLS/authentication, failover and rolling upgrades
remain separate qualification; this sample graph does not qualify arbitrary JPA
proxy/relationship graphs or later Coherence artifacts.
