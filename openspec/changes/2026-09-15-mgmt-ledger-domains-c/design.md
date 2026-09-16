# Design: ④-C 管理台账扩展 6 个生产必需域

## 1. 目标与范围

把原型的 6 个静态域真后端化，**复用既有通用台账能力**，不新增对外接口、不改表结构：

- `key-location`（重点部位）
- `incident-archive`（事件档案）
- `drill-script`（演练脚本）
- `linkage-unit`（联动单位）
- `emergency-pool`（应急池/应急资源池）
- `ef-medium`（消防介质）

## 2. 架构与落点

```
前端 apps/mgmt  →  MgmtLedgerView.vue（既有通用渲染组件，按 route.path 决定 domain）
                ↓  GET /api/v1/mgmt-ledger/{domain}[/meta]
后端            →  MgmtLedgerController / Service（既有，无改动）
                ↓
数据            →  mgmt_ledger_meta / mgmt_ledger_row / mgmt_ledger_cell（既有三表）
                   ← V52 只做**种子化**：插入 6 个域的 meta/row/cell
```

**关键点：本变更不新建 Controller / Service / Mapper / DTO。** 6 个域是既有 `mgmt-ledger` 能力的数据实例，而非新能力。

## 3. 数据设计

- 迁移：**V52**，三方言齐备（`db/migration/{h2,dameng,postgresql}`）。
- 仅 INSERT 种子：`mgmt_ledger_meta`（域元信息）+ `mgmt_ledger_row`（行）+ `mgmt_ledger_cell`（单元格）。
- 列定义沿用前端 `mgmtMenus` 原型页约定，保证「原型看到的列」与「后端返回的列」一致。
- **无 ALTER、无新表、无存量数据修改**；对已有 18 个域零影响。

## 4. 前端落点

- `apps/mgmt/router.ts`：6 个路径加入 `SERVICE_PATHS` 与 `MGMT_LEDGER_PATHS`，命中 `MgmtLedgerView.vue`（优先于 `module-embed` 兜底）。
- `src/data/protoPages.ts`：移除这 6 个 slug（已非原型嵌入页）。**注意**：该清单由 `protoPages.spec.ts` 与原型 `index.html` 的 `data-page` 一致性守护，删项须同步原型侧，否则测试红。

## 5. 决策（ADR）

- **ADR-1：扩种子而非建端点。** 6 个域形态一致（只读二维台账），复用通用能力避免 6 套重复 Controller/Service/契约。代价：列语义差异由种子数据承载，不在类型系统里表达。
- **ADR-2：只读不写。** 沿用 `mgmt-ledger` 的零下行控制红线（仅 GET），不新增写接口。
- **ADR-3：契约不动。** 复用 `frontend-scaffold/docs/api/mgmt-ledger.openapi.json`，无 schema 变更，故 `check-api-contract --strict` 应保持 0 漂移。

## 6. 风险与依赖

| 风险 | 影响 | 处置 |
| --- | --- | --- |
| 达梦/PG 方言未实跑 | V52 的方言正确性未验证 | 沿 V51 状态标注「未实跑」，待 DM8/PG 实例 `flyway migrate` 校验（本 Change 保留的未勾项） |
| `protoPages.ts` 删项破坏原型一致性测试 | `protoPages.spec.ts` 失败 | 与原型 `index.html` 的 `data-page` 同步增删 |
| 6 域列定义与原型漂移 | 页面列与设计不符 | 列定义直接沿用 `mgmtMenus` 原型页约定，不做二次加工 |

## 7. 回退

删除 V52 迁移、清空 6 个域的 `mgmt_ledger_*` 行，并恢复 `router.ts` / `protoPages.ts` 相应条目即可；风险极低，无存量数据影响。

## 8. 验收口径

端到端：`GET /api/v1/mgmt-ledger/{domain}` 对 6 个域均返回 `code=0` 且 `rows` 非空；`MGMT_LEDGER_PATHS` 命中真实视图而非 `module-embed` 兜底；`type-check` 通过；`check-api-contract --strict` 0 漂移。
