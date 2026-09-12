package com.kb.controller;

import com.kb.common.Result;
import com.kb.dto.CategoryNode;
import com.kb.dto.CategoryRequest;
import com.kb.entity.KnowledgeCategory;
import com.kb.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Result<KnowledgeCategory> create(@RequestBody @Valid CategoryRequest req) {
        return Result.ok(categoryService.create(req.getName(), req.getParentId(), req.getSort()));
    }

    @GetMapping("/tree")
    public Result<List<CategoryNode>> tree() {
        return Result.ok(categoryService.tree());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid CategoryRequest req) {
        categoryService.update(id, req.getName(), req.getSort());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
