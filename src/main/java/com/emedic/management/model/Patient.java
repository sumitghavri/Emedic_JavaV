package com.emedic.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Patient")

public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId;

    @NotBlank(message = " Enter Your First Name:")
    private String firstName;

    @NotBlank(message = " Enter Your Last Name:")
    private String lastName;

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull(message = " Enter Your Date OF Birth:")
    private LocalDate dob;

    @NotBlank(message = " Enter Your Address:")
    private String address;

    @NotBlank(message = " Enter Your Mail:")
    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Enter a valid Mobile Number"
    )
    private String phone;

    @NotBlank
    private String allergies;

}
