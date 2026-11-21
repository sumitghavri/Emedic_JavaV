package com.emedic.management.services.consultation;

import com.emedic.management.modeldto.consultation.ConsultationRequestDTO;
import com.emedic.management.modeldto.consultation.ConsultationResponseDTO;

import java.util.List;
import org.springframework.data.domain.Page;

public interface ConsultationService {
    List<ConsultationResponseDTO> getAllConsultations();
    Page<ConsultationResponseDTO> getConsultations(int page, int size);
    Page<ConsultationResponseDTO> getConsultationsForDoctor(String email, int page, int size);
    ConsultationResponseDTO getConsultationById(Long id);
    ConsultationResponseDTO createConsultation(ConsultationRequestDTO requestDTO);
    ConsultationResponseDTO createConsultationForDoctor(ConsultationRequestDTO requestDTO, String doctorEmail);
    ConsultationResponseDTO updateConsultation(Long id, ConsultationRequestDTO requestDTO);
    void deleteConsultation(Long id);
    Page<ConsultationResponseDTO> getConsultationsByPatientId(Long patientId, int page, int size);
}
