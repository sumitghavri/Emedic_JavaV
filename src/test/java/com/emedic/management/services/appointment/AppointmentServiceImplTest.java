package com.emedic.management.services.appointment;

import com.emedic.management.mapper.AppointmentMapper;
import com.emedic.management.mapper.PatientMapper;
import com.emedic.management.model.*;
import com.emedic.management.modeldto.appointment.AppointmentBookingRequestDTO;
import com.emedic.management.modeldto.appointment.AppointmentResponseDTO;
import com.emedic.management.repositories.AppointmentRepository;
import com.emedic.management.repositories.DoctorRepository;
import com.emedic.management.repositories.DoctorScheduleRepository;
import com.emedic.management.repositories.PatientRepository;
import com.emedic.management.services.patient.PatientPhoneLockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private PatientPhoneLockService patientPhoneLockService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void createAppointment_shouldCreatePatientAndSaveAppointment() {
        AppointmentBookingRequestDTO request = new AppointmentBookingRequestDTO();
        request.setFirstName("Sara");
        request.setLastName("Ali");
        request.setPhone("9876543210");
        request.setEmail("sara@example.com");
        request.setDob(LocalDate.of(1992, 5, 10));
        request.setGender(Gender.FEMALE);
        request.setDoctorId(7L);
        request.setAppointmentDateTime(LocalDateTime.of(2027, 10, 15, 10, 30));
        request.setReason("General checkup");
        request.setAddress("Sample address");
        request.setAllergies("None reported");

        Doctor doctor = new Doctor();
        doctor.setDoctorId(7L);
        doctor.setName("Dr. Mehta");

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(DayOfWeek.FRIDAY);
        schedule.setStartTime(java.time.LocalTime.of(9, 0));
        schedule.setEndTime(java.time.LocalTime.of(12, 0));
        schedule.setSlotDuration(30);

        Patient patient = new Patient();
        patient.setPatientId(55L);
        patient.setFirstName("Sara");
        patient.setLastName("Ali");
        patient.setPhone("9876543210");
        patient.setEmail("sara@example.com");

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(100L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(request.getAppointmentDateTime());
        appointment.setReason("General checkup");
        appointment.setStatus(AppointmentStatus.REQUESTED);
        appointment.setBookingSource(BookingSource.ONLINE);

        AppointmentResponseDTO response = new AppointmentResponseDTO();
        response.setAppointmentId(100L);
        response.setDoctorId(7L);
        response.setPatientId(55L);
        response.setReason("General checkup");
        response.setStatus(AppointmentStatus.REQUESTED);

        when(patientRepository.findByPhone("9876543210")).thenReturn(Optional.empty());
        when(patientMapper.toEntity(request)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(patient);
        when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findForUpdateByDoctorIdAndDayOfWeek(7L, DayOfWeek.FRIDAY))
                .thenReturn(Optional.of(schedule));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponseDTO(appointment)).thenReturn(response);

        AppointmentResponseDTO result = appointmentService.createAppointment(request);

        assertNotNull(result);
        assertEquals(100L, result.getAppointmentId());
        assertEquals(AppointmentStatus.REQUESTED, result.getStatus());
        verify(appointmentRepository).save(any(Appointment.class));
    }
}
