# Retro · 告警字段对齐（2026-09-07）

## 背景
清完告警 mock 后，发现后端 `FacAlarm` 与前端 `AlarmItem` 字段严重失配：命名（alarmId↔id）、类型（status int↔string 枚举）、缺字段（location/category/warned/planId）、枚举不一致（type）。这是比 mock 更深的契约债。

## 决策
开 `openspec/changes/align-alarm-contract-fields`，走规范 §7 四件套（proposal/design/tasks/spec-delta）固化设计，再实现。

## 经验
1. **跨库字段对齐优先用 DTO 转换层**，不要改实体序列化直接对外。实体保留物理主键 `id`，业务 ID `alarm_id` 单独列，对外只露 `AlarmItem`。这样内部模型演进不影响契约。
2. **int→string 枚举映射放 assembler**，避免污染实体（实体仍存 int，便于 DB 紧凑 + 排序）。
3. **种子数据枚举必须前后端统一**：本次把后端 `type` 从 {FIRE,GAS,FLOOD,INTRUSION,TEMP} 收敛为前端 {FIRE,GAS,TEMP,CCTV,SOS}，否则前端运行时类型校验报错。
4. **dev 用 H2 全量重建**：迁移直接改 `CREATE TABLE`，不写 ALTER（dev 库每次启动重建）。生产迁移策略 Change 里未覆盖，需另议（Flyway 缺失，见 P3 债）。

## 待办
- 生产库在线迁移策略（Flyway 未引入）
- q-2：Random 演示桩（AlarmSimulator / DashboardService）清理为真实逻辑，依赖本 Change 的真实字段
