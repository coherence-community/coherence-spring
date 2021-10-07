/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import com.oracle.coherence.spring.configuration.support.CoherenceGenericConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterRegistry;

/**
 * {@link BeanFactoryPostProcessor} that adds the {@link CoherenceGenericConverter} to the {@link ConversionService} if
 * available.
 *
 * @author Gunnar Hillert
 * @since 3.0.1
 */
public class CoherenceConversionServicePostProcessor implements BeanFactoryPostProcessor {

	protected static final Log logger = LogFactory.getLog(CoherenceConversionServicePostProcessor.class);

	private CoherenceGenericConverter coherenceGenericConverter = new CoherenceGenericConverter();

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (hasUserDefinedConversionService(beanFactory)) {
			if (logger.isWarnEnabled()) {
				logger.warn("Detected user-defined ConversionService, please ensure to manually add the CoherenceGenericConverter.");
			}
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving ConversionService from BeanFactory.");
		}
		final ConversionService service = beanFactory.getConversionService();
		if (service == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("ConversionService is null. Consider providing a ConversionService and add the CoherenceGenericConverter.");
			}
		}
		else if (service instanceof ConverterRegistry) {
			if (logger.isInfoEnabled()) {
				logger.info("Adding CoherenceGenericConverter to existing ConversionService.");
			}
			final ConverterRegistry registry = (ConverterRegistry) service;
			registry.addConverter(this.coherenceGenericConverter);
		}
		else {
			if (logger.isWarnEnabled()) {
				logger.warn("ConversionService does not implement ConverterRegistry. Unable to add CoherenceGenericConverter.");
			}
		}
	}

	private boolean hasUserDefinedConversionService(ConfigurableListableBeanFactory beanFactory) {
		return beanFactory.containsBean(AbstractApplicationContext.CONVERSION_SERVICE_BEAN_NAME)
				&& beanFactory.isTypeMatch(AbstractApplicationContext.CONVERSION_SERVICE_BEAN_NAME, ConversionService.class);
	}

}
