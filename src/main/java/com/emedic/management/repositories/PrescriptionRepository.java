package com.emedic.management.repositories;

import com.emedic.management.model.Prescription;
import com.emedic.management.model.PrescriptionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface PrescriptionRepository
        extends JpaRepository<Prescription, Long> {

    List<Prescription> findByConsultation_ConsultationId(
            Long consultationId
    );

    Page<Prescription> findByConsultation_ConsultationId(Long consultationId, Pageable pageable);

    boolean existsByConsultation_ConsultationIdAndStatus(Long consultationId, PrescriptionStatus status);

    @EntityGraph(attributePaths = {"items", "items.medicine"})
    @Query("select distinct prescription from Prescription prescription " +
            "where prescription.consultation.consultationId in :consultationIds")
    List<Prescription> findWithItemsByConsultationIds(
            @Param("consultationIds") Collection<Long> consultationIds
    );
}
