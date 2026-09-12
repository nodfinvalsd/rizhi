package com.kb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class KnowledgeRequest {

    @NotBlank(message = "不能为空")
    private String title;

    private String content;

    private Long categoryId;

    private String status;

    private List<Long> tagIds;
}
