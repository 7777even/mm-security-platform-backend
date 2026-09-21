# Proposal: 应急力量「无明细」三类别补真实参考数据

## Why

大屏「应急指挥首页」（fm-emergency）右侧「应急力量救援」面板，对 8 个类别点击后弹出左列表 + 地图散点 + 右详情浮层。
其中「应急场所 / 医疗机构 / 消防设施」三类当前后端 `GET /api/v1/emergency/strength` 只返回统计 `count`、`items` 恒为 `null`
（契约 `emergency-reference` 第 45 行曾明确"不虚构明细"），导致浮层为空、与已落地的"救援资源浮层统一"体验不一致。

事实核查：
- **消防设施** 已有真实台账表 `fac_fire_facility_ledger`（V20 种子 42 条，含 `facility_name`/`location_name`/`facility_type`），可**零虚构**复用。
- **应急场所 / 医疗机构** 无真实台账，但属"运营可维护参考数据"（与值班表/通讯录/知识库同性质），新增一张
  `sys_emergency_strength_item` 参考表并种子贴近厂区实际的名单即可，同样不算凭空捏造。

## What Changes

- `GET /api/v1/emergency/strength`：
  - 「应急场所 / 医疗机构」从新增参考表 `sys_emergency_strength_item`（`kind`/`name`/`meta`/`sort_no`）取前 20 条填充 `items`。
  - 「消防设施」从真实台账 `fac_fire_facility_ledger` 取前 20 条填充 `items`，且 `count` 由台账实时计数覆盖（取代 V8 手填 42）。
  - 仅「装备车辆」保持 `items` 为 `null`（该类别由前端 `fac_rescue_equipment` 台账另路展示，不在此接口范围内）。
- **DB 结构变更（L4）**：Flyway `V61__emergency_strength_item.sql` 三方言（h2 / postgresql / dameng）创建 `sys_emergency_strength_item` 并种子应急场所(6)/医疗机构(3)。
- 契约真源 `frontend-scaffold/docs/api/emergency.openapi.json` 的 `EmergencyResource.items` 描述/example 更新（schema 不变）。

## Capabilities

- 大屏点击「应急场所 / 医疗机构 / 消防设施」→ 浮层展示真实/参考名单（名称 + 位置/类型说明），与救援资源浮层体验一致。

## Impact

- 存量接口**仅新增响应字段内容**，路径/方法/鉴权不变，向后兼容；前端旧解析不受影响（前端此前已按"非空即展示"实现）。
- DB：新增一张参考表，可空 `meta`；种子仅命中应急场所/医疗机构两类，无存量数据迁移风险。
- 回退：删除 V61（表随迁移回滚策略处理）+ 移除 service 三分支 + 契约描述回滚。

## 人工确认关卡（L4 必过）

| 关卡 | 状态 | 说明 |
| --- | --- | --- |
| DB 结构变更确认 | ☑ 已确认 | 用户明确选择"正式迁移 + 改契约"，同意新增 `sys_emergency_strength_item` 参考表 |
| 接口范围确认 | ☑ 已确认 | 仅富化既有只读端点 `GET /emergency/strength`，不新增路由、不引入写端点 |
| 三方言迁移方案确认 | ☑ 已确认 | 依既有 V8/V59+ 迁移风格；DM 用 `NUMBER(19) IDENTITY` + `VARCHAR2(n CHAR)`，与 V8 一致 |
| 契约语义变更确认 | ☑ 已确认 | 解除原"不虚构明细"约束，改为"消防设施取真实台账、应急场所/医疗机构取运营参考表" |
