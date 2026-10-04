# Spec Delta: fire-facility（后端实现增量）

## ADDED Requirements

### fire-facility.alarm-handle

后端 **应（SHALL）** 提供按报警 id 处置消防设施报警的写端点：

- **SHALL** 暴露 `PUT /api/v1/fire-facility/alarms/{alarmId}`，`alarmId` 为报警列表返回的
  `AL-<故障号数字部分>` 字符串。
- **SHALL** 以 `REPLACE(REPLACE(fault_code,'FLT-',''),'-','') = <数字串>` 反查底层故障，
  数字串仅保留数字字符；空数字串 → `PARAM_INVALID`，未命中 → `NOT_FOUND`。
- **SHALL** 复用故障处置语义：状态流转（`faultStatus`，取值受既有故障状态枚举约束）、
  派单/维修/验收字段**局部更新**（未传不更新）、可选追加故障时间线。
- **SHALL** 要求权限码 `fire-facility:handle`（V68 已登记并授权 ADMIN 及岗位角色）。
- **SHALL** 成功后返回更新后的 `FireFacilityFaultItem`（B3 包络，`code=0`），并触发
  `fire-facility.fault` 实时广播，使已订阅的报警派生列表自动刷新。

### fire-facility.fault-write-refactor（内部，非对外契约）

- **SHALL** 将故障写回核心逻辑收敛为 `doUpdateFault`，由 `updateFault` 与 `updateAlarm` 共用，
  保证两入口行为一致且各自触发一次实时广播（Spring 代理型切面不拦截自调用，故不得在同类内互调标注方法）。
