/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import com.tangosol.coherence.config.ParameterMacroExpressionParser;
import com.tangosol.config.expression.NullParameterResolver;
import com.tangosol.config.expression.ParameterResolver;

import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateAwareExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * A {@link org.springframework.beans.factory.config.BeanExpressionResolver}
 * implementation that bridges Coherence configuration concepts with Spring
 * configuration concepts. Ultimately this class and it's children support
 * the ability to reference Coherence parameter macros within a spring
 * application context file.
 *
 * @author Harvey Raja
 * @author Gunnar Hillert
 *
 * @see SpringNamespaceHandler
 */
public class CoherenceBeanExpressionResolver extends StandardBeanExpressionResolver {
	// ----- data members ---------------------------------------------------

	/**
	 * A thread local {@link ParameterResolver} used to hold the context-sensitive {@link ParameterResolver} based on
	 * the bean's reference in the cache configuration file.
	 * <p>
	 * This member is thread local to avoid a context bleeding when used by
	 * multiple threads such as when a backing map is created in parallel to
	 * a service being processed.
	 */
	private final ThreadLocal<ParameterResolver> m_tlResolver = new ThreadLocal<ParameterResolver>() {

		/** {@inheritDoc } **/
		@Override
		protected ParameterResolver initialValue() {
			return new NullParameterResolver();
		}
	};


	// ----- constructors ---------------------------------------------------

	/**
	 * Creates a CoherenceBeanExpressionResolver instance.
	 * @param exprParser the ExpressionParser
	 */
	public CoherenceBeanExpressionResolver(com.tangosol.config.expression.ExpressionParser exprParser) {
		super();
		setExpressionParser(new CoherenceExpressionParser(exprParser));
	}


