package com.emedic.management.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import com.emedic.management.model.Patient;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientRepository extends JpaRepository<Patient , Long> {
    Optional<Patient> findByPhone(String phone);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select patient from Patient patient where patient.phone = :phone")
    Optional<Patient> findByPhoneForUpdate(@Param("phone") String phone);

    Optional<Patient> findByEmail(String email);

    @Query("select distinct patient from Patient patient join Appointment appointment on appointment.patient = patient where lower(appointment.doctor.email) = lower(:email)")
    Page<Patient> findPatientsForDoctor(@Param("email") String email, Pageable pageable);

    @Query("select count(appointment) > 0 from Appointment appointment where appointment.patient.patientId = :patientId and lower(appointment.doctor.email) = lower(:email)")
    boolean existsAppointmentForDoctor(@Param("patientId") Long patientId, @Param("email") String email);
}
