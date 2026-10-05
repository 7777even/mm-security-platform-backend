# Change: 归档 spec-delta 回填（第二批）——spec 覆盖缺口清零

## 为什么

首批（Change `2026-10-06-spec-delta-backfill`）把缺口从 196/288（68%）降到 66/288（22%），
100% 缺口的 11 个域全部清零。本批收尾剩余 **66 条**，目标是把缺口压到 **0**，
使「端点 → capability spec」这条追溯链完整闭合。

与首批同口径：根因仍是**归档 Change 的 `spec-delta.md` 未合并进 `openspec/specs/`**，
不是需求缺失。本批同样**只回填、不新增需求**；少数端点确无 delta 来源的，
按契约真源 + 实现反推并在 spec 内显式标注来源。

## 变更内容

**新建 1 个 capability**：`device`（3 端点：20 位 MDM 编码设备台账 CRUD）。

**补充 10 个既有 capability**：

| capability | 补入端点数 | 主要来源（已归档 spec-delta） |
| ---------- | ---: | ---------------------------- |
| `system-management` | 15 | `2026-09-10-system-management-rbac` |
| `rescue-resource` | 14 | `2026-10-02-mgmt-p1-rescue-resource-crud`、`2026-09-21-rescue-force-single-source` |
| `communication` | 7 | `2026-10-03-mgmt-device-comm-record-crud-realtime`、`2026-10-03-mgmt-monitor-video-comm-crud-realtime` |
| `emergency-reference` | 7 | `2026-10-01-mgmt-p0-batch-crud`、契约与实现反推（process.* 三个端点无 delta） |
| `production-alarm` | 7 | `2026-09-23-production-alarm-writeback`、契约与实现反推（生产域只读端点） |
| `tv` | 5 | `2026-09-30-tv-maintenance-order`、`2026-09-30-tv-monitor-category`（inspections / map-points 无 delta） |
| `dashboard-analytics` | 3 | `2026-09-13-workstation-datascope-list`、契约与实现反推（overview / messages） |
| `mgmt-ledger` | 3 | `2026-10-03-mgmt-ledger-write`、`2026-10-04-mgmt-ledger-write-perm` |
| `auth-rbac` | 1 | `2026-09-10-system-management-rbac`（`/auth/profile`） |
| `emergency-event` | 1 | 契约与实现反推（`/emergency-events/evacuation-people`，无 delta） |

## 范围与非目标

- **不改代码、不改契约、不加迁移、不加端点**，纯过程资产治理（L2）。
- **非目标**：
  - 不改写任何既有需求语义（照抄 delta 原文）；
  - 不为凑覆盖率编造 Requirement——无 delta 来源的 8 个端点（production 只读 6、tv 2、
    dashboard 2、emergency process 3、emergency-event 1）在 spec 内标注「来源：契约 + 实现反推」，
    以后若找到原始决策记录应回链替换。

## 兼容性

纯文档资产，无运行时影响；`check-openspec-hygiene`、`check-api-contract --strict`、
`check-endpoint-authz` 均不受影响。
