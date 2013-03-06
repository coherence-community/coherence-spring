/*
 * File: SpringNamespaceHandler.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.coherence.spring;

import com.tangosol.coherence.config.ParameterList;
import com.tangosol.coherence.config.SimpleParameterList;

import com.tangosol.coherence.config.builder.ParameterizedBuilder;

import com.tangosol.config.ConfigurationException;

import com.tangosol.config.annotation.Injectable;

import com.tangosol.config.expression.Expression;
import com.tangosol.config.expression.ExpressionParser;
import com.tangosol.config.expression.Parameter;
import com.tangosol.config.expression.ParameterResolver;

import com.tangosol.config.xml.AbstractNamespaceHandler;
import com.tangosol.config.xml.ElementProcessor;
import com.tangosol.config.xml.ProcessingContext;
import com.tangosol.config.xml.XmlSimpleName;

import com.tangosol.run.xml.QualifiedName;
import com.tangosol.run.xml.XmlElement;

import com.tangosol.util.Base;
import com.tangosol.util.ClassHelper;
import com.tangosol.util.RegistrationBehavior;
import com.tangosol.util.ResourceRegistry;
import com.tangosol.util.Resources;

import org.springframework.beans.BeansException;

import org.springframework.beans.factory.BeanFactory;

import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import org.springframework.context.ConfigurableApplicationContext;

import org.springframework.context.support.FileSystemXmlApplicationContext;

import static com.tangosol.util.BuilderHelper.using;

import java.net.URL;

import java.text.ParseException;

import java.util.Arrays;
import java.util.List;

/**
 * The {@link SpringNamespaceHandler} provides the ability to reference Spring beans in a
 * cache configuration file. In cases where a cache configuration needs to specify
 * an externally provided user class via a {@literal <class-scheme>} or
 * {@literal <instance>} element, this namespace handler can provide a Spring
 * bean from a BeanFactory.
 * <p>
 * In order to use this handler in a cache configuration file, the handler must
 * be declared. This is done via the following declaration:
 *
 * <pre>
 * xmlns:spring="class://com.oracle.coherence.spring.SpringNamespaceHandler"
 * </pre>
 *
 * To guard against invalid configuration, the XSD (coherence-spring-config.xsd)
 * that corresponds to this namespace handler can also be referenced.
 * <p>
 * Here is a complete example:
 *
 * <pre>
 * &lt;cache-config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *   xmlns="http://xmlns.oracle.com/coherence/coherence-cache-config"
 *   xmlns:spring="class://com.oracle.coherence.spring.SpringNamespaceHandler"
 *   xsi:schemaLocation="http://xmlns.oracle.com/coherence/coherence-cache-config coherence-cache-config.xsd
 *   class://com.oracle.coherence.spring.SpringNamespaceHandler coherence-spring-config.xsd"&gt;
 * </pre>
 *
 * To specify a bean, use the {@literal <bean>} and {@literal <bean-name>} elements under
 * {@literal <class-scheme>} or {@literal <instance>} elements:
 *
 * <pre>
 * &lt;class-scheme&gt;
 *   &lt;spring:bean&gt;
 *     &lt;spring:bean-name&gt;listener&lt;/spring:bean-name&gt;
 *   &lt;/spring:bean&gt;
 * &lt;/class-scheme&gt;
 * </pre>
 *
 * Cache configuration properties (including cache configuration macros) can
 * be injected into a bean using two possible mechanisms: push or pull. The
 * push mechanism allows the runtime macro value to be injected into the bean
 * by this {@link com.tangosol.config.xml.NamespaceHandler} such as:
 *
 * <pre>
 * &lt;spring:bean&gt;
 *   &lt;spring:bean-name&gt;listener&lt;/spring:bean-name&gt;
 *   &lt;spring:property name="backingMapManagerContext"&gt;{manager-context}&lt;/spring:property&gt;
 * &lt;/spring:bean&gt;
 * </pre>
 *
 * The pull mechanism allows for cache configuration properties to be
 * referenced in the Spring application context file such as:
 *
 * <pre>
 * &lt;bean id="listener" class="foo.BML" lazy-init="true"&gt;
 *   &lt;property name="backingMapManagerContext" value="#{manager-context}"/&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * A bean factory that will provide the Spring beans can be specified in two ways:
 * <p>
 * <strong>1. Specify an application context in the cache configuration file:</strong>
 * <pre>
 * &lt;spring:bean-factory&gt;
 *   &lt;spring:application-context-uri&gt;application-context.xml&lt;/spring:application-context-uri&gt;
 * &lt;/spring:bean-factory&gt;
 * </pre>
 * Similar to a cache configuration file, the application context file will be loaded either via the file
 * system or the classpath. URLs are supported.
 * <p>
 * <strong>2. Specify a bean factory as a resource:</strong>
 * <p>
 * A Spring bean factory can be manually registered as a resource in the following manner:
 *
 * <pre>
 * ConfigurableCacheFactory factory = CacheFactory.getCacheFactoryBuilder().
 *         getConfigurableCacheFactory(...);
 *
 * factory.getResourceRegistry().registerResource(BeanFactory.class,  // type
 *         SpringNamespaceHandler.DEFAULT_FACTORY_NAME,               // resource name
 *         factory,                                                   // factory reference
 *         null);                                                     // optional ResourceLifecycleObserver
 * </pre>
 *
 * If a resource name other than {@link #DEFAULT_FACTORY_NAME} is specified,
 * that name can be referenced in the bean element:
 *
 * <pre>
 * &lt;spring:bean&gt;
 *   &lt;spring:factory-name&gt;custom-factory&lt;/spring:factory-name&gt;
 *   &lt;spring:bean-name&gt;listener&lt;/spring:bean-name&gt;
 * &lt;/spring:bean&gt;
 * </pre>
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Patrick Peralta
 */
