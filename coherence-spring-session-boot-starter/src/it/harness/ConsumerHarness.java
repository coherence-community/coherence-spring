/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/** Source-launched build guard; never compiled into the dependency-only starter. */
class ConsumerHarness {
    static Path module, target, fixtures;
    static Properties inventory;

    public static void main(String[] args) throws Exception {
        module = Path.of(args[1]); target = module.resolve("target"); fixtures = module.resolve("src/it");
        inventory = load(fixtures.resolve("inventory.properties"));
        if (inventory.isEmpty()) throw new AssertionError("Empty expected consumer inventory");
        switch (args[0]) {
            case "inventory" -> {
                checkInventory();
                if (Boolean.parseBoolean(args[2])) {
                    Properties skipped = new Properties(); skipped.setProperty("result", "SKIPPED");
                    save(skipped, target.resolve("consumer-evidence/summary.properties"));
                    System.out.println("Consumer tests explicitly skipped; this run cannot qualify package C");
                }
            }
            case "freeze" -> freeze(args[2], args[3], args[4]);
            case "audit" -> audit(args[4]);
            default -> throw new IllegalArgumentException(args[0]);
        }
    }

    static void checkInventory() throws Exception {
        Set<String> found;
        try (var files = Files.list(fixtures)) {
            found = files.filter(p -> Files.isRegularFile(p.resolve("pom.xml")))
                    .map(p -> p.getFileName().toString()).collect(Collectors.toSet());
        }
        if (!found.equals(inventory.stringPropertyNames())) throw new AssertionError("Fixture inventory: " + found
                + "; expected " + inventory.stringPropertyNames());
        for (String name : found) {
            String pom = Files.readString(fixtures.resolve(name + "/pom.xml"));
            if (pom.contains("<parent>")) throw new AssertionError("Consumer must have no parent: " + name);
            if (!Files.isRegularFile(fixtures.resolve(name + "/src/test/java/consumer/ConsumerIT.java")))
                throw new AssertionError("Missing consumer test: " + name);
        }
        // Reject stale success evidence even without an outer clean.
        for (String directory : List.of("consumer-evidence", "invoker-reports")) {
            Path path = target.resolve(directory);
            if (Files.exists(path)) try (var paths = Files.walk(path)) {
                for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(p);
            }
        }
        Files.createDirectories(target.resolve("consumer-evidence"));
        Files.copy(fixtures.resolve("inventory.properties"), target.resolve("consumer-evidence/inventory.properties"));
        System.out.println("Required consumers: " + found.size() + ", scenarios: "
                + inventory.values().stream().mapToInt(v -> v.toString().split(",").length).sum());
    }

    static void freeze(String productGroup, String productVersion, String release) throws Exception {
        Properties selected = new Properties(); selected.setProperty("consumer.release", release);
        save(selected, target.resolve("consumer-evidence/outer-release.properties"));
        Properties manifest = new Properties();
        Path repo = target.resolve("it-repository");
        for (String line : Files.readAllLines(target.resolve("consumer-evidence/outer-dependencies.txt"))) {
            String[] fields = dependency(line);
            if (fields.length != 6 || !fields[2].equals("jar")) continue;
            Path jar = Path.of(fields[5].replaceFirst(" --.*", "").trim());
            // Reactor artifacts have their flattened POM beside their source, external ones beside the JAR.
            Path siblingPom = jar.resolveSibling(jar.getFileName().toString().replaceFirst("\\.jar$", ".pom"));
            Path pom = Files.isRegularFile(siblingPom) ? siblingPom
                    : jar.getParent().getParent().resolve(".flattened-pom.xml");
            record(manifest, repo, fields[0], fields[1], fields[3], jar, pom);
        }
        String artifact = "coherence-spring-session-boot-starter";
        record(manifest, repo, productGroup, artifact, productVersion,
                target.resolve(artifact + "-" + productVersion + ".jar"), module.resolve(".flattened-pom.xml"));
        if (manifest.size() < 20) throw new AssertionError("Incomplete outer artifact manifest: " + manifest);
        save(manifest, target.resolve("consumer-evidence/outer-artifacts.properties"));
        System.out.println("Frozen outer JAR/POM identities: " + manifest.size() / 4);
    }

