package com.kb.service;

import com.kb.common.BizException;
import com.kb.entity.Attachment;
import com.kb.mapper.AttachmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentMapper attachmentMapper;

    @Value("${kb.upload-dir:./upload}")
    private String uploadDir;

    public Attachment upload(Long knowledgeId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件为空");
        }
        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            Path dir = Paths.get(uploadDir, datePath).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString().replace("-", "")
                    + (ext != null ? "." + ext : "");
            file.transferTo(dir.resolve(filename));

            Attachment a = new Attachment();
            a.setKnowledgeId(knowledgeId);
            a.setFileName(file.getOriginalFilename());
            a.setFilePath(datePath + "/" + filename);
            a.setContentType(file.getContentType());
            attachmentMapper.insert(a);
            return a;
        } catch (IOException e) {
            throw new BizException(500, "文件保存失败");
        }
    }
}
