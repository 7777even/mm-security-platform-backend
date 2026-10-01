# Tasks: 业务写侧四域补齐修改与删除（后端）

- [x] BusinessWriteService：8 个 update/delete 方法 + `@RealtimeSync` 广播
- [x] EmergencyController：command-records / duty-sign-ins 的 PUT + DELETE
- [x] FireMonitoringController：patrol-executions 的 PUT + DELETE
- [x] TyphoonEmergencyController：dispatch-orders 的 PUT + DELETE
- [x] 补齐三个控制器缺失的 DeleteMapping / PutMapping / PathVariable import
- [x] `mvn compile` 通过
- [x] 契约守门 `check-api-contract.mjs --strict`：路由 209 / 差异 0 / schema 漂移 0
- [ ] 单测：BusinessWriteService 的 update/delete 用例
- [ ] 双仓提交推送并归档
