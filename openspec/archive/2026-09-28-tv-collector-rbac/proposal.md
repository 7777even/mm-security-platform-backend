# Proposal: 工业电视主动采集器 + 采集端点细粒度权限

## 背景

9/24 待办要求「工业电视域打通设备采集与录像截图入库闭环链路」与「补齐 RBAC 细粒度权限」。
核查现状：录像截图**自助上报**闭环（POST /tv/snapshots → 落库 → @RealtimeSync 广播）与
确认权限 `video:snapshot:ack`（V79）已就绪，但仍有两块缺口：

1. **设备采集缺少「后端主动拉取」链路** —— 现有仅设备自助上报，无对标消防
   `FireFacilityCollector` 的主动采集器脚手架（默认关 + NoOp 适配器）。
2. **采集端点仅登录态** —— `submitSnapshot` 长期挂在 `check-endpoint-authz.mjs` 的
   ALLOWLIST 豁免里（注释本身建议引入 `video:snapshot:create`），不符合细粒度 RBAC。

## 方案

- 新增 `TvCollector`（`@ConditionalOnProperty(tv.collector.enabled=false)` + `@Scheduled`）+
  `TvSourceAdapter` 接口 + `NoOpTvSourceAdapter` + `TvCollectorProperties`，复用既有入库/广播链路。
- 新增 V80 迁移登记 `video:snapshot:create` 按钮级权限码（授权 ADMIN + 5 岗位角色，镜像 V79）。
- `TvController.submitSnapshot` 由 `@RequireAuth` 收紧为 `@RequireAuth(perm="video:snapshot:create")`，
  并清理 ALLOWLIST 豁免。
- dev 体验：新增 `TvSnapshotRenderer`(@Profile dev) + `TvSnapshotSeeder`(@Profile dev)，为无真实设备时
  生成样例截图，使「录像截图采集」面板开箱有数据。

## 非目标

- 不实现真实工业电视源对接（仅留 `TvSourceAdapter` 扩展点）。
- 不改 `fac_tv_snapshot` 表结构（V78 已满足）。
