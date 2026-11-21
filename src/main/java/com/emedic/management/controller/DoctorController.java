package com.emedic.management.controller;

import com.emedic.management.modeldto.doctor.DoctorRequestDTO;
import com.emedic.management.modeldto.doctor.DoctorResponseDTO;
import com.emedic.management.services.doctor.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService service;
    public DoctorController(DoctorService service) { this.service = service; }
    @GetMapping public Page<DoctorResponseDTO> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return service.getDoctors(page, size); }
    @GetMapping("/{id}") public DoctorResponseDTO get(@PathVariable Long id) { return service.getDoctorById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public DoctorResponseDTO create(@Valid @RequestBody DoctorRequestDTO body) { return service.createDoctor(body); }
    @PostMapping("/me") @ResponseStatus(HttpStatus.CREATED) public DoctorResponseDTO registerMyDoctorProfile(@Valid @RequestBody DoctorRequestDTO body, @AuthenticationPrincipal Jwt jwt) {
        String email = jwt == null ? null : jwt.getClaimAsString("email");
        if (email == null && jwt != null) email = jwt.getClaimAsString("https://emedic.example/email");
        return service.createDoctorForClinician(body, email);
    }
    @PutMapping("/{id}") public DoctorResponseDTO update(@PathVariable Long id, @Valid @RequestBody DoctorRequestDTO body) { return service.updateDoctor(id, body); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.deleteDoctor(id); }
}
