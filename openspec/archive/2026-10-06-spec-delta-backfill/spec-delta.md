# Spec Delta: 归档 spec-delta 回填至 capability spec（覆盖缺口治理）

> 本 Change **不新增需求**，只把已归档 Change 中写好的 spec-delta 回填（harvest）到
> `openspec/specs/<capability>/spec.md`，修复「端点 → capability spec」追溯断链。
> 因此下列条目全部是 **ADDED（对 `openspec/specs/` 而言是新增）**，语义与源 delta 完全一致。

## ADDED — 新建 capability（11 个）

| capability | 端点数 | 内容来源（已归档 spec-delta） |
| ---------- | ---: | ---------------------------- |
| `fire-facility` | 15 | `2026-10-01-fire-facility-fault-crud`、`2026-10-03-mgmt-p2-fire-facility-patrol-crud`（台账）、`2026-09-23-fire-facility-monitor-report`、`2026-10-04-fire-facility-alarm-handle` |
| `fire-monitoring` | 12 | `2026-10-03-mgmt-p2-fire-facility-patrol-crud`（防火巡查）、`2026-10-01-mgmt-p0-batch-crud`（巡更执行） |
| `fire-situation` | 3 | `2026-09-23-fire-facility-zone-unification` |
| `fire-alarm` | 4 | `2026-09-22-add-fire-alarm-writeback`、`2026-09-22-fire-alarm-disposal-persist`、`2026-10-01-fire-alarm-crud` |
| `emergency-plan` | 16 | `2026-10-02-mgmt-p1-plan-crud`、`2026-10-02-mgmt-p1-plan-catalog-crud` |
| `hazard` | 11 | `2026-10-04-mgmt-hazard-special-operation-crud-realtime`（hazard / hazard.point） |
| `special-operation` | 5 | `2026-10-04-mgmt-hazard-special-operation-crud-realtime`（special-operation） |
| `security` | 29（含 blacklist 3） | `2026-10-03-mgmt-p2-gate-bollard-crud`、`2026-10-03-mgmt-p2-security-crud`、`2026-10-03-mgmt-p2-perimeter-maintenance-closure` |
| `typhoon-emergency` | 7 | `2026-10-01-mgmt-p0-batch-crud`（台风调度）、前端 V42 台风响应板 |
| `video` | 14 | `2026-10-03-mgmt-monitor-video-comm-crud-realtime`（video.camera）、`2026-09-11-video-wall-navigation` |
| `weather` | 1 | **无 delta 来源**——按契约 `weather.openapi.json` + `WeatherController` 反推，spec 内已显式标注 |

## ADDED — 补充既有 capability

- `emergency-reference`：新增「应急通讯录写端点」「应急知识库台账写端点」「事故案例库台账写端点」
  「应急指令与值班签到写端点」「应急辅助统计与指挥只读端点」5 条 Requirement
  （源：`2026-10-02-mgmt-p1-phone-crud`、`mgmt-p1-knowledge-crud`、`mgmt-p1-case-crud`、
  `2026-10-01-mgmt-p0-batch-crud`）。

## 不变

- 不新增 / 修改任何端点、契约、迁移与代码；
- 不改写一个既有决策语义（照抄 delta 原文，仅按 spec 体例重组）；
- `check-api-contract --strict` 与 `check-endpoint-authz` 不受影响。

## 治理效果（可复现）

`python scripts/gen-scope-inventory.py --gaps`：

- 回填前：**196 / 288（68%）** 端点无 spec 归属；
- 回填后：**66 / 288（22%）**；100% 缺口的 11 个域全部清零；
- 已完整覆盖（0 缺口）的域由 8 个增至 **20 个**。
