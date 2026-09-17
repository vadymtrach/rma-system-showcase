package com.github.vadymtrach.rmasystemshowcase.mapper;

import com.github.vadymtrach.rmasystemshowcase.dto.request.ComplaintCreateRequest;
import com.github.vadymtrach.rmasystemshowcase.dto.request.ComplaintUpdateRequest;
import com.github.vadymtrach.rmasystemshowcase.dto.response.*;
import com.github.vadymtrach.rmasystemshowcase.entity.Complaint;
import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class ComplaintMapper {
    public Complaint toEntity(ComplaintCreateRequest request) {
        if (request == null) return null;
        Complaint complaint = new Complaint();

        complaint.setRmaNumber(request.rmaNumber());
        complaint.setProductType(request.productType());
        complaint.setDescription(request.description());
        complaint.setDeliveryAddress(request.deliveryAddress());
        complaint.setInsuranceAmount(request.insuranceAmount());

        return complaint;
    }

    public ComplaintResponse toResponse(Complaint complaint, Role role) {
        return switch (role) {
            case ADMIN -> toAdminDTO(complaint);
            case SERVICE -> toServiceDTO(complaint);
            case WAREHOUSE -> toWarehouseDTO(complaint);
            case EMPLOYEE -> toEmployeeDTO(complaint);
        };
    }

    private AdminComplaintResponse toAdminDTO(Complaint complaint) {
        return new AdminComplaintResponse(
                complaint.getId(),
                complaint.getRmaNumber(),
                complaint.getProductType(),
                complaint.getDescription(),
                complaint.getStatus(),

                assignedToId(complaint),
                assignedToName(complaint),
                complaint.getAssignedDate(),
                complaint.getPickupConfirmed(),
                complaint.getRepairDescription(),
                complaint.getRepairConfirmed(),
                complaint.getReturnConfirmed(),
                complaint.getSentToClient(),

                complaint.getDeliveryAddress(),
                complaint.getInsuranceAmount(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt()
        );
    }

    private ServiceComplaintResponse toServiceDTO(Complaint complaint) {
        return new ServiceComplaintResponse(
                complaint.getId(),
                complaint.getRmaNumber(),
                complaint.getProductType(),
                complaint.getDescription(),
                complaint.getStatus(),

                assignedToId(complaint),
                assignedToName(complaint),
                complaint.getAssignedDate(),
                complaint.getPickupConfirmed(),
                complaint.getRepairDescription(),
                complaint.getRepairConfirmed(),
                complaint.getReturnConfirmed(),
                complaint.getSentToClient(),

                complaint.getDeliveryAddress(),
                complaint.getInsuranceAmount(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt()
        );
    }

    private WarehouseComplaintResponse toWarehouseDTO(Complaint complaint) {
        return new WarehouseComplaintResponse(
                complaint.getId(),
                complaint.getRmaNumber(),
                complaint.getProductType(),
                complaint.getDescription(),
                complaint.getStatus(),

                assignedToId(complaint),
                assignedToName(complaint),
                complaint.getAssignedDate(),
                complaint.getRepairDescription(),
                complaint.getSentToClient(),

                complaint.getDeliveryAddress(),
                complaint.getInsuranceAmount(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt()
        );
    }

    private EmployeeComplaintResponse toEmployeeDTO(Complaint complaint) {
        return new EmployeeComplaintResponse(
                complaint.getId(),
                complaint.getRmaNumber(),
                complaint.getProductType(),
                complaint.getDescription(),
                complaint.getStatus(),

                assignedToId(complaint),
                assignedToName(complaint),
                complaint.getAssignedDate(),
                complaint.getPickupConfirmed(),
                complaint.getRepairDescription(),
                complaint.getRepairConfirmed(),
                complaint.getReturnConfirmed(),

                complaint.getDeliveryAddress(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt()
        );
    }

    public void updateEntity(Complaint complaint, ComplaintUpdateRequest request) {
        if (request.rmaNumber() != null) complaint.setRmaNumber(request.rmaNumber());
        if (request.productType() != null) complaint.setProductType(request.productType());
        if (request.description() != null) complaint.setDescription(request.description());
        if (request.deliveryAddress() != null) complaint.setDeliveryAddress(request.deliveryAddress());
        if (request.insuranceAmount() != null) complaint.setInsuranceAmount(request.insuranceAmount());
    }

    private Long assignedToId(Complaint complaint) {
        return complaint.getAssignedTo() == null
                ? null
                : complaint.getAssignedTo().getId();
    }

    private String assignedToName(Complaint complaint) {
        return complaint.getAssignedTo() == null
                ? null
                : complaint.getAssignedTo().getFullName();
    }
}