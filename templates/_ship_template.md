# Ship — <发布主题 / 版本>

> 发布前检查清单与执行记录模板。每次进入阶段 7「生产就绪 / 部署演练」或任何向测试/生产环境发布时**必填一份**，与 QA / Retro 同属工程记录闭环。复制本模板到 `engineering/ship/<YYYY-MM-DD>-<主题>.md` 填写。

- 日期: <YYYY-MM-DD>
- 版本 / Change: <对应 openspec Change 或镜像 tag>
- 目标环境: <dev 演示 / 测试环境 / 生产(达梦) / 客户环境>
- 操作人: <姓名 / AI 会话 id>
- 效率等级: <L3 / L4>

## 1. 代码检查（发布前门禁）

- [ ] 后端 `mvn -s ci-settings.xml test` 全绿（含 `*IT` 集成层）；jacoco 行覆盖 ≥ 0.80
- [ ] 前端 `npm run type-check` + `vitest run` 全绿
- [ ] 跨库契约四同步已完成（`scripts/check-api-contract.mjs --strict` 通过；前端 `gen:api-types` 已重跑）
- [ ] 无遗留 `TODO` / 占位实现（见 `end-to-end-roadmap.mdc` 禁止占位规则）
- [ ] 提交按 scope 拆分、无 `--amend`、无密钥/`.env` 入库

## 2. 数据库迁移（仅涉及库结构变更时）

- [ ] 新增仅走 Flyway V 文件（达梦补 `db/migration/dameng/V*` 与 H2 版本对齐；**禁改/删已有 V-file**）
- [ ] 迁移在**影子库 / 测试库**先完整跑通（Flyway `flyway_schema_history` 行数核对）
- [ ] 种子数据（`sys_menu` 5 个 fm-* 菜单 / 应急参考表）与代码口径一致
- [ ] 生产库迁移有回滚预案（新增反向 `V*` 脚本，禁用 `flyway clean`）

## 3. 环境配置

- [ ] `JWT_SECRET` / `SIGNATURE_SECRET` 已注入（≥ 32 / 16 字节，非占位）
- [ ] `CORS_ALLOWED_ORIGINS` 显式白名单（非 `*`）
- [ ] `COOKIE_SECURE=true`（生产 HTTPS）
- [ ] 数据源 `DB_*` 已配；`app.cookie.secure` 与 `signature.enabled=true`
- [ ] `/actuator/prometheus` 限内网（不暴露公网）
- [ ] 参照 `docs/deployment/customer-environment-questionnaire.md` 已与甲方对齐

## 4. 部署步骤

1. 构建：后端 `mvn package -DskipTests -P<dm|prod>`；前端 `npm run build`（VITE_API_BASE 等构建期固化）
2. 推送镜像 / 制品到目标环境
3. 起库迁移（Flyway 自动）→ 应用启动 → 健康检查 `GET /actuator/health` 期望 `{"status":"UP"}`
4. 冒烟（对齐 `docs/deployment/README.md §7`）：登录 → `/auth/menus` 5 菜单 → `/devices` → `/dashboard/overview` → `ws://.../ws/alarm` 收推送
5. 中文冒烟用 Python(utf-8)，勿裸 `curl` 带中文 body

## 5. 回滚方案

- [ ] 应用回退：镜像/制品回退到上一版本（K8s 回滚 / compose 旧 tag / systemd 旧 jar）
- [ ] 库结构回退：用新增反向 `V*` 脚本，绝不用 `flyway clean`（会清空整库）
- [ ] 配置回退：`.env` / 配置中心回退到上一生效版本
- [ ] 回滚触发条件与责任人：<如冒烟失败 / 健康连续 N 次异常 / 关键接口 5xx 超阈值>
- [ ] 回滚后验证：重复 §4 冒烟，确认恢复

## 6. 发布结论（三选一，禁止模糊）

- [ ] 通过：可投入使用
- [ ] 有条件通过：<列出待办与时限>
- [ ] 不通过：<阻塞项与重发条件>

> 本模板与 `templates/_qa_template.md` / `_retro_template.md` 同源，发布后若发现步骤缺失，回到 `templates/` 闭环补充，避免第二套发布流程。
