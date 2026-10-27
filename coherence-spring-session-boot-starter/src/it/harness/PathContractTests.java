/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
import consumer.PathContract;
import java.io.File;
import java.nio.file.*;
import java.util.*;

/** Exercises the actual helper used by all consumers, on the running OS. */
class PathContractTests {
    interface Checked { void run() throws Exception; }
    static void reject(String diagnostic, Checked action) throws Exception {
        try { action.run(); }
        catch (AssertionError expected) {
            if (!expected.getMessage().contains(diagnostic)) throw expected;
            return;
        }
        throw new AssertionError("Accepted invalid path: " + diagnostic);
    }

    public static void main(String[] args) throws Exception {
        Path work = Path.of(args[1]);
        switch (args[0]) {
            case "repository" -> repository(work);
            case "war" -> war(work);
            case "classpaths" -> classpaths();
            case "dependencies" -> dependencies(work);
            default -> throw new IllegalArgumentException(args[0]);
        }
        System.out.println("PASS " + args[0] + " on " + System.getProperty("os.name") + "; separator=" + File.separator);
    }

    static Path write(Path path) throws Exception {
        Files.createDirectories(path.getParent()); Files.writeString(path, "identical packaged bytes"); return path;
    }

    static void repository(Path work) throws Exception {
        Path repository = work.resolve("workspace with spaces/target/it-repository");
        String coordinate = "com/oracle/coherence/ce/coherence/26.04/coherence-26.04.jar";
        Path jar = write(repository.resolve(coordinate));
        PathContract.artifact(repository, jar, "com.oracle.coherence.ce", "coherence", "26.04");
        PathContract.artifact(repository, jar.getParent().resolve("../26.04/" + jar.getFileName()),
                "com.oracle.coherence.ce", "coherence", "26.04");
        for (String other : List.of("it-repository-sibling", "other/target/it-repository")) {
            Path wrong = write(repository.resolveSibling(other).resolve(coordinate));
            reject("exact isolated packaged artifact", () -> PathContract.artifact(repository, wrong,
                    "com.oracle.coherence.ce", "coherence", "26.04"));
        }
        Path wrongCoordinate = write(repository.resolve("wrong/" + coordinate));
        reject("exact isolated packaged artifact", () -> PathContract.artifact(repository, wrongCoordinate,
                "com.oracle.coherence.ce", "coherence", "26.04"));
        Path different = write(repository.resolve(coordinate.replace("26.04", "26.10")));
        reject("exact isolated packaged artifact", () -> PathContract.artifact(repository, different,
                "com.oracle.coherence.ce", "coherence", "26.04"));
        // A symlink at the expected coordinate must not escape the real repository.
        Path outside = write(work.resolve("outside/coherence-26.04.jar"));
        Files.delete(jar);
        try { Files.createSymbolicLink(jar, outside.toAbsolutePath()); }
        catch (FileSystemException | UnsupportedOperationException unavailable) {
            // Windows may require developer mode/elevation for symlinks. All ordinary
            // real-path boundary controls above still run there without that privilege.
            System.out.println("Symlink control unavailable: " + unavailable); return;
        }
        reject("exact isolated packaged artifact", () -> PathContract.artifact(repository, jar,
                "com.oracle.coherence.ce", "coherence", "26.04"));
    }

    static void war(Path work) throws Exception {
        Path deployment = work.resolve("tomcat with spaces/webapps/consumer");
        String filename = "coherence-26.04.jar";
        Path jar = write(deployment.resolve("WEB-INF/lib/" + filename));
        PathContract.deployedJar(deployment, jar, filename);
        for (String wrong : List.of("WEB-INF/lib-other/", "nested/WEB-INF/lib/", "WEB-INF/lib/subdir/")) {
            Path lookalike = write(deployment.resolve(wrong + filename));
            reject("actual deployed WAR", () -> PathContract.deployedJar(deployment, lookalike, filename));
        }
        Path sibling = write(deployment.resolveSibling("consumer-sibling").resolve("WEB-INF/lib/" + filename));
        reject("actual deployed WAR", () -> PathContract.deployedJar(deployment, sibling, filename));
        Path other = write(deployment.resolve("WEB-INF/lib/coherence-other.jar"));
        reject("actual deployed WAR", () -> PathContract.deployedJar(deployment, other, filename));
    }

