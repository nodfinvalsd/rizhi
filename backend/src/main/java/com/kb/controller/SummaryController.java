package com.kb.controller;

import com.kb.common.Result;
import com.kb.dto.SummaryRequest;
import com.kb.entity.DailySummary;
import com.kb.service.SummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping("/day")
    public Result<DailySummary> day(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(summaryService.getByDate(date));
    }

    @PostMapping
    public Result<DailySummary> save(@RequestBody @Valid SummaryRequest req) {
        return Result.ok(summaryService.save(req));
    }

    /** 有记录的日期（用于回顾），默认最近 30 天 */
    @GetMapping("/recent")
    public Result<List<DailySummary>> recent(@RequestParam(defaultValue = "30") int limit) {
        return Result.ok(summaryService.recent(limit));
    }
}
