package com.github.vadymtrach.rmasystemshowcase.controller;


import com.github.vadymtrach.rmasystemshowcase.dto.request.*;
import com.github.vadymtrach.rmasystemshowcase.dto.response.ComplaintResponse;
import com.github.vadymtrach.rmasystemshowcase.security.SecurityUser;
import com.github.vadymtrach.rmasystemshowcase.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/complaints")
public class ComplaintController {
    private final ComplaintService complaintService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @PostMapping
    public ResponseEntity<ComplaintResponse> create(@Valid @RequestBody ComplaintCreateRequest request,
                                                    @AuthenticationPrincipal SecurityUser securityUser){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(complaintService.create(request, securityUser));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> getAll(@AuthenticationPrincipal SecurityUser securityUser){
        return ResponseEntity.ok(complaintService.getAll(securityUser));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getById(@PathVariable Long id,
                                                     @AuthenticationPrincipal SecurityUser securityUser){
        return ResponseEntity.ok(complaintService.getById(id, securityUser));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @PutMapping("/{id}")
    public ResponseEntity<ComplaintResponse> update(@PathVariable Long id, @Valid @RequestBody ComplaintUpdateRequest request,
                                                    @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(complaintService.update(id, request, securityUser));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @PatchMapping("/{id}/assign")
    public ResponseEntity<ComplaintResponse> assignToUser(@PathVariable Long id, @Valid @RequestBody ComplaintAssignmentRequest request,
                                                          @AuthenticationPrincipal SecurityUser securityUser){
        return ResponseEntity.ok(complaintService.assignToUser(id, request, securityUser));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @PatchMapping("/{id}/pickup")
    public ResponseEntity<ComplaintResponse> confirmPickup(@PathVariable Long id, @Valid @RequestBody PickupConfirmationRequest request,
                                                           @AuthenticationPrincipal SecurityUser securityUser){
        return ResponseEntity.ok(complaintService.confirmPickup(id, request, securityUser));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @PatchMapping("/{id}/repair")
    public ResponseEntity<ComplaintResponse> confirmRepair(@PathVariable Long id, @Valid @RequestBody RepairUpdateRequest request,
                                                           @AuthenticationPrincipal SecurityUser securityUser){
        return ResponseEntity.ok(complaintService.confirmRepair(id, request, securityUser));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @PatchMapping("/{id}/return")
    public ResponseEntity<ComplaintResponse> confirmReturn(@PathVariable Long id, @Valid @RequestBody ReturnConfirmationRequest request,
                                                           @AuthenticationPrincipal SecurityUser securityUser){
        return ResponseEntity.ok(complaintService.confirmReturn(id, request, securityUser));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @PatchMapping("/{id}/shipment")
    public ResponseEntity<ComplaintResponse> confirmShipment(@PathVariable Long id, @Valid @RequestBody ShipmentConfirmationRequest request,
                                                             @AuthenticationPrincipal SecurityUser securityUser){
        return ResponseEntity.ok(complaintService.confirmShipment(id, request, securityUser));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        complaintService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
