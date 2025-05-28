/*
 * Copyright (c) 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.tangosol.net.NamedMap;
import com.tangosol.util.Base;

import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link org.springframework.aop.TargetSource} implementation that lazily creates
 * a Coherence cache or map.
 *
 * @author Vaso Putica
 * @since 4.3.1
 */
class LazyTargetSource implements TargetSource, Serializable {
	private final DefaultListableBeanFactory beanFactory;
	private final InjectionPoint injectionPoint;
	private final String originalFactoryBeanName;
	private final String originalFactoryMethodName;
	private final boolean isAsyncCache;
	private final Object lock = new Object();
	private transient Object cachedTarget;

	LazyTargetSource(DefaultListableBeanFactory beanFactory, InjectionPoint injectionPoint,
			String originalFactoryBeanName, String originalFactoryMethodName, boolean isAsyncCache) {
		this.beanFactory = beanFactory;
		this.injectionPoint = injectionPoint;
		this.originalFactoryBeanName = originalFactoryBeanName;
		this.originalFactoryMethodName = originalFactoryMethodName;
		this.isAsyncCache = isAsyncCache;
	}

	@Override
	public Class<?> getTargetClass() {
		try {
			Object factoryBean = this.beanFactory.getBean(this.originalFactoryBeanName);
			Method method = ReflectionUtils.findMethod(factoryBean.getClass(), this.originalFactoryMethodName, InjectionPoint.class);
			if (method != null) {
				return method.getReturnType();
			}
		}
		catch (Exception ex) {
			throw Base.ensureRuntimeException(ex);
		}
		throw new IllegalStateException(String.format("Original factory method '%s' not found on bean '%s' with InjectionPoint parameter.",
				this.originalFactoryMethodName,
				this.originalFactoryBeanName));
	}

	@Override
	public Object getTarget() throws Exception {
		Object cachedTarget = this.cachedTarget;
		if (cachedTarget == null) {
			synchronized (this.lock) {
				cachedTarget = this.cachedTarget;
				if (cachedTarget == null) {
					Object factoryBean = this.beanFactory.getBean(this.originalFactoryBeanName);
					Method factoryMethod = ReflectionUtils.findMethod(factoryBean.getClass(), this.originalFactoryMethodName, InjectionPoint.class);
					if (factoryMethod == null) {
						throw new IllegalStateException(String.format("Original factory method '%s' not found on bean '%s' with InjectionPoint parameter.",
								this.originalFactoryMethodName,
								this.originalFactoryBeanName));
					}
					cachedTarget = factoryMethod.invoke(factoryBean, this.injectionPoint);
					if (this.isAsyncCache) {
						cachedTarget = ((NamedMap) cachedTarget).async();
					}
					this.cachedTarget = cachedTarget;
				}
			}
		}
		return cachedTarget;
	}

	@Override
	public boolean isStatic() {
		// Returning false even though we're always returning the same object,
		// because when isStatic is true, CglibAopProxy fetches the target during
		// proxy creation, thus defeating the purpose of using this TargetSource.
		return false;
	}
}
