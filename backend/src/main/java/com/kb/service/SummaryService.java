package com.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kb.dto.SummaryRequest;
import com.kb.entity.DailySummary;
import com.kb.mapper.DailySummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final DailySummaryMapper summaryMapper;

    /** 某天的总结；没有则返回空壳（id 为 null），前端可直接编辑 */
    public DailySummary getByDate(LocalDate date) {
        DailySummary s = summaryMapper.selectOne(new LambdaQueryWrapper<DailySummary>()
                .eq(DailySummary::getSummaryDate, date));
        if (s != null) {
            return s;
        }
        DailySummary empty = new DailySummary();
        empty.setSummaryDate(date);
        empty.setContent("");
        return empty;
    }

    /** 保存某天的总结，已存在则覆盖 */
    public DailySummary save(SummaryRequest req) {
        LocalDate date = req.getSummaryDate();
        DailySummary s = summaryMapper.selectOne(new LambdaQueryWrapper<DailySummary>()
                .eq(DailySummary::getSummaryDate, date));
        if (s == null) {
            s = new DailySummary();
            s.setSummaryDate(date);
            s.setContent(req.getContent());
            summaryMapper.insert(s);
        } else {
            s.setContent(req.getContent());
            summaryMapper.updateById(s);
        }
        return s;
    }

    /** 有记录的日期列表，最新的在前，用于回顾 */
    public List<DailySummary> recent(int limit) {
        return summaryMapper.selectList(new LambdaQueryWrapper<DailySummary>()
                .orderByDesc(DailySummary::getSummaryDate)
                .last("limit " + Math.max(1, Math.min(limit, 100))));
    }
}
