# 日知 (Rizhi)

个人知识库桌面应用：单用户、本地运行、数据自己掌控。

名字取自顾炎武《日知录》"日知其所亡"——每天随手记一点，日积月累。

## 功能

- **知识管理**：Markdown 编辑（图片粘贴上传）、树形分类、标签、收藏
- **搜索**：标题 / 正文关键词 + 分类 / 标签筛选
- **日程**：今日 / 明日 / 本周视图，状态勾选
- **桌面小组件**：托盘常驻 + `Ctrl+Shift+Space` 呼出悬浮窗（快速记录 / 今日计划）
- **数据安全**：单篇导出 .md、全库导出 zip、一键数据库备份

## 技术栈

| 层 | 技术 |
| ---- | ---- |
| 桌面壳 | Electron（托盘 / 悬浮窗 / 窗口管理 / 拉起后端） |
| 前端 | Vue 3 + Vite + Element Plus + md-editor-v3 |
| 后端 | Java 21 + Spring Boot 4 + MyBatis-Plus |
| 数据库 | MySQL 8（7 张表，见 `backend/sql/init.sql`） |

## 目录结构

```text
backend\   Spring Boot 后端（entity / mapper / service / controller / dto / config / common）
app\       Electron + Vue 3 前端（electron 主进程 / src 页面 / widget 悬浮窗）
```

## 本地运行

前置：Java 21、Maven、Node.js 18+、MySQL 8

```bash
# 1. 初始化数据库
mysql -u root -p < backend/sql/init.sql

# 2. 配置数据库密码（不入版本库）
cp app/electron/backend-env.example.json app/electron/backend-env.json
# 编辑 backend-env.json 填入你的密码

# 3. 构建后端
cd backend && mvn package -DskipTests

# 4. 构建前端
cd ../app && npm install && npm run build

# 5. 启动桌面应用（自动拉起后端，退出时自动停止）
npx electron .
```

开发模式（前端热更新）：`cd app && npm run dev`

打包便携 exe（双击即用，产出 `app/release/Rizhi-1.0.0.exe`）：先构建后端，再 `cd app && npm run package`。使用时把 `backend-env.json` 放在 exe 同目录，`upload/`、`backup/` 数据目录会自动生成在 exe 旁。

## 其他说明

- 附件存 `app/upload/`，数据库备份存 `app/backup/`
- 手动启动后端时用环境变量传密码：`KB_DB_PASSWORD=xxx java -jar backend/target/kb-backend-1.0.0.jar`
- 方案文档见 `个人知识库实现方案.md`
