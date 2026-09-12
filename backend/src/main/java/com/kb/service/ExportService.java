package com.kb.service;

import com.kb.common.BizException;
import com.kb.entity.Knowledge;
import com.kb.mapper.KnowledgeMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final KnowledgeMapper knowledgeMapper;

    @Value("${kb.upload-dir:./upload}")
    private String uploadDir;

    private static String safeName(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public void exportMd(Long id, HttpServletResponse response) throws IOException {
        Knowledge k = knowledgeMapper.selectById(id);
        if (k == null) {
            throw new BizException(404, "知识不存在");
        }
        String filename = URLEncoder.encode(safeName(k.getTitle()) + ".md", StandardCharsets.UTF_8);
        response.setContentType("text/markdown;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.getWriter().write(k.getContent() == null ? "" : k.getContent());
    }

    public void exportAll(HttpServletResponse response) throws IOException {
        List<Knowledge> all = knowledgeMapper.selectList(null);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"kb-export-" + LocalDate.now() + ".zip\"");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            Set<String> used = new HashSet<>();
            for (Knowledge k : all) {
                String name = safeName(k.getId() + "-" + k.getTitle());
                if (name.length() > 100) {
                    name = name.substring(0, 100);
                }
                while (!used.add(name)) {
                    name = name + "-" + k.getId();
                }
                zos.putNextEntry(new ZipEntry("knowledge/" + name + ".md"));
                zos.write((k.getContent() == null ? "" : k.getContent()).getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            Path upload = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (Files.exists(upload)) {
                try (var walk = Files.walk(upload)) {
                    for (Path p : walk.filter(Files::isRegularFile).toList()) {
                        String rel = upload.relativize(p).toString().replace('\\', '/');
                        zos.putNextEntry(new ZipEntry("upload/" + rel));
                        zos.write(Files.readAllBytes(p));
                        zos.closeEntry();
                    }
                }
            }
        }
    }
}
