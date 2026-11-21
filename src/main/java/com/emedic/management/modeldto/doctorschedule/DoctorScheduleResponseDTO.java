package com.emedic.management.modeldto.doctorschedule;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorScheduleResponseDTO {
    private Long scheduleId;
    private Long  doctorId;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDuration;
    private boolean slotDurationEditable;
    private String slotDurationEditMessage;
}
