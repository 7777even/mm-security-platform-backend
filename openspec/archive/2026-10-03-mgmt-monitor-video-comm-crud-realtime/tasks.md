# Tasks: 视频摄像头与通讯设备台账 CRUD 及实时广播

## 实现
- [x] V103 三方言迁移为 fac_video_camera / fac_comm_device 增加 version 乐观锁列
- [x] FacVideoCamera / FacCommDevice 实体补 @Version 字段
- [x] 新增 VideoCameraWriteRequest / CommDeviceWriteRequest 写请求 DTO
- [x] VideoService 的 createCamera / updateCamera / deleteCamera 标注 @RealtimeSync(video.camera)
- [x] CommDeviceService 的 createDevice / updateDevice / deleteDevice 标注 @RealtimeSync(communication.device)
- [x] VideoController / CommDeviceController 增加 POST / PUT / DELETE 写端点
- [x] 提交 scope 枚举新增 video 与 communication 域（AGENTS.md §6.5 与 commit-msg-lint 同步）

## 验证
- [x] mvn -o compile 通过
- [x] check-api-contract.mjs --strict：schema 漂移 0，本批 6 条写路由全部对齐（预存 12 条路由差异为技术债，非本批引入）
- [x] 双仓提交并按 scope 推送（后端 db / video / communication / openspec，前端 contract / mgmt / docs），关联本 Change 归档
