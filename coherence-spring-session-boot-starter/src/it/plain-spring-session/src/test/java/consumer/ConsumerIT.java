/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;
import java.nio.file.*;
import java.util.EnumSet;
import com.tangosol.net.Coherence;
import jakarta.servlet.*;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
class ConsumerIT {
    @Test void explicitSpring() throws Exception {
        Contract.begin(); Tomcat tomcat = new Tomcat(); tomcat.setBaseDir("target/tomcat"); tomcat.setPort(0); tomcat.getConnector();
        Files.createDirectories(Path.of("target/webroot"));
        var deployment = tomcat.addContext("", Path.of("target/webroot").toAbsolutePath().toString());
        // Let forward() close Spring Session's response wrapper before Tomcat suspends it.
        // Tomcat 11's default bypasses that hook and loses the first session cookie.
        deployment.setSuspendWrappedResponseAfterForward(false);
        var spring = new AnnotationConfigWebApplicationContext();
        deployment.addServletContainerInitializer((classes, servletContext) -> {
            spring.setServletContext(servletContext); spring.register(Application.class); spring.refresh();
            var filter = (Filter) spring.getBean("springSessionRepositoryFilter");
            servletContext.addFilter("springSessionRepositoryFilter", filter)
                    .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
            servletContext.addServlet("counter", new CounterServlet()).addMapping("/counter");
            servletContext.addServlet("forward", new ForwardingServlet()).addMapping("/forward");
        }, null);
        try {
            tomcat.start(); assertTrue(deployment.getState().isAvailable()); Contract.cache(spring);
            HttpChecks.coherenceSession(spring, tomcat.getConnector().getLocalPort(), java.time.Duration.ofSeconds(123));
            HttpChecks.coherenceSession(spring, tomcat.getConnector().getLocalPort(), java.time.Duration.ofSeconds(123), "/forward");
            Contract.pass("explicitSpring");
        }
        finally { spring.close(); tomcat.stop(); tomcat.destroy(); Coherence.closeAll(); }
    }
}
