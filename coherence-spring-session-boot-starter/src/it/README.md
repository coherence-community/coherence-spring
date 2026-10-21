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
The two shared consumer-only helpers are copied into each cloned test source
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
`java.home` and the requested security mode explicitly. `Contract` checks actual
test JVM properties, classpaths, product bytecode major 61, dependency graphs and
JAR/POM bytes. An unset security mode remains absent; an explicit
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

## Evidence contract v1 (for package E)

All paths below are relative to this module's `target/`. Retain these small files
and logs, never the whole isolated repository:

- `consumer-evidence/inventory.properties`: copied reviewed fixture/method list.
- `consumer-evidence/outer-dependencies.txt`: outer absolute packaged paths.
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
  security-mode presence/value, classpath, release and Coherence version, compared
  product/Coherence JAR/POM hashes; WAR additionally records loaded archive bytes.

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
