package com.github.vadymtrach.rmasystemshowcase.support;

import com.github.vadymtrach.rmasystemshowcase.config.AdminBootstrap;
import com.github.vadymtrach.rmasystemshowcase.enums.ComplaintStatus;
import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base for tests that run the full application against PostgreSQL. Every test starts from an
 * empty database containing only the bootstrap admin, and requests go through the real
 * Spring Security filter chain via MockMvc.
 */
@SpringBootTest(properties = {
        "app.bootstrap-admin.email=" + IntegrationTest.ADMIN_EMAIL,
        "app.bootstrap-admin.password=" + IntegrationTest.ADMIN_PASSWORD,
        // Every test logs in from the same IP; keep the per-IP limit out of the way.
        // The per-username limit keeps its default and is tested in LoginTest.
        "security.login-rate-limit.max-failures-per-ip=1000"
})
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {

    protected static final String ADMIN_EMAIL = "admin@test.local";
    protected static final String ADMIN_PASSWORD = "admin-password";
    protected static final String USER_PASSWORD = "user-password";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    protected JdbcTemplate jdbc;
    @Autowired
    private AdminBootstrap adminBootstrap;
    @Autowired
    private JsonMapper jsonMapper;

    protected MockMvc mvc;

    @BeforeEach
    void resetDatabaseAndClient() {
        jdbc.execute("TRUNCATE complaints, users RESTART IDENTITY CASCADE");
        adminBootstrap.run(null);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    protected MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/login").param("username", email).param("password", password))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    protected MockHttpSession loginAsAdmin() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    /** Creates a user with {@link #USER_PASSWORD} and returns its id. */
    protected long createUser(String email, Role role) throws Exception {
        MvcResult result = postJson(loginAsAdmin(), "/api/users", Map.of(
                "email", email, "password", USER_PASSWORD, "fullName", "Test " + role, "role", role))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(result);
    }

    protected void deactivate(long userId) throws Exception {
        patchJson(loginAsAdmin(), "/api/users/" + userId + "/status", Map.of("active", false))
                .andExpect(status().isNoContent());
    }

    protected long createComplaint(String rmaNumber) throws Exception {
        MvcResult result = postJson(loginAsAdmin(), "/api/complaints", complaintBody(rmaNumber))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(result);
    }

    protected static Map<String, Object> complaintBody(String rmaNumber) {
        return Map.of(
                "rmaNumber", rmaNumber,
                "productType", "LAPTOP",
                "description", "Does not boot",
                "deliveryAddress", "Main St 1",
                "insuranceAmount", 100);
    }

    /** Moves a NEW complaint forward through the workflow (as admin) until it reaches {@code target}. */
    protected void advanceTo(long complaintId, ComplaintStatus target, long assigneeId) throws Exception {
        MockHttpSession admin = loginAsAdmin();
        String today = LocalDate.now().toString();
        String base = "/api/complaints/" + complaintId;
        record Step(ComplaintStatus status, String path, Map<String, Object> body) {}
        Step[] steps = {
                new Step(ComplaintStatus.ASSIGNED, "/assign", Map.of("assignedToId", assigneeId, "assignedDate", today)),
                new Step(ComplaintStatus.ACCEPTED, "/pickup", Map.of("pickupConfirmed", today)),
                new Step(ComplaintStatus.REPAIRED, "/repair", Map.of("repairDescription", "Replaced board", "repairDate", today)),
                new Step(ComplaintStatus.RETURNED, "/return", Map.of("returnConfirmed", today)),
                new Step(ComplaintStatus.SHIPPED, "/shipment", Map.of("sentToClient", today)),
        };
        for (Step step : steps) {
            patchJson(admin, base + step.path(), step.body()).andExpect(status().isOk());
            if (step.status() == target) {
                return;
            }
        }
    }

    protected ResultActions postJson(MockHttpSession session, String url, Object body) throws Exception {
        return mvc.perform(post(url).session(session).contentType(MediaType.APPLICATION_JSON).content(json(body)));
    }

    protected ResultActions putJson(MockHttpSession session, String url, Object body) throws Exception {
        return mvc.perform(put(url).session(session).contentType(MediaType.APPLICATION_JSON).content(json(body)));
    }

    protected ResultActions patchJson(MockHttpSession session, String url, Object body) throws Exception {
        return mvc.perform(patch(url).session(session).contentType(MediaType.APPLICATION_JSON).content(json(body)));
    }

    protected String json(Object body) {
        return jsonMapper.writeValueAsString(body);
    }

    protected long idOf(MvcResult result) throws Exception {
        return jsonMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
