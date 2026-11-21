package com.emedic.management.services.patient;

import com.emedic.management.mapper.PatientMapper;
import com.emedic.management.model.Patient;
import com.emedic.management.modeldto.patient.PatientRequestDTO;
import com.emedic.management.modeldto.patient.PatientResponseDTO;
import com.emedic.management.repositories.PatientRepository;
import com.emedic.management.services.patient.PatientPhoneLockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private PatientPhoneLockService patientPhoneLockService;

    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    void createPatient_shouldSaveAndReturnDto() {
        PatientRequestDTO request = new PatientRequestDTO();
        request.setFirstName("Aisha");
        request.setLastName("Khan");
        request.setEmail("aisha@example.com");
        request.setPhone("9876543210");
        request.setDob(LocalDate.of(1995, 7, 15));

        Patient patient = new Patient();
        patient.setPatientId(1L);
        patient.setFirstName("Aisha");
        patient.setLastName("Khan");
        patient.setEmail("aisha@example.com");
        patient.setPhone("9876543210");

        PatientResponseDTO response = new PatientResponseDTO();
        response.setPatientId(1L);
        response.setFirstName("Aisha");
        response.setLastName("Khan");
        response.setEmail("aisha@example.com");
        response.setPhone("9876543210");

        when(patientRepository.findByPhone("9876543210")).thenReturn(Optional.empty());
        when(patientMapper.toEntity(request)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toResponseDTO(patient)).thenReturn(response);

        PatientResponseDTO result = patientService.createPatient(request);

        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertEquals("Aisha", result.getFirstName());
        verify(patientRepository).save(patient);
    }

    @Test
    void getAllPatients_shouldReturnMappedList() {
        Patient patient = new Patient();
        patient.setPatientId(2L);
        patient.setFirstName("Rahul");
        patient.setLastName("Verma");

        PatientResponseDTO response = new PatientResponseDTO();
        response.setPatientId(2L);
        response.setFirstName("Rahul");

        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientMapper.toResponseDTO(patient)).thenReturn(response);

        List<PatientResponseDTO> result = patientService.getAllPatients();

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getPatientId());
    }
}
