# engineering/ship/

发布检查与执行记录目录。每次向测试 / 生产环境发布前，从 `templates/_ship_template.md` 复制一份 `<YYYY-MM-DD>-<主题>.md` 填写，覆盖：代码检查 / 数据库迁移 / 环境配置 / 部署步骤 / 回滚方案 五段式。

与 `engineering/qa/`（质量记录）、`engineering/retro/`（复盘）同属工程记录闭环；发布结论须明确「通过 / 有条件通过 / 不通过」，禁止「基本可以」等模糊结论（对齐 AGENTS.md §9）。

阶段 7（达梦实测 / prod 联调 / 部署演练 / 安全渗透复核）启动后首个真实发布即应落盘第一份 ship 记录。
