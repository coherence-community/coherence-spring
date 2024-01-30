package com.oracle.coherence.spring.boot.autoconfigure.data;

import java.lang.annotation.Annotation;
import java.util.Locale;

import com.oracle.coherence.spring.data.config.EnableCoherenceRepositories;
import com.oracle.coherence.spring.data.support.CoherenceRepositoryConfigurationExtension;

import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.context.annotation.ImportBeanDefinitionRegistrar} used to auto-configure Coherence
 * Repositories for Spring Data.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceRepositoriesRegistrar extends AbstractRepositoryConfigurationSourceSupport {
	private BootstrapMode bootstrapMode = null;

	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableCoherenceRepositories.class;
	}

	@Override
	protected Class<?> getConfiguration() {
		return EnableCoherenceRepositoriesConfiguration.class;
	}

	@Override
	protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
		return new CoherenceRepositoryConfigurationExtension();
	}

	@Override
	protected BootstrapMode getBootstrapMode() {
		return (this.bootstrapMode == null) ? BootstrapMode.DEFAULT : this.bootstrapMode;
	}

	@Override
	public void setEnvironment(Environment environment) {
		super.setEnvironment(environment);
		configureBootstrapMode(environment);
	}

	private void configureBootstrapMode(Environment environment) {
		String property = environment.getProperty("coherence.spring.data.repositories.bootstrap-mode");
		if (StringUtils.hasText(property)) {
			this.bootstrapMode = BootstrapMode.valueOf(property.toUpperCase(Locale.ENGLISH));
		}
	}

	@EnableCoherenceRepositories
	private static final class EnableCoherenceRepositoriesConfiguration {

	}
}
