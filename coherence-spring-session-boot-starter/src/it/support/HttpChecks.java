/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public final class HttpChecks {
    private HttpChecks() { }

    public static void coherenceSession(Object context, int port, Duration timeout) throws Exception {
        coherenceSession(context, port, timeout, "/counter");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void coherenceSession(Object context, int port, Duration timeout, String path) throws Exception {
        Object repository = bean(context, "sessionRepository");
        assertEquals("com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository", repository.getClass().getName());
        assertEquals(1, beans(context, "org.springframework.session.SessionRepository").size());
        assertEquals(1, beans(context, "org.springframework.session.web.http.SessionRepositoryFilter").size());
        String id = requests(port, path);
        Object session = repository.getClass().getMethod("findById", String.class).invoke(repository, id);
        Class<?> sessionApi = Class.forName("org.springframework.session.Session", true, repository.getClass().getClassLoader());
        assertNotNull(session);
        assertEquals(2, sessionApi.getMethod("getAttribute", String.class).invoke(session, "count"));
        if (timeout != null) assertEquals(timeout, sessionApi.getMethod("getMaxInactiveInterval").invoke(session));
        // Check the underlying Coherence map as well, independently of findById.
        var field = repository.getClass().getDeclaredField("sessionCache"); field.setAccessible(true);
        var stored = ((java.util.Map<?, ?>) field.get(repository)).get(id);
        assertNotNull(stored);
        assertEquals(2, sessionApi.getMethod("getAttribute", String.class).invoke(stored, "count"));
    }

    public static Object bean(Object context, String name) throws Exception {
        return context.getClass().getMethod("getBean", String.class).invoke(context, name);
    }
    public static java.util.Map<?, ?> beans(Object context, String className) throws Exception {
        Class<?> type = Class.forName(className, true, context.getClass().getClassLoader());
        return (java.util.Map<?, ?>) context.getClass().getMethod("getBeansOfType", Class.class).invoke(context, type);
    }

    public static String requests(int port) throws Exception {
        return requests(port, "/counter");
    }

    public static String requests(int port, String path) throws Exception {
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        URI uri = URI.create("http://127.0.0.1:" + port + path);
        var first = client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20)).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, first.statusCode(), first.body());
        var setCookie = first.headers().firstValue("set-cookie");
        assertTrue(setCookie.isPresent(), "First response from " + path + " must return the session cookie");
        String cookie = setCookie.orElseThrow().split(";", 2)[0];
        assertTrue(cookie.startsWith("SESSION="));
        var second = client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20)).header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, second.statusCode(), second.body());
        String id = first.body().substring(0, first.body().lastIndexOf(':'));
        assertEquals(id + ":1", first.body()); assertEquals(id + ":2", second.body());
        return id;
    }
}
