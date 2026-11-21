package com.emedic.management.services.appointment;

import com.emedic.management.modeldto.appointment.AppointmentBookingRequestDTO;
import com.emedic.management.modeldto.appointment.AppointmentResponseDTO;

import java.util.List;
import org.springframework.data.domain.Page;

public interface AppointmentService {
    List<AppointmentResponseDTO> getAllAppointments();
    Page<AppointmentResponseDTO> getAppointments(int page, int size);
    Page<AppointmentResponseDTO> getAppointmentsForDoctor(String email, int page, int size);
    AppointmentResponseDTO getAppointmentById(Long id);
    AppointmentResponseDTO createAppointment(AppointmentBookingRequestDTO requestDTO);
    AppointmentResponseDTO createOnlineAppointment(AppointmentBookingRequestDTO requestDTO);
    AppointmentResponseDTO updateAppointment(Long id, AppointmentBookingRequestDTO requestDTO);
    AppointmentResponseDTO confirmAppointment(Long id);
    void cancelAppointment(Long id);
}
