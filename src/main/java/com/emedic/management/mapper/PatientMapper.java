package com.emedic.management.mapper;

import com.emedic.management.model.Patient;
import com.emedic.management.modeldto.appointment.AppointmentBookingRequestDTO;
import com.emedic.management.modeldto.patient.PatientRequestDTO;
import com.emedic.management.modeldto.patient.PatientResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "patientId", ignore = true)
    Patient toEntity(PatientRequestDTO dto);

    @Mapping(target = "patientId", ignore = true)
    Patient toEntity(AppointmentBookingRequestDTO dto);

    @Mapping(target = "patientId", source = "patientId")
    PatientResponseDTO toResponseDTO(Patient patient);
}