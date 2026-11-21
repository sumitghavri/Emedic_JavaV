package com.emedic.management.modeldto.consultation;

import com.emedic.management.modeldto.prescription.PrescriptionItemRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalRecordRequestDTO {
    @Valid
    @NotNull(message = "Consultation details are required")
    private ConsultationRequestDTO consultation;

    @Valid
    private List<PrescriptionItemRequestDTO> prescriptionItems;

    private LocalDateTime prescriptionExpiryDate;
}
