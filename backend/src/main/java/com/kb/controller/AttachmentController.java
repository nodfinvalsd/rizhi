package com.kb.controller;

import com.kb.common.Result;
import com.kb.entity.Attachment;
import com.kb.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    public Result<Attachment> upload(@RequestParam(required = false) Long knowledgeId,
                                     @RequestParam("file") MultipartFile file) {
        return Result.ok(attachmentService.upload(knowledgeId, file));
    }
}
