package com.github.vadymtrach.rmasystemshowcase.service;

import com.github.vadymtrach.rmasystemshowcase.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserManagementTest extends IntegrationTest {

    @Test
    void deactivatingLastActiveAdminIsRejected() throws Exception {
        Long adminId = jdbc.queryForObject("SELECT id FROM users WHERE email=?", Long.class, ADMIN_EMAIL);

        patchJson(loginAsAdmin(), "/api/users/" + adminId + "/status", Map.of("active", false))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("last active admin")));

        boolean active = jdbc.queryForObject("SELECT active FROM users WHERE id=?", Boolean.class, adminId);
        assertThat(active).isTrue();
    }
}
