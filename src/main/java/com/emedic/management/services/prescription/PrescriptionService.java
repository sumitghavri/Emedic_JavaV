package com.emedic.management.services.prescription;

import com.emedic.management.modeldto.prescription.PrescriptionRequestDTO;
import com.emedic.management.modeldto.prescription.PrescriptionResponseDTO;

import java.util.List;
import org.springframework.data.domain.Page;

public interface PrescriptionService {
    List<PrescriptionResponseDTO> getAllPrescriptions();
    Page<PrescriptionResponseDTO> getPrescriptions(int page, int size);
    PrescriptionResponseDTO getPrescriptionById(Long id);
    PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO requestDTO);
    PrescriptionResponseDTO updatePrescription(Long id, PrescriptionRequestDTO requestDTO);
    void deletePrescription(Long id);
    Page<PrescriptionResponseDTO> getPrescriptionsByConsultationId(Long consultationId, int page, int size);
}
