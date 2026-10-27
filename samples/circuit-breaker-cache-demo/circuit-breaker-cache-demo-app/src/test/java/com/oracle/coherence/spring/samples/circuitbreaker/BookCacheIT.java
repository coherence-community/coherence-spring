/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.circuitbreaker;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

import com.oracle.coherence.spring.samples.cachetesting.BookCacheProbe;
import com.oracle.coherence.spring.samples.circuitbreaker.model.Book;
import com.tangosol.net.Coherence;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Runs the packaged application against a separate, model-free Extend server.
 * Both archive-root metadata and BOOT-INF classes/resources must be visible.
 */
class BookCacheIT {

	private static final String POLICY = "META-INF/coherence/security-config.xml";

	@Test
	void readsPopulatedBookGraphsAndExposesMissingPermissionFallback() throws Exception {
		Path output = Path.of("target", "book-cache-tests").toAbsolutePath();
		Files.createDirectories(output);
		Path work = Files.createTempDirectory(output, "run-");
		try {
			Path main = work.resolve("permitted-app");
			extract(Path.of(System.getProperty("book.app.archive")), main);
			Path original = Path.of(Book.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			assertThat(Files.readAllBytes(main.resolve(POLICY))).isEqualTo(Files.readAllBytes(original.resolve(POLICY)));
			Path tests = Path.of(BookCacheProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			Path probes = work.resolve("probes");
			copyTree(tests.resolve("com/oracle/coherence/spring/samples/cachetesting"),
					probes.resolve("com/oracle/coherence/spring/samples/cachetesting"));
			assertThat(Files.readString(main.resolve(POLICY))).contains("name=\"" + Book.class.getName() + "\"")
					.doesNotContain("executable=", "*");
			Path withoutPolicy = work.resolve("without-policy");
			copyTree(main, withoutPolicy);
			Files.delete(withoutPolicy.resolve(POLICY));
			Path appConfig = work.resolve("client.xml");
			Files.writeString(appConfig, Files.readString(main.resolve("BOOT-INF/classes/coherence-cache-config.xml"))
					.replace("<port>7574</port>", "<port system-property=\"coherence.clusterport\">7574</port>"));
			Path serverConfig = Path.of("../circuit-breaker-cache-demo-server/src/main/resources/coherence-cache-config.xml")
					.toAbsolutePath();
			assertThat(serverConfig).isRegularFile();
			for (boolean permitted : new boolean[] {true, false}) {
				String scenario = permitted ? "permitted" : "missing-permission";
				int port;
				try (ServerSocket socket = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
					port = socket.getLocalPort();
				}
				Path serverLog = output.resolve(scenario + "-server.log");
				String serverClasspath = Path.of(Coherence.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
				Process server = launch(port, serverLog, serverClasspath, "com.tangosol.net.Coherence",
						List.of(), List.of("-Dcoherence.cacheconfig=" + serverConfig));
				try {
					await().atMost(Duration.ofSeconds(60)).untilAsserted(() -> {
						assertThat(server.isAlive()).as(Files.readString(serverLog)).isTrue();
						assertThat(Files.readString(serverLog)).contains("Started Coherence server");
					});
					Path appRoot = permitted ? main : withoutPolicy;
					String assertions = Path.of(org.assertj.core.api.Assertions.class.getProtectionDomain()
							.getCodeSource().getLocation().toURI()).toString();
					String classpath = String.join(File.pathSeparator, probes.toString(), appRoot.toString(),
							appRoot.resolve("BOOT-INF/classes").toString(), appRoot.resolve("BOOT-INF/lib/*").toString(), assertions);
					Path appLog = output.resolve(scenario + "-app.log");
					Process app = launch(port, appLog, classpath, BookCacheProbe.class.getName(),
							List.of(Boolean.toString(permitted), appConfig.toString()), List.of());
					try {
						assertThat(app.waitFor(100, TimeUnit.SECONDS)).as("Application deadline; see " + appLog).isTrue();
						assertThat(app.exitValue()).as(Files.readString(appLog)).isZero();
						assertThat(Files.readString(appLog)).contains("BOOK_CACHE_PROBE_PASS permitted=" + permitted);
						if (permitted) {
							assertThat(Files.readString(appLog)).doesNotContain("getBook fallback method", "Executing fallback method",
									"denied-class=" + Book.class.getName());
						}
					}
					finally {
						stop(app);
					}
				}
				finally {
					stop(server);
				}
			}
		}
		finally {
			try (var paths = Files.walk(work)) {
				for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
					Files.delete(path);
				}
			}
		}
	}

	private Process launch(int port, Path log, String classpath, String main, List<String> args, List<String> properties)
			throws Exception {
		List<String> command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
				"-Xmx768m", "-XX:ActiveProcessorCount=3", "-Dcoherence.cluster=CircuitBreakerBookCacheIT",
				"-Dcoherence.clusterport=" + port, "-Dcoherence.localhost=127.0.0.1", "-Dcoherence.wka=127.0.0.1",
				"-Dcoherence.ttl=0", "-Djava.net.preferIPv4Stack=true"));
		String mode = System.getProperty("coherence.security.mode");
		if (mode != null) {
			command.add("-Dcoherence.security.mode=" + mode);
		}
		command.addAll(properties);
		command.addAll(List.of("-cp", classpath, main));
		command.addAll(args);
		Files.writeString(log.resolveSibling(log.getFileName() + ".command"), String.join("\n", command));
		return new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
	}

	private static void stop(Process process) throws InterruptedException {
		process.destroy();
		if (!process.waitFor(10, TimeUnit.SECONDS)) {
			process.destroyForcibly();
			assertThat(process.waitFor(10, TimeUnit.SECONDS)).isTrue();
		}
	}

	private static void extract(Path archive, Path destination) throws Exception {
		assertThat(archive).isRegularFile();
		try (JarFile jar = new JarFile(archive.toFile())) {
			var entries = jar.entries();
			while (entries.hasMoreElements()) {
				var entry = entries.nextElement();
				Path target = destination.resolve(entry.getName());
				if (entry.isDirectory()) {
					Files.createDirectories(target);
				}
				else {
					Files.createDirectories(target.getParent());
					try (InputStream input = jar.getInputStream(entry)) {
						Files.copy(input, target);
					}
				}
			}
		}
	}

	private static void copyTree(Path source, Path destination) throws Exception {
		try (var paths = Files.walk(source)) {
			for (Path path : paths.toList()) {
				Path target = destination.resolve(source.relativize(path));
				if (Files.isDirectory(path)) {
					Files.createDirectories(target);
				}
				else {
					Files.copy(path, target);
				}
			}
		}
	}
}
