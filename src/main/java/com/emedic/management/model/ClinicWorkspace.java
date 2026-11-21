package com.emedic.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "clinic_workspace") @Getter @Setter
public class ClinicWorkspace {
    @Id private Long id = 1L;
    @Column(nullable = false) private String name = "Northstar Clinic";
    private String address;
    private String phone;
    private String email;
    private String website;
}
