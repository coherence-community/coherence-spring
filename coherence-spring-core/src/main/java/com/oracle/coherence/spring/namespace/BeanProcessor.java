/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.namespace;

import com.oracle.coherence.spring.CoherenceContext;

import com.tangosol.config.ConfigurationException;
import com.tangosol.config.xml.ElementProcessor;
import com.tangosol.config.xml.ProcessingContext;
import com.tangosol.run.xml.XmlElement;

import org.springframework.context.ApplicationContext;

import java.util.function.Supplier;

/**
 * Element processor for {@code <spring:bean/>} XML element.
 *
 * @author Jonathan Knight
 * @author rl
 * @since 3.0
 */
public class BeanProcessor implements ElementProcessor<BeanBuilder> {


	/**
	 * The {@link Supplier} used to provide an instance of {@link ApplicationContext}.
	 */
	private final Supplier<ApplicationContext> contextSupplier;


	/**
	 * The default constructor used by the Coherence XML processor.
	 */
	BeanProcessor() {
		this(null);
	}

	/**
	 * Create a {@code BeanProcessor}.
	 * @param contextSupplier The {@link java.util.function.Supplier} used to provide an
	 * instance of {@link ApplicationContext}
	 */
	BeanProcessor(Supplier<ApplicationContext> contextSupplier) {
		this.contextSupplier = contextSupplier == null
				? CoherenceContext::getApplicationContext
				: contextSupplier;
	}

	@Override
	public BeanBuilder process(final ProcessingContext processingContext,
	                           final XmlElement xmlElement)
			throws ConfigurationException {
		return processingContext.inject(new BeanBuilder(contextSupplier.get(),
				xmlElement.getString()), xmlElement);
	}
}
