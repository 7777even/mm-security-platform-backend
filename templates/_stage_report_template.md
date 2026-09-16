# 阶段汇报 — <阶段 #N：名称>

- 汇报日期：<YYYY-MM-DD>
- 汇报人：<姓名 / AI 协作体>
- 对应路线图阶段：<docs/architecture/roadmap.md §1 阶段 #N>
- 受众：甲方 / 内部团队 / 双端

> 用途：按阶段向甲方或团队汇报进展，严格对照 `roadmap.md §2` 阶段完成判据，用**证据**而非形容词说话。
> 复制本模板到 `engineering/plans/YYYY-MM-DD-stage-<n>-report.md` 填充。

## 1. 阶段目标（摘抄路线图硬判据）

- <摘抄 roadmap §2 中本阶段适用的判据：端到端闭环 / 回归全绿 / 契约对拍零漂移 / 文档同步 / 双轴 Review>

## 2. 完成情况（逐条对照判据）

| 判据 | 状态 | 证据 |
| --- | --- | --- |
| 端到端闭环（真实链路，非 Mock/空表/占位） | ✅ / ⬜ | <链接或一句话证据> |
| 回归全绿（后端 `mvn test` 0 failure；前端 `vitest` 0 failed、`type-check` 0 错） | ✅ / ⬜ | <例：后端 168/0；前端 338 passed/0> |
| 契约对拍零漂移（`check-api-contract --strict` 通过） | ✅ / ⬜ | <通过 / 差异数> |
| 文档同步（`docs/` 与 `openspec/specs/` 反映最新事实） | ✅ / ⬜ | <更新了哪些文档> |
| 双轴 Review（规格符合性 + 代码质量） | ✅ / ⬜ | <通过 / 需修改 / 需人工决策> |

## 3. 关键交付

- <列表：可链接 PR / commit / 设计文档 / spec。例如：新建 `auth-rbac` spec、补齐三篇设计文档代码、收口 roadmap §4 债务>

## 4. 质量证据（可机读）

- **后端**：单测 <N> 例 0 failure；JaCoCo 行覆盖 <X%>（≥0.80）；契约对拍 0 漂移
- **前端**：vitest <N> passed / 0 failed；`type-check` 0 错；契约四铁律通过
- **安全**：<如适用：密钥 fail-fast、CORS 白名单、越权守门 `assertSelfOrAdmin` / `@RequireAuth`、刷新令牌 Cookie 化>

## 5. 风险与阻塞

- <例：达梦 DM8 实测迁移依赖环境；甲方《功能项清单》未到，需求追溯列挂账；Testcontainers 版 DB-IT 依赖 Docker>

## 6. 下一步

- <下一阶段或待决项，含需要甲方 / 环境配合的前置条件>
