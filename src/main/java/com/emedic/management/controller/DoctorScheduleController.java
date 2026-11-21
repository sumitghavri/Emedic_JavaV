package com.emedic.management.controller;

import com.emedic.management.modeldto.doctorschedule.DoctorScheduleRequestDTO;
import com.emedic.management.modeldto.doctorschedule.DoctorScheduleResponseDTO;
import com.emedic.management.services.doctorschedule.DoctorScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/schedules")
public class DoctorScheduleController {
    private final DoctorScheduleService service;
    public DoctorScheduleController(DoctorScheduleService service) { this.service = service; }
    @GetMapping public Page<DoctorScheduleResponseDTO> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return service.getSchedules(page, size); }
    @GetMapping("/{id}") public DoctorScheduleResponseDTO get(@PathVariable Long id) { return service.getScheduleById(id); }
    @GetMapping("/{id}/slot-duration-editability") public Map<String, Object> slotDurationEditability(@PathVariable Long id) {
        DoctorScheduleResponseDTO schedule = service.getScheduleById(id);
        return Map.of("editable", schedule.isSlotDurationEditable(),
                "message", schedule.getSlotDurationEditMessage() == null ? "" : schedule.getSlotDurationEditMessage());
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public DoctorScheduleResponseDTO create(@Valid @RequestBody DoctorScheduleRequestDTO body) { return service.createSchedule(body); }
    @PutMapping("/{id}") public DoctorScheduleResponseDTO update(@PathVariable Long id, @Valid @RequestBody DoctorScheduleRequestDTO body) { return service.updateSchedule(id, body); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.deleteSchedule(id); }
}
