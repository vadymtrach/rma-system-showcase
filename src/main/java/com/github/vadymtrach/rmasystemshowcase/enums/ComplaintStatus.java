package com.github.vadymtrach.rmasystemshowcase.enums;

public enum ComplaintStatus {
    NEW,
    ASSIGNED,      // Przekazano dla
    ACCEPTED,      // Potwierdzenie odbioru
    REPAIRED,      // Oddane po naprawie
    RETURNED,      // Potwierdzenie oddania
    SHIPPED        // Odesłanie do klienta
}