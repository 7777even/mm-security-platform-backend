# 报警三端口径（后端设计真源）

> **文档性质**：设计真源（decision record）。回答「同一个『报警』为什么在后端有多张表 / 多个端点」。
> **事实基线**：表与端点的**具体清单**以 `../system-facts.md` 为准；本文只固化**决策与边界**，避免被误当缺口而擅自"统一"。
> **前端对侧**：`frontend-scaffold/docs/system-facts.md` §6.1（三端分工表）。两端口径必须一致，改动须两端同步。

---

## 1. 决策（2026-09-16 定）

「报警」在本系统**不是一个域，而是三个用例**，因此后端保留**三张表 / 三组端点**：

| # | 用例 | 表 | 端点 | 语义 | Controller / Service |
| --- | --- | --- | --- | --- | --- |
| A | **通用告警流**（移动端告警明细/详情、大屏安全报警面板与地图撒点） | `fac_alarm` | `GET /api/v1/alarms` | 全类型告警（消防/气体/温度/视频AI/SOS 五类）+ 来源设备编码 | `AlarmController` / `AlarmService` |
| B | **消防专项**（大屏消防报警列表弹窗、管理端报警记录） | `fac_fire_alarm` | `GET /api/v1/fire-alarms` | 消防单一类型，含 `type_label` 细类与误报标记 | `FireAlarmController` / `FireAlarmService` |
| C | **生产域告警**（大屏生产区域面板） | `fac_production_alarm` | `GET /api/v1/production/alarms` | 生产域口径，与安全域解耦 | `ProductionController` / `ProductionService` |

相邻但**不属本决策**的两组（同属「告警」字样，用途不同，勿混入上表）：

- `GET /api/v1/map/alarms`（`MapController` / `MapService.alarmPoints()`）——**地图撒点视图**，为地图图层服务，不是告警业务流。
- `GET /api/v1/fire-facility/alarms`（`FireFacilityController` / `FireFacilityService.alarms()`）——**消防设施台账视角**的告警子集，按 `level`/`status` 过滤，供设施台账页使用。

## 2. 判据（为什么不能合并成一张表）

1. **字段能力不同**：移动端需按 `type` 分五类展示，并显示来源设备编码（`device_code`）。实测只有 `fac_alarm` **同时具备 `type` 与 `device_code`**；`fac_fire_alarm` 两者皆无，只有 `type_label`（细类）与 `source` / `object_name`。
2. **数据构成不同**：`fac_fire_alarm` 的种子全为消防记录（含误报标记、关联灭火事件），它是「消防专题数据集」，不是「全类型告警的子集」。
3. **用例期望不同**：管理端的「报警记录」要的是**消防专项全部状态**的可检索台账；大屏安全报警面板只要 `status=ACTIVE` 的消防活动项。二者共用 B 路，但过滤条件不同，属端点参数而非表的差异。
4. **职责边界**：生产域告警（C）与安全域告警（A/B）由不同业务口径产生，合并会强行把两套判定规则塞进一张表。

## 3. 约束

- **不得**把三张表合并为一张，或把三组端点收敛为一个"万能报警端点"；三端"同一内容读不同表"是**刻意分工**，不是数据不一致。
- **不得**依据「个数不唯一」就判定某路是重复实现或历史遗留（本决策即为该判定提供依据）。
- 新增报警消费方时，**先按用例归属**选择 A/B/C，**不要新增第四路**；确需新口径须走 L4 + 契约四同步，并回到本文补一行。
- 移动端与「大屏地图报警撒点」对齐 **A 路**；管理端「报警记录」与「大屏消防报警弹窗」对齐 **B 路**。
- 任一表 / 端点的增删改，须同步：`../system-facts.md`、`frontend-scaffold/docs/system-facts.md` §6.1、以及前端契约 `docs/api/{alarm,fire-alarm,production}.openapi.json`。

## 4. 反模式（曾出现 / 易犯）

- ❌ **「统一到一张表」**：会让移动端丢失 `type` / `device_code`，或让消防专题丢失 `type_label` / 误报语义——两者都是功能回退。
- ❌ **拿 B 路的 `level`（GDS 阈值文本）去对齐 A 路的 `level`**：两路 level 语义不同，不可直接比较或互换。
- ❌ **把 `map/alarms` 或 `fire-facility/alarms` 当成 A/B 路的重复实现**而删除其一：它们是不同视图（地图图层 / 设施台账），消费方不同。
- ❌ **前端为「看起来一致」而自行跨路合并**：三端读哪一路由本文与前端 §6.1 指定，不由页面自行决定。

---

## 相关文档

- [../../docs/system-facts.md](../system-facts.md) — 表、端点、迁移版本的事实基线
- [README.md](./README.md) — 架构文档索引
- [module-boundary.md](./module-boundary.md) — 分层与模块边界红线
- `frontend-scaffold/docs/system-facts.md` §6.1 — 三端分工对照表（前端侧事实）
