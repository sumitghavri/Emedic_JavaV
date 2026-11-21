package com.emedic.management.services.medicine;

import com.emedic.management.mapper.MedicineMapper;
import com.emedic.management.model.Medicine;
import com.emedic.management.modeldto.medicine.MedicineRequestDTO;
import com.emedic.management.modeldto.medicine.MedicineResponseDTO;
import com.emedic.management.repositories.MedicineRepository;
import com.emedic.management.repositories.PrescriptionItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineServiceImplTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private PrescriptionItemRepository prescriptionItemRepository;

    @Mock
    private MedicineMapper medicineMapper;

    @InjectMocks
    private MedicineServiceImpl medicineService;

    @Test
    void createMedicine_shouldSaveAndReturnDto() {
        MedicineRequestDTO request = new MedicineRequestDTO();
        request.setMedicineName("Paracetamol");
        request.setMedicineDescription("Pain reliever");
        request.setGenericUse("Fever");
        request.setActiveIng("Acetaminophen");
        request.setStrength("500mg");

        Medicine medicine = new Medicine();
        medicine.setMedicineId(5L);
        medicine.setMedicineName("Paracetamol");
        medicine.setMedicineDescription("Pain reliever");
        medicine.setGenericUse("Fever");
        medicine.setActiveIng("Acetaminophen");
        medicine.setStrength("500mg");

        MedicineResponseDTO response = new MedicineResponseDTO();
        response.setMedicineId(5L);
        response.setMedicineName("Paracetamol");

        when(medicineRepository.findByMedicineName("Paracetamol")).thenReturn(Optional.empty());
        when(medicineMapper.toEntity(request)).thenReturn(medicine);
        when(medicineRepository.save(medicine)).thenReturn(medicine);
        when(medicineMapper.toResponseDTO(medicine)).thenReturn(response);

        MedicineResponseDTO result = medicineService.createMedicine(request);

        assertNotNull(result);
        assertEquals("Paracetamol", result.getMedicineName());
        verify(medicineRepository).save(medicine);
    }
}
