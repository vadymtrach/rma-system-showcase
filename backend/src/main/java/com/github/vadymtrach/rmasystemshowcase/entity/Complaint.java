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
    private String rmaNumber; // rma

    @Enumerated(value = EnumType.STRING)
    private ProductType productType; // nazwa produktu

    private String description; // opis usterki

    @Enumerated(value = EnumType.STRING)
    private ComplaintStatus status = ComplaintStatus.NEW;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo; // przekazano dla

    private LocalDate assignedDate; // data przekazania

    private LocalDate pickupConfirmed; // potwierdzenie odbioru

    private String repairDescription; // co zostalo naprawione

    private LocalDate repairConfirmed; // oddane po naprawie (data)

    private LocalDate returnConfirmed; // potwierdzenie oddania

    private LocalDate sentToClient; // odeslanie do klienta

    private String deliveryAddress; // adres odsylki

    private BigDecimal insuranceAmount; // kwota ubezpieczenia

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
