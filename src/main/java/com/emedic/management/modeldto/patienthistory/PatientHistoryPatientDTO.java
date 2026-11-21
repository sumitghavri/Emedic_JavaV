package com.emedic.management.modeldto.patienthistory;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientHistoryPatientDTO {
        private Long patientId;
        private String firstName;
        private String lastName;
        private String phone;
        private LocalDate dob;
        private Integer age;
        private String gender;
        private String bloodType;
        private String allergies;
    }
