/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/** Filesystem boundaries and classpath syntax for the independent consumers. */
public final class PathContract {
    private PathContract() { }

    public static Path artifact(Path repository, Path jar, String group, String artifact, String version) throws Exception {
        Path root = repository.toRealPath();
        Path expected = root.resolve(group.replace('.', File.separatorChar)).resolve(artifact).resolve(version)
                .resolve(artifact + "-" + version + ".jar").toRealPath();
        Path actual = jar.toRealPath();
        require(Files.isRegularFile(actual) && actual.startsWith(root) && actual.equals(expected),
                "must consume exact isolated packaged artifact " + expected + ": " + actual);
        return actual;
    }

    public static Path deployedJar(Path deployment, Path jar, String filename) throws Exception {
        Path root = deployment.toRealPath();
        Path libraries = root.resolve("WEB-INF").resolve("lib").toRealPath();
        Path actual = jar.toRealPath();
        require(Files.isRegularFile(actual) && libraries.startsWith(root) && actual.getParent().equals(libraries)
                        && actual.getFileName().toString().equals(filename),
                "Coherence must load from the actual deployed WAR " + libraries + ": " + actual);
        return actual;
    }

    public static List<String> classpath(String value, String separator) {
        // Classpath lists use the platform's path separator; ':' is part of Windows drive letters.
        return Arrays.asList(value.strip().split(Pattern.quote(separator), -1));
    }

    public static void runtimeClasspath(String value, String separator) {
        for (String entry : classpath(value, separator)) {
            require(!entry.isBlank(), "empty runtime classpath entry");
            require(!components(entry, "target", "classes") && !components(entry, "test-classes"),
                    "reactor/test class directory in runtime dependencies: " + entry);
            for (String forbidden : List.of("junit", "spring-test"))
                require(!entry.contains(forbidden), forbidden + " in runtime dependencies: " + entry);
        }
    }

    public static void testClasspath(String value, String separator, boolean withoutBoot) {
        for (String entry : classpath(value, separator)) {
            for (String module : List.of("core", "session", "boot-starter", "session-boot-starter", "data", "tests"))
                for (String classes : List.of("classes", "test-classes"))
                    require(!components(entry, "coherence-spring-" + module, "target", classes),
                            "reactor class directory in test JVM: " + entry);
            if (withoutBoot) require(!components(entry, "org", "springframework", "boot"),
                    "Boot artifact in plain Spring Session test JVM: " + entry);
        }
    }

    static boolean components(String value, String... expected) {
        // Lexical classpath inspection also supports diagnostic strings from the other OS.
        // Actual filesystem identity/containment above always uses native real Paths.
        List<String> parts = Arrays.asList(value.split("[/\\\\]+"));
        return Collections.indexOfSubList(parts, List.of(expected)) >= 0;
    }

    public static String[] dependency(String line) {
        // Keep the entire absolute filename (including C:) in the sixth field.
        return line.trim().split(":", 6);
    }

    public static Path dependencyJar(String[] fields) {
        return Path.of(fields[5].replaceFirst(" --.*", "").trim());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
