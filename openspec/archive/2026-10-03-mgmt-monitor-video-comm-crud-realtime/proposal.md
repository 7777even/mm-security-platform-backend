# Proposal: 视频摄像头与通讯设备台账 CRUD 及实时广播

## 背景
监测 / 通信域下 10 个后台视图（视频监控管理、视频健康度、广播 / 电话 / 无线对讲设备管理等）此前只有后端只读 GET 端点：既无法在管理端维护台账，写方法也无从标注 `@RealtimeSync`，三端同源实时刷新链路在本域完全缺失。第 1 批先补覆盖面最大的两个域：`video.camera`（2 个视图）与 `communication.device`（3 个视图）。

## 目标
- 为 `fac_video_camera` 与 `fac_comm_device` 增加 POST / PUT / DELETE 写端点；主键与排序号经 `LedgerIdSupport` 显式分配，实体补 `@Version` 乐观锁并由 V103 三方言迁移加列。
- 写方法标注 `@RealtimeSync`，分别广播 `video.camera.changed` 与 `communication.device.changed`。
- 契约四同步：`video.openapi.json` / `communication.openapi.json` 补写端点与写请求 schema。

## 非目标
- 不新增权限码与 `sys_menu` 按钮种子：第 1 批前端仅订阅刷新、无写 UI，写端点沿用相邻 `video.linkage` 的 `@RequireAuth(role = "ADMIN")`；待前端落写弹窗时再按域补权限种子。
- 不动其余 5 个只读域（hazard.point / communication.record / special-operation / device / hazard），留待后续批次。
