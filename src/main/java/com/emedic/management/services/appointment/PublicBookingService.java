package com.emedic.management.services.appointment;

import com.emedic.management.model.Appointment;
import com.emedic.management.model.AppointmentStatus;
import com.emedic.management.model.Doctor;
import com.emedic.management.model.DoctorSchedule;
import com.emedic.management.modeldto.appointment.AppointmentBookingRequestDTO;
import com.emedic.management.modeldto.appointment.AppointmentResponseDTO;
import com.emedic.management.modeldto.publicbooking.PublicAvailabilityDTO;
import com.emedic.management.modeldto.publicbooking.PublicBookingReceiptDTO;
import com.emedic.management.modeldto.publicbooking.PublicDoctorDTO;
import com.emedic.management.repositories.AppointmentRepository;
import com.emedic.management.repositories.DoctorRepository;
import com.emedic.management.repositories.DoctorScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class PublicBookingService {
    private static final Set<AppointmentStatus> BLOCKING_STATUSES = EnumSet.of(
            AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULED);

    private final DoctorRepository doctors;
    private final DoctorScheduleRepository schedules;
    private final AppointmentRepository appointments;
    private final AppointmentService appointmentService;

    public PublicBookingService(DoctorRepository doctors, DoctorScheduleRepository schedules,
                                AppointmentRepository appointments, AppointmentService appointmentService) {
        this.doctors = doctors;
        this.schedules = schedules;
        this.appointments = appointments;
        this.appointmentService = appointmentService;
    }

    @Transactional(readOnly = true)
    public List<PublicDoctorDTO> getBookableDoctors() {
        return doctors.findDoctorsWithSchedules().stream()
                .map(doctor -> new PublicDoctorDTO(doctor.getDoctorId(), doctor.getName(), doctor.getSpecialization()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicAvailabilityDTO getAvailability(Long doctorId, LocalDate date) {
        if (date == null || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Choose today or a future date for your appointment");
        }
        doctors.findById(doctorId)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found"));

        DayOfWeek day = date.getDayOfWeek();
        DoctorSchedule schedule = schedules.findByDoctor_DoctorIdAndDayOfWeek(doctorId, day).orElse(null);
        if (schedule == null) {
            return new PublicAvailabilityDTO(doctorId, date, List.of());
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay().minusNanos(1);
        List<Appointment> existing = appointments.findByDoctor_DoctorIdAndAppointmentDateTimeBetween(
                doctorId, dayStart, dayEnd);
        List<Appointment> capacityHolders = existing.stream()
                .filter(appointment -> BLOCKING_STATUSES.contains(appointment.getStatus()))
                .toList();

        int duration = schedule.getSlotDuration();
        LocalTime slot = schedule.getStartTime();
        LocalTime end = schedule.getEndTime();
        LocalDateTime now = LocalDateTime.now();
        java.util.ArrayList<LocalTime> available = new java.util.ArrayList<>();
        while (!slot.plusMinutes(duration).isAfter(end)) {
            LocalDateTime startDateTime = date.atTime(slot);
            LocalDateTime endDateTime = startDateTime.plusMinutes(duration);
            boolean overlaps = capacityHolders.stream().anyMatch(appointment -> {
                LocalDateTime existingStart = appointment.getAppointmentDateTime();
                if (existingStart == null) return false;
                LocalDateTime existingEnd = existingStart.plusMinutes(duration);
                return startDateTime.isBefore(existingEnd) && existingStart.isBefore(endDateTime);
            });
            if (startDateTime.isAfter(now) && !overlaps) available.add(slot);
            slot = slot.plusMinutes(duration);
        }
        return new PublicAvailabilityDTO(doctorId, date, List.copyOf(available));
    }

    @Transactional
    public PublicBookingReceiptDTO book(AppointmentBookingRequestDTO request) {
        AppointmentResponseDTO created = appointmentService.createOnlineAppointment(request);
        return new PublicBookingReceiptDTO(created.getDoctorName(), created.getAppointmentDateTime(),
                created.getStatus().name(),
                "Your request was sent to the clinic. It is not confirmed until the receptionist reviews it.");
    }
}
