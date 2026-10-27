<!--
  Copyright (c) 2026, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at
  https://oss.oracle.com/licenses/upl.
-->

# Packaged consumers

These independent Maven projects exercise the installed JARs and flattened POMs.
They have no Coherence Spring parent and never use reactor class directories.
`inventory.properties` is the reviewed fixture/method inventory: ten fixtures,
nineteen tests, no permitted skips. Changing that inventory is an explicit review
change; it is never derived from a passing run. The accepted A/B module tests
remain in the general starter and complement these consumers.

| Fixture | Proof |
| --- | --- |
| general-starter | No Session/servlet classes; Boot discovery, real cluster/cache operation |
| general-web | Servlet application without Session dependencies; real cluster/cache operation |
| session-starter | New starter supplies the three integration modules; embedded HTTP session, disabled and none/redis/empty store-type back-off |
| manual-composition | Three explicit dependencies produce the same HTTP/repository behavior |
| library-without-boot-session | Old two-artifact recipe starts with default/explicit enabling; missing Boot Session condition is observed |
| boot-session-without-coherence-session | Boot Session alone does not create a Coherence repository |
| non-web | Plain and reactive contexts back off despite servlet APIs being present |
| custom-repository | Bare repository owns back-off without a filter; explicit @EnableSpringHttpSession with an application-owned Coherence repository gives one filter/repository and stored HTTP state |
| plain-spring-session | No Boot runtime or test artifacts; explicit enable annotations, direct and forwarded servlet/filter/cache/session requests |
| boot-war | Actual WAR/SCI/Boot imports discovery in Tomcat; direct and forwarded sessions, no ServerProperties class, timeout override/default, actual filter mapping/dispatcher types/order |

Every positive Coherence session case makes two HTTP requests with the same
`SESSION` cookie and checks count=2 in both the actual repository and its stored
Coherence cache entry. The standalone and WAR cases repeat this check through
`RequestDispatcher.forward`, creating a session before dispatch as an MVC
controller would before rendering a view. Their test Tomcat contexts set
`suspendWrappedResponseAfterForward=false`: Tomcat 11's default suspends the
response before Spring Session's wrapper can write the first cookie. This is
test-container configuration; the production library is unchanged. The traditional
WAR sample supplies a per-application Tomcat Context with the same setting; see
its [deployment guide](../../../samples/spring-session-demo/README.adoc).
These additional HTTP
assertions retain the ten-fixture/nineteen-method inventory and evidence contract.
The standalone path registers its Spring context/filter
explicitly. The WAR path uses Spring's `ServletContainerInitializer` to discover
`SpringBootServletInitializer`; it neither imports selected auto-configurations nor
registers that initializer manually. The WAR parent loader exposes only container
and JDK classes. Its loaded Coherence JAR must come from `WEB-INF/lib` and match
the outer build's hash. WAR timeout defaults preserve A/B semantics: neither a
container timeout nor `server.servlet.session.timeout` supplies the Boot WAR
`SessionTimeout`; absent `spring.session.timeout`, the repository retains 30 min.

Run from the repository root, with `JAVA_HOME` and `PATH` selected explicitly:

```sh
./mvnw -B -ntp -Pjdk17 -Dmaven.test.failure.ignore=false \
  -pl coherence-spring-session-boot-starter -am clean verify
```

`verify` installs current artifacts with Invoker 3.6.1, freezes outer-resolved
Coherence/product JAR/POM identities, clones fixtures under `target/it`, then runs
`clean verify` sequentially using `target/it-repository`. Failsafe executes tests
after packaging, which lets the WAR test deploy the actual built archive.
`invoker:run` propagates failures; a source-launched Java guard checks the exact
fixture inventory at initialize and exact method/report/evidence inventory at
verify. Missing fixtures, tests, reports, failures and skips are fatal. Java source
launch requires the project's Java 17 baseline and adds no starter classes.

The same focused command on Windows PowerShell (with an installed JDK and Maven
wrapper prerequisites) is:

```powershell
$env:JAVA_HOME = 'C:\Java\jdk-17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -B -ntp -Pjdk17 -Dmaven.test.failure.ignore=false `
  -pl coherence-spring-session-boot-starter -am clean verify
