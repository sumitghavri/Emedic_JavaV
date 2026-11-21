package com.emedic.management.mapper;

import com.emedic.management.model.DoctorSchedule;
import com.emedic.management.modeldto.doctorschedule.DoctorScheduleRequestDTO;
import com.emedic.management.modeldto.doctorschedule.DoctorScheduleResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorScheduleMapper {

    @Mapping(target = "scheduleId", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    DoctorSchedule toEntity(DoctorScheduleRequestDTO dto);

    @Mapping(target = "doctorId", source = "doctor.doctorId")
    @Mapping(target = "slotDurationEditable", ignore = true)
    @Mapping(target = "slotDurationEditMessage", ignore = true)
    DoctorScheduleResponseDTO toResponseDTO(DoctorSchedule schedule);
}
