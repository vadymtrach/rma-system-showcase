package com.github.vadymtrach.rmasystemshowcase.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks failed login attempts per client IP and per username and blocks further attempts
 * once a limit is exceeded within the window. Counting both keys means rotating usernames
 * doesn't escape the IP limit and rotating IPs doesn't escape the per-account limit.
 * State is in-memory, so it resets on restart and isn't shared between instances.
 */
@Service
public class LoginAttemptService {

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final int maxFailuresPerUsername;
    private final int maxFailuresPerIp;
    private final Duration window;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(
            @Value("${security.login-rate-limit.max-failures-per-username:5}") int maxFailuresPerUsername,
            @Value("${security.login-rate-limit.max-failures-per-ip:20}") int maxFailuresPerIp,
            @Value("${security.login-rate-limit.window:15m}") Duration window
    ) {
        this(maxFailuresPerUsername, maxFailuresPerIp, window, Clock.systemUTC());
    }

    LoginAttemptService(int maxFailuresPerUsername, int maxFailuresPerIp, Duration window, Clock clock) {
        this.maxFailuresPerUsername = maxFailuresPerUsername;
        this.maxFailuresPerIp = maxFailuresPerIp;
        this.window = window;
        this.clock = clock;
    }

    /**
     * Returns how long the caller must wait before trying again, or {@link Duration#ZERO} if allowed.
     */
    public Duration retryAfter(String ip, String username) {
        Instant now = clock.instant();
        Duration byIp = retryAfter(ipKey(ip), maxFailuresPerIp, now);
        Duration byUser = username == null ? Duration.ZERO : retryAfter(userKey(username), maxFailuresPerUsername, now);
        return byIp.compareTo(byUser) >= 0 ? byIp : byUser;
    }

    public void recordFailure(String ip, String username) {
        Instant now = clock.instant();
        increment(ipKey(ip), now);
        if (username != null) {
            increment(userKey(username), now);
        }
    }

    /**
     * Clears the per-username counter only; the IP counter must still expire on its own,
     * otherwise an attacker holding one valid account could reset it between guesses.
     */
    public void recordSuccess(String username) {
        if (username != null) {
            attempts.remove(userKey(username));
        }
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    void evictExpired() {
        Instant now = clock.instant();
        attempts.values().removeIf(a -> a.isExpired(now));
    }

    private Duration retryAfter(String key, int limit, Instant now) {
        Attempts a = attempts.get(key);
        if (a == null || a.isExpired(now) || a.failures < limit) {
            return Duration.ZERO;
        }
        return Duration.between(now, a.windowEnd);
    }

    private void increment(String key, Instant now) {
        attempts.compute(key, (k, a) -> {
            if (a == null || a.isExpired(now)) {
                return new Attempts(1, now.plus(window));
            }
            return new Attempts(a.failures + 1, a.windowEnd);
        });
    }

    private static String ipKey(String ip) {
        return "ip:" + ip;
    }

    private static String userKey(String username) {
        return "user:" + username.trim().toLowerCase(Locale.ROOT);
    }

    private record Attempts(int failures, Instant windowEnd) {
        boolean isExpired(Instant now) {
            return !now.isBefore(windowEnd);
        }
    }
}
