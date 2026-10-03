# Spec Delta: 四域写端点语义

## 变更（行为）
- PUT `/communication/devices/{deviceCode}`：不再以请求体 `deviceCode` 覆写实体编码，路径变量为权威。
- PUT `/communication/records/{recordNo}`：不再以请求体 `recordNo` 覆写记录编号，路径变量为权威。
- 两者与既有 `PUT /devices/{deviceCode}`（`DeviceService` 本就不覆写）及第 3 批修好的
  `PUT /monitoring/points/{id}` 统一为同一语义：填充方法只写业务列，创建分支单独设置定位键。

## 不变
- HTTP 路径、请求体字段、响应 schema 全部不变（`MajorHazardWriteRequest` 等写请求 DTO 不改）。
- 读端点行为与返回结构不变。
- `@RealtimeSync` 广播域不变：`video.camera` / `communication.device` / `device` / `communication.record`。
- `@RequireAuth(role = "ADMIN")` 授权口径不变。