public class SpringNamespaceHandler extends AbstractNamespaceHandler
{
    // ----- constants ------------------------------------------------------

    /**
     * Default {@link BeanFactory} name.
     */
    public static final String DEFAULT_FACTORY_NAME = "default";


    // ----- constructors ---------------------------------------------------

    /**
     * Construct a {@link SpringNamespaceHandler}.
     */
    public SpringNamespaceHandler()
    {
        registerProcessor("bean-factory", new ElementProcessor<Void>()
        {
            @Override
            public Void process(ProcessingContext context,
                                XmlElement        element) throws ConfigurationException
            {
                ResourceRegistry registry = context.getResourceRegistry();
                SpringBeanFactoryBuilder bldr = context.inject(new SpringBeanFactoryBuilder(registry,
                                                                                            context.getExpressionParser()),
                                                               element);

                registry.registerResource(SpringBeanFactoryBuilder.class,
                                          getFactoryNameAsString(bldr.getFactoryName(),
                                                                 context.getDefaultParameterResolver()),
                                          bldr);

                return null;
            }
        });

        registerProcessor("bean", new ElementProcessor<SpringBeanBuilder>()
        {
            @SuppressWarnings("unchecked")
            @Override
            public SpringBeanBuilder process(ProcessingContext context,
                                             XmlElement        element) throws ConfigurationException
            {
                SpringBeanBuilder bldr = new SpringBeanBuilder(context.getResourceRegistry());

                context.inject(bldr, element);

                SimpleParameterList listParam = new SimpleParameterList();
                String              sPrefix   = element.getQualifiedName().getPrefix();

                for (XmlElement e : (List<XmlElement>) element.getElementList())
                {
                    QualifiedName qName = e.getQualifiedName();

                    if (Base.equals(sPrefix, qName.getPrefix()) && qName.getLocalName().equals("property"))
                    {
                        listParam.add(context.processElement(e));
                    }
                }

                bldr.setParameterList(listParam);

                return bldr;
            }
        });
    }


    // ----- helper methods --------------------------------------------------

