package com.emedic.management.modeldto.publicbooking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PublicAvailabilityDTO(Long doctorId, LocalDate date, List<LocalTime> availableSlots) { }
