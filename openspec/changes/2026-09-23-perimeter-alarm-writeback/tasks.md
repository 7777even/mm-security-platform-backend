# Tasks: perimeter-alarm-writeback（后端）

- [x] 起草 proposal / design（L4：@Version 列 + 权限码种子 + DDL 迁移）
- [x] FacPerimeterAlarm 加 @Version 乐观锁列
- [x] 新增 DTO PerimeterAlarmUpdateRequest（全字段可选）
- [x] 实现 SecurityService.updatePerimeterAlarm（存在性 + 字典校验 + read-modify-write + @RealtimeSync）
- [x] 实现控制器端点 PUT /security/perimeter-alarms/{id}（perm=security:perimeter-ack）
- [x] 新增 V69（version 列）/ V70（security:perimeter-ack 权限码种子）三方言迁移
- [x] 补充 SecurityPerimeterAlarmWriteBackTest（纯 Mockito 6 例）并跑通
- [x] 跑 mvn test 全量回归（40/40 类绿，含 V69/V70 迁移在 H2 文件库下无破坏）
- [x] 跑 check-api-contract.mjs --strict（0 漂移）与 check-endpoint-authz.mjs（写端点带 perm，不进 ALLOWLIST；仅 1 处预存 FormRecordController 无关）
- [x] 同步前端契约 + gen:api-types + 按 scope 拆分双仓提交
- [ ] 归档至 openspec/archive/（全勾后按纪律归档，需补 spec-delta 并回填 openspec/specs/）
