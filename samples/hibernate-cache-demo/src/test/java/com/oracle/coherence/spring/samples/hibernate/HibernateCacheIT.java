/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.hibernate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.jar.JarFile;

import com.oracle.coherence.spring.samples.cacheprobe.HibernateCacheProbe;
import com.oracle.coherence.spring.samples.cachetests.PackagedCacheSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Requires actual second-level and query hits; a provider incompatibility fails this test. */
class HibernateCacheIT extends PackagedCacheSupport {

	@Test
	void readsEntityAndQueryCachesAcrossMembers() throws Exception {
		Path output = Path.of("target/hibernate-cache-paths").toAbsolutePath();
		Files.createDirectories(output);
		Path work = Files.createTempDirectory(output, "run-");
		try {
			Path root = extract(Path.of(System.getProperty("sample.app.archive")), work.resolve("app"));
			Path probes = probes(HibernateCacheProbe.class, work.resolve("probes"));
			String cp = classpath(root, probes);
			Path provider;
			try (var libraries = Files.list(root.resolve("BOOT-INF/lib"))) {
				provider = libraries.filter((path) -> path.getFileName().toString().startsWith("coherence-hibernate-cache-core-")).findFirst().orElseThrow();
			}
			String xml;
			try (JarFile jar = new JarFile(provider.toFile())) {
				xml = new String(jar.getInputStream(jar.getJarEntry("hibernate-second-level-cache-config.xml")).readAllBytes(),
						java.nio.charset.StandardCharsets.UTF_8);
			}
			assertThat(xml).contains("<local-storage>true</local-storage>");
			Path client = work.resolve("client.xml");
			Files.writeString(client, xml.replace("<local-storage>true</local-storage>", "<local-storage>false</local-storage>"));
			var common = cluster("HibernateCacheIT", port());
			var storageCommand = new ArrayList<>(common);
			storageCommand.add("-Dcoherence.cacheconfig=hibernate-second-level-cache-config.xml");
			Path storageLog = output.resolve("storage.log");
			Process storage = launch(storageLog, command(storageCommand, cp, HibernateCacheProbe.class.getName(), "storage"));
			try {
				started(storage, storageLog, "Started Coherence server");
				var appCommand = new ArrayList<>(common);
				appCommand.add("-Dcoherence.distributed.localstorage=false");
				Path appLog = output.resolve("app.log");
				Process app = launch(appLog, command(appCommand, cp, HibernateCacheProbe.class.getName(), "app",
						"--server.port=0", "--spring.jpa.properties.com.oracle.coherence.hibernate.cache.cache_config_file_path=" + client));
				try {
					completed(app, appLog, "HIBERNATE_CACHE_PATH_PASS");
				}
				finally {
					stop(app);
				}
			}
			finally {
				stop(storage);
			}
		}
		finally {
			remove(work);
		}
	}
}
