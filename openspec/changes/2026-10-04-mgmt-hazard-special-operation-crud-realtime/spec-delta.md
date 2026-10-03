# Spec Delta: realtime-broadcast 与 hazard / special-operation 契约

## 新增广播域
- `hazard`：`HazardService` 的 createHazard / updateHazard / deleteHazard 在事务提交成功后广播 `hazard.changed`。
- `hazard.point`：`HazardService` 的 createPoint / updatePoint / deletePoint 在事务提交成功后广播 `hazard.point.changed`。
- `special-operation`：`SpecialOperationService` 的 createTicket / updateTicket / deleteTicket 在事务提交成功后广播 `special-operation.changed`。

## 新增 HTTP 端点
- POST `/hazards`、PUT `/hazards/{id}`、DELETE `/hazards/{id}`（ADMIN）。
- POST `/monitoring/points`、PUT `/monitoring/points/{id}`、DELETE `/monitoring/points/{id}`（ADMIN）；POST 遇主键重复返回 409。
- POST `/special-operations`、PUT `/special-operations/{id}`、DELETE `/special-operations/{id}`（ADMIN）。
- 三者均标注 `@RequireAuth(role = "ADMIN")`，满足 `check-endpoint-authz` 的角色约束要求（无需登记豁免）。

## 新增 schema
- `MajorHazardWriteRequest`（10 字段：name / level / rValue / monitorCount / videoCount / enterprise / category / code / longitude / latitude）。
- `MonitoringPointWriteRequest`（8 字段：id / name / category / status / lastTime / org / longitude / latitude）。
- `SpecialOperationWriteRequest`（26 字段，含 opType / ticketArea / opLevel / ticketStatus / workUnit / workLocation / permitNo 等）。
- `hazard.openapi.json` 与 `special-operation.openapi.json` 各自新增本域本地 `DeleteResult`（各域契约各自本地定义，不可跨域共享引用）。

## 变更
- `fac_major_hazard` / `fac_monitoring_point` / `fac_special_operation_ticket` 三表新增 `version BIGINT DEFAULT 0` 列（V105，三方言一致），实体相应补 `@Version`。

## 修正
- `HazardService.applyPointFields` 不再以请求体 id 覆盖实体主键；PUT 子路径的主键改由路径变量决定。

## 不变
- 读端点响应 schema 不变：`MajorHazardItem` / `MajorHazardDetail` / `MonitoringPoint` / `MonitoringAlarm` / `FacilityDetailInfo` / `SpecialOperationPage` / `SpecialOperationDetail`。
- 重大危险源的 7 类 JSON 明细列与特殊作业票的三张子表（视频 / 气体 / 人员）仍不经写端点编辑。
- 既有广播域不受影响。
