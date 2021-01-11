/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.namespace;

import com.tangosol.config.xml.AbstractNamespaceHandler;

/**
 * Custom namespace handler for the {@code micronaut} namespace.
 * <p>
 * This namespace handler supports only one XML element:
 * <ul>
 * <li><pre>{@code &lt;cdi:bean&gt;beanName&lt;/cdi:bean&gt;}</pre>, where {@code beanName}
 * is the unique name of a Spring bean.
 * This element can only be used as a child of the standard
 * {@code <instance>} element.
 * </ul>
 *
 * @author Ryan Lubke
 * @since 3.0
 */
public class NamespaceHandler extends AbstractNamespaceHandler {

	/**
	 * Construct a {@code MicronautNamespaceHandler} instance.
	 */
	public NamespaceHandler() {
		registerProcessor("bean", new BeanProcessor());
	}
}
