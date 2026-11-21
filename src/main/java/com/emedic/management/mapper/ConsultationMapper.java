package com.emedic.management.mapper;

import com.emedic.management.model.Consultation;
import com.emedic.management.modeldto.consultation.ConsultationRequestDTO;
import com.emedic.management.modeldto.consultation.ConsultationResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {

    @Mapping(target = "consultationId", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "consultationDatetime", ignore = true)
    @Mapping(
            target = "examinationFindings",
            source = "examinationFinding"
    )
    Consultation toEntity(ConsultationRequestDTO dto);

    @Mapping(
            target = "appointmentId",
            source = "appointment.appointmentId"
    )
    @Mapping(
            target = "examinationFinding",
            source = "examinationFindings"
    )
    ConsultationResponseDTO toResponseDTO(Consultation consultation);
}