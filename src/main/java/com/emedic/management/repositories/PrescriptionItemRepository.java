package com.emedic.management.repositories;

import com.emedic.management.model.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionItemRepository
        extends JpaRepository<PrescriptionItem, Long> {
    List<PrescriptionItem> findByPrescription_PrescriptionId(
            Long prescriptionId
    );

    boolean existsByMedicine_MedicineId(Long medicineId);
}
