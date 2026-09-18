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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ComplaintMapper complaintMapper;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String WS_TOPIC = "/topic/rma-updates";

    private static final Set<Role> ASSIGNABLE_ROLES = Set.of(Role.EMPLOYEE, Role.SERVICE);

    /**
     * Server and browser dates can differ by a day around midnight (the server runs in UTC),
     * so a date one day ahead of the server's "today" is still accepted.
     */
    private static final int FUTURE_DATE_TOLERANCE_DAYS = 1;

    /**
     * Broadcasts only after the transaction commits; clients refetch on this message, and
     * sending it earlier lets them read the data before the change is visible.
     */
    private void notifyClients() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            messagingTemplate.convertAndSend(WS_TOPIC, "REFRESH");
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend(WS_TOPIC, "REFRESH");
            }
        });
    }

    /**
     * Flushes so the response carries the incremented version and audit timestamps,
     * then schedules the client notification.
     */
    private ComplaintResponse respond(Complaint complaint, SecurityUser securityUser) {
        complaintRepository.flush();
        notifyClients();
        return complaintMapper.toResponse(complaint, securityUser.role());
    }


    @Transactional
    public ComplaintResponse create(ComplaintCreateRequest request,
                                    SecurityUser securityUser) {
        if (complaintRepository.existsByRmaNumber(request.rmaNumber())) {
            throw new ConflictException("Complaint with RMA number " + request.rmaNumber() + " already exists");
        }
        Complaint complaint = complaintMapper.toEntity(request);
        return respond(complaintRepository.save(complaint), securityUser);
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
        if (!assignedUser.isActive()) {
            throw new BusinessLogicException("Cannot assign a complaint to an inactive user");
        }
        if (!ASSIGNABLE_ROLES.contains(assignedUser.getRole())) {
            throw new BusinessLogicException("Complaints can only be assigned to employees or service engineers");
        }
        validateDate(request.assignedDate(), "Assignment date", null, null);

        existing.setAssignedTo(assignedUser);
        existing.setAssignedDate(request.assignedDate());
        return respond(existing, securityUser);
    }

    @Transactional
    public ComplaintResponse confirmPickup(Long complaintId, PickupConfirmationRequest request,
                                           SecurityUser securityUser) {
        Complaint existing = findById(complaintId);
        transition(existing, ComplaintStatus.ACCEPTED);
        validateDate(request.pickupConfirmed(), "Pickup date", existing.getAssignedDate(), "assignment date");
        existing.setPickupConfirmed(request.pickupConfirmed());
        return respond(existing, securityUser);
    }

    @Transactional
    public ComplaintResponse confirmRepair(Long complaintId, RepairUpdateRequest request,
                                           SecurityUser securityUser) {
        Complaint existing = findById(complaintId);
        transition(existing, ComplaintStatus.REPAIRED);
        validateDate(request.repairDate(), "Repair date", existing.getPickupConfirmed(), "pickup date");
        existing.setRepairConfirmed(request.repairDate());
        existing.setRepairDescription(request.repairDescription());
        return respond(existing, securityUser);
    }

    @Transactional
    public ComplaintResponse confirmReturn(Long complaintId, ReturnConfirmationRequest request,
                                           SecurityUser securityUser) {
        Complaint existing = findById(complaintId);
        transition(existing, ComplaintStatus.RETURNED);
        validateDate(request.returnConfirmed(), "Return date", existing.getRepairConfirmed(), "repair date");
        existing.setReturnConfirmed(request.returnConfirmed());
        return respond(existing, securityUser);
    }

    @Transactional
    public ComplaintResponse confirmShipment(Long complaintId, ShipmentConfirmationRequest request,
                                             SecurityUser securityUser) {
        Complaint existing = findById(complaintId);
        transition(existing, ComplaintStatus.SHIPPED);
        validateDate(request.sentToClient(), "Shipment date", existing.getReturnConfirmed(), "return date");
        existing.setSentToClient(request.sentToClient());
        return respond(existing, securityUser);
    }

    @Transactional
    public ComplaintResponse update(Long complaintId, ComplaintUpdateRequest request,
                                    SecurityUser securityUser) {
        Complaint existing = findById(complaintId);
        if (!existing.getVersion().equals(request.version())) {
            throw new ConflictException("This complaint was changed by someone else. Reload it and try again.");
        }
        if (request.rmaNumber() != null
                && complaintRepository.existsByRmaNumberAndIdNot(request.rmaNumber(), complaintId)) {
            throw new ConflictException("Complaint with RMA number " + request.rmaNumber() + " already exists");
        }
        complaintMapper.updateEntity(existing, request);
        return respond(existing, securityUser);
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

    private void validateDate(LocalDate date, String label, LocalDate notBefore, String notBeforeLabel) {
        if (date.isAfter(LocalDate.now().plusDays(FUTURE_DATE_TOLERANCE_DAYS))) {
            throw new BusinessLogicException(label + " cannot be in the future");
        }
        if (notBefore != null && date.isBefore(notBefore)) {
            throw new BusinessLogicException(
                    label + " cannot be before the " + notBeforeLabel + " (" + notBefore + ")");
        }
    }

    private Complaint findById(Long complaintId) {
        return complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", complaintId));
    }

}
