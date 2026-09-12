package com.kb.dto;

import com.kb.entity.KnowledgeCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryNode extends KnowledgeCategory {

    private List<CategoryNode> children = new ArrayList<>();

    public static CategoryNode from(KnowledgeCategory c) {
        CategoryNode node = new CategoryNode();
        node.setId(c.getId());
        node.setName(c.getName());
        node.setParentId(c.getParentId());
        node.setSort(c.getSort());
        node.setCreateTime(c.getCreateTime());
        return node;
    }
}
