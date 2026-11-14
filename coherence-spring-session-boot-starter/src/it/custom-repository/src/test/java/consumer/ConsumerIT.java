/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;
import com.tangosol.net.Coherence;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
class ConsumerIT {
    @BeforeEach void contract() throws Exception { Contract.begin(); }
    @AfterEach void shutdown() { Coherence.closeAll(); }
    private ConfigurableApplicationContext start(String... settings) {
        var app = new SpringApplication(Application.class);
        app.setDefaultProperties(java.util.Map.of("server.port", "0", "spring.main.banner-mode", "off"));
        return app.run(settings);
    }
    private void discovery(ConfigurableApplicationContext context, boolean enabled) {
        var outcome = ConditionEvaluationReport.get(context.getBeanFactory()).getConditionAndOutcomesBySource()
                .get("com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration");
        assertNotNull(outcome, "must exercise Boot's imports discovery");
        assertEquals(enabled, outcome.isFullMatch(), outcome.toString());
    }
    @org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
    static class Custom {
        @org.springframework.context.annotation.Bean
        org.springframework.session.SessionRepository<org.springframework.session.MapSession> ownedRepository() {
            return new org.springframework.session.MapSessionRepository(new java.util.concurrent.ConcurrentHashMap<>());
        }
    }
    @org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
    @org.springframework.session.config.annotation.web.http.EnableSpringHttpSession
    static class EnabledCustom {
        @org.springframework.context.annotation.Bean({"ownedRepository", "sessionRepository"})
        @org.springframework.context.annotation.DependsOn("coherenceServer")
        com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository ownedRepository(Coherence coherence) {
            var repository = new com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository(coherence.getSession());
            repository.setSessionMapName("application-owned-sessions");
            repository.setDefaultMaxInactiveInterval(java.time.Duration.ofSeconds(123));
            return repository;
        }
    }

    @Test void customRepositoryOnly() throws Exception {
        var app = new SpringApplication(Application.class, Custom.class);
        try (var context = app.run("--server.port=0", "--spring.main.banner-mode=off")) {
            discovery(context, false); Contract.cache(context);
            assertEquals(1, context.getBeansOfType(org.springframework.session.SessionRepository.class).size());
            assertEquals(0, context.getBeansOfType(com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository.class).size());
            assertEquals(0, context.getBeansOfType(org.springframework.session.web.http.SessionRepositoryFilter.class).size());
            assertFalse(context.containsBean("sessionRepositoryFilterRegistration"));
            Contract.pass("customRepositoryOnly");
        }
    }
    @Test void customOwnership() throws Exception {
        var app = new SpringApplication(Application.class, EnabledCustom.class);
        try (var context = app.run("--server.port=0", "--spring.main.banner-mode=off")) {
            discovery(context, false); Contract.cache(context);
            assertEquals(1, context.getBeansOfType(org.springframework.session.SessionRepository.class).size());
            assertEquals(1, context.getBeansOfType(com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository.class).size());
            assertSame(context.getBean("ownedRepository"), context.getBean("sessionRepository"));
            assertEquals(1, context.getBeansOfType(org.springframework.session.web.http.SessionRepositoryFilter.class).size());
            assertTrue(context.containsBean("sessionRepositoryFilterRegistration"));
            HttpChecks.coherenceSession(context, context.getEnvironment().getProperty("local.server.port", Integer.class),
                    java.time.Duration.ofSeconds(123));
            Contract.pass("customOwnership");
        }
    }
}
