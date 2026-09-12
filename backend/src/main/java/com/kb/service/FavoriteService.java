package com.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kb.common.BizException;
import com.kb.dto.KnowledgeItem;
import com.kb.entity.Knowledge;
import com.kb.entity.KnowledgeFavorite;
import com.kb.mapper.KnowledgeFavoriteMapper;
import com.kb.mapper.KnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final KnowledgeFavoriteMapper favoriteMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final KnowledgeService knowledgeService;

    public void add(Long knowledgeId) {
        if (knowledgeMapper.selectById(knowledgeId) == null) {
            throw new BizException(404, "知识不存在");
        }
        if (favoriteMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeFavorite>().eq(KnowledgeFavorite::getKnowledgeId, knowledgeId)) > 0) {
            return;
        }
        KnowledgeFavorite f = new KnowledgeFavorite();
        f.setKnowledgeId(knowledgeId);
        favoriteMapper.insert(f);
    }

    public void remove(Long knowledgeId) {
        favoriteMapper.delete(new LambdaQueryWrapper<KnowledgeFavorite>()
                .eq(KnowledgeFavorite::getKnowledgeId, knowledgeId));
    }

    public IPage<KnowledgeItem> favorites(long page, long size) {
        Page<KnowledgeFavorite> fp = favoriteMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<KnowledgeFavorite>().orderByDesc(KnowledgeFavorite::getId));
        List<Long> ids = fp.getRecords().stream().map(KnowledgeFavorite::getKnowledgeId).toList();
        if (ids.isEmpty()) {
            return new Page<>(page, size);
        }
        Map<Long, Knowledge> km = knowledgeMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(Knowledge::getId, k -> k));
        List<Knowledge> ordered = ids.stream().map(km::get).filter(Objects::nonNull).toList();
        Page<KnowledgeItem> result = new Page<>(page, size, fp.getTotal());
        result.setRecords(knowledgeService.toItems(ordered));
        return result;
    }
}
