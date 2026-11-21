package com.emedic.management.mapper;

import com.emedic.management.model.PrescriptionItem;
import com.emedic.management.modeldto.prescription.PrescriptionItemRequestDTO;
import com.emedic.management.modeldto.prescription.PrescriptionItemResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrescriptionItemMapper {

    @Mapping(target = "itemId", ignore = true)
    @Mapping(target = "prescription", ignore = true)
    @Mapping(target = "medicine", ignore = true)
    PrescriptionItem toEntity(PrescriptionItemRequestDTO dto);

    @Mapping(target = "itemId", source = "itemId")
    @Mapping(target = "medicineId", source = "medicine.medicineId")
    @Mapping(target = "medicineName", source = "medicine.medicineName")
    @Mapping(target = "strength", source = "medicine.strength")
    @Mapping(target = "genericUse", source = "medicine.genericUse")
    PrescriptionItemResponseDTO toResponseDTO(PrescriptionItem item);
}