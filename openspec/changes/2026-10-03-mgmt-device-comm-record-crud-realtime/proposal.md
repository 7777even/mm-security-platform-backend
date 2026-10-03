# Proposal: 设备台账与通讯通知记录 CRUD 及实时广播

## 背景
第 1 批已为 `video.camera` 与 `communication.device` 补齐写端点与实时广播。监测 / 通信域仍有 5 个只读域无写能力、因而也无广播源。第 2 批取其中两个「scope 已存在、无需再扩枚举」的域先行：`device`（装置/设备台账，`DeviceView`）与 `communication.record`（短信 / 电话 / 广播 / APP推送 / 对讲五类通知记录，`CommRecordView` 五页共用）。

## 目标
- `device`：`/api/v1/devices` 增加 POST，`/api/v1/devices/{code}` 增加 PUT / DELETE（软删除），广播 `device.changed`。
- `communication.record`：`/api/v1/communication/records` 增加 POST，`/api/v1/communication/records/{recordNo}` 增加 PUT / DELETE，广播 `communication.record.changed`。
- 两表（`fac_device` / `fac_comm_record`）由 V104 三方言迁移补 `version` 乐观锁列，实体补 `@Version`。
- 契约四同步：`device.openapi.json` / `communication.openapi.json` 补写端点与写请求 schema。

## 非目标
- 不新增权限码与 `sys_menu` 按钮种子：本批前端仍只订阅刷新、无写 UI，写端点沿用 `@RequireAuth(role = "ADMIN")`。
- 不动剩余 3 个只读域（hazard / hazard.point / special-operation）——其后端 scope 尚未收录，需先扩枚举，留待第 3 批。
