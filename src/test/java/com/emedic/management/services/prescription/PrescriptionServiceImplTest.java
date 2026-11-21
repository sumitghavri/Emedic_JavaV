package com.emedic.management.services.prescription;

import com.emedic.management.mapper.PrescriptionItemMapper;
import com.emedic.management.mapper.PrescriptionMapper;
import com.emedic.management.model.Consultation;
import com.emedic.management.model.Medicine;
import com.emedic.management.model.Prescription;
import com.emedic.management.modeldto.prescription.PrescriptionItemRequestDTO;
import com.emedic.management.modeldto.prescription.PrescriptionRequestDTO;
import com.emedic.management.modeldto.prescription.PrescriptionResponseDTO;
import com.emedic.management.repositories.ConsultationRepository;
import com.emedic.management.repositories.MedicineRepository;
import com.emedic.management.repositories.PrescriptionRepository;
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
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private PrescriptionMapper prescriptionMapper;

    @Mock
    private PrescriptionItemMapper prescriptionItemMapper;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    @Test
    void createPrescription_shouldSavePrescriptionWithItems() {
        Consultation consultation = new Consultation();
        consultation.setConsultationId(30L);

        Medicine medicine = new Medicine();
        medicine.setMedicineId(5L);
        medicine.setMedicineName("Paracetamol");

        PrescriptionItemRequestDTO itemRequest = new PrescriptionItemRequestDTO();
        itemRequest.setMedicineId(5L);
        itemRequest.setDosage("500mg");
        itemRequest.setFrequency("Twice daily");
        itemRequest.setDurationDays(5);
        itemRequest.setInstructions("After meals");

        PrescriptionRequestDTO request = new PrescriptionRequestDTO();
        request.setConsultationId(30L);
        request.setItems(List.of(itemRequest));
        request.setExpiryDate(LocalDateTime.of(2026, 11, 20, 0, 0));

        Prescription prescription = new Prescription();
        prescription.setPrescriptionId(88L);
        prescription.setConsultation(consultation);

        PrescriptionResponseDTO response = new PrescriptionResponseDTO();
        response.setPrescriptionId(88L);
        response.setConsultationId(30L);

        when(consultationRepository.findById(30L)).thenReturn(Optional.of(consultation));
        when(medicineRepository.findById(5L)).thenReturn(Optional.of(medicine));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);
        when(prescriptionMapper.toResponseDTO(prescription)).thenReturn(response);

        PrescriptionResponseDTO result = prescriptionService.createPrescription(request);

        assertNotNull(result);
        assertEquals(88L, result.getPrescriptionId());
        verify(prescriptionRepository).save(any(Prescription.class));
    }
}
