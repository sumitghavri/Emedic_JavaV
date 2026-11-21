package com.emedic.management.controller;

import com.emedic.management.modeldto.appointment.AppointmentBookingRequestDTO;
import com.emedic.management.modeldto.publicbooking.PublicAvailabilityDTO;
import com.emedic.management.modeldto.publicbooking.PublicBookingReceiptDTO;
import com.emedic.management.modeldto.publicbooking.PublicDoctorDTO;
import com.emedic.management.services.appointment.PublicBookingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/public/booking")
public class PublicBookingController {
    private final PublicBookingService bookingService;

    public PublicBookingController(PublicBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/doctors")
    public List<PublicDoctorDTO> doctors() {
        return bookingService.getBookableDoctors();
    }

    @GetMapping("/doctors/{doctorId}/availability")
    public PublicAvailabilityDTO availability(@PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return bookingService.getAvailability(doctorId, date);
    }

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PublicBookingReceiptDTO book(@Valid @RequestBody AppointmentBookingRequestDTO request) {
        return bookingService.book(request);
    }
}
