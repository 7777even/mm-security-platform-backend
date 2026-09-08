# 操作审计设计（Audit Logging）

> 操作审计的权威设计说明。审计落库口径、字段语义、写入路径的变更属 **L3**（业务能力 + 数据结构），须 proposal + 评审。本文与 `AGENTS.md §6.4`、`uplink.openapi.json#/AuditEventBatch` 互为镜像。

## 1. 决策（Decisions）

- **审计即等保要求**：前端操作审计落库对应等保二级「安全审计」（D1 C-2）。批量审计事件由前端经 `uplink` 上报，后端 `UplinkService.reportAudit` **尽力落库** `fac_audit_log`，单条失败不阻断业务主链路。
- **不可变 append-only**：`fac_audit_log` **不带逻辑删除、不带审计字段**（`created_at` 仅记落库时间），写入后不可改不可删——审计记录本身不可被篡改。
- **契约对齐**：`AuditEvent` / `AuditEventBatch` DTO 与前端 `uplink.openapi.json#/AuditEvent` 严格对齐（`action` 必填、`module` / `detail` / `at` 可选）。
- **脱敏在写入前完成**：审计 `detail` 可能含 PII（用户名、设备编码等），写入 `fac_audit_log.detail_json` 前须按 `data-masking.md` 脱敏，避免审计表成为敏感数据汇聚点。

## 2. 现状（Current State）

| 组件 | 类 / 文件 | 状态 |
| ---- | --------- | ---- |
| 落库实体 | `entity/FacAuditLog.java`（`action`/`module`/`detail_json`/`event_at`/`created_at`，无逻辑删除） | ✅ |
| Mapper | `mapper/AuditLogMapper.java`（`extends BaseMapper`） | ✅ |
| 事件 DTO | `dto/AuditEvent.java` / `dto/AuditEventBatch.java` | ✅ |
| 上报入口 | `controller/UplinkController.reportAudit(@RequestBody AuditEventBatch)` | ✅ |
| 落库逻辑 | `service/UplinkService.reportAudit`（逐条 `auditLogMapper.insert`） | ✅ |

## 3. 约束（Constraints）

- `reportAudit` 必须**尽力（best-effort）**：遍历 `batch.getEvents()`，单条 `insert` 异常捕获后记录日志并继续，不让审计失败拖垮主业务。
- `event_at` 缺省由服务端取当前时间；`created_at` 始终服务端写入，前端不可伪造落库时间。
- 审计写入路径**不触发二次审计**（避免无限递归），且不得依赖 `UserContext`（上报可能来自匿名 uplink 通道，按契约口径）。
- 审计表增长快，需配保留期策略（见 `deployment/` 阶段 7 演练项），但**保留期清理不得物理删除行**（用独立归档表或冷存，保持 append-only 语义）。

## 4. 反模式（Anti-patterns）

- ❌ 把审计表当业务表做逻辑删除 / 更新 —— 破坏 append-only 与等保追溯。
- ❌ 审计写入失败抛异常阻断主流程 —— 审计是旁路，必须尽力。
- ❌ 把明文 PII 直接写 `detail_json` —— 见 `data-masking.md`，审计表更该脱敏。
- ❌ 让 `reportAudit` 依赖 `UserContext` 鉴权 —— uplink 通道语义不同，按契约处理。
