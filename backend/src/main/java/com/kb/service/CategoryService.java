package com.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kb.common.BizException;
import com.kb.dto.CategoryNode;
import com.kb.entity.Knowledge;
import com.kb.entity.KnowledgeCategory;
import com.kb.mapper.KnowledgeCategoryMapper;
import com.kb.mapper.KnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final KnowledgeCategoryMapper categoryMapper;
    private final KnowledgeMapper knowledgeMapper;

    public KnowledgeCategory create(String name, Long parentId, Integer sort) {
        KnowledgeCategory c = new KnowledgeCategory();
        c.setName(name);
        c.setParentId(parentId == null ? 0L : parentId);
        c.setSort(sort == null ? 0 : sort);
        categoryMapper.insert(c);
        return c;
    }

    public List<CategoryNode> tree() {
        List<KnowledgeCategory> all = categoryMapper.selectList(
                new LambdaQueryWrapper<KnowledgeCategory>()
                        .orderByAsc(KnowledgeCategory::getSort)
                        .orderByAsc(KnowledgeCategory::getId));
        Map<Long, CategoryNode> nodeMap = all.stream()
                .collect(Collectors.toMap(KnowledgeCategory::getId, CategoryNode::from));
        List<CategoryNode> roots = new ArrayList<>();
        for (KnowledgeCategory c : all) {
            CategoryNode node = nodeMap.get(c.getId());
            if (c.getParentId() == null || c.getParentId() == 0 || !nodeMap.containsKey(c.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(c.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }

    public void update(Long id, String name, Integer sort) {
        KnowledgeCategory c = categoryMapper.selectById(id);
        if (c == null) {
            throw new BizException(404, "分类不存在");
        }
        c.setName(name);
        if (sort != null) {
            c.setSort(sort);
        }
        categoryMapper.updateById(c);
    }

    public void delete(Long id) {
        Long children = categoryMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeCategory>().eq(KnowledgeCategory::getParentId, id));
        if (children > 0) {
            throw new BizException("存在子分类，无法删除");
        }
        Long count = knowledgeMapper.selectCount(
                new LambdaQueryWrapper<Knowledge>().eq(Knowledge::getCategoryId, id));
        if (count > 0) {
            throw new BizException("分类下存在知识，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
