# 设计：救援力量唯一真源

## 术语与既存事实

- **中队**：8 个基层单位（乙烯/炼油/罐区/仓储/码头/芳烃/特勤一/特勤二中队）。两套表中队名完全同名。
- `fac_brigade_team`（8 条）：中队档案（区域/负责人/GPS/描述/在编人数），**保留**。
- 扁平台账 `fac_rescue_*`：按「中队」登记的装备/人员/车辆（字段更细）。
- 队伍子表 `fac_brigade_{equipment,person,vehicle}`：按 `team_id` 登记的同类数据（**退役**）。

## ADR

### ADR-1 以扁平台账为唯一真源（而非队伍体系）
- 决策：装备/人员/车辆一律取 `fac_rescue_{equipment,personnel,vehicle}`。
- 理由：扁平台账字段更完整（装备含型号/防护类型/有效期/检修/报废预警等），且管理端 `GET /rescue-resources/equipment|personnel|vehicles` 已基于它。
- 代价：大屏「消防救援力量」与队伍详情数字由原队伍子表的 110/71/39 降为 52/35/12（用户已确认）。

### ADR-2 队伍详情按「中队名」归组（不引入 FK）
- 决策：`toBrigadeTeams()` 以 `fac_brigade_team.team_name` 关联 `fac_rescue_*.squadron` 归组。
- 理由：两表中队名严格一致（V19 种子）；避免新增外键迁移。
- 风险：若运营改队名导致不一致，该队子集合为空（可在后续以校验/约束兜底）。

### ADR-3 补列而非保留队伍子表
- 决策：`fac_rescue_personnel` 补 `person_group/phone/duty_status`，`fac_rescue_equipment` 补 `category/unit`。
- 理由：队伍详情 UI（`FireBrigadeDetailPanel`）需按「组别」「装备类别」分组，扁平表原缺这两组字段；补列后可完全由扁平表驱动。
- 回填口径：`person_group` 由 `person_role`（班长/副班长→指挥、战斗员→战斗、驾驶员→驾驶、通信员→通信、其余→保障）推导；`duty_status` 按 id 取模轮转；`category`/`unit` 按装备名 CASE 归类。

### ADR-4 应急物资脱离装备台账
- 决策：`应急物资` 不再取 `fac_rescue_equipment`（该表语义为「救援装备」），回落 `sys_emergency_strength` 人工统计值（3510），`items` 返回 null（统计口径）。
- 理由：`应急物资` 与 `救援装备` 概念不同，共用一张表会造成两卡片同源同值。
- 后续：如需逐项明细，可另建物资台账（本次不做）。

### ADR-5 移除 mock 常量
- 决策：删除 `RescueResourceService` 的 `EQUIPMENT_TOTAL_SETS=375` / `PERSONNEL_TOTAL_COUNT=375`，改为 `selectCount(null)` 实时计数。
- 理由：注释自述「总量为独立常量」，与台账脱节，是 375 之谜的根源。

## 数据影响

| 位置 | 之前 | 之后 |
| ---- | ---- | ---- |
| `/rescue-resources/equipment` totalSets | 375（写死） | 35（台账条数） |
| `/rescue-resources/personnel` totalCount | 375（写死） | 52（台账条数） |
| `/fire-monitoring/rescue-forces` 救援人员 | 110 | 52 |
| `/fire-monitoring/rescue-forces` 救援装备 | 71 | 35 |
| `/fire-monitoring/rescue-forces` 救援车辆 | 39 | 12 |
| `/emergency/strength` 装备车辆→救援装备 | 28（手填）/ items null | 35 / 35 条明细 |
| `/emergency/strength` 应急物资 | 35（误取装备台账） | 3510（统计口径，items null） |

## 风险与回滚

- **不可逆点**：V62 `DROP TABLE` 3 张队伍子表。开发阶段可接受；生产执行前需备份（表格数据为 mock 衍生，已被扁平表取代）。
- **回滚**：以扁平表备份 + 反向迁移恢复队伍子表；本次不做自动化回滚脚本。
- **前端兼容**：大屏数字随接口变化，无需改前端代码；应急面板枚举改名需前端同步（已含在本次交付）。
