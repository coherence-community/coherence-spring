# Coherence Spring security hardening review

The 5.x integration keeps Coherence 26.04 and Java 17 as its normal baseline.
The proposed changes also support the published Coherence 26.10 snapshots with
explicit hardened mode on JDK 21 while compiling Java 17 output.

The implementation adds a named Session reset processor and exact production
metadata, preserves Spring Security 7 authentication in POF, makes remote Session
serializer choices explicit, and supplies exact fixture/sample metadata.
CacheStore's four existing integration methods now run under ordinary Failsafe
with bounded Boot readiness checks and deterministic process cleanup.

Read the [assessment](assessment.md), [Session implementation](session-implementation.md),
[CacheStore permission report](cachestore-person-permission-2026-10-16.md), and
[latest published snapshot local qualification](latest-snapshot-local-2026-10-19.md).
The historical lock and frozen reports are provenance only; current snapshot CI
uses Maven `-U`, discards restored Coherence snapshot directories and records actual
resolved JAR/POM identities and timestamp metadata for every run.

Manual `.github/workflows/maven-simple.yml` runs these mandatory rows with normal
`clean verify`, `-Pjdk17`, normal checks and `maven.test.failure.ignore=false`:

| Runtime / dependency | Mode | Expected results |
| --- | --- | --- |
| Java 17 / 26.04 | unset baseline | 28 modules; 697 passes + 9 absent-security-API skips |
| JDK 21 / latest published 26.10-SNAPSHOT | explicit hardened | 28 modules; 706 passes, no skips |

Both rows require 83 suites, four passing CacheStore ITs, actual properties for
all nine Coherence child JVMs, Java 17 class-file major 61, consistent runtime and
packaged dependencies, and uploaded reports/diagnostics even on failure. Use
`coherence-version=26.04` when dispatching to avoid the optional third row.
The repository-owner guard remains in place; the review branch must be upstream.

Hosted qualification is pending when this document is committed. Match the
workflow run's `head_sha` to the exact draft-PR head and inspect uploaded XML,
runtime/bytecode/dependency evidence and logs before reporting a pass. Retain final
results separately with that tested SHA so no untested documentation commit is
confused with the tested review revision. Production deployment, TLS/authentication,
failover, actual remote Data installation and rolling upgrades remain separate.
