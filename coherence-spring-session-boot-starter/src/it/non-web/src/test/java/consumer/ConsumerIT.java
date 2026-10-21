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
    @Test void nonWeb() throws Exception {
        assertTrue(Contract.present("jakarta.servlet.Servlet"));
        var app = new SpringApplication(Application.class); app.setWebApplicationType(WebApplicationType.NONE);
        try (var context = app.run("--spring.main.banner-mode=off")) {
            discovery(context, false); Contract.noSessions(context); Contract.cache(context); Contract.pass("nonWeb");
        }
    }
    @Test void reactive() throws Exception {
        var app = new SpringApplication(Application.class); app.setWebApplicationType(WebApplicationType.REACTIVE);
        try (var context = app.run("--server.port=0", "--spring.main.banner-mode=off")) {
            assertInstanceOf(org.springframework.boot.web.context.reactive.ReactiveWebApplicationContext.class, context);
            discovery(context, false); Contract.noSessions(context); Contract.cache(context); Contract.pass("reactive");
        }
    }
}
