# Change: mgmt 全模块 CRUD（P0 批次）业务写侧四域补齐修改与删除

## 为什么

用户要求 mgmt 后台管理端全部模块实现 CRUD、实时更新、三端数据互通。P0 批次中
应急指令 / 值班签到 / 台风调度 / 巡更执行 四域此前**只有 POST 创建端点**，管理端无法
修改或删除历史记录，CRUD 不完整。本 Change 为这四域补齐 PUT / DELETE。

## 变更内容

`BusinessWriteService`（统一承载四域业务写）新增 8 个方法，全部带 `@RealtimeSync` 广播：

| 域 | 新增方法 | 广播域 |
| --- | --- | --- |
| 应急指令 | `updateCommandRecord` / `deleteCommandRecord` | emergency.command |
| 值班签到 | `updateDutySignIn` / `deleteDutySignIn` | emergency.duty |
| 台风调度 | `updateDispatchOrder` / `deleteDispatchOrder` | typhoon.dispatch |
| 巡更执行 | `updatePatrolExecution` / `deletePatrolExecution` | fire.patrol |

对应控制器新增端点（复用既有写权限码，无需新权限种子）：

- `PUT /api/v1/emergency/command-records/{id}`、`DELETE /api/v1/emergency/command-records/{id}`（emergency:command:write）
- `PUT /api/v1/emergency/duty-sign-ins/{id}`、`DELETE /api/v1/emergency/duty-sign-ins/{id}`（emergency:duty:write）
- `PUT /api/v1/typhoon/dispatch-orders/{id}`、`DELETE /api/v1/typhoon/dispatch-orders/{id}`（typhoon:dispatch:write）
- `PUT /api/v1/fire/patrol-executions/{id}`、`DELETE /api/v1/fire/patrol-executions/{id}`（fire-alarm:patrol:write）

## 设计要点

- 修改一律 **read-modify-write 局部更新**：仅覆盖传入的非空字段，兼容移动端/大屏只传部分字段的调用。
- 强枚举校验：`dispatchAction`(ASSIGN/CONFIRM/RELEASE)、`execResult`(NORMAL/ABNORMAL)、
  `signAction`(SIGN_IN/SIGN_OUT) 非法值返回 B3 `PARAM_INVALID`。
- 删除为**物理删除**（这四张表虽有 `deleted` 列但列表查询未过滤，逻辑删除会导致"删了还在"）。
- 写操作全部走 `SystemAuditHelper` 留痕；**零下行控制红线不变**——这些是业务留痕，不触发任何物理设备。

## 范围与非目标

- 非目标：不改 POST 创建语义、不改列表查询、不改既有权限码授权范围。
