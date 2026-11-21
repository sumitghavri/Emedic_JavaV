package com.emedic.management.repositories;

import com.emedic.management.model.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.emedic.management.model.Doctor;
import jakarta.persistence.LockModeType;
import java.time.DayOfWeek;
import java.util.Optional;

public interface DoctorScheduleRepository
        extends JpaRepository<DoctorSchedule, Long> {

    Optional<DoctorSchedule> findByDoctor_DoctorIdAndDayOfWeek(
            Long doctorId,
            DayOfWeek dayOfWeek
    );

    boolean existsByDoctor_DoctorId(Long doctorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select schedule from DoctorSchedule schedule " +
            "where schedule.doctor.doctorId = :doctorId and schedule.dayOfWeek = :dayOfWeek")
    Optional<DoctorSchedule> findForUpdateByDoctorIdAndDayOfWeek(
            @Param("doctorId") Long doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select schedule from DoctorSchedule schedule where schedule.scheduleId = :scheduleId")
    Optional<DoctorSchedule> findLockedByScheduleId(@Param("scheduleId") Long scheduleId);
}
