package com.emedic.management.modeldto.prescription;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionRequestDTO {

    @NotNull(message = "Consultation is required")
    private Long consultationId;

    @NotEmpty(message = "Prescription must contain at least one medicine")
    @Valid
    private List<PrescriptionItemRequestDTO> items;

    private LocalDateTime expiryDate;
}