	// ----- StandardBeanExpressionResolver methods -------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void customizeEvaluationContext(StandardEvaluationContext evalContext) {
		evalContext.setVariable("resolver", getResolver());
	}

	/**
	 * Returns a thread local instance of a {@link ParameterResolver}.
	 * @return a context sensitive {@link ParameterResolver}
	 */
	public ParameterResolver getResolver() {
		return this.m_tlResolver.get();
	}

	/**
	 * Set the thread local {@link ParameterResolver}.
	 * @param resolver  resolver to use to determine variables
	 */
	public void setParameterResolver(ParameterResolver resolver) {
		this.m_tlResolver.set(resolver);
	}

	/**
	 * Cleaned up {@link ThreadLocal} ParameterResolver when no longer used.
	 */
	public void cleanupParameterResolver() {
		this.m_tlResolver.remove();
	}

	/**
	 * A CoherenceExpressionParser determines whether a string expression can
	 * be processed by a Coherence {@link ParameterMacroExpressionParser}.
	 * If not it will be delegated to the {@link ExpressionParser} this class
	 * is initialized with.
	 */
	private class CoherenceExpressionParser extends TemplateAwareExpressionParser {
		// ----- data members -----------------------------------------------

		/**
		 * The {@link com.tangosol.config.expression.ExpressionParser}
		 * used to parse the string expression.
		 */
		private com.tangosol.config.expression.ExpressionParser m_exprParserCoh;

		/**
		 * The Spring {@link ExpressionParser} used when the Coherence
		 * {@link com.tangosol.config.expression.ExpressionParser} is not
		 * applicable.
		 */
		private ExpressionParser m_exprParserSpring;

		// ----- constructors -----------------------------------------------

		/**
		 * Creates a CoherenceExpressionParser with a
		 * {@link SpelExpressionParser}.
		 * @param exprParserCoh the used Coherence ExpressionParser
		 */
		CoherenceExpressionParser(com.tangosol.config.expression.ExpressionParser exprParserCoh) {
			this(exprParserCoh, new SpelExpressionParser());
		}

		/**
		 * Creates a CoherenceExpressionParser with the provided
		 * {@link ExpressionParser}.
		 * @param exprParserCoh the used Coherence ExpressionParser
		 * @param exprParserSpring the used Spring ExpressionParser
		 */
		CoherenceExpressionParser(com.tangosol.config.expression.ExpressionParser exprParserCoh,
				ExpressionParser exprParserSpring) {
			this.m_exprParserCoh = exprParserCoh;
			this.m_exprParserSpring = exprParserSpring;
		}

		// ----- TemplateAwareExpressionParser methods ----------------------

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Expression doParseExpression(String sExpression, ParserContext context) throws ParseException {
			sExpression = (sExpression != null) ? sExpression.trim() : "";

			return new DelegatingExpression("{" + sExpression + "}",
											this.m_exprParserSpring.parseExpression("#{" + sExpression + "}", context));
		}

		/**
		 * An {@link Expression} implementation that delegates expression
		 * evaluation to a Coherence
		 * {@link com.tangosol.config.expression.ExpressionParser}.
		 */
		private class DelegatingExpression implements Expression {
			// ----- data members -------------------------------------------

			/**
			 * The string expression used to derive some object.
			 */
			private String m_sExpression;

			/**
			 * The original spring expression evaluated if we are unsuccessful
			 * in evaluation.
			 */
			private Expression m_exprSpring;

			// ----- constructors -------------------------------------------

			/**
			 * Creates DelegatingExpression instance witch the Coherence
			 * {@link com.tangosol.config.expression.ExpressionParser} and
			 * teh string expression.
			 * @param sExpression the string expression to use
			 * @param exprSpring the original spring expression
			 */
			DelegatingExpression(String sExpression, Expression exprSpring) {
				this.m_sExpression = sExpression;
				this.m_exprSpring  = exprSpring;
			}

			// ----- Expression methods -------------------------------------

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Object getValue() throws EvaluationException {
				Object oValue = evaluate(Object.class);

				if (oValue == null) {
					oValue = this.m_exprSpring.getValue();
				}

				return oValue;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Object getValue(Object rootObject) throws EvaluationException {
				Object oValue = evaluate(Object.class);

				if (oValue == null) {
					oValue = this.m_exprSpring.getValue(rootObject);
				}

				return oValue;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public <T> T getValue(Class<T> desiredResultType) throws EvaluationException {
				T value = evaluate(desiredResultType);

				if (value == null) {
					value = this.m_exprSpring.getValue(desiredResultType);
				}

				return value;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public <T> T getValue(Object   rootObject, Class<T> desiredResultType) throws EvaluationException {
				T value = evaluate(desiredResultType);

				if (value == null) {
					value = this.m_exprSpring.getValue(rootObject, desiredResultType);
				}

				return value;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Object getValue(EvaluationContext context) throws EvaluationException {
				Object oValue = evaluate(Object.class, getParamResolver(context));

				if (oValue == null) {
					oValue = this.m_exprSpring.getValue(context);
				}

				return oValue;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Object getValue(EvaluationContext context, Object rootObject) throws EvaluationException {
				Object oValue = evaluate(Object.class, getParamResolver(context));

				if (oValue == null) {
					oValue = this.m_exprSpring.getValue(context, rootObject);
				}

				return oValue;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public <T> T getValue(EvaluationContext context, Class<T> desiredResultType) throws EvaluationException {
				T value = evaluate(desiredResultType, getParamResolver(context));

				if (value == null) {
					value = this.m_exprSpring.getValue(context, desiredResultType);
				}

				return value;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public <T> T getValue(EvaluationContext context, Object rootObject, Class<T> desiredResultType)
					throws EvaluationException {
				T value = evaluate(desiredResultType, getParamResolver(context));

				if (value == null) {
					value = this.m_exprSpring.getValue(context, rootObject, desiredResultType);
				}

				return value;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Class getValueType() throws EvaluationException {
				return Object.class;
			}


			/**
			 * {@inheritDoc}
			 */
			@Override
			public Class getValueType(Object rootObject) throws EvaluationException {
				return getValueType();
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Class getValueType(EvaluationContext context) throws EvaluationException {
				return getValueType();
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Class getValueType(EvaluationContext context, Object rootObject) throws EvaluationException {
				return getValueType();
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public TypeDescriptor getValueTypeDescriptor() throws EvaluationException {
				return TypeDescriptor.valueOf(Object.class);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public TypeDescriptor getValueTypeDescriptor(Object rootObject) throws EvaluationException {
				return getValueTypeDescriptor();
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public TypeDescriptor getValueTypeDescriptor(EvaluationContext context) throws EvaluationException {
				return getValueTypeDescriptor();
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public TypeDescriptor getValueTypeDescriptor(EvaluationContext context, Object rootObject)
					throws EvaluationException {
				return getValueTypeDescriptor();
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean isWritable(EvaluationContext context) throws EvaluationException {
				return false;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean isWritable(EvaluationContext context, Object rootObject) throws EvaluationException {
				return false;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean isWritable(Object rootObject) throws EvaluationException {
				return false;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void setValue(EvaluationContext context, Object value) throws EvaluationException {
				return;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void setValue(Object rootObject, Object value) throws EvaluationException {
				return;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void setValue(EvaluationContext context, Object rootObject, Object value) throws EvaluationException {
				return;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public String getExpressionString() {
				return this.m_sExpression;
			}

			// ----- helpers ------------------------------------------------

			/**
			 * Return the thread local cached resolver and if not present
			 * use the provided {@link EvaluationContext} to determine whether
			 * a {@link ParameterResolver} is accessible, with the well known
			 * reference {@literal "resolver"}. The thread local cached resolver
			 * takes precedence as it is considered to be more correct than
			 * the set-once EvaluationContext resolver.
			 * @param ctx  the context that holds the appropriate
			 *             {@link ParameterResolver}
			 * @return either a {@link ParameterResolver} on the context or
			 *         a thread local version
			 */
			protected ParameterResolver getParamResolver(EvaluationContext ctx) {
				ParameterResolver resolver = getResolver();

				if (resolver == null) {
					Object oResolver = ctx.lookupVariable("resolver");

					if (oResolver instanceof ParameterResolver) {
						resolver = (ParameterResolver) oResolver;
					}
				}

				return resolver;
			}

			/**
			 * Based on the known string expression create an instance of the
			 * destined type.
			 * @param clzDestined  the type of object parsing the string
			 *                     should resolve
			 * @param <T>          the instance type to return
			 * @return an instance of type {@link T} created from resolving
			 *         the string expression
			 */
			protected <T> T evaluate(Class<T> clzDestined) {
				return evaluate(clzDestined, getResolver());
			}

			/**
			 * Based on the known string expression and the
			 * {@link ParameterResolver} create an instance of the destined
			 * type.
			 * @param clzDestined  the type of object parsing the string
			 *                     should resolve
			 * @param resolver the parameter resolver used by the expression
			 * @param <T>          the instance type to return
			 * @return an instance of type {@link T} created from resolving
			 *         the string expression
			 */
			protected <T> T evaluate(Class<T> clzDestined, ParameterResolver resolver) {
				try {
					return CoherenceExpressionParser.this.m_exprParserCoh.parse(this.m_sExpression, clzDestined).evaluate(resolver);
				}
				catch (Throwable throwable) {
				}

				return null;
			}
		}
	}
}
