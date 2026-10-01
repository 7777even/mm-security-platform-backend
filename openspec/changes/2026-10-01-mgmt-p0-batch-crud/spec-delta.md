# Spec Delta: business-write（应急指令 / 值班签到 / 台风调度 / 巡更执行）

## 新增 capability 要求

- 四域资源均须提供 `PUT /{资源}/{id}` 与 `DELETE /{资源}/{id}`。
- 修改须为局部更新：空值字段表示"不更新"，不得清空未传字段。
- 删除为物理删除；记录不存在返回 B3 `NOT_FOUND`。
- 强枚举字段须校验：`dispatchAction`∈{ASSIGN,CONFIRM,RELEASE}、
  `execResult`∈{NORMAL,ABNORMAL}、`signAction`∈{SIGN_IN,SIGN_OUT}，非法返回 B3 `PARAM_INVALID`。
- 八个写操作均须发布对应域的变更事件：emergency.command / emergency.duty /
  typhoon.dispatch / fire.patrol。
- 所有写操作须落审计（`SystemAuditHelper`），且不得触发任何物理设备下行。

## 变更的既有约束

- 无。既有 POST 创建与 GET 列表语义保持不变。
