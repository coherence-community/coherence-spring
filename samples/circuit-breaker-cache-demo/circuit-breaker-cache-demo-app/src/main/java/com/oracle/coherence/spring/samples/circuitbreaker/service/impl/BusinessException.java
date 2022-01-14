/*
 * Copyright (c) 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.circuitbreaker.service.impl;

/**
 * Simple business exception.
 * @author Gunnar Hillert
 */
public class BusinessException extends RuntimeException {

	public BusinessException(String s) {
		super(s);
	}
}
