# QA · 告警域 mock 清理（2026-09-07）

## 范围
清理 `AlarmController` / `AlarmService` 的 `devFallbackList` 假数据回落分支（与 Device 域同构），改为强类型 `AlarmPageResult` DTO + `data.sql` 种子数据。属 `harden-backend-baseline` 之后第二轮技术债清理。

## 改动
- `AlarmController`：移除 `p.getTotal()==0` 时返回随机列表 + `mock:true` 的回落分支，返回 `Result<AlarmPageResult>`
- `AlarmService`：删除 `devFallbackList` / `randomCode` / `Random`
- 新增 `AlarmPageResult`（`list/total/page/size`，与前端 `_shared.json#/PageResult` 同构，元素为 `FacAlarm`）
- `data.sql`：补 8 条告警种子（替代演示数据；`status` 用后端 int 语义）

## 验证
- `mvn test`：**23 case 全绿**（新增 `AlarmControllerTest` 2 + `AlarmServiceTest` 3）
- `scripts/check-api-contract.mjs`：实现有/契约无 = 0，无新增漂移

## 残余债（已识别，待定范围）
1. **告警实体-契约字段失配**：前端 `AlarmItem` 用 `alarmId`(string) / `status`(string 枚举 ACTIVE/ACKED/DISPATCHED/CLOSED) / `ts` / `location` / `category` / `warned` / `planId`；后端 `FacAlarm` 用 `id`(Long) / `status`(int) / `occurred_at` / `content`，且**缺** `location` / `category` / `warned` / `planId`。需单独开 Change 对齐（字段重命名 + 加列 + 枚举映射）。本次仅清 mock，未动字段语义。
2. **401/403 口径**：`GlobalExceptionHandler.handleBusiness` 一律 `HTTP 200 + 业务 code`；前端契约声明 `401` / `403`。需确认改 HTTP 状态码还是改契约声明。
3. **残余 Random 演示桩（非本次分页 mock 范围）**：
   - `websocket/AlarmSimulator.java`：dev 每 12s 推模拟告警（实时推送桩，`realtime.openapi.json` 已对齐）
   - `service/DashboardService.java`：Dashboard 概览 mock 汇总
   属"演示/实时模拟"性质，生产应去 Random 或接真实聚合，待定范围。

## 决策点
- 401/403 口径方向（改后端 HTTP 状态码 / 改前端契约声明 / 暂不动）
- 告警字段失配是否开 Change 对齐
- 残余 Random 桩是否清理
