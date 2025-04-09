package com.oracle.coherence.spring.tracing;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.oracle.coherence.spring.CoherenceContext;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * A {@link PropertySource} implementation that exposes Spring's configuration to the
 * {@code Coherence} {@code OpenTelemetry} integration.
 *
 * @author Ryan Lubke
 */
public class PropertySource implements com.tangosol.internal.tracing.PropertySource {

	public Map<String, String> getProperties() {
		ApplicationContext applicationContext = CoherenceContext.getApplicationContext();
		ConfigurableEnvironment environment = (ConfigurableEnvironment) applicationContext.getEnvironment();

		Map<String, String> otelProps = StreamSupport.stream(environment.getPropertySources().spliterator(), false)
			.filter((propertySource) -> propertySource.getSource() instanceof Map)
			.flatMap((propertySource) -> {
				@SuppressWarnings("unchecked")
				Map<String, Object> sourceMap = (Map<String, Object>) propertySource.getSource();
				return sourceMap.entrySet()
					.stream()
					.filter((entry) -> entry.getKey().startsWith("otel."))
					.map((entry) -> Map.entry(entry.getKey(), entry.getValue().toString()));
			})
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Add the service name manually
		if (!otelProps.containsKey("otel.service.name")) {
			otelProps.put("otel.service.name", environment.getProperty("spring.application.name", "spring.coherence"));
		}


		return otelProps;
	}
}
