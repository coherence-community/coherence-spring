# Journal

## Resume Context

- Scope: focused Coherence 26.10 hardening review and hosted two-row qualification.
- Base: upstream `5.x`, `37cf05ea59bd60c03b21b52acc1eb4c1045859a3`.
- Status: imported implementation unchanged; helper/lint/source preflight passes; hosted matrix pending.
- Canonical entry: `design/features/coherence-security-hardening/README.md`.
- Read `.ai/dev-environment.md`, `assessment.md`, `latest-snapshot-local-2026-10-19.md`
  and `.github/workflows/maven-simple.yml` before continuing.
- Next action: inspect final diff, run helper/lint checks, commit, create a draft
  PR, dispatch Simple CI on the review branch with `coherence-version=26.04`,
  verify run head SHA equals review SHA, and inspect both rows and artifacts.
- Expected: each row 28 modules, 706 cases / 83 suites / four CacheStore ITs.
  Java17/26.04: 697 passes + 9 absent-security-API skips, unset mode.
  JDK21/latest26.10/release17/hardened: 706 passes, no skips.
- Keep guard `github.repository_owner == 'coherence-community'`, all checks, fatal
  failures and latest publication resolution. No optional third row is requested.
- User authorized scoped staging/commit/push/draft PR and non-deploying dispatch.
  Do not merge, deploy, release or force-push. Preserve original user checkout.
- Local latest snapshot evidence: `/private/tmp/codex-work/spring-latest-snapshot-p3e4za53/evidence/`.
  Those historical results are not hosted results for the eventual review SHA.
- Final hosted report should record the tested SHA explicitly; do not advance the
  review branch with an untested results-only commit after qualification.

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
