package com.emedic.management.modeldto.appointment;

import com.emedic.management.model.AppointmentStatus;
import com.emedic.management.model.BookingSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentBookingResponseDTO {

    private Long appointmentId;

    private PatientBookingResponseDTO patient;

    private DoctorBookingResponseDTO doctor;

    private LocalDateTime appointmentDateTime;

    private String reason;

    private AppointmentStatus status;

    private BookingSource bookingSource;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}