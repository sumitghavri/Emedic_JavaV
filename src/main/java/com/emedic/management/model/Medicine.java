package com.emedic.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "Medicine")

public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicineId;

    @NotBlank
    private String medicineName;

    @NotBlank
    private String medicineDescription;

    @NotBlank
    private String strength;

    @NotBlank
    private String genericUse;

    @NotBlank
    private String activeIng;




}
