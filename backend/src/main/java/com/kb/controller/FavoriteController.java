package com.kb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kb.common.Result;
import com.kb.dto.KnowledgeItem;
import com.kb.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/page")
    public Result<IPage<KnowledgeItem>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return Result.ok(favoriteService.favorites(page, size));
    }
}
