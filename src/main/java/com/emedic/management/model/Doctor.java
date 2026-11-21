package com.emedic.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Doctor")

public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId;

    @NotBlank
    private String name;

    @NotBlank
    private String specialization;

    @Column(unique = true, nullable = false)
    private String licenseNo;

    @Column(unique = true, nullable = false)
    @NotBlank @Email
    private String email;

    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Enter a valid Mobile Number"
    )
    private String phone;

}
