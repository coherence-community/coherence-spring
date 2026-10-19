# Change Journal

## Resume Context

See `.ai/branches/codex/security-hardening-26-10/journal.md` and
`design/features/coherence-security-hardening/README.md`.

## October 19, 2026 at 13:23:40 CEST (UTC+0200; Europe/Belgrade) — Prepare focused security-hardening review

- Imported the existing security-hardening implementation, policies, tests, guide,
  three CI workflows and three qualification helpers from the inventoried `5.x`
  working tree at base `37cf05ea59bd60c03b21b52acc1eb4c1045859a3`. The review uses
  branch `codex/security-hardening-26-10` in an isolated local clone to preserve the user's
  original branch, complete index and staged/unstaged distinctions.
- Retained all feature assessment/diagnostic reports and the inert historical
  lock as provenance; they do not control current Maven snapshot resolution.
- Excluded local AGENTS.md, the old branch journal and unrelated historical
  JOURNAL.md entries. Added this scoped change journal, a current branch resume
  journal and feature README so the review has a concise restart path.
- Current authorization permits scoped commit/push, a draft PR against upstream
  `5.x`, and the non-deploying manual Simple CI matrix. No merge/release/deploy
  or force push is authorized. No new runtime, permission or test policy changes.
- Validation to run: helper regressions, actionlint, diff/source preservation,
  then both mandatory hosted rows on one exact review SHA. Historical local
  results are documented separately and do not establish hosted qualification.

Imported additions/updates and reasons:

- `.ai/dev-environment.md` — record the supported runtime/target matrix and moving snapshot policy.
- `.github/scripts/coherence_artifacts.py` — run/audit the complete fatal reactor and record per-run dependency/JVM/bytecode evidence.
- `.github/scripts/full_reactor.py` — run/audit the complete fatal reactor and record per-run dependency/JVM/bytecode evidence.
- `.github/scripts/test_snapshot_ci.py` — run/audit the complete fatal reactor and record per-run dependency/JVM/bytecode evidence.
- `.github/workflows/ci-pr.yml` — preserve Java 17 output and encode the full qualification matrix with safe snapshot cache cleanup.
- `.github/workflows/maven-simple.yml` — preserve Java 17 output and encode the full qualification matrix with safe snapshot cache cleanup.
- `.github/workflows/maven.yml` — preserve Java 17 output and encode the full qualification matrix with safe snapshot cache cleanup.
- `coherence-spring-core/src/test/java/com/oracle/coherence/spring/FixtureSerializationTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-core/src/test/resources/META-INF/coherence/security-config.xml` — supply exact owner-scoped serialization permissions without wildcard grants.
- `coherence-spring-data/src/test/resources/META-INF/coherence/security-config.xml` — supply exact owner-scoped serialization permissions without wildcard grants.
- `coherence-spring-docs/src/main/asciidoc/spring-session.adoc` — document production POF registration, application policy ownership and explicit serializer configuration.
- `coherence-spring-docs/src/test/java/com/oracle/coherence/spring/doc/security/FixtureSerializationTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-docs/src/test/resources/META-INF/coherence/security-config.xml` — supply exact owner-scoped serialization permissions without wildcard grants.
- `coherence-spring-session/src/main/java/com/oracle/coherence/spring/session/CoherenceIndexedSessionRepository.java` — replace captured reset work and preserve current Spring Security authentication graphs under serialization.
- `coherence-spring-session/src/main/java/com/oracle/coherence/spring/session/SessionResetEntryProcessor.java` — replace captured reset work and preserve current Spring Security authentication graphs under serialization.
- `coherence-spring-session/src/main/java/com/oracle/coherence/spring/session/serialization/pof/AuthenticationPofSerializer.java` — replace captured reset work and preserve current Spring Security authentication graphs under serialization.
- `coherence-spring-session/src/main/java/com/oracle/coherence/spring/session/serialization/pof/FactorGrantedAuthorityPofSerializer.java` — replace captured reset work and preserve current Spring Security authentication graphs under serialization.
- `coherence-spring-session/src/main/resources/META-INF/coherence/security-config.xml` — supply exact owner-scoped serialization permissions without wildcard grants.
- `coherence-spring-session/src/test/java/com/oracle/coherence/spring/session/AbstractCoherenceIndexedSessionRepositoryTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/java/com/oracle/coherence/spring/session/AbstractRemoteSessionTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/java/com/oracle/coherence/spring/session/AuthenticationPofSerializerTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/java/com/oracle/coherence/spring/session/ExtendSessionCoherenceIndexedSessionRepositoryTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/java/com/oracle/coherence/spring/session/GrpcSessionCoherenceIndexedSessionRepositoryTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/java/com/oracle/coherence/spring/session/PofCoherenceIndexedSessionRepositoryTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/java/com/oracle/coherence/spring/session/SessionRejectionEvidence.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/java/com/oracle/coherence/spring/session/SessionRejectionEvidenceTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/java/com/oracle/coherence/spring/session/SessionResetEntryProcessorTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/java/com/oracle/coherence/spring/session/SessionTestSupport.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/resources/META-INF/coherence/security-config.xml` — supply exact owner-scoped serialization permissions without wildcard grants.
- `coherence-spring-session/src/test/resources/client-coherence-cache-config.xml` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/resources/grpc-pof-test-coherence-cache-config.xml` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/resources/grpc-test-coherence-cache-config.xml` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/resources/pof-config.xml` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `coherence-spring-session/src/test/resources/server-coherence-cache-config.xml` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `design/features/coherence-security-hardening/assessment.md` — retain feature analysis and dated qualification evidence; historical inputs remain historical.
- `design/features/coherence-security-hardening/cachestore-lifecycle-readiness-2026-10-16.md` — retain feature analysis and dated qualification evidence; historical inputs remain historical.
- `design/features/coherence-security-hardening/cachestore-person-permission-2026-10-16.md` — retain feature analysis and dated qualification evidence; historical inputs remain historical.
- `design/features/coherence-security-hardening/core-docs-fixture-fix-2026-10-16.md` — retain feature analysis and dated qualification evidence; historical inputs remain historical.
- `design/features/coherence-security-hardening/data-diagnostic.md` — retain feature analysis and dated qualification evidence; historical inputs remain historical.
- `design/features/coherence-security-hardening/full-matrix-ci-2026-10-16.md` — retain feature analysis and dated qualification evidence; historical inputs remain historical.
- `design/features/coherence-security-hardening/full-reactor-qualification-2026-10-16.md` — retain feature analysis and dated qualification evidence; historical inputs remain historical.
- `design/features/coherence-security-hardening/historical-coherence-lock-2026-10-16.json` — retain feature analysis and dated qualification evidence; historical inputs remain historical.
- `design/features/coherence-security-hardening/latest-snapshot-local-2026-10-19.md` — retain feature analysis and dated qualification evidence; historical inputs remain historical.
- `design/features/coherence-security-hardening/session-implementation.md` — retain feature analysis and dated qualification evidence; historical inputs remain historical.
- `design/features/coherence-security-hardening/test-results.json` — retain feature analysis and dated qualification evidence; historical inputs remain historical.
- `samples/cachestore-demo/coherence-spring-cachestore-demo-app/pom.xml` — bind all four CacheStore integration methods to ordinary Failsafe verification.
- `samples/cachestore-demo/coherence-spring-cachestore-demo-app/src/test/java/com/oracle/coherence/spring/example/CacheStoreDemoIT.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `samples/cachestore-demo/coherence-spring-cachestore-demo-app/src/test/java/com/oracle/coherence/spring/example/IsSpringUp.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `samples/cachestore-demo/coherence-spring-cachestore-demo-app/src/test/java/com/oracle/coherence/spring/example/IsSpringUpTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `samples/cachestore-demo/coherence-spring-cachestore-demo-app/src/test/java/com/oracle/coherence/spring/example/PersonSerializationTests.java` — verify serialization/remote rejection boundaries, actual endpoint behavior and bounded CacheStore readiness/lifecycle.
- `samples/cachestore-demo/coherence-spring-cachestore-demo-core/src/main/resources/META-INF/coherence/security-config.xml` — supply exact owner-scoped serialization permissions without wildcard grants.
- `samples/spring-session-demo/spring-session-demo-app/src/main/resources/META-INF/coherence/security-config.xml` — supply exact owner-scoped serialization permissions without wildcard grants.
- `samples/spring-session-demo/spring-session-demo-server/src/main/resources/META-INF/coherence/security-config.xml` — supply exact owner-scoped serialization permissions without wildcard grants.
- `samples/spring-session-demo/spring-session-demo-server/src/main/resources/pof-config.xml` — replace captured reset work and preserve current Spring Security authentication graphs under serialization.
- `samples/spring-session-demo/spring-session-demo-war/src/main/resources/pof-config.xml` — replace captured reset work and preserve current Spring Security authentication graphs under serialization.

