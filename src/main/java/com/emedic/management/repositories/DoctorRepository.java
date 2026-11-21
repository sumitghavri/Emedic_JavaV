package com.emedic.management.repositories;

import com.emedic.management.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query("select distinct doctor from Doctor doctor, DoctorSchedule schedule " +
            "where schedule.doctor = doctor order by doctor.name")
    java.util.List<Doctor> findDoctorsWithSchedules();
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByEmailIgnoreCase(String email);
    Optional<Doctor> findByPhone(String phone);
    Optional<Doctor> findByLicenseNo(String licenseNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select doctor from Doctor doctor where doctor.doctorId = :doctorId")
    Optional<Doctor> findLockedByDoctorId(@Param("doctorId") Long doctorId);
}
