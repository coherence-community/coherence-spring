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
    @Test void oldDefault() throws Exception {
        try (var context = start()) {
            discovery(context, false); Contract.noSessions(context);
            var report = ConditionEvaluationReport.get(context.getBeanFactory()).getConditionAndOutcomesBySource()
                .get("com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration");
            assertTrue(java.util.stream.StreamSupport.stream(report.spliterator(), false)
                .anyMatch(outcome -> String.valueOf(outcome.getOutcome().getMessage()).contains("org.springframework.boot.session"))); Contract.cache(context); Contract.pass("oldDefault");
        }
    }
    @Test void oldExplicit() throws Exception {
        try (var context = start("--coherence.spring.session.enabled=true")) {
            discovery(context, false); Contract.noSessions(context);
            var report = ConditionEvaluationReport.get(context.getBeanFactory()).getConditionAndOutcomesBySource()
                .get("com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration");
            assertTrue(java.util.stream.StreamSupport.stream(report.spliterator(), false)
                .anyMatch(outcome -> String.valueOf(outcome.getOutcome().getMessage()).contains("org.springframework.boot.session"))); Contract.cache(context); Contract.pass("oldExplicit");
        }
    }
}
