package com.emedic.management.services.prescription;

import com.emedic.management.mapper.PrescriptionItemMapper;
import com.emedic.management.mapper.PrescriptionMapper;
import com.emedic.management.model.Consultation;
import com.emedic.management.model.Medicine;
import com.emedic.management.model.Prescription;
import com.emedic.management.model.PrescriptionItem;
import com.emedic.management.model.PrescriptionStatus;
import com.emedic.management.modeldto.prescription.PrescriptionItemRequestDTO;
import com.emedic.management.modeldto.prescription.PrescriptionRequestDTO;
import com.emedic.management.modeldto.prescription.PrescriptionResponseDTO;
import com.emedic.management.repositories.ConsultationRepository;
import com.emedic.management.repositories.MedicineRepository;
import com.emedic.management.repositories.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import com.emedic.management.util.Pagination;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final ConsultationRepository consultationRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository,
                                  ConsultationRepository consultationRepository,
                                  MedicineRepository medicineRepository,
                                  PrescriptionMapper prescriptionMapper,
                                  PrescriptionItemMapper prescriptionItemMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.consultationRepository = consultationRepository;
        this.medicineRepository = medicineRepository;
        this.prescriptionMapper = prescriptionMapper;
        this.prescriptionItemMapper = prescriptionItemMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getAllPrescriptions() {
        return prescriptionRepository.findAll().stream()
                .map(prescriptionMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionResponseDTO> getPrescriptions(int page, int size) {
        return prescriptionRepository.findAll(Pagination.request(page, size)).map(prescriptionMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDTO getPrescriptionById(Long id) {
        return prescriptionMapper.toResponseDTO(findPrescriptionById(id));
    }

    @Override
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Prescription request cannot be null");
        }
        if (requestDTO.getConsultationId() == null) {
            throw new IllegalArgumentException("Consultation id is required");
        }
        if (requestDTO.getItems() == null || requestDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("Prescription must contain at least one item");
        }
        if (requestDTO.getExpiryDate() != null && !requestDTO.getExpiryDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Prescription expiry must be in the future");
        }
        requestDTO.getItems().forEach(this::validatePrescriptionItem);
        Map<Long, Medicine> medicines = resolveMedicines(requestDTO.getItems());

        Consultation consultation = consultationRepository.findLockedByConsultationId(requestDTO.getConsultationId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Consultation not found with id: " + requestDTO.getConsultationId()));
        if (prescriptionRepository.existsByConsultation_ConsultationIdAndStatus(
                consultation.getConsultationId(), PrescriptionStatus.ACTIVE)) {
            throw new IllegalStateException("An active prescription already exists for this consultation; cancel it before creating a replacement");
        }

        Prescription prescription = prescriptionMapper.toEntity(requestDTO);
        if (prescription == null) {
            prescription = new Prescription();
        }
        prescription.setConsultation(consultation);
        prescription.setStatus(PrescriptionStatus.ACTIVE);
        prescription.setPrescribedDate(LocalDateTime.now());
        prescription.setExpiryDate(requestDTO.getExpiryDate());

        List<PrescriptionItem> items = new ArrayList<>();
        for (PrescriptionItemRequestDTO itemRequest : requestDTO.getItems()) {
            PrescriptionItem item = prescriptionItemMapper.toEntity(itemRequest);
            if (item == null) {
                item = new PrescriptionItem();
            }
            item.setMedicine(medicines.get(itemRequest.getMedicineId()));
            item.setPrescription(prescription);
            items.add(item);
        }
        prescription.setItems(items);

        Prescription saved = prescriptionRepository.save(prescription);
        return prescriptionMapper.toResponseDTO(saved);
    }

    @Override
    public PrescriptionResponseDTO updatePrescription(Long id, PrescriptionRequestDTO requestDTO) {
        throw new IllegalStateException(
                "Prescriptions are immutable after creation; cancel this prescription and create a replacement to preserve the clinical history");
    }

    @Override
    public void deletePrescription(Long id) {
        Prescription prescription = findPrescriptionById(id);
        if (prescription.getStatus() == PrescriptionStatus.CANCELLED) {
            return;
        }
        if (prescription.getStatus() != PrescriptionStatus.ACTIVE) {
            throw new IllegalStateException("Only an active prescription can be cancelled");
        }
        prescription.setStatus(PrescriptionStatus.CANCELLED);
        prescriptionRepository.save(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionResponseDTO> getPrescriptionsByConsultationId(Long consultationId, int page, int size) {
        if (consultationId == null) {
            throw new IllegalArgumentException("Consultation id cannot be null");
        }
        return prescriptionRepository.findByConsultation_ConsultationId(consultationId, Pagination.request(page, size))
                .map(prescriptionMapper::toResponseDTO);
    }

    private Prescription findPrescriptionById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Prescription not found with id: " + id));
    }

    private void validatePrescriptionItem(PrescriptionItemRequestDTO itemRequest) {
        if (itemRequest == null || itemRequest.getMedicineId() == null
                || itemRequest.getDosage() == null || itemRequest.getDosage().isBlank()
                || itemRequest.getFrequency() == null || itemRequest.getFrequency().isBlank()
                || itemRequest.getDurationDays() == null || itemRequest.getDurationDays() <= 0
                || itemRequest.getInstructions() == null || itemRequest.getInstructions().isBlank()) {
            throw new IllegalArgumentException("Each prescription item requires a medicine, dosage, frequency, positive duration, and instructions");
        }
    }

    private Map<Long, Medicine> resolveMedicines(List<PrescriptionItemRequestDTO> itemRequests) {
        Set<Long> medicineIds = new HashSet<>();
        itemRequests.forEach(item -> medicineIds.add(item.getMedicineId()));

        Map<Long, Medicine> medicinesById = new HashMap<>();
        medicineRepository.findAllById(medicineIds)
                .forEach(medicine -> medicinesById.put(medicine.getMedicineId(), medicine));
        if (medicinesById.size() != medicineIds.size()) {
            Set<Long> missingMedicineIds = new HashSet<>(medicineIds);
            missingMedicineIds.removeAll(medicinesById.keySet());
            throw new NoSuchElementException("Medicine not found for id(s): " + missingMedicineIds);
        }
        return medicinesById;
    }
}
