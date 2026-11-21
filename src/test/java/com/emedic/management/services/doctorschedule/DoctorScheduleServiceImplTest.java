package com.emedic.management.services.doctorschedule;

import com.emedic.management.mapper.DoctorScheduleMapper;
import com.emedic.management.model.Doctor;
import com.emedic.management.model.DoctorSchedule;
import com.emedic.management.modeldto.doctorschedule.DoctorScheduleRequestDTO;
import com.emedic.management.modeldto.doctorschedule.DoctorScheduleResponseDTO;
import com.emedic.management.repositories.DoctorRepository;
import com.emedic.management.repositories.DoctorScheduleRepository;
import com.emedic.management.repositories.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceImplTest {

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorScheduleMapper doctorScheduleMapper;

    @InjectMocks
    private DoctorScheduleServiceImpl doctorScheduleService;

    @Test
    void createSchedule_shouldSaveAndReturnDto() {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(1L);
        doctor.setName("Dr. Ali");

        DoctorScheduleRequestDTO request = new DoctorScheduleRequestDTO();
        request.setDoctorId(1L);
        request.setDayOfWeek("MONDAY");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(12, 0));
        request.setSlotDuration(30);

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setScheduleId(10L);
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(12, 0));
        schedule.setSlotDuration(30);

        DoctorScheduleResponseDTO response = new DoctorScheduleResponseDTO();
        response.setScheduleId(10L);
        response.setDoctorId(1L);
        response.setDayOfWeek("MONDAY");

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctor_DoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(Optional.empty());
        when(doctorScheduleMapper.toEntity(request)).thenReturn(schedule);
        when(doctorScheduleRepository.save(schedule)).thenReturn(schedule);
        when(doctorScheduleMapper.toResponseDTO(schedule)).thenReturn(response);

        DoctorScheduleResponseDTO result = doctorScheduleService.createSchedule(request);

        assertNotNull(result);
        assertEquals(10L, result.getScheduleId());
        assertEquals("MONDAY", result.getDayOfWeek());
        verify(doctorScheduleRepository).save(schedule);
    }
}
