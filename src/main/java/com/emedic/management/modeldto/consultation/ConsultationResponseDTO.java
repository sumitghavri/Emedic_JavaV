package com.emedic.management.modeldto.consultation;

import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ConsultationResponseDTO {

    private Long consultationId;

    private Long appointmentId;

    private LocalDateTime consultationDatetime;

    private String chiefComplaint;

    private String symptoms;

    private String clinicalNotes;

    private String diagnosis;

    private String examinationFinding;

    private String treatmentPlan;

    private LocalDateTime followUpdate;
}
