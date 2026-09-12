package com.kb.service;

import com.kb.common.BizException;
import com.kb.entity.Knowledge;
import com.kb.mapper.KnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final KnowledgeMapper knowledgeMapper;

    /**
     * 批量导入 Markdown 文件：标题取首行 "# 标题"，否则取文件名。
     * 统一以 DRAFT 状态入库，标签导入后再补。
     */
    public int importMd(List<MultipartFile> files, Long categoryId) {
        int count = 0;
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) {
                continue;
            }
            String original = f.getOriginalFilename() == null ? "untitled.md" : f.getOriginalFilename();
            String title = original.replaceAll("(?i)\\.md$", "");
            String content;
            try {
                content = new String(f.getBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new BizException(500, "读取文件失败: " + original);
            }
            String firstLine = content.lines().findFirst().orElse("");
            if (firstLine.startsWith("# ")) {
                title = firstLine.substring(2).trim();
                content = content.substring(firstLine.length()).stripLeading();
            }
            if (title.isBlank()) {
                title = "未命名";
            }

            Knowledge k = new Knowledge();
            k.setTitle(title);
            k.setContent(content);
            k.setCategoryId(categoryId == null ? 0L : categoryId);
            k.setStatus("DRAFT");
            knowledgeMapper.insert(k);
            count++;
        }
        return count;
    }
}
