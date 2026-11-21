package com.emedic.management.repositories;

import com.emedic.management.model.Appointment;
import com.emedic.management.model.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    Page<Appointment> findByDoctor_DoctorId(Long doctorId, Pageable pageable);

    List<Appointment> findByDoctor_DoctorIdAndAppointmentDateTimeBetween(
            Long doctorId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select appointment from Appointment appointment " +
            "where appointment.doctor.doctorId = :doctorId " +
            "and appointment.appointmentDateTime between :start and :end")
    List<Appointment> findForUpdateByDoctorAndAppointmentDateTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select appointment from Appointment appointment where appointment.appointmentId = :appointmentId")
    java.util.Optional<Appointment> findLockedByAppointmentId(@Param("appointmentId") Long appointmentId);

    boolean existsByDoctor_DoctorIdAndAppointmentDateTimeAfterAndStatusIn(
            Long doctorId,
            LocalDateTime after,
            Collection<AppointmentStatus> statuses
    );

    boolean existsByDoctor_DoctorId(Long doctorId);

    @Query("select distinct appointment.doctor.doctorId from Appointment appointment " +
            "where appointment.appointmentDateTime > :after and appointment.status in :statuses")
    List<Long> findDoctorIdsWithFutureAppointmentsInStatuses(
            @Param("after") LocalDateTime after,
            @Param("statuses") Collection<AppointmentStatus> statuses
    );

    List<Appointment> findByPatient_PatientIdOrderByAppointmentDateTimeDesc(
            Long patientId
    );

    List<Appointment> findByStatusOrderByAppointmentDateTimeAsc(
            AppointmentStatus status
    );
}
