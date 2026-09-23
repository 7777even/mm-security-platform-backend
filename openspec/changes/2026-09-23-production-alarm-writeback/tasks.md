# Tasks: production-alarm-writeback（后端）

- [x] 起草 proposal / design（L4：@Version 列 + 权限码种子 + DDL 迁移）
- [x] FacProductionAlarm 加 6 个处置字段（含 @Version 乐观锁列）
- [x] 新增 DTO ProductionAlarmUpdateRequest（全字段可选）
- [x] 实现 ProductionService.update（存在性 + 字典校验 + read-modify-write + @RealtimeSync）
- [x] 实现控制器端点 PUT /production/alarms/{id}（perm=production:ack）
- [x] ProductionAlarmItem 增 5 个回填字段（falseAlarm/handleResult/handleTime/dispatchPersonnel/notifyMethod）
- [x] 新增 V73（处置列）/ V74（production:ack 权限码种子）三方言迁移
- [x] KPI「未处置告警」聚合未处置 + 已确认
- [x] 跑 mvn compile（通过）+ check-api-contract.mjs --strict（0 漂移）+ check-endpoint-authz.mjs（写端点带 perm，不进 ALLOWLIST）
- [x] 隔离实例（:8899 新文件库）curl 验证写回落库：PUT 返回 code=0，re-GET 确认 status/falseAlarm/handleResult/dispatchPersonnel/notifyMethod 持久化；未知 id→404、非法枚举→100、无 token→401
- [ ] 按 scope 拆分双仓提交（backend: common/production，frontend: screen/production/shared）
- [ ] 归档至 openspec/archive/（全勾后按纪律归档，补 spec-delta 并回填 openspec/specs/）
