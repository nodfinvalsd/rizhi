package com.kb.controller;

import com.kb.common.Result;
import com.kb.dto.TagRequest;
import com.kb.entity.Tag;
import com.kb.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public Result<Tag> create(@RequestBody @Valid TagRequest req) {
        return Result.ok(tagService.create(req.getName()));
    }

    @GetMapping("/list")
    public Result<List<Tag>> list() {
        return Result.ok(tagService.list());
    }
}
