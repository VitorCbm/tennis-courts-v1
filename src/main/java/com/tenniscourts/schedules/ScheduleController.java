package com.tenniscourts.schedules;

import com.tenniscourts.config.BaseRestController;

import lombok.AllArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@AllArgsConstructor
public class ScheduleController extends BaseRestController {

    private final ScheduleService scheduleService;

    @PostMapping(value = "addScheduleTennisCourt")
    public ResponseEntity<ScheduleDTO> addScheduleTennisCourt(@RequestParam(value = "tennisCourtId", required = true) Long tennisCourtId,
        @RequestParam(value = "startDate", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate) {
        return ResponseEntity.ok(scheduleService.addSchedule(tennisCourtId, startDate));
    }

    @GetMapping(value = "findSchedulesByDate")
    public ResponseEntity<List<ScheduleDTO>> findSchedulesByDates(@RequestParam(value = "startDate", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
        @RequestParam(value = "endDate", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        return ResponseEntity.ok(scheduleService.findSchedulesByDates(startDate, endDate));
    }

    @GetMapping(value = "findScheduleById")
    public ResponseEntity<ScheduleDTO> findByScheduleId(Long scheduleId) {
        return ResponseEntity.ok(scheduleService.findSchedule(scheduleId));
    }
}
