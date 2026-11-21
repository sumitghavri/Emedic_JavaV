package com.emedic.management.services.doctor;

import com.emedic.management.mapper.DoctorMapper;
import com.emedic.management.model.Doctor;
import com.emedic.management.modeldto.doctor.DoctorRequestDTO;
import com.emedic.management.modeldto.doctor.DoctorResponseDTO;
import com.emedic.management.repositories.DoctorRepository;
import com.emedic.management.repositories.AppointmentRepository;
import com.emedic.management.repositories.DoctorScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @Test
    void createDoctor_shouldSaveAndReturnDto() {
        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setName("Dr. Sharma");
        request.setLicenseNo("LIC-123");
        request.setEmail("sharma@example.com");
        request.setPhone("9988776655");
        request.setSpecialization("Cardiology");

        Doctor doctor = new Doctor();
        doctor.setDoctorId(10L);
        doctor.setName("Dr. Sharma");
        doctor.setLicenseNo("LIC-123");
        doctor.setEmail("sharma@example.com");
        doctor.setPhone("9988776655");
        doctor.setSpecialization("Cardiology");

        DoctorResponseDTO response = new DoctorResponseDTO();
        response.setDoctorId(10L);
        response.setName("Dr. Sharma");
        response.setLicenseNo("LIC-123");

        when(doctorRepository.findByLicenseNo("LIC-123")).thenReturn(Optional.empty());
        when(doctorMapper.toEntity(request)).thenReturn(doctor);
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(response);

        DoctorResponseDTO result = doctorService.createDoctor(request);

        assertNotNull(result);
        assertEquals("LIC-123", result.getLicenseNo());
        verify(doctorRepository).save(doctor);
    }
}
