package com.emedic.management.services.appointment;

import com.emedic.management.mapper.AppointmentMapper;
import com.emedic.management.mapper.PatientMapper;
import com.emedic.management.model.*;
import com.emedic.management.modeldto.appointment.AppointmentBookingRequestDTO;
import com.emedic.management.modeldto.appointment.AppointmentResponseDTO;
import com.emedic.management.repositories.AppointmentRepository;
import com.emedic.management.repositories.DoctorRepository;
import com.emedic.management.repositories.DoctorScheduleRepository;
import com.emedic.management.repositories.PatientRepository;
import com.emedic.management.services.patient.PatientPhoneLockService;
import com.emedic.management.error.DuplicateResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import com.emedic.management.util.Pagination;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final PatientPhoneLockService patientPhoneLockService;
    private final PatientMapper patientMapper;
    private final AppointmentMapper appointmentMapper;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                 PatientRepository patientRepository,
                                 DoctorRepository doctorRepository,
                                 DoctorScheduleRepository doctorScheduleRepository,
                                 PatientPhoneLockService patientPhoneLockService,
                                 PatientMapper patientMapper,
                                 AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.patientPhoneLockService = patientPhoneLockService;
        this.patientMapper = patientMapper;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(appointmentMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAppointments(int page, int size) {
        return appointmentRepository.findAll(Pagination.request(page, size)).map(appointmentMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAppointmentsForDoctor(String email, int page, int size) {
        if (email == null || email.isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Your API access token is missing the clinician email claim. Add https://emedic.example/email in the Auth0 Post Login Action, deploy it, then sign out and back in.");
        }
        Doctor doctor = doctorRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException(
                        "No doctor profile matches the email claim in your API access token. Ask an admin to set the doctor profile email to the signed-in clinician email, then sign in again."));
        return appointmentRepository.findByDoctor_DoctorId(doctor.getDoctorId(), Pagination.request(page, size))
                .map(appointmentMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long id) {
        return appointmentMapper.toResponseDTO(findAppointmentById(id));
    }

    @Override
    public AppointmentResponseDTO createAppointment(AppointmentBookingRequestDTO requestDTO) {
        return createAppointment(requestDTO, BookingSource.RECEPTIONIST);
    }

    @Override
    public AppointmentResponseDTO createOnlineAppointment(AppointmentBookingRequestDTO requestDTO) {
        return createAppointment(requestDTO, BookingSource.ONLINE);
    }

    private AppointmentResponseDTO createAppointment(AppointmentBookingRequestDTO requestDTO, BookingSource source) {
        validateRequest(requestDTO);

        Doctor doctor = findDoctor(requestDTO.getDoctorId());
        DoctorSchedule schedule = findAndLockSchedule(doctor.getDoctorId(), requestDTO.getAppointmentDateTime());
        validateSlot(schedule, requestDTO.getAppointmentDateTime());
        ensureNoAppointmentConflict(doctor.getDoctorId(), requestDTO.getAppointmentDateTime(), schedule.getSlotDuration(), null);

        // A phone match identifies the existing patient for booking, but request data
        // is not trusted to overwrite that patient's demographic or clinical record.
        patientPhoneLockService.acquire(requestDTO.getPhone());
        Patient patient = patientRepository.findByPhoneForUpdate(requestDTO.getPhone())
                .orElseGet(() -> createPatientForBooking(requestDTO));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setReason(requestDTO.getReason());
        appointment.setAppointmentDateTime(requestDTO.getAppointmentDateTime());
        appointment.setStatus(AppointmentStatus.REQUESTED);
        appointment.setBookingSource(source);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponseDTO(saved);
    }

    @Override
    public AppointmentResponseDTO updateAppointment(Long id, AppointmentBookingRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Appointment request cannot be null");
        }

        Appointment appointment = appointmentRepository.findLockedByAppointmentId(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new IllegalStateException("A terminal appointment cannot be changed");
        }

        Doctor targetDoctor = appointment.getDoctor();
        if (requestDTO.getDoctorId() != null) {
            targetDoctor = findDoctor(requestDTO.getDoctorId());
        }
        LocalDateTime targetDateTime = requestDTO.getAppointmentDateTime() != null
                ? requestDTO.getAppointmentDateTime()
                : appointment.getAppointmentDateTime();
        if (targetDateTime == null) {
            throw new IllegalStateException("Appointment has no scheduled date and time");
        }

        boolean slotChanged = !targetDoctor.getDoctorId().equals(appointment.getDoctor().getDoctorId())
                || !targetDateTime.equals(appointment.getAppointmentDateTime());
        if (slotChanged) {
            if (targetDateTime == null || !targetDateTime.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Appointment must be scheduled in the future");
            }
            DoctorSchedule schedule = findAndLockSchedule(targetDoctor.getDoctorId(), targetDateTime);
            validateSlot(schedule, targetDateTime);
            ensureNoAppointmentConflict(targetDoctor.getDoctorId(), targetDateTime,
                    schedule.getSlotDuration(), appointment.getAppointmentId());
            appointment.setDoctor(targetDoctor);
            appointment.setAppointmentDateTime(targetDateTime);
            appointment.setStatus(AppointmentStatus.RESCHEDULED);
        }

        if (requestDTO.getReason() != null) {
            appointment.setReason(requestDTO.getReason());
        }

        appointment.setUpdatedAt(LocalDateTime.now());
        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));
    }

    @Override
    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findLockedByAppointmentId(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return;
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new IllegalStateException("A completed or missed appointment cannot be cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }

    @Override
    public AppointmentResponseDTO confirmAppointment(Long id) {
        Appointment appointment = appointmentRepository.findLockedByAppointmentId(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            return appointmentMapper.toResponseDTO(appointment);
        }
        if (appointment.getStatus() != AppointmentStatus.REQUESTED
                && appointment.getStatus() != AppointmentStatus.RESCHEDULED) {
            throw new IllegalStateException("Only requested or rescheduled appointments can be confirmed");
        }
        if (appointment.getAppointmentDateTime() == null
                || !appointment.getAppointmentDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Only future appointments can be confirmed");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(LocalDateTime.now());
        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));
    }

    private Appointment findAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
    }

    private void validateRequest(AppointmentBookingRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Appointment request cannot be null");
        }
        if (request.getDoctorId() == null) {
            throw new IllegalArgumentException("Doctor is required");
        }
        if (request.getAppointmentDateTime() == null
                || !request.getAppointmentDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment must be scheduled in the future");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("Appointment reason is required");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("Patient phone is required");
        }
        if (!request.getPhone().matches("^[6-9][0-9]{9}$")) {
            throw new IllegalArgumentException("Enter a valid mobile number");
        }
    }

    private Doctor findDoctor(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found with id: " + doctorId));
    }

    private DoctorSchedule findAndLockSchedule(Long doctorId, LocalDateTime appointmentDateTime) {
        DayOfWeek dayOfWeek = appointmentDateTime.getDayOfWeek();
        return doctorScheduleRepository.findForUpdateByDoctorIdAndDayOfWeek(doctorId, dayOfWeek)
                .orElseThrow(() -> new IllegalArgumentException("Doctor has no schedule for " + dayOfWeek));
    }

    private void validateSlot(DoctorSchedule schedule, LocalDateTime appointmentDateTime) {
        Integer slotDuration = schedule.getSlotDuration();
        if (slotDuration == null || slotDuration <= 0) {
            throw new IllegalStateException("Doctor schedule has an invalid slot duration");
        }

        LocalTime slotStart = appointmentDateTime.toLocalTime();
        LocalTime slotEnd = slotStart.plusMinutes(slotDuration);
        if (slotStart.isBefore(schedule.getStartTime()) || slotEnd.isAfter(schedule.getEndTime())) {
            throw new IllegalArgumentException("Appointment falls outside the doctor's working hours");
        }

        long minutesFromStart = ChronoUnit.MINUTES.between(schedule.getStartTime(), slotStart);
        if (minutesFromStart % slotDuration != 0
                || slotStart.getSecond() != 0 || slotStart.getNano() != 0) {
            throw new IllegalArgumentException("Appointment must start on an available schedule slot");
        }
    }

    private void ensureNoAppointmentConflict(Long doctorId, LocalDateTime requestedStart,
                                             int slotDuration, Long excludedAppointmentId) {
        LocalDateTime dayStart = requestedStart.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1).minusNanos(1);
        LocalDateTime requestedEnd = requestedStart.plusMinutes(slotDuration);
        Set<AppointmentStatus> blockingStatuses = EnumSet.of(
                AppointmentStatus.REQUESTED,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.RESCHEDULED
        );

        boolean conflict = appointmentRepository
                .findForUpdateByDoctorAndAppointmentDateTimeBetween(doctorId, dayStart, dayEnd)
                .stream()
                .filter(existing -> excludedAppointmentId == null
                        || !excludedAppointmentId.equals(existing.getAppointmentId()))
                .filter(existing -> blockingStatuses.contains(existing.getStatus()))
                .anyMatch(existing -> {
                    LocalDateTime existingStart = existing.getAppointmentDateTime();
                    if (existingStart == null) {
                        return false;
                    }
                    LocalDateTime existingEnd = existingStart.plusMinutes(slotDuration);
                    return requestedStart.isBefore(existingEnd) && existingStart.isBefore(requestedEnd);
                });

        if (conflict) {
            throw new IllegalStateException("Doctor already has an appointment during this time slot");
        }
    }

    private Patient createPatientForBooking(AppointmentBookingRequestDTO request) {
        if (request.getFirstName() == null || request.getFirstName().isBlank()
                || request.getLastName() == null || request.getLastName().isBlank()
                || request.getDob() == null || request.getDob().isAfter(java.time.LocalDate.now())
                || request.getGender() == null
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getAddress() == null || request.getAddress().isBlank()
                || request.getAllergies() == null || request.getAllergies().isBlank()) {
            throw new IllegalArgumentException("Complete patient details are required when registering a new patient");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && patientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Patient email is already associated with another patient");
        }
        Patient newPatient = patientMapper.toEntity(request);
        return patientRepository.save(newPatient);
    }
}
