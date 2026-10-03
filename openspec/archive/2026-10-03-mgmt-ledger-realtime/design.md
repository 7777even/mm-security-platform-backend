# Design: 通用台账实时广播接入

## 机制
`@RealtimeSync` 是静态注解（`domain()` 为常量字符串），由 `RealtimeSyncAspect` 在标注方法成功返回后发布 `EntityChangedEvent`，`RealtimePublisher` 经 WebSocket 广播 `<domain>.changed`。通用台账 25 域共用单一 `{domain}` 路径变量，注解无法按运行时域动态化，故统一广播常量通道 `mgmt-ledger`；前端 `MgmtLedgerView` 订阅该常量通道，仅重拉自身路由域（跨域重拉无害）。

## 落点
- `createRow` / `updateRow` / `deleteRow` 三个 `@Transactional` 方法各加 `@RealtimeSync(domain = "mgmt-ledger")`。
- 广播发生在事务提交后（Aspect 在方法返回时触发），保证读到的是已提交数据。

## 鉴权
写端点已 `@RequireAuth(role = "ADMIN")`；广播本身不含下行控制，仅通知已订阅的同源视图重拉（重拉仍受读端点仅登录态约束）。