    static void classpaths() throws Exception {
        for (boolean windows : List.of(false, true)) {
            String slash = windows ? "\\" : "/", separator = windows ? ";" : ":";
            String prefix = windows ? "C:\\workspace with spaces\\" : "/workspace with spaces/";
            String packaged = prefix + "repository/org/example/library/1/library-1.jar".replace("/", slash);
            String other = (windows ? "D:\\" : "/") + "dependencies/another.jar".replace("/", slash);
            if (!PathContract.classpath(packaged + separator + other, separator).equals(List.of(packaged, other)))
                throw new AssertionError("classpath separator/drive-letter loss");
            PathContract.runtimeClasspath(packaged + separator + other, separator);
            PathContract.testClasspath(packaged + separator + prefix + "fixture/target/test-classes".replace("/", slash), separator, true);
            for (String leak : List.of("target/classes", "target/test-classes", "some/test-classes", "junit/junit.jar", "spring-test.jar")) {
                String value = packaged + separator + prefix + leak.replace("/", slash);
                reject(leak.contains("junit") ? "junit" : leak.contains("spring-test") ? "spring-test" : "class directory",
                        () -> PathContract.runtimeClasspath(value, separator));
            }
            for (String module : List.of("core", "session", "boot-starter", "session-boot-starter", "data", "tests")) {
                for (String classes : List.of("classes", "test-classes")) {
                    String leak = prefix + ("coherence-spring-" + module + "/target/" + classes).replace("/", slash);
                    reject("reactor class directory", () -> PathContract.testClasspath(packaged + separator + leak, separator, false));
                }
            }
            String boot = prefix + "org/springframework/boot/spring-boot/4/spring-boot-4.jar".replace("/", slash);
            reject("Boot artifact", () -> PathContract.testClasspath(packaged + separator + boot, separator, true));
            PathContract.testClasspath(boot, separator, false);
            for (String near : List.of("target/classes-sibling", "coherence-spring-core-other/target/classes-other",
                    "org/springframework/boot-other/library.jar")) {
                String value = prefix + near.replace("/", slash);
                PathContract.runtimeClasspath(value, separator); PathContract.testClasspath(value, separator, true);
            }
            reject("empty runtime", () -> PathContract.runtimeClasspath(packaged + separator, separator));
        }
    }

    static void dependencies(Path work) throws Exception {
        for (String path : List.of("/workspace with spaces/repo/library.jar", "C:\\workspace with spaces\\repo\\library.jar", "C:/repo/library.jar")) {
            String line = "  org.example:library:jar:1:runtime:" + path + " -- module library (auto)";
            String[] fields = PathContract.dependency(line);
            if (!Arrays.equals(fields, ConsumerHarness.dependency(line))) throw new AssertionError("outer/inner parser disagreement");
            if (fields.length != 6 || !fields[5].startsWith(path)) throw new AssertionError("drive-letter/path loss");
            if (!PathContract.dependencyJar(fields).toString().equals(Path.of(path).toString()))
                throw new AssertionError("module suffix/path loss");
        }
        // Exercise outer freeze's actual native path parser and exact copy/hash behavior.
        ConsumerHarness.module = work; ConsumerHarness.target = work.resolve("target");
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            Path jar = write(work.resolve("outer repo with spaces/artifact" + i + "/artifact" + i + "-1.jar"));
            write(jar.resolveSibling("artifact" + i + "-1.pom"));
            lines.append("org.example:artifact").append(i).append(":jar:1:compile:").append(jar.toAbsolutePath()).append(" -- module example\n");
        }
        Path list = ConsumerHarness.target.resolve("consumer-evidence/outer-dependencies.txt");
        Files.createDirectories(list.getParent()); Files.writeString(list, lines);
        write(ConsumerHarness.target.resolve("coherence-spring-session-boot-starter-1.jar"));
        write(work.resolve(".flattened-pom.xml"));
        ConsumerHarness.freeze("com.oracle.coherence.spring", "1", "17");
        Properties manifest = ConsumerHarness.load(list.resolveSibling("outer-artifacts.properties"));
        if (manifest.size() != 24) throw new AssertionError("outer parsed inventory");
        for (int i = 0; i < 5; i++) {
            Path jar = ConsumerHarness.target.resolve("it-repository/org/example/artifact" + i + "/1/artifact" + i + "-1.jar");
            PathContract.artifact(ConsumerHarness.target.resolve("it-repository"), jar, "org.example", "artifact" + i, "1");
            if (!ConsumerHarness.hash(jar).equals(manifest.getProperty("org.example:artifact" + i + ":1.jar.sha256")))
                throw new AssertionError("outer/inner packaged bytes differ");
        }
    }
}
