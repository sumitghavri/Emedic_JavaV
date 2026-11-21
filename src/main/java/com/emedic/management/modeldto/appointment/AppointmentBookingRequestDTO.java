package com.emedic.management.modeldto.appointment;
import com.emedic.management.model.BloodType;
import com.emedic.management.model.Gender;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentBookingRequestDTO {

    private String firstName;

    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Enter a valid mobile number"
    )
    private String phone;

    @Email(message = "Enter a valid email address")
    private String email;

    private LocalDate dob;

    private Gender gender;

    private BloodType bloodType;

    private String address;

    private String allergies;

    @NotNull(message = "Doctor is required")
    private Long doctorId;

    @NotNull(message = "Appointment date and time is required")
    private LocalDateTime appointmentDateTime;

    @NotBlank(message = "Reason is required")
    private String reason;
}
