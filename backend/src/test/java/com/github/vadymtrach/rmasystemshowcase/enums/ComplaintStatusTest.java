package com.github.vadymtrach.rmasystemshowcase.enums;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.vadymtrach.rmasystemshowcase.enums.ComplaintStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ComplaintStatusTest {

    private static final Map<ComplaintStatus, ComplaintStatus> NEXT = Map.of(
            NEW, ASSIGNED,
            ASSIGNED, ACCEPTED,
            ACCEPTED, REPAIRED,
            REPAIRED, RETURNED,
            RETURNED, SHIPPED
    );

    @Test
    void onlyNextStepInWorkflowIsAllowed() {
        for (ComplaintStatus from : values()) {
            for (ComplaintStatus to : values()) {
                assertEquals(to == NEXT.get(from), from.canTransitionTo(to), from + " -> " + to);
            }
        }
    }
}
