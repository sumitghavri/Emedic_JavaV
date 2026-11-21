package com.emedic.management.modeldto.prescription;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionItemResponseDTO {

    private Long itemId;

    private Long medicineId;

    private String medicineName;

    private String strength;

    private String genericUse;

    private String dosage;

    private String frequency;

    private Integer durationDays;

    private String instructions;
}