package com.kb.dto;

import com.kb.entity.Tag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class KnowledgeDetail {

    private Long id;

    private String title;

    private String content;

    private Long categoryId;

    private String categoryName;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<Tag> tags;
}
