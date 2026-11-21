package com.emedic.management.controller;

import com.emedic.management.modeldto.consultation.ConsultationRequestDTO;
import com.emedic.management.modeldto.consultation.ConsultationResponseDTO;
import com.emedic.management.modeldto.consultation.ClinicalRecordRequestDTO;
import com.emedic.management.modeldto.consultation.ClinicalRecordResponseDTO;
import com.emedic.management.services.consultation.ConsultationService;
import com.emedic.management.services.consultation.ClinicalRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {
    private final ConsultationService service;
    private final ClinicalRecordService clinicalRecordService;
    public ConsultationController(ConsultationService service, ClinicalRecordService clinicalRecordService) {
        this.service = service;
        this.clinicalRecordService = clinicalRecordService;
    }
    @GetMapping public Page<ConsultationResponseDTO> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        boolean clinicianOnly = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLINICIAN"))
                && authentication.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return clinicianOnly ? service.getConsultationsForDoctor(clinicianEmail(jwt), page, size)
                : service.getConsultations(page, size);
    }
    @GetMapping("/{id}") public ConsultationResponseDTO get(@PathVariable Long id) { return service.getConsultationById(id); }
    @GetMapping("/by-patient/{patientId}") public Page<ConsultationResponseDTO> byPatient(@PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.getConsultationsByPatientId(patientId, page, size);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ConsultationResponseDTO create(@Valid @RequestBody ConsultationRequestDTO body, Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        boolean clinicianOnly = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLINICIAN"))
                && authentication.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return clinicianOnly ? service.createConsultationForDoctor(body, clinicianEmail(jwt))
                : service.createConsultation(body);
    }
    @PostMapping("/record") @ResponseStatus(HttpStatus.CREATED)
    public ClinicalRecordResponseDTO createClinicalRecord(@Valid @RequestBody ClinicalRecordRequestDTO body,
                                                           Authentication authentication,
                                                           @AuthenticationPrincipal Jwt jwt) {
        boolean clinicianOnly = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLINICIAN"))
                && authentication.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return clinicalRecordService.create(body, clinicianEmail(jwt), clinicianOnly);
    }
    private String clinicianEmail(Jwt jwt) {
        if (jwt == null) return null;
        String email = jwt.getClaimAsString("email");
        return email != null ? email : jwt.getClaimAsString("https://emedic.example/email");
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.deleteConsultation(id); }
}
