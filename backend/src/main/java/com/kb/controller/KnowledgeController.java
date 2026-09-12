package com.kb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kb.common.Result;
import com.kb.dto.KnowledgeDetail;
import com.kb.dto.KnowledgeItem;
import com.kb.dto.KnowledgeRequest;
import com.kb.entity.Knowledge;
import com.kb.service.ExportService;
import com.kb.service.FavoriteService;
import com.kb.service.KnowledgeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final FavoriteService favoriteService;
    private final ExportService exportService;

    @PostMapping
    public Result<Knowledge> create(@RequestBody @Valid KnowledgeRequest req) {
        return Result.ok(knowledgeService.create(req));
    }

    @GetMapping("/{id}")
    public Result<KnowledgeDetail> detail(@PathVariable Long id) {
        return Result.ok(knowledgeService.detail(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid KnowledgeRequest req) {
        knowledgeService.update(id, req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.ok();
    }

    @GetMapping("/page")
    public Result<IPage<KnowledgeItem>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort) {
        return Result.ok(knowledgeService.page(page, size, keyword, categoryId, tagId, status, sort));
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> favorite(@PathVariable Long id) {
        favoriteService.add(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavorite(@PathVariable Long id) {
        favoriteService.remove(id);
        return Result.ok();
    }

    @GetMapping("/{id}/export")
    public void export(@PathVariable Long id, HttpServletResponse response) throws java.io.IOException {
        exportService.exportMd(id, response);
    }
}
