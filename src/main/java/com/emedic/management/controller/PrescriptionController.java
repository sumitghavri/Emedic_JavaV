package com.emedic.management.controller;

import com.emedic.management.modeldto.prescription.PrescriptionRequestDTO;
import com.emedic.management.modeldto.prescription.PrescriptionResponseDTO;
import com.emedic.management.services.prescription.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionService service;
    public PrescriptionController(PrescriptionService service) { this.service = service; }
    @GetMapping public Page<PrescriptionResponseDTO> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return service.getPrescriptions(page, size); }
    @GetMapping("/{id}") public PrescriptionResponseDTO get(@PathVariable Long id) { return service.getPrescriptionById(id); }
    @GetMapping("/by-consultation/{consultationId}") public Page<PrescriptionResponseDTO> byConsultation(@PathVariable Long consultationId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.getPrescriptionsByConsultationId(consultationId, page, size);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public PrescriptionResponseDTO create(@Valid @RequestBody PrescriptionRequestDTO body) { return service.createPrescription(body); }
    @PostMapping("/{id}/cancel") @ResponseStatus(HttpStatus.NO_CONTENT) public void cancel(@PathVariable Long id) { service.deletePrescription(id); }
}
