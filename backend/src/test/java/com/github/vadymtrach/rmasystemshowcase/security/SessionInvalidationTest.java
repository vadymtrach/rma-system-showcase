package com.github.vadymtrach.rmasystemshowcase.security;

import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import com.github.vadymtrach.rmasystemshowcase.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Account changes must end the user's existing sessions, because the principal cached in a
 * session (including its role) is never reloaded.
 */
class SessionInvalidationTest extends IntegrationTest {

    private static final String EMAIL = "emp@test.local";

    private long userId;

    @BeforeEach
    void createEmployee() throws Exception {
        userId = createUser(EMAIL, Role.EMPLOYEE);
    }

    @Test
    void roleChangeEndsUserSessions() throws Exception {
        MockHttpSession session = login(EMAIL, USER_PASSWORD);
        assertSessionValid(session);

        updateUser(Role.WAREHOUSE, "Test EMPLOYEE");

        assertSessionEnded(session);
    }

    @Test
    void nameOnlyChangeKeepsUserSessions() throws Exception {
        MockHttpSession session = login(EMAIL, USER_PASSWORD);

        updateUser(Role.EMPLOYEE, "Renamed");

        assertSessionValid(session);
    }

    @Test
    void deactivationEndsSessionsAndBlocksLogin() throws Exception {
        MockHttpSession session = login(EMAIL, USER_PASSWORD);

        deactivate(userId);

        assertSessionEnded(session);
        mvc.perform(post("/api/login")
                        .param("username", EMAIL).param("password", USER_PASSWORD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordChangeKeepsCurrentSessionAndEndsOthers() throws Exception {
        MockHttpSession current = login(EMAIL, USER_PASSWORD);
        MockHttpSession other = login(EMAIL, USER_PASSWORD);

        patchJson(current, "/api/users/me/password",
                Map.of("currentPassword", USER_PASSWORD, "newPassword", "new-password-1"))
                .andExpect(status().isNoContent());

        assertSessionValid(current);
        assertSessionEnded(other);
        login(EMAIL, "new-password-1");
    }

    private void updateUser(Role role, String fullName) throws Exception {
        putJson(loginAsAdmin(), "/api/users/" + userId, Map.of("email", EMAIL, "fullName", fullName, "role", role))
                .andExpect(status().isOk());
    }

    private void assertSessionValid(MockHttpSession session) throws Exception {
        mvc.perform(get("/api/users/me").session(session)).andExpect(status().isOk());
    }

    private void assertSessionEnded(MockHttpSession session) throws Exception {
        mvc.perform(get("/api/users/me").session(session)).andExpect(status().isUnauthorized());
    }
}
