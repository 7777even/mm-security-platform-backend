# Design: 视频摄像头与通讯设备台账写能力与实时广播

## 机制
`@RealtimeSync` 是静态注解（`domain()` 为常量字符串），由 `RealtimeSyncAspect` 在标注方法成功返回后发布 `EntityChangedEvent`，`RealtimePublisher` 经 WebSocket 广播 `<domain>.changed`。两个域各自用独立常量通道，前端按同名域订阅。

## 落点
- `VideoService.createCamera / updateCamera / deleteCamera` 各标注 `@RealtimeSync(domain = "video.camera")`。
- `CommDeviceService.createDevice / updateDevice / deleteDevice` 各标注 `@RealtimeSync(domain = "communication.device")`。
- 广播发生在事务提交后（Aspect 在方法返回时触发），保证订阅端重拉读到的是已提交数据。

## 主键与并发
两张表的种子迁移曾显式插入 id，三方言自增序列未同步推进，直接依赖自增会从 id=1 撞主键。故一律用 `LedgerIdSupport.nextId / nextSortNo` 取 `max+1`，三方言行为一致、零迁移；并给实体补 `@Version`，由 V103 加 `version BIGINT DEFAULT 0` 列，供后续写 UI 的乐观锁使用。

## 自然键
通讯设备以 `deviceCode`（如 BC-001）为业务自然键，PUT / DELETE 走 `/communication/devices/{deviceCode}`，与既有详情端点一致；摄像头则以数值 id 定位。

## 鉴权
写端点 `@RequireAuth(role = "ADMIN")`，与相邻 `video.linkage` 写端点一致；广播本身不含下行控制，仅通知已订阅的同源视图重拉（重拉仍受读端点仅登录态约束）。
