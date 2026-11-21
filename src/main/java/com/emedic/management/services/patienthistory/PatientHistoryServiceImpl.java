package com.emedic.management.services.patienthistory;

import com.emedic.management.mapper.PatientHistoryMapper;
import com.emedic.management.mapper.PrescriptionMapper;
import com.emedic.management.model.Consultation;
import com.emedic.management.model.Patient;
import com.emedic.management.model.Prescription;
import com.emedic.management.modeldto.patienthistory.ConsultationHistoryDTO;
import com.emedic.management.modeldto.patienthistory.PatientHistoryDTO;
import com.emedic.management.repositories.ConsultationRepository;
import com.emedic.management.repositories.PatientRepository;
import com.emedic.management.repositories.PrescriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PatientHistoryServiceImpl implements PatientHistoryService {

    private final PatientRepository patientRepository;
    private final ConsultationRepository consultationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PatientHistoryMapper patientHistoryMapper;
    private final PrescriptionMapper prescriptionMapper;

    public PatientHistoryServiceImpl(PatientRepository patientRepository,
                                   ConsultationRepository consultationRepository,
                                   PrescriptionRepository prescriptionRepository,
                                   PatientHistoryMapper patientHistoryMapper,
                                   PrescriptionMapper prescriptionMapper) {
        this.patientRepository = patientRepository;
        this.consultationRepository = consultationRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.patientHistoryMapper = patientHistoryMapper;
        this.prescriptionMapper = prescriptionMapper;
    }

    @Override
    public PatientHistoryDTO getPatientHistory(Long patientId, int page, int size) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient id cannot be null");
        }
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("Patient not found with id: " + patientId));

        Pageable pageable = PageRequest.of(page, size);
        Page<Consultation> consultationPage = consultationRepository
                .findByAppointment_Patient_PatientIdOrderByConsultationDatetimeDesc(patientId, pageable);

        List<ConsultationHistoryDTO> consultations = consultationPage.getContent().stream()
                .map(patientHistoryMapper::toConsultationHistoryDTO)
                .collect(Collectors.toCollection(ArrayList::new));

        if (!consultationPage.isEmpty()) {
            List<Long> consultationIds = consultationPage.getContent().stream()
                    .map(Consultation::getConsultationId)
                    .toList();
            Map<Long, List<com.emedic.management.modeldto.prescription.PrescriptionResponseDTO>> prescriptionsByConsultation =
                    new HashMap<>();
            List<Prescription> prescriptions = prescriptionRepository.findWithItemsByConsultationIds(consultationIds);
            for (Prescription prescription : prescriptions) {
                Long consultationId = prescription.getConsultation().getConsultationId();
                prescriptionsByConsultation
                        .computeIfAbsent(consultationId, ignored -> new ArrayList<>())
                        .add(prescriptionMapper.toResponseDTO(prescription));
            }
            for (ConsultationHistoryDTO consultation : consultations) {
                consultation.setPrescriptions(prescriptionsByConsultation
                        .getOrDefault(consultation.getConsultationId(), List.of()));
            }
        }

        PatientHistoryDTO response = new PatientHistoryDTO();
        response.setPatient(patientHistoryMapper.toPatientHistoryPatientDTO(patient));
        response.setConsultations(consultations);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(consultationPage.getTotalElements());
        response.setTotalPages(consultationPage.getTotalPages());
        return response;
    }
}
