package com.emedic.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "consultation")

public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consultationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointmentId", nullable = false, unique = true)
    private Appointment appointment;

    private LocalDateTime consultationDatetime;

    @NotBlank
    private String symptoms;

    @NotBlank
    private String chiefComplaint;

    @NotBlank
    private  String clinicalNotes;

    @NotBlank
    private String diagnosis;

    @NotBlank
    private String examinationFindings;

    @NotBlank
    private String treatmentPlan;

    private LocalDateTime followUpdate;
}
