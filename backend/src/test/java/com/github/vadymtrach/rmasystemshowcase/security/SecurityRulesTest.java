package com.github.vadymtrach.rmasystemshowcase.security;

import com.github.vadymtrach.rmasystemshowcase.enums.ComplaintStatus;
import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import com.github.vadymtrach.rmasystemshowcase.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Role-based access rules, exercised through the real security filter chain.
 */
class SecurityRulesTest extends IntegrationTest {

    private static final String TODAY = LocalDate.now().toString();

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mvc.perform(get("/api/complaints")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void employeeSeesOnlyComplaintsAssignedToThem() throws Exception {
        long employee = createUser("emp@test.local", Role.EMPLOYEE);
        long assigned = createComplaint("RMA-ASSIGNED");
        long other = createComplaint("RMA-OTHER");
        advanceTo(assigned, ComplaintStatus.ASSIGNED, employee);

        MockHttpSession session = login("emp@test.local", USER_PASSWORD);
        mvc.perform(get("/api/complaints").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rmaNumber").value("RMA-ASSIGNED"))
                // Employees get a reduced DTO without financial or shipping fields.
                .andExpect(jsonPath("$[0].insuranceAmount").doesNotExist())
                .andExpect(jsonPath("$[0].sentToClient").doesNotExist());
        mvc.perform(get("/api/complaints/" + assigned).session(session)).andExpect(status().isOk());
        mvc.perform(get("/api/complaints/" + other).session(session)).andExpect(status().isForbidden());
    }

    @Test
    void employeeCannotManageComplaintsOrUsers() throws Exception {
        createUser("emp@test.local", Role.EMPLOYEE);
        long complaint = createComplaint("RMA-1");
        MockHttpSession session = login("emp@test.local", USER_PASSWORD);

        postJson(session, "/api/complaints", complaintBody("RMA-2")).andExpect(status().isForbidden());
        patchJson(session, "/api/complaints/" + complaint + "/assign", Map.of("assignedToId", 1, "assignedDate", TODAY))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/complaints/" + complaint).session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/api/users").session(session)).andExpect(status().isForbidden());
    }

    @Test
    void warehouseCanShipButNotPerformServiceSteps() throws Exception {
        long employee = createUser("emp@test.local", Role.EMPLOYEE);
        createUser("wh@test.local", Role.WAREHOUSE);
        long complaint = createComplaint("RMA-1");
        advanceTo(complaint, ComplaintStatus.RETURNED, employee);
        MockHttpSession session = login("wh@test.local", USER_PASSWORD);

        postJson(session, "/api/complaints", complaintBody("RMA-2")).andExpect(status().isForbidden());
        patchJson(session, "/api/complaints/" + complaint + "/return", Map.of("returnConfirmed", TODAY))
                .andExpect(status().isForbidden());
        patchJson(session, "/api/complaints/" + complaint + "/shipment", Map.of("sentToClient", TODAY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void serviceCanManageComplaintsButOnlyAdminChangesUserStatus() throws Exception {
        long employee = createUser("emp@test.local", Role.EMPLOYEE);
        createUser("svc@test.local", Role.SERVICE);
        MockHttpSession service = login("svc@test.local", USER_PASSWORD);

        postJson(service, "/api/complaints", complaintBody("RMA-1")).andExpect(status().isCreated());
        mvc.perform(get("/api/users").session(service)).andExpect(status().isOk());
        patchJson(service, "/api/users/" + employee + "/status", Map.of("active", false))
                .andExpect(status().isForbidden());
        patchJson(loginAsAdmin(), "/api/users/" + employee + "/status", Map.of("active", false))
                .andExpect(status().isNoContent());
    }
}
