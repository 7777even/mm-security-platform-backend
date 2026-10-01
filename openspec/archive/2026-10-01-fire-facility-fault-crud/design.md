# Design: 消防设施故障全量 CRUD

## 决策 1：扩展既有 PUT，而不是新增「全量替换」端点

既有 `PUT /faults/{faultId}` 已被大屏 `FireFacilityMonitoringDialog` 用于状态流转写入，
若改成「全字段覆盖」会破坏该链路（未传字段被清空）。故改为**在既有端点上追加可选字段**，
保持 `null` 表示「不更新」的 read-modify-write 语义，大屏旧调用零影响。

## 决策 2：物理删除 + 级联时间线

故障记录与 `fac_fire_facility_fault_timeline` 是主从关系且无软删字段（`deleted` 列不存在于
这两张表），故删除采用**先删时间线再删主记录**的物理删除，不做回收站。
删除前校验存在性，不存在返回 B3 `NOT_FOUND`（HTTP 200 + code=404），符合 B3 包络约定。

## 决策 3：权限码粒度

沿用 V68 的按钮级权限码风格：`fire-facility:fault-create` / `fire-facility:fault-delete`，
与既有 `fire-facility:handle`（状态流转）区分——录入与删除属台账维护，处置属运行动作，
授权角色集合保持一致（ADMIN / COMMANDER / SCHEDULER / TEAM_LEADER / INNER_OPER / OUTER_OPER）。

## 决策 4：编号唯一性

`faultCode` 为业务编号，新增时重复返回 B3 `CONFLICT`（409）；编辑态前端将其置灰不可改，
避免「改编号撞车」的语义歧义。

## 决策 5：排序

新增记录 `sort_no` 取当前最大值 +1，保证列表按既有顺序稳定追加，不依赖插入顺序。

## 校验矩阵

| 场景 | 结果 |
| --- | --- |
| 必填字段（faultCode/facilityCode/faultType/faultLevel/discoverTime）缺失 | B3 `PARAM_INVALID` |
| faultLevel 不在 紧急/重要/一般 | B3 `PARAM_INVALID` |
| faultStatus 不在六态枚举 | B3 `PARAM_INVALID` |
| faultCode 重复 | B3 `CONFLICT` |
| faultId 非数字 | B3 `PARAM_INVALID` |
| 记录不存在 | B3 `NOT_FOUND` |
