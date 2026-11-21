package com.emedic.management.services.doctorschedule;

import com.emedic.management.modeldto.doctorschedule.DoctorScheduleRequestDTO;
import com.emedic.management.modeldto.doctorschedule.DoctorScheduleResponseDTO;

import java.util.List;
import org.springframework.data.domain.Page;

public interface DoctorScheduleService {
    List<DoctorScheduleResponseDTO> getAllSchedules();
    Page<DoctorScheduleResponseDTO> getSchedules(int page, int size);
    DoctorScheduleResponseDTO getScheduleById(Long id);
    DoctorScheduleResponseDTO createSchedule(DoctorScheduleRequestDTO requestDTO);
    DoctorScheduleResponseDTO updateSchedule(Long id, DoctorScheduleRequestDTO requestDTO);
    boolean canChangeSlotDuration(Long id);
    void deleteSchedule(Long id);
}
