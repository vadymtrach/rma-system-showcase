package com.github.vadymtrach.rmasystemshowcase.entity;

import com.github.vadymtrach.rmasystemshowcase.enums.ComplaintStatus;
import com.github.vadymtrach.rmasystemshowcase.enums.ProductType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter @Setter
public class Complaint {
    @Setter(AccessLevel.NONE)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String rmaNumber;

    @Enumerated(value = EnumType.STRING)
    private ProductType productType;

    private String description;

    @Enumerated(value = EnumType.STRING)
    private ComplaintStatus status = ComplaintStatus.NEW;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    private LocalDate assignedDate;

    private LocalDate pickupConfirmed;

    private String repairDescription;

    private LocalDate repairConfirmed;

    private LocalDate returnConfirmed;

    private LocalDate sentToClient;

    private String deliveryAddress;

    private BigDecimal insuranceAmount;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Setter(AccessLevel.NONE)
    @Version
    private Long version;

}
