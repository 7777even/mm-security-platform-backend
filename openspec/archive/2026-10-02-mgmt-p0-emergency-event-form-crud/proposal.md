# Change: 应急事件与流程填报补齐编辑/删除写端点（P0 收尾）

## 为什么

mgmt 全模块 CRUD 盘点（见 `docs/superpowers/plans/2026-10-01-mgmt-all-modules-crud-realtime.md`）
把「应急事件」与「表单记录」列为 P0 收尾项：

- **应急事件**：后端只有 `POST /emergency-events`（新增）、`POST /{id}/report`（报送）、
  `POST /{id}/start-response`（启动响应），**没有编辑与删除**；且三个写端点此前**不广播任何域**
  ——大屏改了状态，管理端列表只能手动刷新，三端不同源。
- **流程填报**：`POST` 新增 / `PUT` 审核已有，**缺 DELETE**，且写操作同样无广播域。

## 变更内容

| 资源 | 新增端点 | 权限口径 | 广播域 |
| --- | --- | --- | --- |
| 应急事件 | `PUT /api/v1/emergency-events/{id}` | `emergency:event:write`（V92 登记） | emergency.event |
| 应急事件 | `DELETE /api/v1/emergency-events/{id}` | `emergency:event:write` | emergency.event |
| 流程填报 | `DELETE /api/v1/form-records/{id}` | ADMIN 角色（与既有 PUT 同口径） | form.record |

同时为**既有**写端点补广播：`EmergencyEventService.create/report/startResponse` 与
`FormRecordService.create/update` 统一标记 `@RealtimeSync`，使三端在任一写入路径上都能实时跟随。

新增 Flyway **V92**（h2 / dameng / postgresql 三方言同名同序）：在 `fm-emergency` 下登记按钮级
菜单 `emergency:event:write`，授权 ADMIN / COMMANDER / SCHEDULER。

## 设计要点

- **编辑 = 局部更新**（read-modify-write）：字段为 `null` 表示不修改，兼容只传部分字段的调用方。
- **status 强枚举校验**：`pending | processing | done`，非法值走 B3 `PARAM_INVALID`；
  只传 `status` 时按枚举推导中文标签（未处置 / 处置中 / 已处置），两者都传以传入为准。
- **双源同步**：编辑事件时同事务同步 `fac_accident_incident` 与 `fac_accident_detail_field`
  中「事故时间 / 事发地点 / 事件描述 / 事件名称 / 事件级别 / 涉事区域」六项，
  避免「事件改了、救援详情还是旧值」的双源漂移；按 label 命中才更新，**不新增行**
  （新增会改变大屏面板行序）。
- **删除顺序不可颠倒**：先清 `fac_accident_detail_field` → 再删 `fac_accident_incident`
  → 最后删 `fac_emergency_event`，避免留下引用已删除事件的孤儿行。
  两表均无 `deleted` 列，故为**物理删除**；重复删除返回 `NOT_FOUND` 而非静默成功。
- **权限口径分层**（关键取舍）：`create / report / start-response` 保持「仅登录态」——
  大屏值守岗自助上报若被权限码挡住会直接打断既有业务；只有 `update / delete`
  这类台账维护动作才收 `emergency:event:write`。

## 范围与非目标

- 非目标：不改事件分组维度（scene / kind / eventCategory / groupCode 不进更新请求，
  避免把事件挪组后产生孤儿分组）；不改列表查询；不改既有权限码授权范围。
- 零下行红线不变：本 Change 全部是业务台账留痕，不触发任何物理设备。
