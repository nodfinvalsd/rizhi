package com.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kb.common.BizException;
import com.kb.dto.ScheduleRequest;
import com.kb.entity.Schedule;
import com.kb.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;

    public Schedule create(ScheduleRequest req) {
        Schedule s = new Schedule();
        apply(s, req);
        scheduleMapper.insert(s);
        return s;
    }

    public void update(Long id, ScheduleRequest req) {
        Schedule s = getById(id);
        apply(s, req);
        scheduleMapper.updateById(s);
    }

    public void delete(Long id) {
        getById(id);
        scheduleMapper.deleteById(id);
    }

    public void updateStatus(Long id, String status) {
        Schedule s = getById(id);
        s.setStatus(status);
        scheduleMapper.updateById(s);
    }

    /** 今天 */
    public List<Schedule> today() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        return listBetween(start, start.plusDays(1));
    }

    /** 明天 */
    public List<Schedule> tomorrow() {
        LocalDateTime start = LocalDate.now().plusDays(1).atStartOfDay();
        return listBetween(start, start.plusDays(1));
    }

    /** 未来 7 天 */
    public List<Schedule> week() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        return listBetween(start, start.plusDays(7));
    }

    private List<Schedule> listBetween(LocalDateTime start, LocalDateTime end) {
        return scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                .ge(Schedule::getStartTime, start)
                .lt(Schedule::getStartTime, end)
                .orderByAsc(Schedule::getStartTime));
    }

    private Schedule getById(Long id) {
        Schedule s = scheduleMapper.selectById(id);
        if (s == null) {
            throw new BizException(404, "日程不存在");
        }
        return s;
    }

    private void apply(Schedule s, ScheduleRequest req) {
        s.setTitle(req.getTitle());
        s.setDescription(req.getDescription());
        s.setStartTime(req.getStartTime() == null ? LocalDate.now().atTime(LocalTime.MIN) : req.getStartTime());
        s.setEndTime(req.getEndTime());
        s.setStatus(req.getStatus() == null ? "TODO" : req.getStatus());
        s.setPriority(req.getPriority() == null ? "MEDIUM" : req.getPriority());
    }
}
