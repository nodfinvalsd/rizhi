package com.kb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "不能为空")
    private String name;

    private Long parentId;

    private Integer sort;
}
