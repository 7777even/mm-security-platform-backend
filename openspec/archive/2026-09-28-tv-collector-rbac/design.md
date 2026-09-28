# Design: 工业电视主动采集器 + 采集端点细粒度权限

## 采集器脚手架（对标 FireFacilityCollector）

- `TvCollectorProperties`：`@ConfigurationProperties(prefix="tv.collector")`，`enabled` 默认 `false`，
  `mode`/`cron`/`upstream`（url/token/timeout/insecureTls）。
- `TvSourceAdapter`：接口 `List<TvSnapshotIngestRequest> fetchSnapshots()`；`NoOpTvSourceAdapter`
  `@Component` 返回空列表（默认）。真实源就绪时新增 `@Primary` 实现对接视频平台 / NVR / 流媒体网关。
- `TvCollector`：`@ConditionalOnProperty(tv.collector.enabled=true)` + `@Scheduled(cron="tv.collector.cron")`；
  拉取 → 过滤缺 `monitorCode`/`imageBase64` 的无效项 → `tvService.submitSnapshots(valid)` 批量落库；
  全程容错（异常/空/落库失败均记日志跳过），绝不写假数据。

## 入库链路（TvService 复用）

- 抽出私有 `ingestOne(req)`（校验 + 解码 + 落库，不含缓存失效/广播）。
- `submitSnapshot(req)` `@RealtimeSync`：单条设备端点，逐次广播。
- 新增 `submitSnapshots(List)` `@RealtimeSync`：循环 `ingestOne`（单条非法仅告警跳过），写后单次缓存
  失效 + 单次广播，避免 N 条上报触发 N 次大屏刷新（采集器主用）。

## 细粒度 RBAC

- V80（h2/pg/dm）：`fm-tv` 下登记按钮 `video:snapshot:create`（`fm-tv-snapshot-create`），授权
  ADMIN/COMMANDER/SCHEDULER/TEAM_LEADER/INNER_OPER/OUTER_OPER（镜像 V79）。
- `TvController.submitSnapshot`：`@RequireAuth(perm="video:snapshot:create")`；清理 ALLOWLIST 豁免。
- 前端 `TvSnapshotFeedPanel`：「模拟设备抓拍」按钮加 `hasPerm('video:snapshot:create')` 门控，
  「确认」按钮加 `hasPerm('video:snapshot:ack')` 门控（大屏 `useScreenPermission`）。

## dev 占位图

- `TvSnapshotRenderer`(@Profile dev)：JDK BufferedImage 画带厂点/事件/时间/REC 角标的占位 JPEG。
- `TvSnapshotSeeder`(@Profile dev, @Order 101)：启动若 `fac_tv_snapshot` 为空，取若干 `fac_tv_monitor`
  生成样例截图行（带占位图），幂等（已有数据则跳过）。
