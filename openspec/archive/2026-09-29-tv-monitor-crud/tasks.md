# Tasks: tv-monitor-crud（后端）

- [x] 新增 V87 三方言迁移：登记权限码 tv:monitor:create/update/delete 并授权 ADMIN+5 角色
- [x] 新增 DTO TvMonitorUpsertRequest（monitorCode 必填，其余可选）
- [x] TvService.createMonitor/updateMonitor/deleteMonitor（均 @RealtimeSync(domain=tv.monitor)，update 仅覆盖非空字段）
- [x] TvController 新增 POST /tv/monitors、PUT /tv/monitors/{code}、DELETE /tv/monitors/{code}（@RequireAuth）
- [x] mvn compile 通过
- [ ] check-api-contract.mjs --strict 通过（0 差异）+ check-endpoint-authz.mjs 通过（新端点带 perm）
- [ ] 隔离实例 curl 验证：创建/更新/删除成功 + 负例 401/403/重复编码
- [ ] 按 scope 拆分提交（backend: db / common / docs(openspec)）+ 推送
- [ ] 归档至 openspec/archive/（全勾后 git mv + 卫生检查）
