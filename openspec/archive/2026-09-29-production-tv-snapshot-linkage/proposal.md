# Proposal: production-tv-snapshot-linkage（生产告警 ↔ 工业电视抓拍数据级联通）

## 背景
生产告警详情面板（`AlarmDetailPanel`）需要展示「现场工业电视抓拍」区块。此前仅有按 `location` 软匹配的取数（`fetchProductionAlarmSnapshots` → `GET /production/alarms/{id}/snapshots`，后端 `TvService.listSnapshots(alarmId=..., alarmType='PRODUCTION')`），但生产告警**不会自动关联到抓拍**——一旦该告警没有显式 `alarm_id` 绑定，区块恒为空，跨域联动形同虚设。

本变更实现**数据级关联兜底**：当 `GET /production/alarms/{id}/snapshots` 查不到显式关联抓拍时，后端按「时间窗 ±15 分钟 + 位置关键词包含」自动把符合条件的 `fac_tv_snapshot` 反写 `alarm_id/alarm_type`，再查一次返回；并提供种子数据，使 dev 环境前 2 条生产告警各绑定 1 张样例抓拍。

## 分级：L3
- 复用既有表 `fac_tv_snapshot`，仅新增关联逻辑与种子，`fac_production_alarm` 不反向加字段（关联维度留在抓拍侧，符合「抓拍是关联事实真源」的现有口径）。
- 属 L3（新增业务查询链路 + 后台种子），无新权限码（沿用既有读/采集权限）。

## 范围
1. 后端：`TvService.autoRelateSnapshotsForAlarm(Long alarmId, String alarmType, String location, String occurredAt)`（时间窗 ±15min + 位置关键词匹配，反写 alarm_id/alarm_type，命中即失效 `snapshotListCache`）；`ProductionController.alarmSnapshots` 显式关联为空时触发兜底；`TvSnapshotSeeder` 给前 2 条生产告警各生成 1 张绑定样例抓拍。
2. 前端（既有，`fetchProductionAlarmSnapshots` 已在 `services/tv.ts`）：本变更无前端契约新增，仅依赖后端关联结果；详情区块渲染空态（无关联时不编造）。

## 人工确认关卡
- [x] **匹配口径**：时间窗 ±15 分钟（设备上报时刻 vs 告警发生时刻 `occurredAt`），位置关键词双向包含（`location` 与 `monitorName + zoneName` 任一方向包含即命中），避免误关联。
- [x] **幂等**：仅对 `alarm_id IS NULL` 的候选抓拍做关联；已关联的不再重复处理。
- [x] **零下行/零造假**：关联失败或窗口内无候选抓拍时返回空列表，前端渲染空态，绝不编造关联。
- [ ] 隔离实例 curl 验证：生产告警详情抓拍区块返回样例关联；负例（窗口外/位置不符）返回空。
- [ ] 按 scope 拆分提交（backend: common / docs(openspec)）+ 推送。

## 不在范围
- 不反向给 `fac_production_alarm` 加抓拍/坐标字段。
- 不接入真实录像流（抓拍快照时间轴即满足「历史回放」诉求，本期不做）。
