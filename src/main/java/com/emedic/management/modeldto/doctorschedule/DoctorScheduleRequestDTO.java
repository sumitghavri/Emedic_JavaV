package com.emedic.management.modeldto.doctorschedule;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorScheduleRequestDTO {
    private Long  doctorId;

    @NotBlank(message = "Enter day of the week")
    private String dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDuration;
}
