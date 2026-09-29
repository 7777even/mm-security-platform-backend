# Tasks: production-tv-snapshot-linkage（后端）

- [x] TvService.autoRelateSnapshotsForAlarm（时间窗 ±15min + 位置关键词双向包含，仅关联 alarm_id IS NULL 候选，命中失效 snapshotListCache）
- [x] ProductionController.alarmSnapshots 显式关联为空时触发兜底再查
- [x] TvSnapshotSeeder 给前 2 条生产告警各绑定 1 张样例抓拍（dev 环境）
- [x] mvn compile 通过
- [ ] 隔离实例 curl 验证：告警详情抓拍区块返回样例关联；负例返回空
- [ ] 按 scope 拆分提交（backend: common / docs(openspec)）+ 推送
- [ ] 归档至 openspec/archive/（全勾后 git mv + 卫生检查）
