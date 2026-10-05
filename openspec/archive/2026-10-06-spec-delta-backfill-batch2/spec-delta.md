# Spec Delta: 第二批 spec-delta 回填（缺口清零）

> 与首批同口径：**不新增需求**，只把已归档 `spec-delta.md` 回填（harvest）到
> `openspec/specs/<capability>/spec.md`。少数端点确无 delta 来源，按契约 + 实现反推并标注。

## ADDED — 新建 capability（1 个）

| capability | 端点数 | 来源 |
| ---------- | ---: | ---- |
| `device` | 5（缺口 3） | `2026-10-03-mgmt-device-comm-record-crud-realtime` |

## ADDED — 补充既有 capability（10 个）

| capability | 补入端点数 | 主要来源 |
| ---------- | ---: | -------- |
| `system-management` | 15 | `2026-09-10-system-management-rbac` |
| `rescue-resource` | 14 | `2026-10-02-mgmt-p1-rescue-resource-crud` |
| `communication` | 7 | `2026-10-03-mgmt-device-comm-record-crud-realtime`、`mgmt-monitor-video-comm-crud-realtime` |
| `emergency-reference` | 7 | `2026-10-01-mgmt-p0-batch-crud` + 反推（process.*） |
| `production-alarm` | 7 | `2026-09-23-production-alarm-writeback` + 反推（只读 6 端点） |
| `tv` | 5 | `2026-09-30-tv-maintenance-order`、`tv-monitor-category` + 反推 |
| `dashboard-analytics` | 3 | `2026-09-13-workstation-datascope-list` + 反推 |
| `mgmt-ledger` | 3 | `2026-10-03-mgmt-ledger-write`、`2026-10-04-mgmt-ledger-write-perm` |
| `auth-rbac` | 1 | `2026-09-10-system-management-rbac` |
| `emergency-event` | 1 | 反推 |

## 反推来源标注（8 个端点，已在 spec 内显式标注）

`GET /production/overview`、`/production/areas/{facilityId}`、`/production/devices`、
`/production/personnel`、`/production/risk-warnings`、`/production/risk-warnings/{id}`、
`GET /tv/inspections`、`GET /tv/map-points`、`GET /dashboard/overview`、`GET /dashboard/messages`、
`GET /emergency/process/panorama`、`/process/guidances`、`PUT /process/node-configs`、
`GET /emergency-events/evacuation-people`
——在 74 个归档 Change 的 md 中全库检索无命中，按契约真源 + Controller 实现反推写入。

## 不变

- 不新增 / 修改任何端点、契约、迁移与代码；
- 不改写一个既有决策语义；
- `check-api-contract --strict`、`check-endpoint-authz` 不受影响。

## 治理效果（可复现）

`python scripts/gen-scope-inventory.py --gaps`：

- 首批前：**196 / 288（68%）**
- 首批后：**66 / 288（22%）**
- **本批后：0 / 288（0%）——31 个契约域全部 0 缺口，「端点 → capability spec」追溯链完整闭合。**

> ⚠️ 回填踩坑：spec 里写 `{resource}` 这类**占位路径**不会被追溯脚本识别，
> 必须把 `{resource}` ∈ {personnel, brigades, vehicles, equipment} 展开成显式路径清单
> （首批 rescue-resource 补了 9 条却仍显示缺口，即因占位符未命中）。
