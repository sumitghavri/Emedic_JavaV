package com.emedic.management.modeldto.consultation;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ConsultationRequestDTO {
    private Long appointmentId;

    @NotBlank(message = "Enter your Reason for Visit")
    private String chiefComplaint;

    @NotBlank(message = "Enter your Symptoms")
    private String symptoms;

    @NotBlank(message = "Provide ClinicalNotes")
    private String clinicalNotes;

    @NotBlank(message = "Provide Diagnosis")
    private String diagnosis;

    @NotBlank(message = "List Physical Examination Findings")
    private String examinationFinding;

    @NotBlank(message = "Provide Treatment Plan")
    private String treatmentPlan;

    private LocalDateTime followUpdate;


}
