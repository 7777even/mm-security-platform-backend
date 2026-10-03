# Spec Delta: realtime-broadcast（video.camera / communication.device 域）

## 新增广播域
- `video.camera`：由 `VideoService` 的 createCamera / updateCamera / deleteCamera 在事务提交成功后广播 `video.camera.changed`。
- `communication.device`：由 `CommDeviceService` 的 createDevice / updateDevice / deleteDevice 在事务提交成功后广播 `communication.device.changed`。

## 新增 HTTP 端点
- POST `/video/cameras`、PUT `/video/cameras/{id}`、DELETE `/video/cameras/{id}`（ADMIN）。
- POST `/communication/devices`、PUT `/communication/devices/{id}`、DELETE `/communication/devices/{id}`（ADMIN）。

## 新增 schema
- `VideoCameraWriteRequest`（6 字段：name / cameraType / location / statusName / hd / thumbIndex）。
- `CommDeviceWriteRequest`（15 字段：deviceCode / deviceType / groupKey / groupLabel / deviceName / areaName / locationName / deviceStatus / longitude / latitude / categoryName / installTime / ownerName / ipAddress / lastCheckTime）。

## 不变
- 读端点（`/video/cameras` GET、`/communication/devices` GET 与 `/communication/devices/{id}` GET）响应 schema 不变。
- 既有广播域（alarm / emergency.* / rescue.* / fire-facility.* / security.gate-control / system.* / tv.monitor / video.linkage / form.record / mgmt-ledger 等）不受影响。
