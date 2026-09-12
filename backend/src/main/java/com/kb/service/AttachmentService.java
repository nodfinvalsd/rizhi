package com.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kb.common.BizException;
import com.kb.entity.Attachment;
import com.kb.mapper.AttachmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.UUID;

@Slf4j
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

    /** 删除知识时清理其全部附件：磁盘文件 + 记录 */
    public int deleteByKnowledgeId(Long knowledgeId) {
        List<Attachment> list = attachmentMapper.selectList(
                new LambdaQueryWrapper<Attachment>().eq(Attachment::getKnowledgeId, knowledgeId));
        for (Attachment a : list) {
            try {
                Path p = Paths.get(uploadDir, a.getFilePath()).toAbsolutePath().normalize();
                Files.deleteIfExists(p);
            } catch (IOException e) {
                log.warn("delete attachment file failed: {}", a.getFilePath());
            }
        }
        if (!list.isEmpty()) {
            attachmentMapper.delete(new LambdaQueryWrapper<Attachment>()
                    .eq(Attachment::getKnowledgeId, knowledgeId));
        }
        return list.size();
    }
}
