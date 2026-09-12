package com.kb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_knowledge_favorite")
public class KnowledgeFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long knowledgeId;

    private LocalDateTime createTime;
}
