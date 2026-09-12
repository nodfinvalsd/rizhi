package com.kb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleRequest {

    @NotBlank(message = "不能为空")
    private String title;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** TODO / DONE / CANCELLED */
    private String status;

    /** LOW / MEDIUM / HIGH */
    private String priority;
}
