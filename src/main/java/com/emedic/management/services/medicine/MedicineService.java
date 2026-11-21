package com.emedic.management.services.medicine;

import com.emedic.management.modeldto.medicine.MedicineRequestDTO;
import com.emedic.management.modeldto.medicine.MedicineResponseDTO;

import java.util.List;
import org.springframework.data.domain.Page;

public interface MedicineService {
    List<MedicineResponseDTO> getAllMedicines();
    Page<MedicineResponseDTO> getMedicines(int page, int size);
    MedicineResponseDTO getMedicineById(Long id);
    MedicineResponseDTO createMedicine(MedicineRequestDTO requestDTO);
    MedicineResponseDTO updateMedicine(Long id, MedicineRequestDTO requestDTO);
    void deleteMedicine(Long id);
}
