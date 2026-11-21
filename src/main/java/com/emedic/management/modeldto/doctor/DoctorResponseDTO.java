package com.emedic.management.modeldto.doctor;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponseDTO {
    private Long doctorId;
    private String name;
    private String licenseNo;
    private String email;
    private String specialization;
    private String phone;
}
