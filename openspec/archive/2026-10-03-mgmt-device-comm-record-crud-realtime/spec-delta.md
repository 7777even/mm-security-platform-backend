# Spec Delta: realtime-broadcast（device / communication.record 域）

## 新增广播域
- `device`：由 `DeviceService` 的 createDevice / updateDevice / deleteDevice 在事务提交成功后广播 `device.changed`。
- `communication.record`：由 `CommRecordService` 的 createRecord / updateRecord / deleteRecord 在事务提交成功后广播 `communication.record.changed`。

## 新增 HTTP 端点
- POST `/devices`、PUT `/devices/{code}`、DELETE `/devices/{code}`（ADMIN；DELETE 为软删除）。
- POST `/communication/records`、PUT `/communication/records/{recordNo}`、DELETE `/communication/records/{recordNo}`（ADMIN）。

## 新增 schema
- `DeviceWriteRequest`（7 字段：deviceCode / deviceName / deviceType / zone / status / lat / lon）。
- `CommRecordWriteRequest`（12 字段：recordNo / recordType / occurredAt / category / sender / receiver / summary / result / duration / channel / direction / contentType）。
- `device.openapi.json` 新增本域本地 `DeleteResult`（各域契约各自本地定义，与本仓既有约定一致）。

## 不变
- 读端点（`/devices` GET、`/devices/{code}` GET、`/communication/records` GET）响应 schema 不变。
- `CommunicationRecord`（12 字段）响应 schema 不变，仍与后端 dto 对齐。
- 既有广播域不受影响。
