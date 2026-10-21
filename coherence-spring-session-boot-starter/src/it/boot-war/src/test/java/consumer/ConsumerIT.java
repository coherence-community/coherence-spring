/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.core.StandardContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ConsumerIT {
    @Test void warDiscovery() throws Exception { deploy("warDiscovery", null, Duration.ofMinutes(30)); }
    @Test void warTimeoutOverride() throws Exception { deploy("warTimeoutOverride", "123s", Duration.ofSeconds(123)); }
    @Test void warContainerTimeout() throws Exception { deploy("warContainerTimeout", null, Duration.ofMinutes(30)); }

    void deploy(String scenario, String timeout, Duration expected) throws Exception {
        Contract.begin();
        Files.createDirectories(Path.of("target/tomcat-" + scenario + "/webapps"));
        Tomcat tomcat = new Tomcat(); tomcat.setBaseDir("target/tomcat-" + scenario); tomcat.setPort(0); tomcat.setAddDefaultWebXmlToWebapp(false); tomcat.getConnector();
        var deployment = (StandardContext) tomcat.addWebapp("", Path.of("target/consumer.war").toAbsolutePath().toString());
        // Let forward() close Spring Session's response wrapper before Tomcat suspends it.
        // Tomcat 11's default bypasses that hook and loses the first session cookie.
        deployment.setSuspendWrappedResponseAfterForward(false);
        // Expose only container/JDK classes through the parent. Product, Boot and Spring
        // must load from this actual WAR, never the Failsafe application classpath.
        ClassLoader container = Tomcat.class.getClassLoader();
        deployment.setParentClassLoader(new ClassLoader(ClassLoader.getPlatformClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("jakarta.servlet.") || name.startsWith("org.apache.catalina.")
                        || name.startsWith("org.apache.coyote.") || name.startsWith("org.apache.juli.")
                        || name.startsWith("org.apache.tomcat.") || name.startsWith("org.apache.naming.")) return container.loadClass(name);
                return super.loadClass(name, resolve);
            }
        });
        deployment.setSessionTimeout(9);
        deployment.addParameter("server.servlet.session.timeout", "20m");
        deployment.addParameter("spring.session.servlet.filter-order", "123");
        deployment.addParameter("spring.session.servlet.filter-dispatcher-types", "REQUEST,ERROR");
        if (timeout != null) deployment.addParameter("spring.session.timeout", timeout);
        try {
            tomcat.start(); assertTrue(deployment.getState().isAvailable(), "Boot WAR did not initialize");
            Object context = deployment.getServletContext().getAttribute("org.springframework.web.context.WebApplicationContext.ROOT");
            assertNotNull(context, "SCI must discover Boot initializer");
            var loader = context.getClass().getClassLoader();
            assertThrows(ClassNotFoundException.class, () -> Class.forName("org.springframework.boot.web.server.autoconfigure.ServerProperties", false, loader));
            Object registration = HttpChecks.bean(context, "sessionRepositoryFilterRegistration");
            assertEquals(123, registration.getClass().getMethod("getOrder").invoke(registration));
            assertEquals("springSessionRepositoryFilter", registration.getClass().getMethod("getFilterName").invoke(registration));
            var mappings = Arrays.stream(deployment.findFilterMaps()).filter(m -> m.getFilterName().equals("springSessionRepositoryFilter")).toList();
            assertEquals(1, mappings.size());
            assertEquals(Set.of("REQUEST", "ERROR"), Set.of(mappings.get(0).getDispatcherNames()));
            assertEquals(Set.of("/*"), Set.of(mappings.get(0).getURLPatterns()));
            Object repository = HttpChecks.bean(context, "sessionRepository");
            Contract.loadedCoherence(repository.getClass().getClassLoader());
            Class<?> reportClass = Class.forName("org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport", true, loader);
            Class<?> factoryClass = Class.forName("org.springframework.beans.factory.config.ConfigurableListableBeanFactory", true, loader);
            Object factory = context.getClass().getMethod("getBeanFactory").invoke(context);
            Object report = reportClass.getMethod("get", factoryClass).invoke(null, factory);
            Map<?, ?> outcomes = (Map<?, ?>) reportClass.getMethod("getConditionAndOutcomesBySource").invoke(report);
            Object outcome = outcomes.get("com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration");
            assertNotNull(outcome); assertEquals(true, outcome.getClass().getMethod("isFullMatch").invoke(outcome));
            HttpChecks.coherenceSession(context, tomcat.getConnector().getLocalPort(), expected);
            HttpChecks.coherenceSession(context, tomcat.getConnector().getLocalPort(), expected, "/forward");
            Contract.pass(scenario);
        }
        finally { tomcat.stop(); tomcat.destroy(); }
    }
}
