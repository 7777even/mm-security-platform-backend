# Proposal: perimeter-alarm-create（周界入侵告警手工创建）

## 背景
当前 `fac_perimeter_alarm`（前端「治安报警」）只有查询与处置写回能力：`GET /security/perimeter-alarms/latest`、`GET /{id}`、`GET /{id}/snapshot`、`PUT /{id}`。数据全部来自 DB 种子，操作员**无法在前端手动新增一条周界入侵告警**。

本变更实现**路线 B：操作员在前端点按钮创建**——新增 `POST /security/perimeter-alarms` 写端点 + 前端「新增治安报警」按钮与表单，落库后实时广播，面板自动刷新。

## 分级：L3
- 复用既有表 `fac_perimeter_alarm`，仅新增业务写端点，不改表结构、不引入新实体。
- 属 L3（新增业务读写端点 + 带权限码），**不需要改权限模型**，按 L3 纪律走 openspec 四同步即可。

## 范围
1. 后端：新增权限码 `security:perimeter-create`（三方言种子，授权 ADMIN + 5 岗位角色）；新增 `POST /security/perimeter-alarms` 写端点；`PerimeterAlarmCreateRequest` DTO；`SecurityService.createPerimeterAlarm`。
2. 前端：扩展 `docs/api/security.openapi.json`；生成类型；`services/security.ts` 加 `createPerimeterAlarm`；`SecurityStatusPanel.vue` 加「新增治安报警」按钮 + 表单 Dialog。
3. 契约四同步 + 双仓守门 + 按 scope 提交。

## 人工确认关卡（已与用户对齐）
- [x] **权限策略**：新增独立权限码 `security:perimeter-create`，三方言种子登记并授权 `ADMIN/COMMANDER/SCHEDULER/TEAM_LEADER/INNER_OPER/OUTER_OPER`（照抄 V70 的 `security:perimeter-ack` 范式）。录入与处置权限解耦。
- [x] **表单字段（最小可用）**：告警标题、类型（默认「周界入侵告警」）、等级、位置、发生时间（默认当前时刻、可改）、说明、入侵对象名（可选）。**现场抓拍图留空**（人工录入无设备抓拍）。
- [x] **alarmCode 生成**：后端生成 `PA-yyyyMMdd-HHmmss`（精确到秒，演示环境唯一性足够）。
- [x] **默认状态**：新建记录 `status='未确认'`、`falseAlarm='未核实'`、`source='人工录入'`、`version=0`。
- [x] **实时广播**：`createPerimeterAlarm` 标注 `@RealtimeSync(domain = "security.perimeter-alarm")`，创建后前端 `SecurityStatusPanel`（已订阅该 domain）自动重拉最新告警。
- [ ] 端到端冒烟验证（隔离实例 curl 跑通创建 + 负例 401/403）。

## 不在范围
- 不上传现场抓拍图片（multipart）；不接入设备触发链路。
- 不新增 `fac_perimeter_alarm` 表结构变更（V 文件已应用到 dev 文件库，禁止回改）。
