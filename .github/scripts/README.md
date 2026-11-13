# Qualification runner trust contract

`full_reactor.py` runs repository code, Maven plugins and Java tests. Use a
reviewed checkout, trusted JDK (`JAVA_HOME`), environment and dependency cache.
This is an operator/CI tool, not a sandbox for an untrusted checkout or caller
that can also change those inputs.

The default Maven launcher is the absolute `mvnw` in this checkout, independent
of the caller's current directory. `--maven` cannot select another executable
unless the operator separately configures its absolute path in
`FULL_REACTOR_TRUSTED_MAVEN`. This environment setting is trusted configuration;
do not populate it from untrusted CLI/agent input. Paths are compared after
resolution, and the selected file must be executable. A matching basename does
not confer trust. The operator must protect the configured launcher and its
distribution from replacement, and select a Maven version supported by the
project (3.9 or newer for offline propagation into Invoker).

For example, a local cached exercise can use:

```bash
export FULL_REACTOR_TRUSTED_MAVEN='/trusted/cache with spaces/apache-maven-3.9.9/bin/mvn'
python3 .github/scripts/full_reactor.py --lane baseline \
  --maven "$FULL_REACTOR_TRUSTED_MAVEN" --offline \
  --repository '/trusted/disposable repository' --output '/trusted/fresh evidence'
```

Use a fresh output directory under a trusted parent, with no concurrent writers.
The runner accepts a new or empty directory, or CI's prewritten regular
`source-sha.txt`. It rejects a symlink as the output directory, symlinked source
evidence, and every other pre-existing entry, including dangling symlinks. These
checks prevent accidental reuse/pre-existing output redirection; they do not
protect against an actor that can replace files during the run or alter an
ancestor directory. Ordinary symlinked parent paths (such as `/tmp` on macOS)
remain supported. Report strings remain exact evidence and never choose report
filenames.

The live invocation captures the canonical JDK home before Maven starts and
passes it independently to `audit(..., expected_home=home)`. The manifest must
record that exact home. A missing or changed manifest home fails qualification;
the auditor does not resolve that field as a path. Standalone/replay callers
must provide an absolute expected home from trusted run configuration, not read
it from `invocation.json` or observed evidence. The current environment is not
an implicit fallback. Existing runtime/consumer JDK, release-17, security-mode,
inventory and artifact checks still apply.

Run regressions with `JAVA_HOME` and `PATH` selecting each supported JDK:

```bash
python3 -B -m unittest discover -s .github/scripts -p 'test_*.py' -v
```

The suite includes actual Java helper checks as well as controlled evidence
fixtures and mocked Maven launches. It does not run the full Maven reactor.

## PR startup diagnostics

`ci-pr.yml` runs `capture_test_threads.py` alongside Maven with the selected
`JAVA_HOME`. About every 30 seconds, it records `jcmd -l` and `Thread.print -l`
for each Surefire test JVM (including forks launched through a Surefire booter
JAR). Each command is limited to 15 seconds; partial output and attach failures
are retained when a fork exits or cannot respond. The caller writes a `stop`
file in the output directory after Maven finishes. Use a fresh directory under
a trusted parent, without other writers; this is a local/CI diagnostic tool.

The PR workflow keeps both JDK jobs running when one fails and always attempts
to upload the Maven log, UTC-stamped JVM inventories/thread dumps and Surefire
reports as `startup-jdk<version>-attempt<attempt>` artifacts, retained for seven
days. Compare several dumps from the same PID before the five-minute startup
timeout, especially `main`, `Coherence:default` and cluster/service threads.
The property-resolution fixture keeps its distinctive `foobar` marker while
preserving timestamps, thread/member identities and actual message text, at
log level 5 without character-limit discarding.
