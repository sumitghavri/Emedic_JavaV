package com.emedic.management.services.doctorschedule;

import com.emedic.management.mapper.DoctorScheduleMapper;
import com.emedic.management.model.Doctor;
import com.emedic.management.model.DoctorSchedule;
import com.emedic.management.model.AppointmentStatus;
import com.emedic.management.modeldto.doctorschedule.DoctorScheduleRequestDTO;
import com.emedic.management.modeldto.doctorschedule.DoctorScheduleResponseDTO;
import com.emedic.management.repositories.DoctorRepository;
import com.emedic.management.repositories.DoctorScheduleRepository;
import com.emedic.management.repositories.AppointmentRepository;
import com.emedic.management.error.DuplicateResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import com.emedic.management.util.Pagination;

import java.time.DayOfWeek;
import java.util.List;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    public static final String SLOT_DURATION_LOCK_MESSAGE =
            "Slot duration cannot be changed while you have pending or confirmed future appointments. " +
                    "Please clear or reschedule your existing bookings first.";
    private static final Set<AppointmentStatus> CAPACITY_HOLDING_STATUSES = EnumSet.of(
            AppointmentStatus.REQUESTED,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.RESCHEDULED
    );

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleMapper doctorScheduleMapper;

    public DoctorScheduleServiceImpl(DoctorScheduleRepository doctorScheduleRepository,
                                    DoctorRepository doctorRepository,
                                    AppointmentRepository appointmentRepository,
                                    DoctorScheduleMapper doctorScheduleMapper) {
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorScheduleMapper = doctorScheduleMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponseDTO> getAllSchedules() {
        Set<Long> lockedDoctorIds = Set.copyOf(appointmentRepository
                .findDoctorIdsWithFutureAppointmentsInStatuses(
                        LocalDateTime.now(), CAPACITY_HOLDING_STATUSES));
        return doctorScheduleRepository.findAll().stream()
                .map(schedule -> toResponseDTO(schedule, lockedDoctorIds.contains(schedule.getDoctor().getDoctorId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorScheduleResponseDTO> getSchedules(int page, int size) {
        Set<Long> lockedDoctorIds = Set.copyOf(appointmentRepository
                .findDoctorIdsWithFutureAppointmentsInStatuses(LocalDateTime.now(), CAPACITY_HOLDING_STATUSES));
        return doctorScheduleRepository.findAll(Pagination.request(page, size))
                .map(schedule -> toResponseDTO(schedule,
                        lockedDoctorIds.contains(schedule.getDoctor().getDoctorId())));
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorScheduleResponseDTO getScheduleById(Long id) {
        DoctorSchedule schedule = findScheduleById(id);
        return toResponseDTO(schedule, hasFutureCapacityHoldingAppointments(schedule.getDoctor().getDoctorId()));
    }

    @Override
    public DoctorScheduleResponseDTO createSchedule(DoctorScheduleRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Schedule request cannot be null");
        }

        Doctor doctor = doctorRepository.findLockedByDoctorId(requestDTO.getDoctorId())
                .orElseThrow(() -> new NoSuchElementException("Doctor not found with id: " + requestDTO.getDoctorId()));

        DayOfWeek day = parseDayOfWeek(requestDTO.getDayOfWeek());
        validateScheduleWindow(requestDTO.getStartTime(), requestDTO.getEndTime(), requestDTO.getSlotDuration());

        if (doctorScheduleRepository.findByDoctor_DoctorIdAndDayOfWeek(requestDTO.getDoctorId(), day).isPresent()) {
            throw new DuplicateResourceException("Doctor already has a schedule for this day");
        }

        DoctorSchedule schedule = doctorScheduleMapper.toEntity(requestDTO);
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(day);

        DoctorSchedule saved = doctorScheduleRepository.save(schedule);
        return toResponseDTO(saved, hasFutureCapacityHoldingAppointments(doctor.getDoctorId()));
    }

    @Override
    public DoctorScheduleResponseDTO updateSchedule(Long id, DoctorScheduleRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Schedule request cannot be null");
        }

        DoctorSchedule existing = doctorScheduleRepository.findLockedByScheduleId(id)
                .orElseThrow(() -> new NoSuchElementException("Doctor schedule not found with id: " + id));

        if (requestDTO.getSlotDuration() != null
                && !requestDTO.getSlotDuration().equals(existing.getSlotDuration())) {
            Long currentDoctorId = existing.getDoctor().getDoctorId();
            Long targetDoctorId = requestDTO.getDoctorId() == null ? currentDoctorId : requestDTO.getDoctorId();
            if (hasFutureCapacityHoldingAppointments(currentDoctorId)
                    || (!targetDoctorId.equals(currentDoctorId)
                    && hasFutureCapacityHoldingAppointments(targetDoctorId))) {
                throw new IllegalStateException(SLOT_DURATION_LOCK_MESSAGE);
            }
        }

        if (requestDTO.getDoctorId() != null && !requestDTO.getDoctorId().equals(existing.getDoctor().getDoctorId())) {
            Doctor doctor = doctorRepository.findLockedByDoctorId(requestDTO.getDoctorId())
                    .orElseThrow(() -> new NoSuchElementException("Doctor not found with id: " + requestDTO.getDoctorId()));
            existing.setDoctor(doctor);
        }

        if (requestDTO.getDayOfWeek() != null) {
            DayOfWeek newDay = parseDayOfWeek(requestDTO.getDayOfWeek());
            existing.setDayOfWeek(newDay);
        }
        if (requestDTO.getStartTime() != null) {
            existing.setStartTime(requestDTO.getStartTime());
        }
        if (requestDTO.getEndTime() != null) {
            existing.setEndTime(requestDTO.getEndTime());
        }
        if (requestDTO.getSlotDuration() != null) {
            existing.setSlotDuration(requestDTO.getSlotDuration());
        }

        validateScheduleWindow(existing.getStartTime(), existing.getEndTime(), existing.getSlotDuration());
        Doctor lockedDoctor = doctorRepository.findLockedByDoctorId(existing.getDoctor().getDoctorId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Doctor not found with id: " + existing.getDoctor().getDoctorId()));
        existing.setDoctor(lockedDoctor);
        doctorScheduleRepository.findByDoctor_DoctorIdAndDayOfWeek(
                        existing.getDoctor().getDoctorId(), existing.getDayOfWeek())
                .filter(other -> !other.getScheduleId().equals(existing.getScheduleId()))
                .ifPresent(other -> {
                    throw new DuplicateResourceException("Doctor already has a schedule for this day");
                });

        DoctorSchedule saved = doctorScheduleRepository.save(existing);
        return toResponseDTO(saved, hasFutureCapacityHoldingAppointments(saved.getDoctor().getDoctorId()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canChangeSlotDuration(Long id) {
        DoctorSchedule schedule = findScheduleById(id);
        return !hasFutureCapacityHoldingAppointments(schedule.getDoctor().getDoctorId());
    }

    @Override
    public void deleteSchedule(Long id) {
        DoctorSchedule schedule = doctorScheduleRepository.findLockedByScheduleId(id)
                .orElseThrow(() -> new NoSuchElementException("Doctor schedule not found with id: " + id));
        if (hasFutureCapacityHoldingAppointments(schedule.getDoctor().getDoctorId())) {
            throw new IllegalStateException(
                    "A schedule with active future appointments cannot be deleted");
        }
        doctorScheduleRepository.delete(schedule);
    }

    private DayOfWeek parseDayOfWeek(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Day of week is required");
        }
        try {
            return DayOfWeek.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Day of week must be a valid weekday name", exception);
        }
    }

    private void validateScheduleWindow(LocalTime startTime, LocalTime endTime, Integer slotDuration) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Schedule start time must be before end time");
        }
        if (slotDuration == null || slotDuration <= 0) {
            throw new IllegalArgumentException("Slot duration must be a positive number of minutes");
        }
        long availableMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (availableMinutes % slotDuration != 0) {
            throw new IllegalArgumentException("Schedule duration must be divisible by the slot duration");
        }
    }

    private DoctorSchedule findScheduleById(Long id) {
        return doctorScheduleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Doctor schedule not found with id: " + id));
    }

    private boolean hasFutureCapacityHoldingAppointments(Long doctorId) {
        return appointmentRepository.existsByDoctor_DoctorIdAndAppointmentDateTimeAfterAndStatusIn(
                doctorId,
                java.time.LocalDateTime.now(),
                CAPACITY_HOLDING_STATUSES
        );
    }

    private DoctorScheduleResponseDTO toResponseDTO(DoctorSchedule schedule, boolean hasActiveFutureBookings) {
        DoctorScheduleResponseDTO response = doctorScheduleMapper.toResponseDTO(schedule);
        response.setSlotDurationEditable(!hasActiveFutureBookings);
        response.setSlotDurationEditMessage(hasActiveFutureBookings ? SLOT_DURATION_LOCK_MESSAGE : null);
        return response;
    }
}
