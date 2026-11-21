package com.emedic.management.services.patienthistory;

import com.emedic.management.mapper.PatientHistoryMapper;
import com.emedic.management.model.Patient;
import com.emedic.management.modeldto.patienthistory.PatientHistoryDTO;
import com.emedic.management.modeldto.patienthistory.PatientHistoryPatientDTO;
import com.emedic.management.repositories.ConsultationRepository;
import com.emedic.management.repositories.PatientRepository;
import com.emedic.management.repositories.PrescriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientHistoryServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PatientHistoryMapper patientHistoryMapper;

    @InjectMocks
    private PatientHistoryServiceImpl patientHistoryService;

    @Test
    void getPatientHistory_shouldReturnAggregatedHistory() {
        Patient patient = new Patient();
        patient.setPatientId(10L);
        patient.setFirstName("Aisha");
        patient.setLastName("Khan");
        patient.setDob(LocalDate.of(1995, 4, 12));

        PatientHistoryPatientDTO patientDto = new PatientHistoryPatientDTO();
        patientDto.setPatientId(10L);
        patientDto.setFirstName("Aisha");

        when(patientRepository.findById(10L)).thenReturn(Optional.of(patient));
        when(consultationRepository.findByAppointment_Patient_PatientIdOrderByConsultationDatetimeDesc(eq(10L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(patientHistoryMapper.toPatientHistoryPatientDTO(patient)).thenReturn(patientDto);

        PatientHistoryDTO result = patientHistoryService.getPatientHistory(10L, 0, 10);

        assertNotNull(result);
        assertEquals(10L, result.getPatient().getPatientId());
        assertEquals(0, result.getPage());
    }
}
