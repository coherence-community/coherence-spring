/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.JarFile;
import com.tangosol.net.Coherence;
import org.springframework.context.ApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/** Test-only contract shared by independent consumers; no library test helpers. */
public final class Contract {
    private Contract() { }
    private static Properties evidence;

    public static void begin() throws Exception {
        Path outerManifest = Path.of(System.getProperty("consumer.outer.manifest"));
        Properties release = load(outerManifest.resolveSibling("outer-release.properties"));
        ReleaseContract.Target target = ReleaseContract.selected(System.getProperty("consumer.release"),
                System.getProperty("consumer.compiler.release"), release.getProperty("consumer.release"));
        evidence = new Properties();
        evidence.setProperty("consumer.bytecode.major", "" + target.major());
        for (String property : List.of("java.home", "java.version", "java.specification.version", "java.class.path",
                "consumer.fixture", "consumer.release", "consumer.compiler.release", "consumer.security.mode", "consumer.coherence.version",
                "consumer.coherence.group", "consumer.boot.version")) {
            evidence.setProperty(property, System.getProperty(property));
        }
        assertEquals(Path.of(System.getProperty("consumer.java.home")).toRealPath(), Path.of(System.getProperty("java.home")).toRealPath());
        String mode = System.getProperty("coherence.security.mode");
        assertEquals(System.getProperty("consumer.security.mode"), mode == null ? "unset" : mode);
        evidence.setProperty("coherence.security.mode", mode == null ? "<absent>" : mode);
        assertEquals("127.0.0.1", System.getProperty("coherence.localhost"));
        assertEquals("0", System.getProperty("coherence.ttl"));
        String fixture = System.getProperty("consumer.fixture");
        String graph = Files.readString(Path.of("target/runtime-dependencies.txt"));
        String runtime = Files.readString(Path.of("target/runtime-classpath.txt"));
        String actualClasspath = System.getProperty("java.class.path");
        for (String forbidden : List.of("coherence-spring-tests", "mockito", "assertj", "bedrock", "coherence-grpc-proxy")) {
            assertFalse(graph.contains(forbidden), forbidden + " leaked into runtime graph");
            assertFalse(actualClasspath.contains(forbidden), forbidden + " leaked into test JVM");
        }
        PathContract.runtimeClasspath(runtime, File.pathSeparator);
        PathContract.testClasspath(actualClasspath, File.pathSeparator, fixture.equals("plain-spring-session"));
        if (fixture.startsWith("general-")) {
            assertFalse(graph.contains("org.springframework.session:"));
            assertFalse(graph.contains("spring-boot-session:"));
            assertFalse(present("org.springframework.session.SessionRepository"));
        }
        if (fixture.equals("general-starter")) assertFalse(present("jakarta.servlet.Servlet"));
        if (fixture.equals("plain-spring-session")) {
            assertFalse(graph.contains("org.springframework.boot:"));
        }
        if (fixture.equals("library-without-boot-session")) assertFalse(present("org.springframework.boot.session.autoconfigure.SessionProperties"));
        if (fixture.equals("boot-session-without-coherence-session")) assertFalse(present("com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository"));
        if (List.of("session-starter", "non-web", "custom-repository", "boot-war").contains(fixture)) {
            for (String artifact : List.of("coherence-spring-session-boot-starter", "coherence-spring-boot-starter", "coherence-spring-session", "spring-boot-session"))
                assertTrue(graph.contains(":" + artifact + ":jar:"), "starter must supply " + artifact);
        }
        Properties outer = load(outerManifest);
        Path repository = outerManifest.toAbsolutePath().getParent().getParent().resolve("it-repository");
        int compared = 0;
        for (String line : Files.readAllLines(Path.of("target/test-dependencies.txt"))) {
            String[] fields = PathContract.dependency(line);
            if (fields.length != 6 || !fields[2].equals("jar")) continue;
            Path jar = PathContract.dependencyJar(fields);
            String key = fields[0] + ":" + fields[1] + ":" + fields[3];
            if (fields[0].equals(System.getProperty("consumer.coherence.group"))
                    || fields[0].equals("com.oracle.coherence.spring")) {
                PathContract.artifact(repository, jar, fields[0], fields[1], fields[3]);
                assertEquals(outer.getProperty(key + ".jar.sha256"), hash(jar), key + " JAR drift");
                Path pom = jar.resolveSibling(jar.getFileName().toString().replaceFirst("\\.jar$", ".pom"));
                assertEquals(outer.getProperty(key + ".pom.sha256"), hash(pom), key + " POM drift");
                evidence.setProperty(key + ".jar.sha256", hash(jar));
                evidence.setProperty(key + ".pom.sha256", hash(pom));
                if (fields[0].equals("com.oracle.coherence.spring")) checkBytecode(jar, target);
                compared++;
            }
            if (fields[0].equals("org.springframework.boot")) assertEquals(System.getProperty("consumer.boot.version"), fields[3]);
        }
        assertTrue(compared >= 2, "no artifact comparison");
        Path actual = Path.of(Coherence.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        PathContract.artifact(repository, actual, System.getProperty("consumer.coherence.group"), "coherence",
                System.getProperty("consumer.coherence.version"));
        String coherenceKey = System.getProperty("consumer.coherence.group") + ":coherence:" + System.getProperty("consumer.coherence.version");
        assertEquals(outer.getProperty(coherenceKey + ".jar.sha256"), hash(actual), "actual loaded Coherence bytes");
        evidence.setProperty("loaded.coherence.jar", actual.toString());
        evidence.setProperty("consumer.main.classes", "" + ReleaseContract.directory(Path.of("target/classes"), target));
        evidence.setProperty("consumer.test.classes", "" + ReleaseContract.directory(Path.of("target/test-classes"), target));
        System.setProperty("coherence.cluster", "consumer-" + fixture.substring(0, Math.min(20, fixture.length())) + "-" + UUID.randomUUID());
        // Reserve/check both protocols; release before Coherence's actual bind.
        for (;;) {
            try (var udp = new DatagramSocket(0, InetAddress.getLoopbackAddress())) {
                try (var tcp = new ServerSocket(udp.getLocalPort(), 1, InetAddress.getLoopbackAddress())) {
                    System.setProperty("coherence.clusterport", "" + udp.getLocalPort()); break;
                }
                catch (BindException retry) { }
            }
        }
        evidence.setProperty("coherence.cluster", System.getProperty("coherence.cluster"));
    }

    public static void cache(ApplicationContext context) {
        Coherence coherence = context.getBean(Coherence.class);
        assertTrue(coherence.isStarted());
        var cache = coherence.getSession().<String, String>getCache("consumer-cache");
        cache.put("contract", "packaged"); assertEquals("packaged", cache.get("contract"));
    }

    public static void noSessions(ApplicationContext context) throws Exception {
        assertFalse(context.containsBean("springSessionRepositoryFilter"));
        assertFalse(context.containsBean("sessionRepositoryFilterRegistration"));
        if (present("org.springframework.session.SessionRepository"))
            assertEquals(0, context.getBeansOfType(Class.forName("org.springframework.session.SessionRepository")).size());
    }

    public static void loadedCoherence(ClassLoader loader, Path deployment) throws Exception {
        Class<?> type = Class.forName("com.tangosol.net.Coherence", true, loader);
        Path jar = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI());
        Properties outer = load(Path.of(System.getProperty("consumer.outer.manifest")));
        String key = System.getProperty("consumer.coherence.group") + ":coherence:" + System.getProperty("consumer.coherence.version");
        assertEquals(outer.getProperty(key + ".jar.sha256"), hash(jar));
        PathContract.deployedJar(deployment, jar, "coherence-" + System.getProperty("consumer.coherence.version") + ".jar");
        evidence.setProperty("war.deployment", deployment.toRealPath().toString());
        evidence.setProperty("war.loaded.coherence.jar", jar.toString());
        evidence.setProperty("war.loaded.coherence.sha256", hash(jar));
    }

    public static void pass(String scenario) throws Exception {
        evidence.setProperty("result", "PASS"); evidence.setProperty("scenario", scenario);
        Path path = Path.of("target/consumer-evidence/" + scenario + ".properties");
        Files.createDirectories(path.getParent()); try (var out = Files.newOutputStream(path)) { evidence.store(out, "Actual consumer test JVM v1"); }
    }
    public static boolean present(String name) {
        try { Class.forName(name, false, Contract.class.getClassLoader()); return true; }
        catch (ClassNotFoundException absent) { return false; }
    }
    static void checkBytecode(Path jar, ReleaseContract.Target target) throws Exception {
        ReleaseContract.jar(jar, target);
        try (var file = new JarFile(jar.toFile())) {
            assertTrue(file.stream().noneMatch(e -> e.getName().startsWith("com/oracle/coherence/spring/tests/")));
        }
    }
    static Properties load(Path path) throws Exception { var p = new Properties(); try (var in = Files.newInputStream(path)) { p.load(in); } return p; }
    static String hash(Path path) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
}