```

Repeat on JDK 21 with `-Pjdk17` and `-Pjdk21` to cover both supported targets.
For the bounded consumer-only run, add `-DskipTests`: this skips outer unit tests
while the independent consumers still run all ten fixtures/nineteen methods.
Do not narrow `invoker.test` for acceptance. Run the focused path regressions with
`python -m unittest discover -s .github/scripts -p 'test_consumer_paths.py'` on each selected JDK.
Retain the evidence listed below and the native OS/JDK/Maven versions.

`PathContract` checks native real filesystem identities: each product/Coherence
JAR must be the exact coordinate under this build's isolated repository, and the
WAR's loaded Coherence must be directly in `WEB-INF/lib` of the actual deployed
ServletContext root. Hash checks remain mandatory. Repository siblings, another
build's repository, wrong coordinates and another deployment cannot satisfy these
checks merely by having a similar path string. The loaded test-JVM Coherence must
also be the exact isolated artifact. Classpaths use the running OS's path-list
separator (`:` or `;`); Maven dependency lines retain Windows drive-letter colons
in their final filename field. Component checks reject reactor/test directories
and Boot leakage using either filesystem separator. ZIP/JAR entry names continue
to use `/`, as required by the archive format.

The path regressions exercise the actual shared helper and outer freeze/copy
behavior, including native filesystem boundaries, spaces, POSIX/Windows classpath
strings and drive letters. Cross-platform strings on a POSIX JVM are syntax
checks; they do not establish native Windows execution. The 22 October 2026
item4 validation ran locally on macOS; native Windows consumer qualification
remains outstanding because no authorized Windows runtime was available.

A bare custom `SessionRepository` bean makes Coherence back off and does not
create `springSessionRepositoryFilter`. The custom HTTP case explicitly owns
`@EnableSpringHttpSession` as well as its Coherence repository/cache/timeout.
The positive ownership case checks the application bean alias and stored Coherence
state after two requests; the negative bare-repository case uses MapSessionRepository. This records the existing
behavior rather than changing accepted A/B production configuration. The reactive
fixture declares the Boot WebFlux starter, starts its real server on an ephemeral
port and starts Coherence. Boot supplies the reactive `ServerProperties`/timeout
configuration through discovery; no servlet session repository is created.

The fixtures use explicit pinned plugins compatible with Maven 3.6.0. Invoker's
actual 3.6.1 descriptor requires Maven 3.2.5/Java 8; no Maven-floor change is made.
The surrounding project's pre-existing plugin requirements are separate from
this harness; this task does not qualify the complete reactor on Maven 3.6.0.
The four shared consumer-only helpers are copied into each cloned test source
root. They are never packaged into the starter/application JAR or WAR. No Boot
test starter, library test helper, test resource, mock framework or Bedrock
substitute supplies the consumer application.

`-Dconsumer.tests.skip=true` deliberately skips this consumer harness and records
`result=SKIPPED`; such a run cannot qualify package C. Ordinary unit-test selectors
and `skipTests` do not select/skip nested consumers. `invoker.test` can narrow an
invocation for debugging, but the final inventory audit rejects an incomplete
qualification. Use the full default fixture set for acceptance. Nested Failsafe
has fatal failures and no-test behavior explicitly configured.

The outer build passes product/Boot/Coherence versions and group, release,
`java.home` and the requested security mode explicitly. The default and `-Pjdk17`
select compiler release 17 (class major 61). With JDK 21 selected, `-Pjdk21`
selects release 21 (major 65); JDK 21 with `-Pjdk17` still produces major 61.
For an ordinary release-21 consumer run, use the same command above with
`-Pjdk21` and JDK 21 in `JAVA_HOME`/`PATH`.

`Contract` requires a declared supported release (`17` or `21`), matching the
nested `maven.compiler.release` and the outer build's separately recorded
`outer-release.properties`. The actual JVM must support that target. Missing,
unfiltered, malformed or inconsistent declarations fail; the target is never
inferred from the running JVM. `ReleaseContract` derives the expected class major
from the selected release and checks every fixture main/test class and packaged
product class. `Contract` also checks actual test JVM properties, classpaths,
dependency graphs and JAR/POM bytes. An unset security mode remains absent; an explicit
`-Dcoherence.security.mode=hardened` becomes a test JVM argument. Nothing changes
production security or serialization permissions.

For a snapshot run, resolve fresh outer artifacts using Maven `-U` with
`-Pjdk17 -Dcoherence.version=26.10-SNAPSHOT -Dcoherence.security.mode=hardened` on
JDK 21. Invoker installs the run's exact resolved bytes; the freeze step records
and copies their JAR/POM identities. Nested settings mirror every remote repository
to release Central, which cannot supply a replacement snapshot; installed snapshot
bytes remain the only available inputs. Each consumer compares its dependency
JAR/POM hashes and the actually loaded Coherence JAR to that run's manifest.
These are per-run identities, never historical snapshot pins. Other release
plugins/BOMs/dependencies may be retrieved from Central on the first run.

For an explicitly authorized frozen local exercise, the full runner accepts
`--local-frozen --offline --repository TASK_CACHE --hibernate-version 4.0.1-SNAPSHOT`.
Use cached Maven 3.9 or newer: `MAVEN_ARGS=-o` propagates offline resolution to
Invoker launchers. Seed their isolated repository from independent local cache
copies after outer clean; retain the freshly frozen product artifacts. No cache
copy may overwrite the user repository or replace the outer frozen identities.
The runner records `local-frozen-offline` scope, selects the provider explicitly,
and omits snapshot refresh only for that scope. Such evidence establishes local
compatibility with recorded bytes; fresh published-snapshot CI remains separate.
The local runner disables optional Cloud Config lookup in outer context tests;
the packaged integration still starts and verifies its real native loopback server.

The strict release-qualification matrix is separate from ordinary selected-release
runs. `.github/scripts/full_reactor.py` always selects `-Pjdk17`, including on
JDK 21, and `consumer_evidence.py` requires release 17 / major 61. A successful
`-Pjdk21` run demonstrates ordinary profile support and cannot qualify that
release-17 matrix. The default Java 17 and Coherence 26.04 remain unchanged.

CI script regressions (`python3 -m unittest discover -s .github/scripts -p 'test_*.py'`) include real compiler/class/JAR checks, declaration/runtime and outer
guard negative controls, and a coherent release-21 result rejected by the strict
qualification collector. Run with `JAVA_HOME`/`PATH` on both JDKs; JDK 21 exercises
both compiler targets, while JDK 17 verifies that release 21 cannot execute there.

## Evidence contract v1 (for package E)

All paths below are relative to this module's `target/`. Retain these small files
and logs, never the whole isolated repository:

- `consumer-evidence/inventory.properties`: copied reviewed fixture/method list.
- `consumer-evidence/outer-dependencies.txt`: outer absolute packaged paths.
- `consumer-evidence/outer-release.properties`: independently selected outer
  `consumer.release`, checked against the nested compiler declaration and audit.
- `consumer-evidence/outer-artifacts.properties`: coordinates, original paths and
  SHA-256 for each outer product/Coherence JAR and POM.
- `consumer-evidence/summary.properties`: PASS only after every fixture and exact
  test identity passes, with fixture/test/failure/error/skip counts; SKIPPED for
  an explicit consumer skip.
- `invoker-reports/BUILD-<fixture>.xml` and `TEST-<fixture>.xml`: Invoker results,
  separately classified from library JUnit suites (avoid recursive double count).
- `it/<fixture>/build.log`, `target/failsafe-reports/TEST-*.xml` and summary: actual
  nested Maven/Failsafe outcomes. Reject missing, extra, failed or skipped cases.
- `it/<fixture>/target/{runtime-dependencies,test-dependencies,runtime-classpath}.txt`:
  resolved graph and absolute packaged paths, including scope/test separation.
- `it/<fixture>/target/consumer-evidence/<method>.properties`: actual JVM/home,
  security-mode presence/value, classpath, declared/compiler release, verified
  `consumer.bytecode.major` and fixture main/test class counts, Coherence version, compared
  product/Coherence JAR/POM hashes; WAR additionally records the actual
  `war.deployment` root and loaded archive bytes.

Package E's CI collector cross-checks expected outer values and these identities;
the summary alone is insufficient. The fixture JVMs run Coherence in-process and
introduce no Bedrock child role. `.github/scripts/full-reactor-inventory.json`
owns the reviewed reactor suite/case and separately launched JVM expectations;
this directory's `inventory.properties` remains the canonical consumer list.
Invoker's ten JUnit wrappers are build summaries, while the nineteen real cases
come only from the nested Failsafe reports. Neither enters reactor test totals.
Invoker explicitly passes `maven.test.failure.ignore=false` into nested builds;
the collector checks it in their actual JUnit properties in addition to requiring
successful build/Failsafe summaries and exact zero-skip cases. The
[sample guide](../../../samples/spring-session-demo/README.adoc) documents the
application/storage-server dependency recipes and traditional WAR deployment.
The [migration guide](../../../coherence-spring-docs/src/main/asciidoc/upgrade-5.adoc)
describes the public dependency changes and session compatibility limits.
Packaging/publication attachment checks and broader qualification remain
separate from this CI-evidence integration.
