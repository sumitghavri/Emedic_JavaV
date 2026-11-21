package com.emedic.management.modeldto.patient;

import com.emedic.management.model.BloodType;
import com.emedic.management.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientRequestDTO {
        private String firstName;
        private String lastName;
        private BloodType bloodType;
        private Gender gender;
        private LocalDate dob;
        private String address;
        private String email;
        private String phone;
        private String allergies;
}
