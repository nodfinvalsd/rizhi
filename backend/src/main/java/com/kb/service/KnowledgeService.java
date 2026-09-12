package com.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kb.common.BizException;
import com.kb.dto.KnowledgeDetail;
import com.kb.dto.KnowledgeItem;
import com.kb.dto.KnowledgeRequest;
import com.kb.entity.Attachment;
import com.kb.entity.Knowledge;
import com.kb.entity.KnowledgeCategory;
import com.kb.entity.KnowledgeFavorite;
import com.kb.entity.KnowledgeTag;
import com.kb.entity.Tag;
import com.kb.mapper.AttachmentMapper;
import com.kb.mapper.KnowledgeCategoryMapper;
import com.kb.mapper.KnowledgeFavoriteMapper;
import com.kb.mapper.KnowledgeMapper;
import com.kb.mapper.KnowledgeTagMapper;
import com.kb.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;
    private final KnowledgeTagMapper knowledgeTagMapper;
    private final TagMapper tagMapper;
    private final KnowledgeCategoryMapper categoryMapper;
    private final KnowledgeFavoriteMapper favoriteMapper;
    private final AttachmentMapper attachmentMapper;
    private final AttachmentService attachmentService;

    public Knowledge create(KnowledgeRequest req) {
        Knowledge k = new Knowledge();
        apply(k, req);
        knowledgeMapper.insert(k);
        saveTags(k.getId(), req.getTagIds());
        bindAttachments(k.getId(), req.getAttachmentIds());
        return k;
    }

    public KnowledgeDetail detail(Long id) {
        Knowledge k = getById(id);
        KnowledgeDetail d = new KnowledgeDetail();
        BeanUtils.copyProperties(k, d);
        d.setCategoryName(categoryName(k.getCategoryId()));
        d.setTags(tagsOf(id));
        return d;
    }

    public void update(Long id, KnowledgeRequest req) {
        Knowledge k = getById(id);
        apply(k, req);
        knowledgeMapper.updateById(k);
        knowledgeTagMapper.delete(new LambdaQueryWrapper<KnowledgeTag>().eq(KnowledgeTag::getKnowledgeId, id));
        saveTags(id, req.getTagIds());
    }

    public void delete(Long id) {
        getById(id);
        attachmentService.deleteByKnowledgeId(id);
        favoriteMapper.delete(new LambdaQueryWrapper<KnowledgeFavorite>()
                .eq(KnowledgeFavorite::getKnowledgeId, id));
        knowledgeMapper.deleteById(id);
        knowledgeTagMapper.delete(new LambdaQueryWrapper<KnowledgeTag>().eq(KnowledgeTag::getKnowledgeId, id));
    }

    public IPage<KnowledgeItem> page(long page, long size, String keyword, Long categoryId, Long tagId,
                                    String status, String sort) {
        LambdaQueryWrapper<Knowledge> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(x -> x.like(Knowledge::getTitle, keyword).or().like(Knowledge::getContent, keyword));
        }
        w.eq(categoryId != null && categoryId > 0, Knowledge::getCategoryId, categoryId);
        w.eq(StringUtils.hasText(status), Knowledge::getStatus, status);
        if (tagId != null) {
            List<Long> ids = knowledgeTagMapper.selectList(
                            new LambdaQueryWrapper<KnowledgeTag>().eq(KnowledgeTag::getTagId, tagId))
                    .stream().map(KnowledgeTag::getKnowledgeId).toList();
            if (ids.isEmpty()) {
                return new Page<>(page, size);
            }
            w.in(Knowledge::getId, ids);
        }
        switch (sort == null ? "update_desc" : sort) {
            case "create_desc" -> w.orderByDesc(Knowledge::getCreateTime);
            case "create_asc" -> w.orderByAsc(Knowledge::getCreateTime);
            case "update_asc" -> w.orderByAsc(Knowledge::getUpdateTime);
            default -> w.orderByDesc(Knowledge::getUpdateTime);
        }

        Page<Knowledge> p = knowledgeMapper.selectPage(new Page<>(page, size), w);

        Page<KnowledgeItem> result = new Page<>(page, size, p.getTotal());
        result.setRecords(toItems(p.getRecords()));
        return result;
    }

    /** 批量组装列表项：分类名 + 标签 + 收藏标记 */
    public List<KnowledgeItem> toItems(List<Knowledge> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        List<Long> ids = list.stream().map(Knowledge::getId).toList();
        Map<Long, List<Tag>> tagMap = batchTags(ids);
        Set<Long> favIds = favoriteMapper.selectList(
                        new LambdaQueryWrapper<KnowledgeFavorite>().in(KnowledgeFavorite::getKnowledgeId, ids))
                .stream().map(KnowledgeFavorite::getKnowledgeId).collect(Collectors.toSet());
        List<Long> catIds = list.stream()
                .map(Knowledge::getCategoryId)
                .filter(Objects::nonNull)
                .filter(x -> x > 0)
                .distinct()
                .toList();
        Map<Long, String> catNameMap = catIds.isEmpty() ? Map.of()
                : categoryMapper.selectByIds(catIds).stream()
                        .collect(Collectors.toMap(KnowledgeCategory::getId, KnowledgeCategory::getName));

        return list.stream().map(k -> {
            KnowledgeItem item = new KnowledgeItem();
            BeanUtils.copyProperties(k, item);
            item.setCategoryName(catNameMap.get(k.getCategoryId()));
            item.setTags(tagMap.getOrDefault(k.getId(), List.of()));
            item.setFavorite(favIds.contains(k.getId()));
            return item;
        }).toList();
    }

    private void apply(Knowledge k, KnowledgeRequest req) {
        k.setTitle(req.getTitle());
        k.setContent(req.getContent());
        k.setCategoryId(req.getCategoryId() == null ? 0L : req.getCategoryId());
        k.setStatus(req.getStatus() == null ? "DRAFT" : req.getStatus());
    }

    private Knowledge getById(Long id) {
        Knowledge k = knowledgeMapper.selectById(id);
        if (k == null) {
            throw new BizException(404, "知识不存在");
        }
        return k;
    }

    private void saveTags(Long knowledgeId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds.stream().distinct().toList()) {
            KnowledgeTag kt = new KnowledgeTag();
            kt.setKnowledgeId(knowledgeId);
            kt.setTagId(tagId);
            knowledgeTagMapper.insert(kt);
        }
    }

    /** 新建知识前已上传的附件，保存时绑定到该知识 */
    private void bindAttachments(Long knowledgeId, List<Long> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }
        attachmentMapper.update(null, new LambdaUpdateWrapper<Attachment>()
                .set(Attachment::getKnowledgeId, knowledgeId)
                .in(Attachment::getId, attachmentIds));
    }

    private List<Tag> tagsOf(Long knowledgeId) {
        List<Long> tagIds = knowledgeTagMapper.selectList(
                        new LambdaQueryWrapper<KnowledgeTag>().eq(KnowledgeTag::getKnowledgeId, knowledgeId))
                .stream().map(KnowledgeTag::getTagId).toList();
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return tagMapper.selectByIds(tagIds);
    }

    private String categoryName(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return null;
        }
        KnowledgeCategory c = categoryMapper.selectById(categoryId);
        return c == null ? null : c.getName();
    }

    private Map<Long, List<Tag>> batchTags(List<Long> knowledgeIds) {
        if (knowledgeIds.isEmpty()) {
            return Map.of();
        }
        List<KnowledgeTag> kts = knowledgeTagMapper.selectList(
                new LambdaQueryWrapper<KnowledgeTag>().in(KnowledgeTag::getKnowledgeId, knowledgeIds));
        if (kts.isEmpty()) {
            return Map.of();
        }
        List<Long> tagIds = kts.stream().map(KnowledgeTag::getTagId).distinct().toList();
        Map<Long, Tag> tagMap = tagMapper.selectByIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));
        Map<Long, List<Tag>> result = new HashMap<>();
        for (KnowledgeTag kt : kts) {
            Tag t = tagMap.get(kt.getTagId());
            if (t != null) {
                result.computeIfAbsent(kt.getKnowledgeId(), k -> new ArrayList<>()).add(t);
            }
        }
        return result;
    }
}
