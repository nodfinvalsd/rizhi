package com.kb.service;

import com.kb.common.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class BackupService {

    @Value("${kb.backup-dir:./backup}")
    private String backupDir;

    @Value("${kb.mysqldump:C:/MySQL/MySQL Server 8.0/bin/mysqldump.exe}")
    private String mysqldump;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    public String backup() {
        try {
            Path dir = Paths.get(backupDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = "kb-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".sql";
            Path out = dir.resolve(filename);

            ProcessBuilder pb = new ProcessBuilder(
                    mysqldump,
                    "-u" + dbUser,
                    "-p" + dbPassword,
                    "--databases", "kb",
                    "--result-file=" + out);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String err = new String(p.getInputStream().readAllBytes());
            int code = p.waitFor();
            if (code != 0) {
                log.error("mysqldump failed: {}", err);
                throw new BizException(500, "数据库备份失败");
            }
            return out.toString();
        } catch (IOException | InterruptedException e) {
            log.error("backup error", e);
            throw new BizException(500, "数据库备份失败");
        }
    }
}
