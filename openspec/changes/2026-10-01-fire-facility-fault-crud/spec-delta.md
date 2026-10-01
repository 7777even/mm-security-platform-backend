# Spec Delta: fire-facility（消防设施）

## 新增 capability 要求

### fire-facility.fault CRUD

- 系统应提供 `POST /api/v1/fire-facility/faults` 用于台账录入，
  请求体为 `FireFacilityFaultCreateRequest`，成功返回新建 `FireFacilityFaultItem`。
- 系统应提供 `DELETE /api/v1/fire-facility/faults/{faultId}` 用于真删除，
  并级联清理该故障的 `fac_fire_facility_fault_timeline` 记录。
- `PUT /api/v1/fire-facility/faults/{faultId}` 应额外支持 facilityName / facilityType /
  faultType / faultLevel / discoverTime / discoverMethod / phenomenon / cause 的局部更新。
- 上述三个写操作均须发布 `fire-facility.fault` 域变更事件（`@RealtimeSync`）。
- 权限码须为按钮级：`fire-facility:fault-create`、`fire-facility:fault-delete`；
  既有 `fire-facility:handle` 语义不变。
- 故障级别枚举固定为 紧急 / 重要 / 一般；故障状态枚举固定为
  待确认 / 已确认 / 已派单 / 维修中 / 待验收 / 已闭环。
- 所有失败路径走 B3 包络（HTTP 200 + `code != 0`）：参数非法 100、不存在 404、编号冲突 409。

## 变更的既有约束

- 无（既有只读端点与状态流转语义保持兼容）。
