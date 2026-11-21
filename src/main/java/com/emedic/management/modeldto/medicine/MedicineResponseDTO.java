package com.emedic.management.modeldto.medicine;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicineResponseDTO {
    private Long medicineId;

    private String medicineName;

    private String medicineDescription;

    private String genericUse;

    private String activeIng;

    private String strength;
}