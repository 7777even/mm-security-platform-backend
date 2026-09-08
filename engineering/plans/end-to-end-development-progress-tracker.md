# 后端端到端开发进度跟踪表

> 进度记录真相源：本表只登记**能力域与里程碑状态**，不扩写成 `openspec/` 之外的第二套 Task（实施任务一律以 `openspec/changes/<name>/tasks.md` 为准）。
> 维护纪律：每完成一个已确认 Task / Change，先回填 `tasks.md` 勾选框，再更新本表对应行（状态、Change、验收证据、阻塞项、行更新时间）。
> 长期路线见 `docs/architecture/roadmap.md`。

创建日期：2026-09-08
最后更新：2026-09-08（openspec 回填归档闭环落地：6 个已完结 Change 的 spec 合入 `openspec/specs/` 9 个 capability 并归档至 `openspec/archive/2026-09-08-*`；CI 增 `check-openspec-hygiene.mjs` 守门）

## 能力域状态

| 能力域 / 里程碑 | 状态 | OpenSpec Change | 验收证据 | 阻塞项 | 行更新时间 |
| --- | --- | --- | --- | --- | --- |
| 安全与工程基线加固（生产配置基线、CORS 白名单、过滤器顺序、分页 DTO、去 mock 回落） | ✅ 完成 | `harden-backend-baseline`（已归档） | `engineering/qa/2026-09-07-backend-baseline.md`；retro 同日 | 无 | 2026-09-08 |
| 零依赖单测基线（standalone MockMvc + 纯 Mockito，155 单测全绿；jacoco 行覆盖 87.6% ≥ 0.80 门禁） | ✅ 完成 | `harden-backend-baseline`（已归档） | `mvn test` 全绿 + jacoco 报告 | 无 | 2026-09-08 |
| 报警域契约对齐（AlarmItem 字段与前端契约一致） | ✅ 完成 | `align-alarm-contract-fields`（已归档） | `engineering/qa/2026-09-07-alarm-fields-align.md` | 无 | 2026-09-08 |
| 报警趋势端点（`GET /dashboard/alarm-trend`） | ✅ 完成 | `add-dashboard-alarm-trend`（已归档） | `engineering/qa/2026-09-07-alarm-trend.md` | 无 | 2026-09-08 |
| 契约债清零（报警 CRUD / 应急参考数据 / 地图点位 / 审计与现场回传 / 风险热力） | ✅ 完成 | `implement-remaining-contracts`（已归档） | 155 单测含对应路径全绿；`check-api-contract.mjs --strict` 0 漂移 | 无 | 2026-09-08 |
| Flyway 生产迁移（双轨 + h2/postgresql/dameng 三方言） | ✅ 完成 | `feat-flyway-prod-migration`（已归档） | dev H2 Flyway 实跑；PG/H2 本地可验 | 达梦 DM8 无实例，迁移脚本未在达梦上复核 | 2026-09-08 |
| 可观测性探针（actuator health/liveness/readiness/info） | ✅ 完成 | `feat-observability-probes`（已归档） | `engineering/qa/2026-09-07-backend-baseline.md` 关联；health 冒烟 | 无 | 2026-09-08 |
| CI/CD + 容器化 + openspec 归档闭环守门 | ✅ 完成 | Change 外交付 | `.github/workflows/ci.yml`（test / contract-guard / openspec-hygiene） | 无 | 2026-09-08 |
| `/actuator/prometheus` 指标端点 | ✅ 已实现 / ⚠️ spec 未回填 | 无（Change 外交付） | 免鉴权限定范围验证 | 待随下次可观测性 Change 补 capability spec | 2026-09-08 |
| 认证 / RBAC 域 capability spec（login、me/menus、刷新令牌 Cookie 化、AuthorizationService、白名单口径） | ⬜ 未建 spec（能力已实现，先于 openspec 体系） | 无 | 契约 `auth.openapi.json` + RBAC 单测守护 | 待随下次 auth 相关 Change 回填 | 2026-09-08 |
| 达梦 DM8 生产迁移实测 | ⬜ 未开始 | — | — | 本机无达梦实例/驱动；`application-dm.yml` 与 `db/migration/dameng` 资产保留 | 2026-09-08 |
| 业务域纵深（阶段 6：预警确认流端到端、应急指挥纵深、设备读数、统计真实化） | ⬜ 按 Change 推进 | 待新提案 | — | 以前端业务域需求输入为准 | 2026-09-08 |

## 记录规则

1. 状态枚举：⬜ 未开始 / ⬜ 按 Change 推进 / ✅ 完成 / ⚠️ 有缺口（注明缺口）。
2. 「OpenSpec Change」列只填已确认 Change 名（归档后注明已归档）；Change 外交付必须显式标注，并评估是否补 spec。
3. 验收证据优先链接 `engineering/qa/` 文件；无 QA 文件时写明实际执行的命令与结果，禁止把未执行写成通过。
