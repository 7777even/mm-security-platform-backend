# Retro · Dashboard / 实时推送 去 Random 清理（2026-09-07）

## 做了什么
把 `DashboardService` 的硬编码汇总和 `AlarmSimulator` 的 `Random` 造数，全部替换为基于真实表
（`fac_device` / `fac_alarm` / `fac_workstation`）的聚合与增量轮询。这是后端脚手架技术债清理的
第三轮（前两轮：告警分页 mock 清理、401/403 真实状态码、告警字段对齐）。

## 做对了
- **先确认契约字段再动手**：读前端 `dashboard.openapi.json` 确认 `DashboardOverview`
  （activeAlarm/deviceOnline/deviceTotal/riskIndex/onlineWorkstation/ts）与 `Workstation`
  （id/name/zone/online），以及 device `status` 枚举（0=离线 1=在线 2=告警），避免二次漂移。
- **工位用新增真实表而非空列表**：原 `workstations()` 返回硬编码 `Map`；本次补 `fac_workstation`
  主数据表 + 种子，态势页工位卡片有真实数据，且不残留"看起来像 stub 的空列表"。
- **去重靠物理主键**：`AlarmSimulator` 用 `lastPushedId` 避免同一条告警被反复广播，比"比较时间戳"更稳。
- **类型安全收口**：`broadcastAlarm(Object)` → `broadcastAlarm(AlarmItem)`，配合 `AlarmAssembler` 复用。
- **测试基线同步补**：3 个新测试类 9 case，纯 Mockito 不启 Spring，沿用 `#8` 基线纪律；35 case 全绿。

## 可以更好
- **riskIndex 公式**：当前是透明加权，非风控模型。应与前端/业务确认期望口径（归一化区间、是否接模型），
  避免后续"数值看着对但语义不对"。
- **实时推送方式**：轮询 12s 对 dev 足够，生产建议升级为 binlog/触发器事件驱动，避免空轮询与延迟。
- **跨库四同步提醒**：本轮只动了后端实现与种子，未触碰前端契约（字段本已对齐），故无需重生成类型；
  但新增 `fac_workstation` 后若前端要新增工位写接口，需走 openspec Change + 四同步。

## 行动项
- [ ] P3：生产库在线迁移引入 Flyway（fac_workstation 等表当前仅 H2 重建）
- [ ] 待开 Change：消化 14 项前瞻桩（alarm CRUD / alarm-trend / risk-heatmap / emergency / map / uplink）
- [ ] 待确认：riskIndex 口径是否要归一化或接真实风控模型