    static void record(Properties manifest, Path repo, String group, String artifact, String version,
            Path jar, Path pom) throws Exception {
        String key = group + ":" + artifact + ":" + version;
        Path directory = repo.resolve(group.replace('.', '/') + "/" + artifact + "/" + version);
        Files.createDirectories(directory);
        for (var entry : Map.of("jar", jar, "pom", pom).entrySet()) {
            Path source = entry.getValue();
            Path installed = directory.resolve(artifact + "-" + version + "." + entry.getKey());
            if (!Files.isRegularFile(source)) throw new AssertionError("Missing outer packaged input " + source);
            String hash = hash(source);
            Files.copy(source, installed, StandardCopyOption.REPLACE_EXISTING);
            if (!hash.equals(hash(installed))) throw new AssertionError("Installation drift: " + installed);
            manifest.setProperty(key + "." + entry.getKey() + ".sha256", hash);
            manifest.setProperty(key + "." + entry.getKey() + ".source", source.toString());
        }
        // Mark these exact bytes as locally installed. Remote snapshot lookup is additionally
        // restricted to release Central by settings.xml, so no later publication is reachable.
        Files.writeString(directory.resolve("_remote.repositories"), artifact + "-" + version + ".jar>=\n"
                + artifact + "-" + version + ".pom>=\n");
    }

    static void audit(String release) throws Exception {
        Properties outer = load(target.resolve("consumer-evidence/outer-release.properties"));
        if (!release.equals(outer.getProperty("consumer.release")))
            throw new AssertionError("Outer consumer.release differs from selected release " + release);
        int count = 0;
        for (String name : new TreeSet<>(inventory.stringPropertyNames())) {
            Path clone = target.resolve("it/" + name);
            Path report = target.resolve("invoker-reports/BUILD-" + name + ".xml");
            Element build = xml(report).getDocumentElement();
            if (!"success".equals(build.getAttribute("result"))) throw new AssertionError("Invoker result " + report);
            Set<String> expected = new TreeSet<>(List.of(inventory.getProperty(name).split(",")));
            Set<String> actual = new TreeSet<>();
            Path reports = clone.resolve("target/failsafe-reports");
            if (!Files.isDirectory(reports)) throw new AssertionError("Missing Failsafe evidence: " + name);
            try (var files = Files.list(reports)) {
                for (Path path : files.filter(p -> p.getFileName().toString().startsWith("TEST-")
                        && p.toString().endsWith(".xml")).toList()) {
                    NodeList cases = xml(path).getElementsByTagName("testcase");
                    for (int i = 0; i < cases.getLength(); i++) {
                        Element test = (Element) cases.item(i);
                        String method = test.getAttribute("name");
                        if (!test.getAttribute("classname").equals("consumer.ConsumerIT")
                                || !actual.add(method)) throw new AssertionError("Unexpected/duplicate test " + test);
                        for (String failure : List.of("failure", "error", "skipped"))
                            if (test.getElementsByTagName(failure).getLength() != 0)
                                throw new AssertionError(name + ": " + method + " " + failure);
                        Properties evidence = load(clone.resolve("target/consumer-evidence/" + method + ".properties"));
                        if (!"PASS".equals(evidence.getProperty("result"))
                                || !method.equals(evidence.getProperty("scenario"))
                                || !name.equals(evidence.getProperty("consumer.fixture")))
                            throw new AssertionError("Missing/mismatched pass evidence: " + name + ":" + method);
                        for (String field : List.of("consumer.release", "consumer.compiler.release"))
                            if (!release.equals(evidence.getProperty(field)))
                                throw new AssertionError(name + ":" + method + ": " + field + " differs from selected release " + release);
                        count++;
                    }
                }
            }
            if (!actual.equals(expected)) throw new AssertionError(name + " tests " + actual + ", expected " + expected);
            for (String file : List.of("runtime-dependencies.txt", "runtime-classpath.txt", "test-dependencies.txt"))
                if (Files.size(clone.resolve("target/" + file)) == 0) throw new AssertionError("Empty " + file);
        }
        Properties summary = new Properties();
        summary.setProperty("fixtures", "" + inventory.size()); summary.setProperty("tests", "" + count);
        summary.setProperty("failures", "0"); summary.setProperty("errors", "0"); summary.setProperty("skips", "0");
        summary.setProperty("result", "PASS");
        save(summary, target.resolve("consumer-evidence/summary.properties"));
        System.out.println("Consumer contract PASS: " + inventory.size() + " fixtures, " + count + " tests, zero skips");
    }

    // Source launch on Java17 is standalone; retain drive letters in the final path field.
    static String[] dependency(String line) { return line.trim().split(":", 6); }

    static Document xml(Path path) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(path.toFile());
    }
    static Properties load(Path path) throws Exception {
        var properties = new Properties(); try (var in = Files.newInputStream(path)) { properties.load(in); } return properties;
    }
    static void save(Properties properties, Path path) throws Exception {
        Files.createDirectories(path.getParent()); try (var out = Files.newOutputStream(path)) { properties.store(out, "Consumer evidence v1"); }
    }
    static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
}
