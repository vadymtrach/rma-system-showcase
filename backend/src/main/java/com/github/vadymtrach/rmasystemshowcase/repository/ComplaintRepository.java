package com.github.vadymtrach.rmasystemshowcase.repository;

import com.github.vadymtrach.rmasystemshowcase.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findAllByAssignedToId(Long assignedToId);

    boolean existsByRmaNumber(String rmaNumber);

    boolean existsByRmaNumberAndIdNot(String rmaNumber, Long id);
}
