package com.github.vadymtrach.rmasystemshowcase.service;

import com.github.vadymtrach.rmasystemshowcase.enums.ComplaintStatus;
import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import com.github.vadymtrach.rmasystemshowcase.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ComplaintWorkflowTest extends IntegrationTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    private DataSource dataSource;
    @Autowired
    @Qualifier("brokerChannel")
    private AbstractSubscribableChannel brokerChannel;

    private MockHttpSession admin;
    private long employee;

    @BeforeEach
    void setUp() throws Exception {
        admin = loginAsAdmin();
        employee = createUser("emp@test.local", Role.EMPLOYEE);
    }

    @Test
    void complaintMovesThroughTheWholeWorkflow() throws Exception {
        long id = createComplaint("RMA-1");

        advanceTo(id, ComplaintStatus.SHIPPED, employee);

        assertThat(jdbc.queryForObject("SELECT status FROM complaints WHERE id = ?", String.class, id))
                .isEqualTo("SHIPPED");
    }

    @Test
    void statusStepsCannotBeSkipped() throws Exception {
        long id = createComplaint("RMA-1");

        patchJson(admin, "/api/complaints/" + id + "/pickup", Map.of("pickupConfirmed", TODAY.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("from NEW to ACCEPTED")));
    }

    @Test
    void duplicateRmaNumberIsRejected() throws Exception {
        createComplaint("RMA-1");

        postJson(admin, "/api/complaints", complaintBody("RMA-1")).andExpect(status().isConflict());
    }

    @Test
    void editBasedOnStaleVersionIsRejected() throws Exception {
        long id = createComplaint("RMA-1");

        putJson(admin, "/api/complaints/" + id, edit("First edit", 0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));
        putJson(admin, "/api/complaints/" + id, edit("Concurrent edit", 0))
                .andExpect(status().isConflict());
    }

    @Test
    void assigneeMustBeActiveEmployeeOrServiceEngineer() throws Exception {
        long warehouse = createUser("wh@test.local", Role.WAREHOUSE);
        long inactive = createUser("gone@test.local", Role.EMPLOYEE);
        long service = createUser("svc@test.local", Role.SERVICE);
        deactivate(inactive);
        long id = createComplaint("RMA-1");

        assign(id, warehouse, TODAY).andExpect(status().isBadRequest());
        assign(id, inactive, TODAY).andExpect(status().isBadRequest());
        assign(id, service, TODAY).andExpect(status().isOk());
    }

    @Test
    void workflowDatesCannotBeInTheFutureOrBeforeThePreviousStep() throws Exception {
        long id = createComplaint("RMA-1");

        assign(id, employee, TODAY.plusDays(5))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("future")));
        assign(id, employee, TODAY).andExpect(status().isOk());

        patchJson(admin, "/api/complaints/" + id + "/pickup", Map.of("pickupConfirmed", TODAY.minusDays(1).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("before the assignment date")));
        // One day ahead is tolerated: the server runs in UTC, browsers may already be on the next day.
        patchJson(admin, "/api/complaints/" + id + "/pickup", Map.of("pickupConfirmed", TODAY.plusDays(1).toString()))
                .andExpect(status().isOk());
    }

    @Test
    void overlongFieldsAreRejectedWithTheFieldName() throws Exception {
        Map<String, Object> body = new HashMap<>(complaintBody("R".repeat(256)));

        postJson(admin, "/api/complaints", body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("rmaNumber")));
    }

    @Test
    void clientsAreNotifiedOnlyAfterTheChangeIsCommitted() throws Exception {
        long id = createComplaint("RMA-1");
        // For each notification, record the status another connection sees at that moment.
        List<String> statusSeenAtNotification = new CopyOnWriteArrayList<>();
        ChannelInterceptor probe = new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                statusSeenAtNotification.add(readStatusFromSeparateConnection(id));
                return message;
            }
        };
        brokerChannel.addInterceptor(probe);
        try {
            assign(id, employee, TODAY).andExpect(status().isOk());
        } finally {
            brokerChannel.removeInterceptor(probe);
        }

        assertThat(statusSeenAtNotification).containsExactly("ASSIGNED");
    }

    private ResultActions assign(long complaintId, long userId, LocalDate date) throws Exception {
        return patchJson(admin, "/api/complaints/" + complaintId + "/assign",
                Map.of("assignedToId", userId, "assignedDate", date.toString()));
    }

    private static Map<String, Object> edit(String description, long version) {
        Map<String, Object> body = new HashMap<>(complaintBody("RMA-1"));
        body.put("description", description);
        body.put("version", version);
        return body;
    }

    private String readStatusFromSeparateConnection(long complaintId) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT status FROM complaints WHERE id = " + complaintId)) {
            return rs.next() ? rs.getString(1) : null;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
