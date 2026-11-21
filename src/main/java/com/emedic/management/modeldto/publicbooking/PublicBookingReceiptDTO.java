package com.emedic.management.modeldto.publicbooking;

import java.time.LocalDateTime;

public record PublicBookingReceiptDTO(String doctorName, LocalDateTime appointmentDateTime,
                                     String status, String message) { }
