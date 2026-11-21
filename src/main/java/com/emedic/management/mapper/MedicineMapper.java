package com.emedic.management.mapper;

import com.emedic.management.model.Medicine;
import com.emedic.management.modeldto.medicine.MedicineRequestDTO;
import com.emedic.management.modeldto.medicine.MedicineResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicineMapper {

    @Mapping(target = "medicineId", ignore = true)
    Medicine toEntity(MedicineRequestDTO dto);

    @Mapping(target = "medicineId", source = "medicineId")
    MedicineResponseDTO toResponseDTO(Medicine medicine);
}