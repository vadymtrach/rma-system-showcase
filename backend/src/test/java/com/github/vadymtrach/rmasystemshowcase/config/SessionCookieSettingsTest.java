package com.github.vadymtrach.rmasystemshowcase.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CSRF tokens are disabled for HTTP and STOMP; the SameSite=Strict session cookie is what stops
 * cross-site requests from carrying the user's session. This test fails if that setting is relaxed.
 */
class SessionCookieSettingsTest {

    @Test
    void sessionCookieIsSameSiteStrictAndHttpOnly() throws IOException {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yaml"));
        PropertySource<?> props = sources.getFirst();

        assertThat(props.getProperty("server.servlet.session.cookie.same-site").toString())
                .as("CSRF protection relies on SameSite=Strict; enable CSRF tokens before changing this")
                .isEqualToIgnoringCase("strict");
        assertThat(props.getProperty("server.servlet.session.cookie.http-only").toString())
                .isEqualTo("true");
    }
}
