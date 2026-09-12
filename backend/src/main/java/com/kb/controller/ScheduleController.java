package com.kb.controller;

import com.kb.common.Result;
import com.kb.dto.ScheduleRequest;
import com.kb.entity.Schedule;
import com.kb.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public Result<Schedule> create(@RequestBody @Valid ScheduleRequest req) {
        return Result.ok(scheduleService.create(req));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ScheduleRequest req) {
        scheduleService.update(id, req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        scheduleService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    @GetMapping("/today")
    public Result<List<Schedule>> today() {
        return Result.ok(scheduleService.today());
    }

    @GetMapping("/tomorrow")
    public Result<List<Schedule>> tomorrow() {
        return Result.ok(scheduleService.tomorrow());
    }

    @GetMapping("/week")
    public Result<List<Schedule>> week() {
        return Result.ok(scheduleService.week());
    }
}
