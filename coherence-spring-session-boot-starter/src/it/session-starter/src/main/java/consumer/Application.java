/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
public class Application {
    @Bean org.springframework.boot.web.servlet.ServletRegistrationBean<CounterServlet> counter() {
        return new org.springframework.boot.web.servlet.ServletRegistrationBean<>(new CounterServlet(), "/counter");
    }
}
