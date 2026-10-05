# Design: 归档 spec-delta 回填至 capability spec

## 1. 缺口根因（先定位再动手）

`docs/requirement/scope-inventory.md §3` 用脚本量化出「spec 未提及端点 196/288」后，先查了
`openspec/archive/*/spec-delta.md`：72 个归档 Change 里 70 个带 delta，但 `openspec/specs/` 只有 25 个
能力域目录。对照 delta 标题与目录名，缺口并非「需求缺失」，而是**归档流程只搬运了目录、没合并 delta 内容**。

判据（避免误判成"功能没做"）：
- 契约未登记端点 0、前端零引用端点 0、写端点授权 148/148 → 实现侧闭环；
- 缺的只是「端点 → capability spec」这一层追溯留痕。

## 2. 回填映射表（delta → capability）

| 新建 / 补充 capability | 内容来源（已归档 Change 的 spec-delta） | 端点数 |
| ---------- | -------------------------------------- | ---: |
| `fire-facility`（新） | `2026-10-01-fire-facility-fault-crud`、`2026-10-03-mgmt-p2-fire-facility-patrol-crud`（台账部分）、`2026-10-04-fire-facility-alarm-handle` | 15 |
| `fire-monitoring`（新） | `2026-10-03-mgmt-p2-fire-facility-patrol-crud`（防火巡查部分）、`2026-09-23-fire-facility-monitor-report`（监测上报，归 fire-facility 与 fire-monitoring 两侧按端点归属拆分） | 12 |
| `fire-situation`（新） | `2026-09-23-fire-facility-zone-unification`（装置区设备数真源归一） | 3 |
| `fire-alarm`（新） | `2026-09-22-add-fire-alarm-writeback`、`2026-09-22-fire-alarm-disposal-persist`、`2026-10-01-fire-alarm-crud` | 4 |
| `emergency-plan`（新） | `2026-10-02-mgmt-p1-plan-crud`、`2026-10-02-mgmt-p1-plan-catalog-crud` | 16 |
| `hazard`（新） | `2026-10-04-mgmt-hazard-special-operation-crud-realtime`（hazard / hazard.point 部分） | 11 |
| `special-operation`（新） | `2026-10-04-mgmt-hazard-special-operation-crud-realtime`（special-operation 部分） | 5 |
| `security`（新，含安防黑名单） | `2026-10-03-mgmt-p2-gate-bollard-crud`、`2026-10-03-mgmt-p2-security-crud`、`2026-10-03-mgmt-p2-perimeter-maintenance-closure` | 29（含 blacklist 3） |
| `typhoon-emergency`（新） | `2026-10-01-mgmt-p0-batch-crud`（台风调度部分） | 7 |
| `video`（新） | `2026-10-03-mgmt-monitor-video-comm-crud-realtime`（video.camera 部分） | 14 |
| `weather`（新，无 delta 来源） | 由契约 `weather.openapi.json` 与 `WeatherController` 实现反推，spec 内显式标注来源 | 1 |
| `emergency-reference`（补充） | `2026-10-02-mgmt-p1-phone-crud`、`2026-10-02-mgmt-p1-knowledge-crud`、`2026-10-02-mgmt-p1-case-crud`、`2026-10-01-mgmt-p0-batch-crud`（应急指令 / 值班签到） | 31（本批补写端点） |

## 3. 体例（与既有 spec 对齐）

照 `tv/spec.md` / `msds-domain/spec.md` 体例：

```
# <capability> Specification
## Purpose          —— 能力定位 + 数据来源 + 设计依据（指向归档 Change）
## Endpoints        —— 端点 / 权限 / 说明 一览表（可选，写端点多的域建议给）
## Requirements
### Requirement: <名称>
#### Scenario: <名称>
- **WHEN** ...
- **THEN** ...
```

- 权限码、广播域、B3 错误码均**照抄 delta 原文**，不做「顺手优化」。
- 零下行控制（DTO 不含 `status`、写操作不得触发物理设备下行）在涉及硬控设备的域必须显式写成 Requirement。

## 4. 验收

1. `node scripts/check-openspec-hygiene.mjs` 通过；
2. 重跑 `python scripts/gen-scope-inventory.py` → §3 缺口数由 196 显著下降，且**不出现新增的「契约未登记 / 前端零引用」**；
3. 抽查回填后的 spec 与源 delta 逐条语义一致（不改需求）。

## 5. 剩余缺口（本批不处理，留后续批次）

`emergency` 域读端点（assist-stats / commands / process.* 等）、`system` 域 15 条、`production` 域 7 条、
`dashboard` 3 条、`device` 3 条、`mgmt-ledger` 3 条、`auth` 1 条、`emergency-event` 1 条。
一次性大改不利于 review，按域分批补。
