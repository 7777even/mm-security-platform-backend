# Tasks: 第 1/2 批写端点单测补齐与自然键语义修复

## 实现
- [x] 新建 CommRecordServiceTest（此前该域无测试文件），补 8 例含 list 映射与 CRUD
- [x] VideoServiceTest 补 6 例 createCamera / updateCamera / deleteCamera 全路径
- [x] CommDeviceServiceTest 补 7 例（原 3 例）
- [x] DeviceServiceTest 补 7 例（原 3 例），含软删除口径校验
- [x] 修 CommDeviceService.applyDeviceFields 覆写 deviceCode：改由 createDevice 单独设置
- [x] 修 CommRecordService.applyRecordFields 覆写 recordNo：改由 createRecord 单独设置
- [x] 两处修复各加 …DoesNotOverridePathKey 回归用例

## 验证
- [x] 四域定向测试全绿（22 / 10 / 10 / 8 例）
- [x] 后端全量 mvn -o test 绿、jacoco 达 0.80 门槛
- [x] check-endpoint-authz：写端点全部具备角色/权限约束
- [x] check-api-contract.mjs --strict：schema 漂移 0，路由差异维持基线 12
- [ ] 按 scope 拆分提交推送（communication / device / video / openspec），关联本 Change 归档
