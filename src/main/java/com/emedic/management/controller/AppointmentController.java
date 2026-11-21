package com.emedic.management.controller;

import com.emedic.management.modeldto.appointment.AppointmentBookingRequestDTO;
import com.emedic.management.modeldto.appointment.AppointmentResponseDTO;
import com.emedic.management.services.appointment.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service;
    public AppointmentController(AppointmentService service) { this.service = service; }
    @GetMapping public Page<AppointmentResponseDTO> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        boolean clinicianOnly = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLINICIAN"))
                && authentication.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN") || authority.getAuthority().equals("ROLE_RECEPTIONIST"));
        String email = jwt == null ? null : jwt.getClaimAsString("email");
        if (email == null && jwt != null) email = jwt.getClaimAsString("https://emedic.example/email");
        return clinicianOnly ? service.getAppointmentsForDoctor(email, page, size)
                : service.getAppointments(page, size);
    }
    @GetMapping("/{id}") public AppointmentResponseDTO get(@PathVariable Long id) { return service.getAppointmentById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public AppointmentResponseDTO book(@Valid @RequestBody AppointmentBookingRequestDTO body) { return service.createAppointment(body); }
    @PutMapping("/{id}") public AppointmentResponseDTO reschedule(@PathVariable Long id, @Valid @RequestBody AppointmentBookingRequestDTO body) { return service.updateAppointment(id, body); }
    @PostMapping("/{id}/confirm") public AppointmentResponseDTO confirm(@PathVariable Long id) { return service.confirmAppointment(id); }
    @PostMapping("/{id}/cancel") @ResponseStatus(HttpStatus.NO_CONTENT) public void cancel(@PathVariable Long id) { service.cancelAppointment(id); }
}
