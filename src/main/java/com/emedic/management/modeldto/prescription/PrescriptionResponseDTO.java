package com.emedic.management.modeldto.prescription;

import com.emedic.management.model.PrescriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionResponseDTO {

    private Long prescriptionId;

    private Long consultationId;

    private LocalDateTime prescribedDate;

    private LocalDateTime expiryDate;

    private PrescriptionStatus status;

    private List<PrescriptionItemResponseDTO> items;
}