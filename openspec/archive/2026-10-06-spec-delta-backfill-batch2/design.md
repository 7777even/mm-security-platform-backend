# Design: 第二批 spec-delta 回填（缺口清零）

## 1. 与首批的差异

首批新建 11 个 capability（因为那些域连目录都没有）。本批**以补充既有 spec 为主**——
剩余 66 条端点分属的域大多已有 spec，只是 spec 里没写这些端点（同样是「归档不合并」的后遗症）。

判据不变：先用 `python scripts/gen-scope-inventory.py --gaps` 取出「spec 未提及端点」清单，
再按端点路径去 `openspec/archive/*/` 里搜 delta 原文；**搜到就照抄，搜不到才由契约 + 实现反推**。

## 2. 回填映射表

| capability | 端点 | 来源 |
| ---------- | ---- | ---- |
| `device`（新建） | `GET/POST /devices`、`GET/PUT/DELETE /devices/{code}` | `2026-10-03-mgmt-device-comm-record-crud-realtime` |
| `system-management` | dict-types / dict-items / menus / permissions 四组 CRUD、`PUT /roles/{id}/status`、`PUT /users/{id}/status` | `2026-09-10-system-management-rbac` |
| `rescue-resource` | 四台账 CRUD + 按 id 读（personnel / brigades / vehicles / equipment） | `2026-10-02-mgmt-p1-rescue-resource-crud` |
| `communication` | devices CRUD + records PUT/DELETE | `2026-10-03-mgmt-device-comm-record-crud-realtime`、`mgmt-monitor-video-comm-crud-realtime` |
| `emergency-reference` | command-records / duty-sign-ins 的 PUT 与 DELETE、`process/*` 三端点 | `2026-10-01-mgmt-p0-batch-crud` + 反推 |
| `production-alarm` | 生产域只读 6 端点 + 报警抓拍关联 | `2026-09-23-production-alarm-writeback` + 反推 |
| `tv` | inspections / maintenance-orders / map-points / overview | `2026-09-30-tv-maintenance-order`、`tv-monitor-category` + 反推 |
| `dashboard-analytics` | overview / workstations/{id} / messages | `2026-09-13-workstation-datascope-list` + 反推 |
| `mgmt-ledger` | `POST/PUT/DELETE /mgmt-ledger/{domain}/rows[/ {rowId}]` | `2026-10-03-mgmt-ledger-write` |
| `auth-rbac` | `PUT /auth/profile` | `2026-09-10-system-management-rbac` |
| `emergency-event` | `GET /emergency-events/evacuation-people` | 反推 |

## 3. 「无 delta 来源」的处置约定

以下端点在 74 个归档 Change 的 md 里**检索不到任何提及**（`grep -rl` 全库无命中），
属历史上直接落地、未走 spec 增量的端点：

- `GET /production/overview`、`/production/areas/{facilityId}`、`/production/devices`、
  `/production/personnel`、`/production/risk-warnings`、`/production/risk-warnings/{id}`、
  `/production/alarms/{id}/snapshots`
- `GET /tv/inspections`、`/tv/map-points`
- `GET /dashboard/overview`、`/dashboard/messages`
- `GET /emergency/process/panorama`、`/process/guidances`、`PUT /process/node-configs`
- `GET /emergency-events/evacuation-people`

处置：**照契约真源（`docs/api/*.openapi.json` 的 summary/description）+ Controller 实现反推写入，
并在对应 Requirement 下加一行 `> 来源说明：无已归档 spec-delta，按契约 + 实现反推。`**
不假装存在原始决策记录。

## 4. 验收

1. `python scripts/gen-scope-inventory.py --gaps` → **合计 0**（288/288 全覆盖）；
2. `node scripts/check-openspec-hygiene.mjs` 通过；
3. 抽查回填内容与源 delta 语义一致。
