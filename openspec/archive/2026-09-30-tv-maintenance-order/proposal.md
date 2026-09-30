# 提案：工业电视维修工单真实台账（fac_tv_maintenance_order）

> **状态：`done` —— 已实现并验证（后端单测 22 绿、契约守门 0 漂移）。归档 Change。**
> 配套前端 Change：`frontend-scaffold/openspec/archive/2026-09-30-tv-maintenance-order`。

## 背景
工业电视大屏「维修工单」概览卡片（未接单/处理中/已超时）此前取 `fac_tv_stat_item` 手填字典值（item_count：12/25/8），与真实工单无关联键。用户反馈"维修工单这几项同理也要"——与视频概览此前同款问题：卡片点开只有数量、下钻不出任何具体工单（详情面板只把统计数字再念一遍，无逐工单明细）。

## 目标（后端根治）
1. 新建 `fac_tv_maintenance_order` 真实台账（V88 三方言），种子 45 条工单按状态拆分：12 PENDING 未接单 / 25 PROCESSING 处理中 / 8 OVERTIME 已超时。
2. `TvService.computeOverview()` 改由 `fac_tv_maintenance_order` `GROUP BY order_status` 实时计数；同时删除 `fac_tv_stat_item` 中已被取代的 `MAINTENANCE` 字典行（OVERVIEW / EVENT 字典行保留），避免遗留死数据。
3. 暴露下钻端点：
   - `GET /tv/maintenance-orders?status=`（按 state 过滤，空=全部）
   - `GET /tv/maintenance-orders/{id}`（单工单明细）
4. DTO `TvMaintenanceOrderItem`：承载工单编号/设备/故障描述/状态中文/派单人/部门/防区/时间/处理说明。

## 非目标（本期不做）
- 不改动 `fac_tv_stat_item` 的 OVERVIEW / EVENT 字典行（仍作为无逐点明细类的兜底手填值）。
- 维修工单不进入权限/ABAC 模型（演示数据；真实接入后由维保业务系统回写）。

## ADR
- **ADR-1 台账真源**：`fac_tv_maintenance_order` 为工单计数与下钻真源；`fac_tv_stat_item.MAINTENANCE` 删除不再使用。
- **ADR-2 列名避保留字**：状态列命名 `order_status` 而非 `status`（V15 明确 `status` 为保留字）。实体用 `@TableField("order_status")`。
- **ADR-3 状态 code 约定**：PENDING 未接单 / PROCESSING 处理中 / OVERTIME 已超时；`TvService.MAINTENANCE_STATUS_LABEL` 映射中文化 label，`MAINTENANCE_STATUS_TONE` 映射告警色（grey/blue/red）。
- **ADR-4 缓存**：`computeOverview` 既有 Caffeine 10s TTL 不变，实时计数随工单变更自动失效刷新。

## 风险
- 三方言迁移须一致（h2/postgresql/dameng 均 CREATE TABLE + INDEX + 45 种子 + DELETE MAINTENANCE 字典行）。本机无 Docker 未跑真实容器 IT，H2 通过不代表达梦/PG 通过（已如实标注）。
- 端点 `listMaintenanceOrders` 的 `status` 过滤在 Service 层（MyBatis `LambdaQueryWrapper.eq`），非 SQL 拼接；单测仅断言 Service 端字段映射与空值兜底，SQL 过滤交由 MyBatis-Plus 验证。
