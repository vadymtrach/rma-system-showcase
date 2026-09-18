package com.github.vadymtrach.rmasystemshowcase.enums;

public enum ComplaintStatus {
    NEW,
    ASSIGNED,
    ACCEPTED,
    REPAIRED,
    RETURNED,
    SHIPPED;

    public boolean canTransitionTo(ComplaintStatus target) {
        return switch (this) {
            case NEW -> target == ASSIGNED;
            case ASSIGNED -> target == ACCEPTED;
            case ACCEPTED -> target == REPAIRED;
            case REPAIRED -> target == RETURNED;
            case RETURNED -> target == SHIPPED;
            case SHIPPED -> false;
        };
    }
}
