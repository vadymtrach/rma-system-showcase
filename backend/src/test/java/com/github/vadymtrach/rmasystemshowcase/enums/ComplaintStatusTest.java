package com.github.vadymtrach.rmasystemshowcase.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
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

    static List<Arguments> provideData(){
        List<Arguments> list = new ArrayList<>();
        for (ComplaintStatus from : values()) {
            for (ComplaintStatus to : values()) {
                list.add(Arguments.of(from, to, NEXT.get(from) == to));
            }
        }
        return list;
    }


    @ParameterizedTest(name = "{index}: {0} -> {1} : {2}")
    @MethodSource("provideData")
    void onlyNextStepInWorkflowIsAllowed(ComplaintStatus from, ComplaintStatus to, boolean expectedAllowed) {
        assertEquals(expectedAllowed, from.canTransitionTo(to));
    }
}
