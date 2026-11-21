package com.emedic.management.controller;

import com.emedic.management.modeldto.medicine.MedicineRequestDTO;
import com.emedic.management.modeldto.medicine.MedicineResponseDTO;
import com.emedic.management.services.medicine.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {
    private final MedicineService service;
    public MedicineController(MedicineService service) { this.service = service; }
    @GetMapping public Page<MedicineResponseDTO> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return service.getMedicines(page, size); }
    @GetMapping("/{id}") public MedicineResponseDTO get(@PathVariable Long id) { return service.getMedicineById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MedicineResponseDTO create(@Valid @RequestBody MedicineRequestDTO body) { return service.createMedicine(body); }
    @PutMapping("/{id}") public MedicineResponseDTO update(@PathVariable Long id, @Valid @RequestBody MedicineRequestDTO body) { return service.updateMedicine(id, body); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.deleteMedicine(id); }
}
