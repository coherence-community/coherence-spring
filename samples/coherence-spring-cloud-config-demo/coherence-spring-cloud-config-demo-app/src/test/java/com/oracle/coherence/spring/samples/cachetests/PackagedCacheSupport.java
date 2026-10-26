/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.cachetests;

import java.io.File;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/** Packaged production classpaths; test resources and application test classes are excluded. */
public abstract class PackagedCacheSupport {

	protected static final String POLICY = "META-INF/coherence/security-config.xml";

	protected static Path extract(Path archive, Path destination) throws Exception {
		assertThat(archive).isRegularFile();
		try (JarFile jar = new JarFile(archive.toFile())) {
			for (var entry : jar.stream().toList()) {
				Path target = destination.resolve(entry.getName()).normalize();
				assertThat(target.startsWith(destination)).isTrue();
				if (entry.isDirectory()) {
					Files.createDirectories(target);
				}
				else {
					Files.createDirectories(target.getParent());
					try (var input = jar.getInputStream(entry)) {
						Files.copy(input, target);
					}
				}
			}
		}
		return destination;
	}

	protected static Path probes(Class<?> probe, Path destination) throws Exception {
		Path tests = Path.of(probe.getProtectionDomain().getCodeSource().getLocation().toURI());
		String pkg = probe.getPackageName().replace('.', '/');
		copy(tests.resolve(pkg), destination.resolve(pkg));
		return destination;
	}

	protected static String classpath(Path root, Path probes) throws Exception {
		Path assertions = Path.of(org.assertj.core.api.Assertions.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		return String.join(File.pathSeparator, probes.toString(), productionClasspath(root), assertions.toString());
	}

	protected static String productionClasspath(Path root) {
		return String.join(File.pathSeparator, root.toString(), root.resolve("BOOT-INF/classes").toString(),
				root.resolve("BOOT-INF/lib/*").toString());
	}

	protected static List<String> cluster(String prefix, int port) {
		List<String> args = new ArrayList<>(List.of("-Dcoherence.cluster=" + prefix + "-" + UUID.randomUUID(),
				"-Dcoherence.clusterport=" + port, "-Dcoherence.localhost=127.0.0.1", "-Dcoherence.wka=127.0.0.1",
				"-Dcoherence.ttl=0", "-Dcoherence.management=none", "-Djava.net.preferIPv4Stack=true"));
		String mode = System.getProperty("coherence.security.mode");
		if (mode != null) {
			args.add("-Dcoherence.security.mode=" + mode);
		}
		return args;
	}

	protected static Process launch(Path log, List<String> arguments) throws Exception {
		List<String> command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin/java").toString(),
				"-Xmx768m", "-XX:ActiveProcessorCount=3"));
		command.addAll(arguments);
		Files.writeString(log.resolveSibling(log.getFileName() + ".command"), String.join("\n", command));
		return new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
	}

	protected static List<String> command(List<String> common, String cp, String main, String... args) {
		List<String> result = new ArrayList<>(common);
		result.addAll(List.of("-cp", cp, main));
		result.addAll(List.of(args));
		return result;
	}

	protected static void started(Process process, Path log, String marker) {
		await().atMost(Duration.ofSeconds(60)).untilAsserted(() -> {
			assertThat(process.isAlive()).as(Files.readString(log)).isTrue();
			assertThat(Files.readString(log)).contains(marker);
		});
	}

	protected static void completed(Process process, Path log, String marker) throws Exception {
		assertThat(process.waitFor(100, TimeUnit.SECONDS)).as("Deadline; see " + log).isTrue();
		assertThat(process.exitValue()).as(Files.readString(log)).isZero();
		assertThat(Files.readString(log)).contains(marker);
	}

	protected static int port() throws Exception {
		// Avoid the OS ephemeral range used by concurrent HTTP/observer connections.
		for (int attempt = 0; attempt < 100; attempt++) {
			int candidate = ThreadLocalRandom.current().nextInt(20000, 30000);
			try (ServerSocket tcp = new ServerSocket(candidate, 0, InetAddress.getLoopbackAddress());
					DatagramSocket udp = new DatagramSocket(candidate, InetAddress.getLoopbackAddress())) {
				return candidate;
			}
			catch (java.net.BindException ex) {
				// A task must not claim a port already owned by another process.
			}
		}
		throw new IllegalStateException("No isolated TCP/UDP test port available");
	}

	protected static void stop(Process process) throws Exception {
		process.destroy();
		if (!process.waitFor(10, TimeUnit.SECONDS)) {
			process.destroyForcibly();
			assertThat(process.waitFor(10, TimeUnit.SECONDS)).isTrue();
		}
	}

	protected static void copy(Path source, Path target) throws Exception {
		try (var paths = Files.walk(source)) {
			for (Path path : paths.toList()) {
				Path destination = target.resolve(source.relativize(path));
				if (Files.isDirectory(path)) {
					Files.createDirectories(destination);
				}
				else {
					Files.createDirectories(destination.getParent());
					Files.copy(path, destination);
				}
			}
		}
	}

	protected static void remove(Path root) throws Exception {
		try (var paths = Files.walk(root)) {
			for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
				Files.delete(path);
			}
		}
	}
}
