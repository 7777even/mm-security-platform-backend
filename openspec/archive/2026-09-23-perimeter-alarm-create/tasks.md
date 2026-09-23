# Tasks: perimeter-alarm-create（后端）

- [ ] 新增 V76 三方言迁移：登记权限码 security:perimeter-create 并授权 ADMIN+5 角色
- [ ] 新增 DTO PerimeterAlarmCreateRequest（title 必填，其余可选 + 默认值）
- [ ] SecurityService.createPerimeterAlarm（@RealtimeSync(domain=security.perimeter-alarm)，生成 alarmCode/默认状态，insert，返回 PerimeterAlarmDetail）
- [ ] SecurityController 新增 POST /security/perimeter-alarms（@RequireAuth(perm=security:perimeter-create)）
- [ ] check-api-contract.mjs --strict 通过（0 差异）+ check-endpoint-authz.mjs 通过（新端点带 perm）
- [ ] 隔离实例 curl 验证：创建成功 / 未鉴权 401 / 无权限 403
- [ ] mvn test 全量通过（含新增单测）
- [ ] 按 scope 拆分提交（backend: db / alarm / docs(openspec)）+ 推送
- [ ] 归档至 openspec/archive/（全勾后 git mv + 回填 specs/perimeter-alarm/spec.md + 卫生检查）
