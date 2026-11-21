package com.emedic.management.services.consultation;

import com.emedic.management.modeldto.consultation.ClinicalRecordRequestDTO;
import com.emedic.management.modeldto.consultation.ClinicalRecordResponseDTO;
import com.emedic.management.modeldto.prescription.PrescriptionRequestDTO;
import com.emedic.management.services.prescription.PrescriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicalRecordService {
    private final ConsultationService consultationService;
    private final PrescriptionService prescriptionService;

    public ClinicalRecordService(ConsultationService consultationService, PrescriptionService prescriptionService) {
        this.consultationService = consultationService;
        this.prescriptionService = prescriptionService;
    }

    @Transactional
    public ClinicalRecordResponseDTO create(ClinicalRecordRequestDTO request, String clinicianEmail, boolean clinicianOnly) {
        if (request == null || request.getConsultation() == null) {
            throw new IllegalArgumentException("Consultation details are required");
        }
        var consultation = clinicianOnly
                ? consultationService.createConsultationForDoctor(request.getConsultation(), clinicianEmail)
                : consultationService.createConsultation(request.getConsultation());

        var items = request.getPrescriptionItems();
        if (items == null || items.isEmpty()) {
            return new ClinicalRecordResponseDTO(consultation, null);
        }

        var prescriptionRequest = new PrescriptionRequestDTO();
        prescriptionRequest.setConsultationId(consultation.getConsultationId());
        prescriptionRequest.setItems(items);
        prescriptionRequest.setExpiryDate(request.getPrescriptionExpiryDate());
        var prescription = prescriptionService.createPrescription(prescriptionRequest);
        return new ClinicalRecordResponseDTO(consultation, prescription);
    }
}
