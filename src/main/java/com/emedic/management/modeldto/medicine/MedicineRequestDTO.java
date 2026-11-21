package com.emedic.management.modeldto.medicine;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicineRequestDTO {
    @NotBlank(message = "Provide a Medicine Name")
    private String medicineName;

    @NotBlank(message = "Provide a Medicine Description")
    private String medicineDescription;

    @NotBlank(message = "Give Generic Use of the Medicine")
    private String genericUse;

    @NotBlank(message = "Provide activeIng")
    private String activeIng;

    @NotBlank(message = "Provide Medicine Strenght")
    private String strength;

}
