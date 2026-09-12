package com.kb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_daily_summary")
public class DailySummary {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 一天一条，与 content 一起构成当日总结 */
    private LocalDate summaryDate;

    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
