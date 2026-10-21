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
        evidence = new Properties();
        for (String property : List.of("java.home", "java.version", "java.specification.version", "java.class.path",
                "consumer.fixture", "consumer.release", "consumer.security.mode", "consumer.coherence.version",
                "consumer.coherence.group", "consumer.boot.version")) {
            evidence.setProperty(property, System.getProperty(property));
        }
        assertEquals(Path.of(System.getProperty("consumer.java.home")).toRealPath(), Path.of(System.getProperty("java.home")).toRealPath());
        assertEquals("17", System.getProperty("consumer.release"));
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
        assertFalse(runtime.contains("target/classes"), "reactor class directory in runtime dependencies");
        assertFalse(actualClasspath.matches("(?s).*coherence-spring-(core|session|boot-starter|data)/target/(test-)?classes.*"));
        for (String forbidden : List.of("junit", "spring-test", "test-classes")) assertFalse(runtime.contains(forbidden), forbidden);
        if (fixture.startsWith("general-")) {
            assertFalse(graph.contains("org.springframework.session:"));
            assertFalse(graph.contains("spring-boot-session:"));
            assertFalse(present("org.springframework.session.SessionRepository"));
        }
        if (fixture.equals("general-starter")) assertFalse(present("jakarta.servlet.Servlet"));
        if (fixture.equals("plain-spring-session")) {
            assertFalse(graph.contains("org.springframework.boot:"));
            assertFalse(actualClasspath.contains("/org/springframework/boot/"));
        }
        if (fixture.equals("library-without-boot-session")) assertFalse(present("org.springframework.boot.session.autoconfigure.SessionProperties"));
        if (fixture.equals("boot-session-without-coherence-session")) assertFalse(present("com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository"));
        if (List.of("session-starter", "non-web", "custom-repository", "boot-war").contains(fixture)) {
            for (String artifact : List.of("coherence-spring-session-boot-starter", "coherence-spring-boot-starter", "coherence-spring-session", "spring-boot-session"))
                assertTrue(graph.contains(":" + artifact + ":jar:"), "starter must supply " + artifact);
        }
        Properties outer = load(Path.of(System.getProperty("consumer.outer.manifest")));
        int compared = 0;
        for (String line : Files.readAllLines(Path.of("target/test-dependencies.txt"))) {
            String[] fields = line.trim().split(":", 6);
            if (fields.length != 6 || !fields[2].equals("jar")) continue;
            Path jar = Path.of(fields[5].replaceFirst(" --.*", "").trim());
            String key = fields[0] + ":" + fields[1] + ":" + fields[3];
            if (fields[0].equals(System.getProperty("consumer.coherence.group"))
                    || fields[0].equals("com.oracle.coherence.spring")) {
                assertTrue(jar.toString().contains("/target/it-repository/"), "must consume isolated packaged bytes: " + jar);
                assertEquals(outer.getProperty(key + ".jar.sha256"), hash(jar), key + " JAR drift");
                Path pom = jar.resolveSibling(jar.getFileName().toString().replaceFirst("\\.jar$", ".pom"));
                assertEquals(outer.getProperty(key + ".pom.sha256"), hash(pom), key + " POM drift");
                evidence.setProperty(key + ".jar.sha256", hash(jar));
                evidence.setProperty(key + ".pom.sha256", hash(pom));
                if (fields[0].equals("com.oracle.coherence.spring")) checkBytecode(jar);
                compared++;
            }
            if (fields[0].equals("org.springframework.boot")) assertEquals(System.getProperty("consumer.boot.version"), fields[3]);
        }
        assertTrue(compared >= 2, "no artifact comparison");
        Path actual = Path.of(Coherence.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String coherenceKey = System.getProperty("consumer.coherence.group") + ":coherence:" + System.getProperty("consumer.coherence.version");
        assertEquals(outer.getProperty(coherenceKey + ".jar.sha256"), hash(actual), "actual loaded Coherence bytes");
        evidence.setProperty("loaded.coherence.jar", actual.toString());
        try (var files = Files.walk(Path.of("target/test-classes"))) {
            for (Path p : files.filter(p -> p.toString().endsWith(".class")).toList()) assertEquals(61, major(Files.readAllBytes(p)), p.toString());
        }
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

    public static void loadedCoherence(ClassLoader loader) throws Exception {
        Class<?> type = Class.forName("com.tangosol.net.Coherence", true, loader);
        Path jar = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI());
        Properties outer = load(Path.of(System.getProperty("consumer.outer.manifest")));
        String key = System.getProperty("consumer.coherence.group") + ":coherence:" + System.getProperty("consumer.coherence.version");
        assertEquals(outer.getProperty(key + ".jar.sha256"), hash(jar));
        assertTrue(jar.toString().contains("WEB-INF/lib/"), "Coherence must load from the deployed WAR: " + jar);
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
    static void checkBytecode(Path jar) throws Exception {
        try (var file = new JarFile(jar.toFile())) {
            for (var entry : file.stream().filter(e -> e.getName().endsWith(".class")).toList())
                assertEquals(61, major(file.getInputStream(entry).readAllBytes()), jar + "!" + entry.getName());
            assertTrue(file.stream().noneMatch(e -> e.getName().startsWith("com/oracle/coherence/spring/tests/")));
        }
    }
    static int major(byte[] bytes) { return (bytes[6] & 255) * 256 + (bytes[7] & 255); }
    static Properties load(Path path) throws Exception { var p = new Properties(); try (var in = Files.newInputStream(path)) { p.load(in); } return p; }
    static String hash(Path path) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
}
