package com.emedic.management.modeldto.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorBookingResponseDTO {

    private Long doctorId;

    private String name;

    private String specialization;
}