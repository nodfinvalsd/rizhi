package com.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kb.common.BizException;
import com.kb.entity.Tag;
import com.kb.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;

    public Tag create(String name) {
        if (tagMapper.selectCount(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name)) > 0) {
            throw new BizException("标签已存在");
        }
        Tag t = new Tag();
        t.setName(name);
        tagMapper.insert(t);
        return t;
    }

    public List<Tag> list() {
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getId));
    }
}
