package com.emedic.management.services.consultation;

import com.emedic.management.mapper.ConsultationMapper;
import com.emedic.management.model.Appointment;
import com.emedic.management.model.AppointmentStatus;
import com.emedic.management.model.Consultation;
import com.emedic.management.modeldto.consultation.ConsultationRequestDTO;
import com.emedic.management.modeldto.consultation.ConsultationResponseDTO;
import com.emedic.management.repositories.AppointmentRepository;
import com.emedic.management.repositories.ConsultationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceImplTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ConsultationMapper consultationMapper;

    @InjectMocks
    private ConsultationServiceImpl consultationService;

    @Test
    void createConsultation_shouldSaveConsultationWhenAppointmentExists() {
        ConsultationRequestDTO request = new ConsultationRequestDTO();
        request.setAppointmentId(11L);
        request.setChiefComplaint("Fever");
        request.setSymptoms("Body ache");
        request.setClinicalNotes("Observed low energy");
        request.setDiagnosis("Viral fever");
        request.setExaminationFinding("No dehydration");
        request.setTreatmentPlan("Rest and fluids");
        request.setFollowUpdate(LocalDateTime.of(2026, 10, 20, 10, 30));

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(11L);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        Consultation consultation = new Consultation();
        consultation.setConsultationId(99L);
        consultation.setAppointment(appointment);
        consultation.setSymptoms("Body ache");
        consultation.setChiefComplaint("Fever");
        consultation.setConsultationDatetime(LocalDateTime.now());

        ConsultationResponseDTO response = new ConsultationResponseDTO();
        response.setConsultationId(99L);
        response.setAppointmentId(11L);
        response.setChiefComplaint("Fever");

        when(appointmentRepository.findLockedByAppointmentId(11L)).thenReturn(Optional.of(appointment));
        when(consultationRepository.existsByAppointment_AppointmentId(11L)).thenReturn(false);
        when(consultationMapper.toEntity(request)).thenReturn(consultation);
        when(consultationRepository.save(any(Consultation.class))).thenReturn(consultation);
        when(consultationMapper.toResponseDTO(consultation)).thenReturn(response);

        ConsultationResponseDTO result = consultationService.createConsultation(request);

        assertNotNull(result);
        assertEquals(99L, result.getConsultationId());
        assertEquals(11L, result.getAppointmentId());
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        verify(consultationRepository).save(any(Consultation.class));
    }

    @Test
    void getConsultationsByPatientId_shouldReturnConsultationsForPatient() {
        Consultation consultation = new Consultation();
        consultation.setConsultationId(21L);
        when(consultationRepository.findByAppointment_Patient_PatientIdOrderByConsultationDatetimeDesc(eq(5L), any())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(consultation)));
        when(consultationMapper.toResponseDTO(consultation)).thenReturn(new ConsultationResponseDTO());

        org.springframework.data.domain.Page<ConsultationResponseDTO> result =
                consultationService.getConsultationsByPatientId(5L, 0, 20);

        assertEquals(1, result.getContent().size());
        verify(consultationRepository).findByAppointment_Patient_PatientIdOrderByConsultationDatetimeDesc(eq(5L), any());
    }
}
