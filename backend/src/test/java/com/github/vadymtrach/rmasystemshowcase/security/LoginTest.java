package com.github.vadymtrach.rmasystemshowcase.security;

import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import com.github.vadymtrach.rmasystemshowcase.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginTest extends IntegrationTest {

    @Test
    void bootstrapAdminCanLogIn() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mvc.perform(get("/api/users/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void emailsAreCaseInsensitive() throws Exception {
        createUser("Mixed.Case@Test.local", Role.EMPLOYEE);

        login("MIXED.case@test.LOCAL", USER_PASSWORD);
        postJson(loginAsAdmin(), "/api/users", Map.of(
                "email", "mixed.case@test.local", "password", USER_PASSWORD, "fullName", "Dup", "role", "EMPLOYEE"))
                .andExpect(status().isConflict());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/login").param("username", ADMIN_EMAIL).param("password", "wrong-password"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedFailuresForAUsernameAreRateLimited() throws Exception {
        String email = "ratelimited@test.local";
        createUser(email, Role.EMPLOYEE);
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/login").param("username", email).param("password", "wrong-password"))
                    .andExpect(status().isUnauthorized());
        }

        // Blocked even with the correct password, until the window expires.
        mvc.perform(post("/api/login").param("username", email).param("password", USER_PASSWORD))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
        // Other accounts are unaffected.
        loginAsAdmin();
    }

    @Test
    void passwordsShorterThanEightCharactersAreRejected() throws Exception {
        postJson(loginAsAdmin(), "/api/users", Map.of(
                "email", "short@test.local", "password", "seven77", "fullName", "Short", "role", "EMPLOYEE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("password")));

        patchJson(loginAsAdmin(), "/api/users/me/password",
                Map.of("currentPassword", ADMIN_PASSWORD, "newPassword", "seven77"))
                .andExpect(status().isBadRequest());
    }
}
