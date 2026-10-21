/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer {
    @Override protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class).bannerMode(org.springframework.boot.Banner.Mode.OFF);
    }
    @Override public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);
        servletContext.addServlet("counter", new CounterServlet()).addMapping("/counter");
        servletContext.addServlet("forward", new ForwardingServlet()).addMapping("/forward");
    }
}
