package com.emedic.management.services.medicine;

import com.emedic.management.mapper.MedicineMapper;
import com.emedic.management.model.Medicine;
import com.emedic.management.modeldto.medicine.MedicineRequestDTO;
import com.emedic.management.modeldto.medicine.MedicineResponseDTO;
import com.emedic.management.repositories.MedicineRepository;
import com.emedic.management.repositories.PrescriptionItemRepository;
import com.emedic.management.error.DuplicateResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import com.emedic.management.util.Pagination;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final MedicineMapper medicineMapper;

    public MedicineServiceImpl(MedicineRepository medicineRepository, MedicineMapper medicineMapper,
                               PrescriptionItemRepository prescriptionItemRepository) {
        this.medicineRepository = medicineRepository;
        this.medicineMapper = medicineMapper;
        this.prescriptionItemRepository = prescriptionItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(medicineMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicineResponseDTO> getMedicines(int page, int size) {
        return medicineRepository.findAll(Pagination.request(page, size)).map(medicineMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponseDTO getMedicineById(Long id) {
        return medicineMapper.toResponseDTO(findMedicineById(id));
    }

    @Override
    public MedicineResponseDTO createMedicine(MedicineRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Medicine request cannot be null");
        }

        if (requestDTO.getMedicineName() != null
                && medicineRepository.findByMedicineName(requestDTO.getMedicineName()).isPresent()) {
            throw new DuplicateResourceException("Medicine with this name already exists");
        }

        Medicine medicine = medicineMapper.toEntity(requestDTO);
        Medicine savedMedicine = medicineRepository.save(medicine);
        return medicineMapper.toResponseDTO(savedMedicine);
    }

    @Override
    public MedicineResponseDTO updateMedicine(Long id, MedicineRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Medicine request cannot be null");
        }

        Medicine existingMedicine = findMedicineById(id);

        if (prescriptionItemRepository.existsByMedicine_MedicineId(id)
                && (requestDTO.getMedicineName() != null && !requestDTO.getMedicineName().equals(existingMedicine.getMedicineName())
                || requestDTO.getStrength() != null && !requestDTO.getStrength().equals(existingMedicine.getStrength())
                || requestDTO.getActiveIng() != null && !requestDTO.getActiveIng().equals(existingMedicine.getActiveIng()))) {
            throw new IllegalStateException(
                    "Medicine identity cannot be changed after it is used in a prescription; create a new medicine entry");
        }

        if (requestDTO.getMedicineName() != null && !requestDTO.getMedicineName().equals(existingMedicine.getMedicineName())
                && medicineRepository.findByMedicineName(requestDTO.getMedicineName()).isPresent()) {
            throw new DuplicateResourceException("Medicine with this name already exists");
        }

        if (requestDTO.getMedicineName() != null) {
            existingMedicine.setMedicineName(requestDTO.getMedicineName());
        }
        if (requestDTO.getMedicineDescription() != null) {
            existingMedicine.setMedicineDescription(requestDTO.getMedicineDescription());
        }
        if (requestDTO.getGenericUse() != null) {
            existingMedicine.setGenericUse(requestDTO.getGenericUse());
        }
        if (requestDTO.getActiveIng() != null) {
            existingMedicine.setActiveIng(requestDTO.getActiveIng());
        }
        if (requestDTO.getStrength() != null) {
            existingMedicine.setStrength(requestDTO.getStrength());
        }

        Medicine updatedMedicine = medicineRepository.save(existingMedicine);
        return medicineMapper.toResponseDTO(updatedMedicine);
    }

    @Override
    public void deleteMedicine(Long id) {
        Medicine medicine = findMedicineById(id);
        if (prescriptionItemRepository.existsByMedicine_MedicineId(id)) {
            throw new IllegalStateException("A medicine used in prescription history cannot be deleted");
        }
        medicineRepository.delete(medicine);
    }

    private Medicine findMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Medicine not found with id: " + id));
    }
}
