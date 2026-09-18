package com.github.vadymtrach.rmasystemshowcase.dto.response;

import com.github.vadymtrach.rmasystemshowcase.entity.User;
import com.github.vadymtrach.rmasystemshowcase.enums.ComplaintStatus;
import com.github.vadymtrach.rmasystemshowcase.enums.ProductType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public sealed interface ComplaintResponse
        permits AdminComplaintResponse, ServiceComplaintResponse,
        WarehouseComplaintResponse, EmployeeComplaintResponse {
    Long id();
    String rmaNumber();
    ProductType productType();
    String description();
    ComplaintStatus status();
    String deliveryAddress();
    LocalDateTime createdAt();
    LocalDateTime updatedAt();
    Long version();
}
