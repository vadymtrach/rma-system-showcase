package com.github.vadymtrach.rmasystemshowcase.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private static final String IP = "10.0.0.1";
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private MutableClock clock;
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        service = new LoginAttemptService(3, 5, WINDOW, clock);
    }

    @Test
    void blocksUsernameAfterLimitReached() {
        fail(IP, "user@example.com", 2);
        assertThat(service.retryAfter(IP, "user@example.com")).isZero();

        fail(IP, "user@example.com", 1);
        assertThat(service.retryAfter(IP, "user@example.com")).isEqualTo(WINDOW);
        assertThat(service.retryAfter("10.0.0.2", "USER@example.com ")).isEqualTo(WINDOW);
        assertThat(service.retryAfter(IP, "other@example.com")).isZero();
    }

    @Test
    void blocksIpAcrossDifferentUsernames() {
        for (int i = 0; i < 5; i++) {
            service.recordFailure(IP, "user" + i + "@example.com");
        }
        assertThat(service.retryAfter(IP, "fresh@example.com")).isPositive();
        assertThat(service.retryAfter("10.0.0.2", "fresh@example.com")).isZero();
    }

    @Test
    void unblocksAfterWindowExpires() {
        fail(IP, "user@example.com", 3);
        clock.advance(WINDOW.minusSeconds(1));
        assertThat(service.retryAfter(IP, "user@example.com")).isEqualTo(Duration.ofSeconds(1));

        clock.advance(Duration.ofSeconds(1));
        assertThat(service.retryAfter(IP, "user@example.com")).isZero();
    }

    @Test
    void successResetsUsernameButNotIp() {
        fail(IP, "user@example.com", 2);
        service.recordSuccess("user@example.com");
        fail(IP, "user@example.com", 2);
        assertThat(service.retryAfter(IP, "user@example.com")).isZero();

        service.recordFailure(IP, "user@example.com");
        assertThat(service.retryAfter("10.0.0.2", "someone@example.com")).isZero();
        assertThat(service.retryAfter(IP, "someone@example.com")).isPositive();
    }

    @Test
    void evictRemovesExpiredEntries() {
        fail(IP, "user@example.com", 3);
        clock.advance(WINDOW);
        service.evictExpired();
        fail(IP, "user@example.com", 2);
        assertThat(service.retryAfter(IP, "user@example.com")).isZero();
    }

    private void fail(String ip, String username, int times) {
        for (int i = 0; i < times; i++) {
            service.recordFailure(ip, username);
        }
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
