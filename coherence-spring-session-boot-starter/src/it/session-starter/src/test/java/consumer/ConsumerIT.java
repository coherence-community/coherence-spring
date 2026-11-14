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
    @Test void embeddedSession() throws Exception {
        try (var context = start("--spring.session.timeout=123s", "--server.servlet.session.timeout=20m")) {
            discovery(context, true); Contract.cache(context);
            assertTrue(context.containsBean("sessionRepositoryFilterRegistration"));
            int port = context.getEnvironment().getProperty("local.server.port", Integer.class);
            HttpChecks.coherenceSession(context, port, java.time.Duration.ofSeconds(123)); Contract.pass("embeddedSession");
        }
    }
    @Test void disabled() throws Exception {
        try (var context = start("--coherence.spring.session.enabled=false")) {
            discovery(context, false); Contract.noSessions(context); Contract.cache(context); Contract.pass("disabled");
        }
    }
    @Test void storeTypeNone() throws Exception {
        try (var context = start("--spring.session.store-type=none")) {
            discovery(context, false); Contract.noSessions(context); Contract.cache(context); Contract.pass("storeTypeNone");
        }
    }
    @Test void storeTypeRedis() throws Exception {
        try (var context = start("--spring.session.store-type=redis")) {
            discovery(context, false); Contract.noSessions(context); Contract.cache(context); Contract.pass("storeTypeRedis");
        }
    }
    @Test void storeTypeEmpty() throws Exception {
        try (var context = start("--spring.session.store-type=")) {
            discovery(context, false); Contract.noSessions(context); Contract.cache(context); Contract.pass("storeTypeEmpty");
        }
    }
}
