package com.emedic.management.modeldto.prescription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionItemRequestDTO {

    @NotNull(message = "Medicine is required")
    private Long medicineId;

    @NotBlank(message = "Dosage is required")
    private String dosage;

    @NotBlank(message = "Frequency is required")
    private String frequency;

    @NotNull(message = "Duration is required")
    private Integer durationDays;

    @NotBlank(message = "Instructions are required")
    private String instructions;
}