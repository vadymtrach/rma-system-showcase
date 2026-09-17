package com.github.vadymtrach.rmasystemshowcase.dto.response;

import com.github.vadymtrach.rmasystemshowcase.entity.User;
import com.github.vadymtrach.rmasystemshowcase.enums.ComplaintStatus;
import com.github.vadymtrach.rmasystemshowcase.enums.ProductType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminComplaintResponse(Long id,
                                     String rmaNumber,
                                     ProductType productType,
                                     String description,
                                     ComplaintStatus status,

                                     Long assignedToId,
                                     String assignedToFullName,
                                     LocalDate assignedDate,
                                     LocalDate pickupConfirmed,
                                     String repairDescription,
                                     LocalDate repairConfirmed,
                                     LocalDate returnConfirmed,
                                     LocalDate sentToClient,

                                     String deliveryAddress,
                                     BigDecimal insuranceAmount,
                                     LocalDateTime createdAt,
                                     LocalDateTime updatedAt) implements ComplaintResponse {
}
