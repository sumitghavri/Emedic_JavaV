package com.emedic.management.modeldto.consultation;

import com.emedic.management.modeldto.prescription.PrescriptionResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalRecordResponseDTO {
    private ConsultationResponseDTO consultation;
    private PrescriptionResponseDTO prescription;
}
