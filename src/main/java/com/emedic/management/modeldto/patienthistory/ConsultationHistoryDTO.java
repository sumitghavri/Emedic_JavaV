package com.emedic.management.modeldto.patienthistory;
import com.emedic.management.modeldto.prescription.PrescriptionResponseDTO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConsultationHistoryDTO {
    private Long consultationId;
    private LocalDateTime consultationDateTime;

    private String chiefComplaint;
    private String symptoms;
    private String diagnosis;
    private String examinationFindings;
    private String clinicalNotes;
    private String treatmentPlan;
    private LocalDateTime followUpDate;

    private List<PrescriptionResponseDTO> prescriptions;
}
