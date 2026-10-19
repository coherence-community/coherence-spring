# Development environment

Read `../AGENTS.override.md` and repository `AGENTS.md` before work. Keep
chronological, timestamped repository changes in `JOURNAL.md` and resume state
in `.ai/branches/<branch>/journal.md`.

Use disposable builds under `/private/tmp/codex-work/`. Preserve unrelated
temporary files and remove task-owned builds after retaining compact evidence.
The product target is Java 17 and the default Coherence dependency is 26.04.
Run 26.10-SNAPSHOT on JDK 21 with `-Pjdk17` (release 17). Snapshot CI must
resolve the latest published artifacts with Maven `-U`; historical checksums are
run evidence, not pins for future snapshot runs. Pin the release version once
26.10 is published. The Java 17 baseline remains Coherence 26.04.
Local cluster tests require UDP/TCP socket access. Run clusters sequentially
where ports are shared. Do not infer child JVM properties from Maven properties.
