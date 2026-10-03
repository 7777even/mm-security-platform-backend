# Design: 设备台账与通讯通知记录写能力与实时广播

## 机制
`@RealtimeSync` 是静态注解（`domain()` 为常量字符串），由 `RealtimeSyncAspect` 在标注方法成功返回后发布 `EntityChangedEvent`，`RealtimePublisher` 经 WebSocket 广播 `<domain>.changed`。两域各用独立常量通道，前端同名订阅。

## 落点
- `DeviceService.createDevice / updateDevice / deleteDevice` 各标注 `@RealtimeSync(domain = "device")`。
- `CommRecordService.createRecord / updateRecord / deleteRecord` 各标注 `@RealtimeSync(domain = "communication.record")`。
- 广播发生在事务提交后（Aspect 在方法返回时触发），保证订阅端重拉读到已提交数据。

## 主键策略：两域不同，勿套用同一套
- **`fac_device`**：物理主键是 20 位 MDM `deviceCode`，实体 `@TableId(type = IdType.INPUT)`——由**客户端随请求给定**，既不走自增也不需要 `LedgerIdSupport`。重复编码在写方法内先查一次，命中则抛 `ResultCode.CONFLICT`(409)。
- **`fac_comm_record`**：`id` 为 `IdType.AUTO` 的自增 `Long`，而 V57 种子数据显式插过 id、序列未同步推进，故**必须**用 `LedgerIdSupport.nextId` 取 `max(id)+1`；对外以 `recordNo` 为业务自然键定位（与列表字段一致）。

## 删除语义：device 走软删除
读端点 `DeviceService.page()` 恒定带 `deleted = 0` 过滤，故 `deleteDevice` 置 `deleted = 1` 而非物理删除，与读口径一致、保留台账痕迹；`findActiveByCode` 只认未软删的行，已软删的编码可被重新建档。通讯通知记录无软删列，走物理删除。

## 时间字段
`FacDevice.createdAt / updatedAt` 由服务端维护（新建同值、更新刷新 updatedAt），不出现在写请求中——避免客户端伪造时间戳。

## 鉴权
写端点 `@RequireAuth(role = "ADMIN")`（`DeviceController` 类级 `@RequireAuth` 为登录态，方法级 ADMIN 覆盖之）；广播本身不含下行控制，仅通知已订阅的同源视图重拉。
