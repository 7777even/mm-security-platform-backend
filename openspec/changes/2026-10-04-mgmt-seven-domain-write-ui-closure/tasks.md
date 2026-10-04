# Tasks: 七域写端点权限码落地与前端写 UI 闭合

## 后端
- [x] V106 三方言迁移登记 7 个按钮级权限码并播种给 6 类角色
- [x] VideoController 摄像头 CRUD 改 @RequireAuth(perm = "video:camera-write")
- [x] CommDeviceController 改 @RequireAuth(perm = "communication:device-write")
- [x] CommRecordController 改 @RequireAuth(perm = "communication:record-write")
- [x] DeviceController 改 @RequireAuth(perm = "device:write")
- [x] HazardController 分两域：hazards → hazard:write、monitoring/points → hazard:point-write
- [x] SpecialOperationController 改 @RequireAuth(perm = "special-operation:write")
- [x] 不动 VideoController 的 video.linkage 三个既有端点（仍为 role=ADMIN，不在本批范围）

## 前端
- [x] video.ts / communication.ts / device.ts / hazard.ts / specialOperation.ts 补齐 create / update / delete
- [x] 9 个视图补 MgmtRecordEditDialog、新增按钮、操作列与 v-permission
- [x] 定位键字段（deviceCode / recordNo / 点位 id）编辑态 disabledOnEdit
- [x] 读逻辑、筛选、分页、tab 与订阅语句保持原样

## 验证
- [x] DbLayerIntegrationIT 6 例通过（V106 迁移在 H2 Flyway 链上正常）
- [x] check-endpoint-authz：写端点全部具备角色/权限约束
- [x] check-api-contract.mjs --strict：schema 漂移 0，路由差异维持基线 12
- [x] 前端 vue-tsc --noEmit EXIT=0、eslint 0 error
- [ ] 后端全量 mvn -o test 绿、jacoco 达标
- [ ] 前端 build:subapps 12 子应用全绿
- [ ] 双仓按 scope 拆分提交推送并归档本 Change
