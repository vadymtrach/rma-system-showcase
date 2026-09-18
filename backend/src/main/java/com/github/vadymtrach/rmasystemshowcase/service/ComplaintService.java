package com.github.vadymtrach.rmasystemshowcase.service;

import com.github.vadymtrach.rmasystemshowcase.dto.request.*;
import com.github.vadymtrach.rmasystemshowcase.dto.response.ComplaintResponse;
import com.github.vadymtrach.rmasystemshowcase.entity.Complaint;
import com.github.vadymtrach.rmasystemshowcase.entity.User;
import com.github.vadymtrach.rmasystemshowcase.enums.ComplaintStatus;
import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import com.github.vadymtrach.rmasystemshowcase.exception.BusinessLogicException;
import com.github.vadymtrach.rmasystemshowcase.exception.ConflictException;
import com.github.vadymtrach.rmasystemshowcase.exception.ResourceNotFoundException;
import com.github.vadymtrach.rmasystemshowcase.mapper.ComplaintMapper;
import com.github.vadymtrach.rmasystemshowcase.repository.ComplaintRepository;
import com.github.vadymtrach.rmasystemshowcase.repository.UserRepository;
import com.github.vadymtrach.rmasystemshowcase.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ComplaintMapper complaintMapper;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String WS_TOPIC = "/topic/rma-updates";

    private void notifyClients() {
        messagingTemplate.convertAndSend(WS_TOPIC, "REFRESH");
    }


    @Transactional
    public ComplaintResponse create(ComplaintCreateRequest request,
                                    SecurityUser securityUser) {
        if (complaintRepository.existsByRmaNumber(request.rmaNumber())) {
            throw new ConflictException("Complaint with RMA number " + request.rmaNumber() + " already exists");
        }
        Complaint complaint = complaintMapper.toEntity(request);
        Complaint saved = complaintRepository.save(complaint);
        ComplaintResponse response = complaintMapper.toResponse(saved, securityUser.role());

        notifyClients();
        return response;
    }

    public ComplaintResponse getById(Long complaintId,
                                     SecurityUser securityUser) {
        Complaint complaint = findById(complaintId);
        if (securityUser.role() != Role.ADMIN && securityUser.role() != Role.SERVICE && securityUser.role() != Role.WAREHOUSE) {
            boolean assigned = complaint.getAssignedTo() != null && securityUser.id()
                    .equals(complaint.getAssignedTo().getId());
            if (!assigned) {
                throw new AccessDeniedException("Access denied: You are not assigned to this complaint");
            }
        }
        return complaintMapper.toResponse(complaint, securityUser.role());
    }

    public List<ComplaintResponse> getAll(SecurityUser securityUser) {
        List<Complaint> complaints;
        if (securityUser.role() != Role.ADMIN && securityUser.role() != Role.SERVICE && securityUser.role() != Role.WAREHOUSE) {
            complaints = complaintRepository.findAllByAssignedToId(securityUser.id());
        } else {
            complaints = complaintRepository.findAll();
        }
        return complaints.stream()
                .map(complaint -> complaintMapper.toResponse(complaint, securityUser.role()))
                .toList();
    }

    @Transactional
    public ComplaintResponse assignToUser(Long complaintId, ComplaintAssignmentRequest request,
                                          SecurityUser securityUser) {
        Complaint existing = findById(complaintId);
        transition(existing, ComplaintStatus.ASSIGNED);
        User assignedUser = userRepository.findById(request.assignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.assignedToId()));

        existing.setAssignedTo(assignedUser);
        existing.setAssignedDate(request.assignedDate());

        ComplaintResponse response = complaintMapper.toResponse(existing, securityUser.role());
        notifyClients();
        return response;
    }

    @Transactional
    public ComplaintResponse confirmPickup(Long complaintId, PickupConfirmationRequest request,
                                           SecurityUser securityUser) {
        Complaint existing = findById(complaintId);
        transition(existing, ComplaintStatus.ACCEPTED);
        existing.setPickupConfirmed(request.pickupConfirmed());

        ComplaintResponse response = complaintMapper.toResponse(existing, securityUser.role());
        notifyClients();
        return response;
    }

    @Transactional
    public ComplaintResponse confirmRepair(Long complaintId, RepairUpdateRequest request,
                                           SecurityUser securityUser) {
        Complaint existing = findById(complaintId);
        transition(existing, ComplaintStatus.REPAIRED);
        existing.setRepairConfirmed(request.repairDate());
        existing.setRepairDescription(request.repairDescription());

        ComplaintResponse response = complaintMapper.toResponse(existing, securityUser.role());
        notifyClients();
        return response;
    }

    @Transactional
    public ComplaintResponse confirmReturn(Long complaintId, ReturnConfirmationRequest request,
                                           SecurityUser securityUser) {
        Complaint existing = findById(complaintId);
        transition(existing, ComplaintStatus.RETURNED);
        existing.setReturnConfirmed(request.returnConfirmed());

        ComplaintResponse response = complaintMapper.toResponse(existing, securityUser.role());
        notifyClients();
        return response;
    }

    @Transactional
    public ComplaintResponse confirmShipment(Long complaintId, ShipmentConfirmationRequest request,
                                             SecurityUser securityUser) {
        Complaint existing = findById(complaintId);
        transition(existing, ComplaintStatus.SHIPPED);
        existing.setSentToClient(request.sentToClient());

        ComplaintResponse response = complaintMapper.toResponse(existing, securityUser.role());
        notifyClients();
        return response;
    }

    @Transactional
    public ComplaintResponse update(Long complaintId, ComplaintUpdateRequest request,
                                    SecurityUser securityUser) {
        Complaint existing = findById(complaintId);
        if (request.rmaNumber() != null
                && complaintRepository.existsByRmaNumberAndIdNot(request.rmaNumber(), complaintId)) {
            throw new ConflictException("Complaint with RMA number " + request.rmaNumber() + " already exists");
        }
        complaintMapper.updateEntity(existing, request);

        ComplaintResponse response = complaintMapper.toResponse(existing, securityUser.role());
        notifyClients();
        return response;
    }

    @Transactional
    public void deleteById(Long complaintId) {
        Complaint existing = findById(complaintId);
        complaintRepository.delete(existing);

        notifyClients();
    }

    private void transition(Complaint complaint, ComplaintStatus target) {
        ComplaintStatus current = complaint.getStatus();
        if (!current.canTransitionTo(target)) {
            throw new BusinessLogicException(
                    "Cannot change complaint status from " + current + " to " + target);
        }
        complaint.setStatus(target);
    }

    private Complaint findById(Long complaintId) {
        return complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", complaintId));
    }

}