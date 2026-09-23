# Tasks: perimeter-alarm-writeback（后端）

- [x] 起草 proposal / design（L4：@Version 列 + 权限码种子 + DDL 迁移）
- [x] FacPerimeterAlarm 加 @Version 乐观锁列
- [x] 新增 DTO PerimeterAlarmUpdateRequest（全字段可选）
- [x] 实现 SecurityService.updatePerimeterAlarm（存在性 + 字典校验 + read-modify-write + @RealtimeSync）
- [x] 实现控制器端点 PUT /security/perimeter-alarms/{id}（perm=security:perimeter-ack）
- [x] 新增 V69（version 列）/ V70（security:perimeter-ack 权限码种子）三方言迁移
- [x] 补充 SecurityPerimeterAlarmWriteBackTest（纯 Mockito 6 例）并跑通
- [ ] 跑 mvn test 全量回归（确认 V69/V70 迁移在 H2 文件库下无破坏）
- [ ] 跑 check-api-contract.mjs --strict 与 check-endpoint-authz.mjs（写端点带 perm，不应进 ALLOWLIST）
- [ ] 同步前端契约 + gen:api-types + 按 scope 拆分双仓提交
