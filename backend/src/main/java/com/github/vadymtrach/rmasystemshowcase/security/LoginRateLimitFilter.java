package com.github.vadymtrach.rmasystemshowcase.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Rejects login requests with 429 while the client IP or the submitted username is blocked
 * by {@link LoginAttemptService}. Must run before the form login filter.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;
    private final RequestMatcher loginRequestMatcher;

    public LoginRateLimitFilter(LoginAttemptService loginAttemptService, String loginUrl) {
        this.loginAttemptService = loginAttemptService;
        this.loginRequestMatcher = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, loginUrl);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !loginRequestMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Duration retryAfter = loginAttemptService.retryAfter(request.getRemoteAddr(), request.getParameter("username"));
        if (retryAfter.isPositive()) {
            long seconds = Math.max(1, (retryAfter.toMillis() + 999) / 1000);
            response.setHeader("Retry-After", Long.toString(seconds));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many failed login attempts");
            return;
        }
        chain.doFilter(request, response);
    }
}
