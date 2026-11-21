package com.emedic.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "Prescription")
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prescriptionId;

    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status;

    @ManyToOne
    @JoinColumn(name = "consultationId", nullable = false)
    private Consultation consultation;

    private LocalDateTime prescribedDate;

    private LocalDateTime expiryDate;

    @OneToMany(
            mappedBy = "prescription",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PrescriptionItem> items = new ArrayList<>();

}
