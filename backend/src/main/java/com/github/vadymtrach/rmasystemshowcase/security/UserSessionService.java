package com.github.vadymtrach.rmasystemshowcase.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

/**
 * Expires a user's HTTP sessions so changes to their account (deactivation, role, password)
 * take effect immediately instead of when the session times out. The principal cached in a
 * session is never reloaded, so without this a demoted user keeps their old authorities.
 */
@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final SessionRegistry sessionRegistry;

    public void expireAllSessions(Long userId) {
        expireSessions(userId, null);
    }

    public void expireOtherSessions(Long userId, String keepSessionId) {
        expireSessions(userId, keepSessionId);
    }

    private void expireSessions(Long userId, String keepSessionId) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(p -> p instanceof SecurityUser u && u.id().equals(userId))
                .flatMap(p -> sessionRegistry.getAllSessions(p, false).stream())
                .filter(s -> !s.getSessionId().equals(keepSessionId))
                .forEach(SessionInformation::expireNow);
    }
}
