package com.emedic.management.modeldto.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientBookingResponseDTO {

    private Long patientId;

    private String firstName;

    private String lastName;

    private String phone;

    private String email;

    private LocalDate dob;

    private Integer age;

    private String gender;

    private String bloodType;

    private String address;

    private String allergies;
}