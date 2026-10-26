/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.oracle.coherence.spring.samples.cacheprobe.CloudConfigCacheProbe;
import com.oracle.coherence.spring.samples.cachetests.PackagedCacheSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Actual Config Server and packaged app; each policy variant runs in fresh JVMs. */
class CloudConfigCacheIT extends PackagedCacheSupport {

	@Test
	void readsEventCacheAndChecksEachPermissionWithLocalConfiguration() throws Exception {
		Path output = Path.of("target/cloud-config-cache-paths").toAbsolutePath();
		Files.createDirectories(output);
		Path work = Files.createTempDirectory(output, "run-");
		try {
			Path root = extract(Path.of(System.getProperty("sample.app.archive")), work.resolve("app"));
			Path probes = probes(CloudConfigCacheProbe.class, work.resolve("probes"));
			Path source = Path.of(CoherenceApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			assertThat(Files.readAllBytes(root.resolve(POLICY))).isEqualTo(Files.readAllBytes(source.resolve(POLICY)));
			assertThat(Files.exists(root.resolve("BOOT-INF/classes/" + POLICY))).isFalse();
			String policy = Files.readString(root.resolve(POLICY));
			assertThat(policy).doesNotContain("executable=", "*");
			Path fixture = work.resolve("config");
			Files.createDirectories(fixture);
			Files.writeString(fixture.resolve("config-client.properties"),
					"cache.it.marker=isolated-config-server\ncoherence.logging.severity-level=6\n"
					+ "coherence.member=CloudConfigCacheIT\n");
			int configPort = port();
			Path configLog = output.resolve("config-server.log");
			Path configRoot = extract(Path.of(System.getProperty("sample.config.archive")), work.resolve("server"));
			List<String> configMode = new ArrayList<>();
			if (System.getProperty("coherence.security.mode") != null) {
				configMode.add("-Dcoherence.security.mode=" + System.getProperty("coherence.security.mode"));
			}
			Process config = launch(configLog, command(configMode, productionClasspath(configRoot),
					"com.oracle.coherence.spring.config.SpringCloudConfigServer",
					"--server.port=" + configPort, "--spring.profiles.active=native",
					"--spring.cloud.config.server.native.search-locations=" + fixture.toUri()));
			try {
				started(config, configLog, "Started SpringCloudConfigServer");
				Path appConfig = work.resolve("application.yml");
				String original = Files.readString(root.resolve("BOOT-INF/classes/application.yml"));
				assertThat(original).contains("optional:configserver:http://localhost:8888");
				Files.writeString(appConfig, original.replace("optional:configserver:http://localhost:8888",
						"configserver:http://coherence:rocks@127.0.0.1:" + configPort));
				for (String scenario : List.of("production", "Event", "PersistentSet", "AbstractPersistentCollection", "Timestamp")) {
					Path appRoot = root;
					if (!scenario.equals("production")) {
						appRoot = work.resolve(scenario);
						copy(root, appRoot);
						String line = policy.lines().filter((value) -> value.contains("." + scenario + "\"")).findFirst().orElseThrow();
						Files.writeString(appRoot.resolve(POLICY), policy.replace(line, ""));
					}
					String cp = classpath(appRoot, probes);
					var common = cluster("CloudConfigCacheIT", port());
					Path storageLog = output.resolve(scenario + "-storage.log");
					Process storage = launch(storageLog, command(common, cp, CloudConfigCacheProbe.class.getName(), "storage", scenario));
					try {
						started(storage, storageLog, "Started Coherence server");
						var appCommand = new ArrayList<>(common);
						appCommand.add("-Dcoherence.distributed.localstorage=false");
						Path appLog = output.resolve(scenario + "-app.log");
						Process app = launch(appLog, command(appCommand, cp, CloudConfigCacheProbe.class.getName(), "app", scenario,
								"--server.port=0", "--spring.config.location=" + appConfig.toUri(),
								"--spring.jpa.properties.hibernate.generate_statistics=true"));
						try {
							completed(app, appLog, "CLOUD_CACHE_PATH_PASS scenario=" + scenario);
							if (!scenario.equals("production") && Files.readString(appLog).contains("hardened=true")) {
									String denied = switch (scenario) {
										case "Event" -> "com.oracle.coherence.spring.demo.model.Event";
										case "Timestamp" -> "java.sql.Timestamp";
										default -> "org.hibernate.collection.spi." + scenario;
									};
								assertThat(Files.readString(appLog)).contains("denied-class=" + denied);
							}
						}
						finally {
							stop(app);
						}
					}
					finally {
						stop(storage);
					}
				}
			}
			finally {
				stop(config);
			}
		}
		finally {
			remove(work);
		}
	}
}
