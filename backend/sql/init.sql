-- 个人知识库初始化脚本（7 张表）
CREATE DATABASE IF NOT EXISTS kb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE kb;

-- 知识分类表
CREATE TABLE IF NOT EXISTS t_knowledge_category (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(64)  NOT NULL,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    sort        INT          NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 知识文档表
CREATE TABLE IF NOT EXISTS t_knowledge (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200) NOT NULL,
    content     LONGTEXT,
    category_id BIGINT       NOT NULL DEFAULT 0,
    status      VARCHAR(16)  NOT NULL DEFAULT 'DRAFT',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_category (category_id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 标签表
CREATE TABLE IF NOT EXISTS t_tag (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    deleted TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 知识-标签关联表
CREATE TABLE IF NOT EXISTS t_knowledge_tag (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    knowledge_id BIGINT NOT NULL,
    tag_id       BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_knowledge_tag (knowledge_id, tag_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 收藏表
CREATE TABLE IF NOT EXISTS t_knowledge_favorite (
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    knowledge_id BIGINT   NOT NULL,
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_knowledge (knowledge_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 附件表
CREATE TABLE IF NOT EXISTS t_attachment (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    knowledge_id BIGINT       NOT NULL DEFAULT 0,
    file_name    VARCHAR(255) NOT NULL,
    file_path    VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_knowledge (knowledge_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 日程表
CREATE TABLE IF NOT EXISTS t_schedule (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    start_time  DATETIME,
    end_time    DATETIME,
    status      VARCHAR(16)  NOT NULL DEFAULT 'TODO',
    priority    VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 每日总结表（一天一条）
CREATE TABLE IF NOT EXISTS t_daily_summary (
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    summary_date DATE     NOT NULL,
    content      LONGTEXT,
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_date (summary_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
