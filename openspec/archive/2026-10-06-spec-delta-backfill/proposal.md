# Change: 归档 spec-delta 回填至 capability spec（覆盖缺口治理）

## 为什么

2026-10-06 量化发现（见 `docs/requirement/scope-inventory.md §3`）：后端 **288 个端点中 196 条（68%）**
在 `openspec/specs/*` 里**没有任何归属留痕**，其中 11 个契约域 100% 缺口共 91 条。

根因**不是「功能没做、需求没写」**——三项交叉校验已证明功能是闭环的（契约未登记 0、前端零引用 0、
写端点授权 148/148）。真正原因是：**已归档 Change 的 `spec-delta.md` 从来没有回填进
`openspec/specs/<capability>/spec.md`**。

- 72 个归档 Change 中 **70 个带 `spec-delta.md`**；
- 但 `fire-facility` / `fire-monitoring` / `fire-situation` / `fire-alarm` / `emergency-plan` /
  `hazard` / `special-operation` / `security` / `typhoon-emergency` / `video` / `weather` 这些能力域，
  在 `openspec/specs/` 下**连目录都没有**——deltas 写了却无处落地。

后果：甲方按「功能项 → 端点 → spec → 测试」四层追溯时，**第 3 层断链**；后续 AI 读 `openspec/specs/`
也会误判这些域「无需求规约」。

## 变更内容

1. **新建 11 个 capability spec**（`openspec/specs/<capability>/spec.md`），内容均从已归档
   `spec-delta.md` **原样回填**——不改写一个既有决策、不新增需求：
   `fire-facility`、`fire-monitoring`、`fire-situation`、`fire-alarm`、`emergency-plan`、
   `hazard`、`special-operation`、`security`（含安防黑名单）、`typhoon-emergency`、`video`、`weather`。
2. **回填补充**既有 `emergency-reference` spec：应急通讯录 / 知识库 / 事故案例 / 应急指令 /
   值班签到五组写端点（来自 `2026-10-02-mgmt-p1-*` 与 `2026-10-01-mgmt-p0-batch-crud` 的 delta）。
3. 回填后重跑 `python scripts/gen-scope-inventory.py`，刷新 `scope-inventory.md` §1 / §2 / §3。

## 范围与非目标

- **不改代码、不改契约、不加迁移、不加端点**。本 Change 是纯过程资产治理（L2）。
- **非目标**：
  - 不为凑覆盖率新建**无需求支撑**的 spec——每个新建目录都必须有已归档 delta 作为内容来源；
    无 delta 来源的域（`weather` 仅 1 个端点，来源为契约与实现反推）在 spec 中显式标注来源。
  - 不改写历史决策语义；delta 原文照抄，仅按 spec 文件体例（Purpose / Endpoints / Requirements）重组。
  - 剩余缺口（`emergency` 域读端点、`system` 域部分、`production` 域部分等）不在本批，
    留作后续批次，避免一次性大改难以 review。

## 兼容性

纯文档资产，无运行时影响；`check-openspec-hygiene` 与 `check-api-contract --strict` 均不受影响
（本 Change 不触碰 `openspec/specs/` 之外的任何产物）。
