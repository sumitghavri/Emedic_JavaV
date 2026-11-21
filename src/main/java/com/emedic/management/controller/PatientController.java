package com.emedic.management.controller;

import com.emedic.management.modeldto.patient.PatientRequestDTO;
import com.emedic.management.modeldto.patient.PatientResponseDTO;
import com.emedic.management.modeldto.patienthistory.PatientHistoryDTO;
import com.emedic.management.services.patient.PatientService;
import com.emedic.management.services.patienthistory.PatientHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patients;
    private final PatientHistoryService history;

    private String clinicianEmail(Jwt jwt) {
        if (jwt == null) return null;
        String email = jwt.getClaimAsString("email");
        return email != null ? email : jwt.getClaimAsString("https://emedic.example/email");
    }
    private boolean clinicianOnly(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLINICIAN"))
                && auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_RECEPTIONIST"));
    }

    public PatientController(PatientService patients, PatientHistoryService history) {
        this.patients = patients;
        this.history = history;
    }

    @GetMapping public Page<PatientResponseDTO> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size, Authentication auth, @AuthenticationPrincipal Jwt jwt) {
        return clinicianOnly(auth) ? patients.getPatientsForDoctor(clinicianEmail(jwt), page, size) : patients.getPatients(page, size);
    }
    @GetMapping("/{id}") public PatientResponseDTO get(@PathVariable Long id, Authentication auth, @AuthenticationPrincipal Jwt jwt) { if (clinicianOnly(auth)) patients.assertPatientAccess(id, clinicianEmail(jwt)); return patients.getPatientById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public PatientResponseDTO create(@Valid @RequestBody PatientRequestDTO request) { return patients.createPatient(request); }
    @PutMapping("/{id}") public PatientResponseDTO update(@PathVariable Long id, @Valid @RequestBody PatientRequestDTO request) { return patients.updatePatient(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { patients.deletePatient(id); }
    @GetMapping("/{id}/history") public PatientHistoryDTO history(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Authentication auth, @AuthenticationPrincipal Jwt jwt) {
        if (clinicianOnly(auth)) patients.assertPatientAccess(id, clinicianEmail(jwt));
        return history.getPatientHistory(id, page, size);
    }
}
