# Design: 应急事件新增落库

## 为什么写两张表（同一事务）
- `fac_emergency_event`：大屏事件列表的数据源（按 `scene + group_code` 聚合分组）。
- `fac_accident_incident`：「去处置」的数据源 —— `AccidentRescueService.incident(eventId)` 先按
  `event_id` 命中，未命中回退 `is_default=TRUE` 的默认事件。

仅写前者会让处置页回退默认事件（即「总跳到乙烯裂解装置区火灾」）。因此 `create` 必须在**同一事务内**
写两张表，后者 `is_default=false`、`event_id` 指向新建事件。两表非空列均已覆盖（`map_status="pending"`、
`status_name="未处置"`、`facility_name=areaCode` 等）。

## 分组与 kind 归一化
- 分组 id 固定 `manual-*` 前缀：`manual-event`（事件）/ `manual-weather`（极端天气）/ `manual-drill`（演练），
  与前端手动新增分组 id 对齐；`group_label` 固定「手动新增」/「极端天气」/「手动新增演练」。
- `kind` 落库统一大写（`EVENT`/`DRILL`），`toItem` 返回时归一化小写（契约 `EmergencyEventKind` 为 `event` | `drill`）。

## 不可映射字段
表单中的上报人 / 电话 / 伤亡数 / 事件类型细分无独立列，按既定决策由前端并入 `description`。

## 鉴权与包络
- 仅 `@RequireAuth`（登录态），不要求 ADMIN；与既有只读端点一致，401 / 校验失败均走 B3 统一包络。
- 参数校验失败由 `GlobalExceptionHandler` 收敛为 `code!=0` 包络（HTTP 仍 200，符合 B3 约定）。

## 返回值
返回 `EmergencyEventItem`（含后端生成的真实 `id`），供前端「去处置」直接按 `event_id` 跳转与定位。