## October 19, 2026 at 13:24:19 CEST (UTC+0200; Europe/Belgrade) — Review preflight complete

- Seven permanent snapshot-runner regressions pass. actionlint passes for all
  three changed workflows; git diff --check passes. Reviewed production changes,
  exact policies, POF/user guide, remote negative controls, CacheStore lifecycle
  and the actual runner/cache/artifact paths. No policy widening or test reduction.
- All 55 imported files are byte-identical to the original working tree. All
  original file hashes and complete staged index entries are unchanged.
- Verified current upstream 5.x equals the base SHA, authenticated account has
  push access, manual Simple CI is active and present on default/base branches,
  and no existing matching review branch/PR exists.
- Next: commit this review, push the new upstream branch without force, create a
  draft PR, and dispatch the two required rows on that exact SHA. Hosted results
  remain pending. No Maven reactor was rerun locally during review preparation.

## October 19, 2026 at 13:26:47 CEST (UTC+0200; Europe/Belgrade) — Correct hosted Java setup and early-failure diagnostics

- Hosted run 39244491126 is bound to review commit
  `f8395322c92f2b0d4bbebe38d6e04eafb88f1761`; both mandatory rows fail before
  Maven because actions/setup-java@v5.0.0 distribution=adopt returns HTTP 502.
  Seven runner tests passed on each runner; no project module/test executed.
  Optional requested-version job correctly skipped. No artifact was uploaded
  because setup failed before the runner created any evidence. Cleanup also
  reported the absent Maven repository. Retained raw run JSON/logs task-locally.
- Changed only the manual qualification workflow's two JDK selections to
  Temurin, supported by this exact setup-java version and matching prior local
  qualification. The existing JDK17/JDK21 and release17 matrix stays intact.
- Added pre-setup source-sha.txt evidence for artifact upload on early failure;
  guarded cleanup for an absent cache while preserving snapshot removal.
  No owner/permission/security/test changes and no dependency pinning.
- Both required rows must be dispatched together on the new review head; first
  run is setup-failure evidence only, with no hosted reactor qualification.
