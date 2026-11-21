package com.emedic.management.mapper;

import com.emedic.management.model.Prescription;
import com.emedic.management.modeldto.prescription.PrescriptionRequestDTO;
import com.emedic.management.modeldto.prescription.PrescriptionResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = { PrescriptionItemMapper.class }
)
public interface PrescriptionMapper {

    @Mapping(target = "prescriptionId", ignore = true)
    @Mapping(target = "consultation", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "prescribedDate", ignore = true)
    @Mapping(target = "items", source = "items")
    Prescription toEntity(PrescriptionRequestDTO dto);

    @Mapping(target = "consultationId", source = "consultation.consultationId")
    PrescriptionResponseDTO toResponseDTO(Prescription prescription);
}