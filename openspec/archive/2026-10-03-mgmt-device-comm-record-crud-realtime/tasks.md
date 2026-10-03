# Tasks: 设备台账与通讯通知记录 CRUD 及实时广播

## 实现
- [x] V104 三方言迁移为 fac_device / fac_comm_record 增加 version 乐观锁列
- [x] FacDevice / FacCommRecord 实体补 @Version 字段
- [x] 新增 DeviceWriteRequest（7 字段）与 CommRecordWriteRequest（12 字段）写请求 DTO
- [x] DeviceService 的 createDevice / updateDevice / deleteDevice 标注 @RealtimeSync(device)
- [x] CommRecordService 的 createRecord / updateRecord / deleteRecord 标注 @RealtimeSync(communication.record)
- [x] DeviceController 增加 POST /devices 与 PUT、DELETE /devices/{code}
- [x] CommRecordController 增加 POST /communication/records 与 PUT、DELETE /communication/records/{recordNo}
- [x] device.openapi.json 补写端点与 DeviceWriteRequest、本地 DeleteResult schema（四同步）
- [x] communication.openapi.json 补写端点与 CommRecordWriteRequest schema（四同步）

## 验证
- [x] mvn -o compile 通过
- [x] check-api-contract.mjs --strict：schema 漂移 0，本批 6 条写路由全部对齐（路由差异维持基线 12）
- [x] 双仓提交并按 scope 推送（后端 db / device / communication / openspec，前端 contract / mgmt / docs），关联本 Change 归档
