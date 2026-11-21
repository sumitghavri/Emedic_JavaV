package com.emedic.management.mapper;

import com.emedic.management.model.Doctor;
import com.emedic.management.modeldto.doctor.DoctorRequestDTO;
import com.emedic.management.modeldto.doctor.DoctorResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {
    @Mapping(target = "doctorId", ignore = true)
    Doctor toEntity(DoctorRequestDTO dto);

    @Mapping(target = "doctorId", source = "doctorId")
    DoctorResponseDTO toResponseDTO(Doctor doctor);
}