    /**
     * Return the factory name produced by the provided expression, or
     * {@link SpringNamespaceHandler#DEFAULT_FACTORY_NAME} if the expression is null.
     *
     * @param exprFactoryName  the expression containing the {@link BeanFactory} name
     * @param resolver         the {@link ParameterResolver} to use for resolving
     *                         factory names
     *
     * @return factory name for the {@link BeanFactory}
     */
    protected static String getFactoryNameAsString(Expression<String> exprFactoryName,
                                                   ParameterResolver  resolver)
    {
        return exprFactoryName == null ? DEFAULT_FACTORY_NAME : exprFactoryName.evaluate(resolver);
    }


    // ----- inner class PropertyProcessor ----------------------------------

    /**
     * Implementation of {@link ElementProcessor} that creates an instance
     * of {@link Parameter} containing a property name and value to be
     * injected into a Spring bean.
     */
    @XmlSimpleName("property")
    public static class PropertyProcessor implements ElementProcessor<Parameter>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public Parameter process(ProcessingContext context,
                                 XmlElement        element) throws ConfigurationException
        {
            String sName = context.getMandatoryProperty("name", String.class, element);

            try
            {
                return new Parameter(sName, context.getExpressionParser().parse(element.getString(), Object.class));
            }
            catch (ParseException e)
            {
                throw new ConfigurationException("Error processing <property> element",
                                                 "Ensure a valid value is present in <property> element",
                                                 e);
            }
        }
    }


    // ----- inner class SpringBeanBuilder -----------------------------------

    /**
     * Implementation of {@link ParameterizedBuilder} that "realizes" a named
     * bean from a Spring {@link BeanFactory}. The {@link BeanFactory} is
     * obtained via the following:
     * <ol>
     *     <li>The {@link BeanFactory} is looked up in the constructor
     *     provided {@link ResourceRegistry} via the factory name
     *     indicated by {@link #getFactoryName()}</li>
     *     <li>If the {@link BeanFactory} is not present in the registry,
     *     a {@link SpringBeanFactoryBuilder} via the same name will
     *     be looked up in the registry, and this builder will realize
     *     an instance of the {@link BeanFactory}</li>
     * </ol>
     */
    public static class SpringBeanBuilder implements ParameterizedBuilder<Object>,
                                                     ParameterizedBuilder.ReflectionSupport
    {
        // ----- data members -----------------------------------------------

        /**
         * The registry used to locate the {@link BeanFactory} used to realize the bean.
         */
        private ResourceRegistry m_registry;

        /**
         * The {@link Expression} for the {@link BeanFactory} name.
         */
        private Expression<String> m_exprFactoryName;

        /**
         * The {@link Expression} for the bean name.
         */
        private Expression<String> m_exprBeanName;

        /**
         * The {@link ParameterList} containing optional {@link Parameter}s to inject
         * into the realized bean.
         */
        private ParameterList m_listParameters;


        // ----- constructors -----------------------------------------------

        /**
         * Construct a {@link SpringBeanBuilder}.
         *
         * @param registry  the {@link ResourceRegistry} this builder will use
         *                  to locate the {@link BeanFactory} that will provide
         *                  the requested bean
         */
        public SpringBeanBuilder(ResourceRegistry registry)
        {
            m_registry = registry;
        }


        // ----- accessors --------------------------------------------------

        /**
         * Return an {@link Expression} used to determine the name of the
         * {@link BeanFactory} used by this builder.
         *
         * @return  an {@link Expression} for the {@link BeanFactory} name
         */
        public Expression<String> getFactoryName()
        {
            return m_exprFactoryName;
        }


        /**
         * Set the {@link Expression} used to determine the name of the
         * {@link BeanFactory} used by this builder.
         *
         * @param exprFactoryName  the {@link Expression} for the {@link BeanFactory} name
         */
        @Injectable
        public void setFactoryName(Expression<String> exprFactoryName)
        {
            m_exprFactoryName = exprFactoryName;
        }


        /**
         * Return an {@link Expression} used to determine the name of the
         * bean provided by this builder.
         *
         * @return  an {@link Expression} for the bean name
         */
        public Expression<String> getBeanName()
        {
            return m_exprBeanName;
        }


        /**
         * Set the {@link Expression} used to determine the name of the
         * bean provided by this builder.
         *
         * @param exprBeanName  an {@link Expression} for the bean name
         */
        @Injectable
        public void setBeanName(Expression<String> exprBeanName)
        {
            m_exprBeanName = exprBeanName;
        }


        /**
         * Return the {@link ParameterList} containing optional properties
         * to inject into the realized bean.
         *
         * @return the {@link ParameterList}
         */
        public ParameterList getParameterList()
        {
            return m_listParameters;
        }


        /**
         * Sets the {@link ParameterList} containing optional properties
         * to inject into the realized bean.
         *
         * @param listParameters  the {@link ParameterList}
         */
        public void setParameterList(ParameterList listParameters)
        {
            m_listParameters = listParameters;
        }


        // ----- ParameterizedBuilder interface------------------------------

        /**
         * {@inheritDoc}
         */
        @Override
        public Object realize(ParameterResolver resolver,
                              ClassLoader       loader,
                              ParameterList     listParameters)
        {
            String        sBeanName      = getBeanName().evaluate(resolver);
            Object        oBean          = ensureBeanFactory(resolver, loader).getBean(sBeanName);
            ParameterList listPropParams = listParameters == null ? m_listParameters : listParameters;

            if (listPropParams != null)
            {
                for (Parameter param : listPropParams)
                {
                    Object oValue    = param.evaluate(resolver).get();
                    String sProperty = param.getName();

                    if (sProperty == null || sProperty.isEmpty())
                    {
                        throw new ConfigurationException("Property element missing \"name\" attribute",
                                                         "Ensure that bean property elements have a \"name\" attribute "
                                                         + "(i.e. <property name=\"name\"> ");
                    }

                    String sMethod = "set" + Character.toUpperCase(sProperty.charAt(0)) + sProperty.substring(1);

                    try
                    {
                        ClassHelper.invoke(oBean, sMethod, new Object[] {oValue});
                    }
                    catch (Exception e)
                    {
                        throw new ConfigurationException(String.format("Could not invoke '%s' on bean '%s'", sMethod,
                                                                       sBeanName),
                                                         String
                                                         .format("Ensure that property '%s' contains a 'set' method on bean '%s'",
                                                             sProperty, sBeanName),
                                                         e);
                    }
                }
            }

            return oBean;
        }


        // ----- ParameterizedBuilder.ReflectionSupport interface -----------

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean realizes(Class<?>          clzClass,
                                ParameterResolver resolver,
                                ClassLoader       loader)
        {
            String      sBeanName = getBeanName().evaluate(resolver);
            BeanFactory factory   = ensureBeanFactory(resolver, loader);

            if (factory.containsBean(sBeanName))
            {
                return factory.isTypeMatch(sBeanName, clzClass);
            }
            else
            {
                String        sFactory  = getFactoryNameAsString(getFactoryName(), resolver);
                StringBuilder sbProblem = new StringBuilder();

                sbProblem.append("Spring bean '").append(sBeanName).append("' could not be loaded from bean factory '")
                    .append(sFactory).append("' (").append(factory).append(")");

                StringBuilder sbAdvice = new StringBuilder();

                sbAdvice.append("Ensure that a bean name of '").append(sBeanName).append("' exists in bean factory '")
                    .append(sFactory).append("'.");

                throw new ConfigurationException(sbProblem.toString(), sbAdvice.toString());
            }
        }


        // ----- helper methods ---------------------------------------------

        /**
         * Ensure the {@link BeanFactory} used by this builder to provide the bean indicated
         * via {@link #getBeanName()}.
         *
         * @param resolver  the {@link ParameterResolver} to use for resolving parameters
         * @param loader    the {@link ClassLoader} for loading any necessary classes
         *
         * @return  the {@link BeanFactory} used by this builder
         */
        protected BeanFactory ensureBeanFactory(ParameterResolver resolver,
                                                ClassLoader       loader)
        {
            ResourceRegistry registry     = m_registry;
            String           sFactoryName = getFactoryNameAsString(getFactoryName(), resolver);
            BeanFactory      factory      = registry.getResource(BeanFactory.class, sFactoryName);

            if (factory == null)
            {
                SpringBeanFactoryBuilder bldr = registry.getResource(SpringBeanFactoryBuilder.class, sFactoryName);

                if (bldr == null)
                {
                    throw new ConfigurationException(String.format("Could not locate bean factory '%s'", sFactoryName),
                                                     "Ensure that a bean factory is defined in the cache configuration file via"
                                                     + "<bean-factory> or in the cache factory registry.");
                }

                factory = bldr.realize(resolver, loader, /* params */ null);
            }

            BeanFactory innerFactory = factory;

            if (factory instanceof ConfigurableApplicationContext)
            {
                innerFactory = ((ConfigurableApplicationContext) factory).getBeanFactory();
            }

            if (innerFactory instanceof ConfigurableBeanFactory)
            {
                BeanExpressionResolver exprResolver =
                    ((ConfigurableBeanFactory) innerFactory).getBeanExpressionResolver();

                if (exprResolver instanceof CoherenceBeanExpressionResolver)
                {
                    ((CoherenceBeanExpressionResolver) exprResolver).setParameterResolver(resolver);
                }
            }

            return factory;
        }
    }


    // ----- inner class SpringBeanFactoryBuilder ---------------------------

    /**
     * Implementation of {@link ParameterizedBuilder} that "realizes" an instance
     * of {@link BeanFactory} based on a provided Spring application context uri.
     *
     * Upon realization, the {@link BeanFactory} is registered to the {@link ResourceRegistry}
     * provided in the constructor via the value returned by {@link #getFactoryName()}.
     */
    public static class SpringBeanFactoryBuilder implements ParameterizedBuilder<BeanFactory>,
                                                            ParameterizedBuilder.ReflectionSupport
    {
        // ----- data members -----------------------------------------------

        /**
         * The registry used to register the realized {@link BeanFactory}.
         */
        private ResourceRegistry m_registry;

        /**
         * The {@link Expression} for the {@link BeanFactory} URI.
         */
        private Expression<String> m_exprAppCtxUri;

        /**
         * The {@link Expression} for the {@link BeanFactory} name.
         */
        private Expression<String> m_exprFactoryName;

        /**
         * The {@link ExpressionParser} used to evaluate string expressions
         * to objects.
         */
        private ExpressionParser m_exprParser;


        // ----- constructors -----------------------------------------------

        /**
         * Construct a {@link SpringBeanFactoryBuilder}.
         *
         * @param registry    the {@link ResourceRegistry} this builder will
         *                    use to register the realized {@link BeanFactory}
         * @param exprParser  the {@link ExpressionParser} used to evaluate
         *                    an expression
         */
        public SpringBeanFactoryBuilder(ResourceRegistry registry,
                                        ExpressionParser exprParser)
        {
            m_registry   = registry;
            m_exprParser = exprParser;
        }


        // ----- accessors --------------------------------------------------

        /**
         * Return an {@link Expression} used to determine the name of the
         * {@link BeanFactory} realized by this builder.
         *
         * @return  an {@link Expression} for the {@link BeanFactory} name
         */
        public Expression<String> getFactoryName()
        {
            return m_exprFactoryName;
        }


        /**
         * Set the {@link Expression} used to determine the name of the
         * {@link BeanFactory} realized by this builder.
         *
         * @param exprFactoryName  the {@link Expression} for the {@link BeanFactory} name
         */
        @Injectable
        public void setFactoryName(Expression<String> exprFactoryName)
        {
            m_exprFactoryName = exprFactoryName;
        }


        /**
         * Return an {@link Expression} for the {@link BeanFactory} URI.
         *
         * @return  an {@link Expression} for the {@link BeanFactory} URI
         */
        public Expression<String> getApplicationContextUri()
        {
            return m_exprAppCtxUri;
        }


        /**
         * Set the {@link Expression} for the {@link BeanFactory} URI.
         *
         * @param exprAppCtxUri  the {@link Expression} for the {@link BeanFactory} URI
         */
        @Injectable
        public void setApplicationContextUri(Expression<String> exprAppCtxUri)
        {
            m_exprAppCtxUri = exprAppCtxUri;
        }


        // ----- ParameterizedBuilder.ReflectionSupport interface -----------

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean realizes(Class<?>          clzClass,
                                ParameterResolver resolver,
                                ClassLoader       loader)
        {
            return BeanFactory.class.isAssignableFrom(clzClass);
        }


        // ----- ParameterizedBuilder interface -----------------------------

        /**
         * {@inheritDoc}
         */
        @Override
        public BeanFactory realize(ParameterResolver resolver,
                                   ClassLoader       loader,
                                   ParameterList     listParameters)
        {
            ResourceRegistry registry     = m_registry;
            String           sFactoryName = getFactoryNameAsString(getFactoryName(), resolver);
            BeanFactory      factory      = registry.getResource(BeanFactory.class, sFactoryName);

            if (factory == null)
            {
                Expression<String> exprAppCtxUri = getApplicationContextUri();

                if (exprAppCtxUri == null)
                {
                    throw new ConfigurationException("Missing parameter <application-context-uri>",
                                                     "Ensure that parameter <application-context-uri> is supplied");
                }

                String sAppCtx = exprAppCtxUri.evaluate(resolver);
                URL    url     = Resources.findFileOrResource(sAppCtx, loader);

                if (url == null)
                {
                    throw new ConfigurationException(String.format("Resource '%s' not found for bean factory '%s'",
                                                                   sAppCtx, sFactoryName),
                                                     "Ensure that <application-context-uri> contains a valid file location");
                }

                factory = new CoherenceApplicationContext(url.toExternalForm());

                registry.registerResource(BeanFactory.class,
                                          sFactoryName,
                                          using(factory),
                                          RegistrationBehavior.FAIL,
                                          new ResourceRegistry.ResourceLifecycleObserver<BeanFactory>()
                {
                    @Override
                    public void onRelease(BeanFactory factory)
                    {
                        if (factory instanceof ConfigurableApplicationContext)
                        {
                            ((ConfigurableApplicationContext) factory).close();
                        }
                    }
                });
            }

            return factory;
        }

        // ----- inner class: CoherenceApplicationContext -------------------

        /**
         * Application context implementation that supports Coherence
         * expression macros within a Spring XML application context file.
         * This allows injection of Coherence objects into a bean
         * definition (such as {@code {cache-name}}).
         *
         * @see CoherenceBeanExpressionResolver
         */
        public class CoherenceApplicationContext extends FileSystemXmlApplicationContext
        {
            // ----- constructors -------------------------------------------

            /**
             * Construct a CoherenceApplicationContext.
             *
             * @param configLocation   location of application context xml file
             *
             * @throws BeansException  if the context creation failed
             */
            public CoherenceApplicationContext(String configLocation) throws BeansException
            {
                super(configLocation);
            }


            // ----- FileSystemXmlApplicationContext methods ----------------

            /**
             * {@inheritDoc}
             */
            @Override
            protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory)
            {
                super.prepareBeanFactory(beanFactory);

                beanFactory.setBeanExpressionResolver(new CoherenceBeanExpressionResolver(m_exprParser));
            }


            // ----- object methods -----------------------------------------

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return getClass().getName() + "; loaded from " + Arrays.asList(super.getConfigLocations());
            }
        }
    }
}
