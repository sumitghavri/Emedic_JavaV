package com.emedic.management.repositories;

import com.emedic.management.model.Consultation;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

public interface ConsultationRepository
        extends JpaRepository<Consultation, Long> {

    Page<Consultation> findByAppointment_Doctor_EmailIgnoreCase(String email, Pageable pageable);

    boolean existsByAppointment_AppointmentId(Long appointmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select consultation from Consultation consultation where consultation.consultationId = :consultationId")
    Optional<Consultation> findLockedByConsultationId(@Param("consultationId") Long consultationId);

    Page<Consultation>
    findByAppointment_Patient_PatientIdOrderByConsultationDatetimeDesc(
            Long patientId,
            Pageable pageable
    );
}
