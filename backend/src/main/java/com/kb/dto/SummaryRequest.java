package com.kb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SummaryRequest {

    @NotNull(message = "日期不能为空")
    private LocalDate summaryDate;

    private String content;
}
