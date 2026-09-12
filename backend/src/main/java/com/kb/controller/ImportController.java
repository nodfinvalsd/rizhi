package com.kb.controller;

import com.kb.common.Result;
import com.kb.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping
    public Result<Integer> importMd(@RequestParam("files") List<MultipartFile> files,
                                    @RequestParam(required = false) Long categoryId) {
        return Result.ok(importService.importMd(files, categoryId));
    }
}
