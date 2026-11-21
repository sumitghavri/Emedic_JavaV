package com.emedic.management.modeldto.appointment;

import com.emedic.management.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentQueueResponseDTO {

    private Long appointmentId;

    private Long patientId;
    private String patientName;

    private LocalDateTime appointmentDateTime;

    private AppointmentStatus status;

    private String reason;
}