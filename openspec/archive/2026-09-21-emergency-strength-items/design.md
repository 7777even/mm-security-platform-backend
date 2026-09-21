# Design: 应急力量「无明细」三类别补真实参考数据

## 架构决策（ADR）

### ADR-1：消防设施复用既有真实台账，零虚构
`fac_fire_facility_ledger`（V20 已种子 42 条）即为消防设施真实台账。`strengthItemsFromLedger` 对「消防设施」
直接 `SELECT ... FROM fac_fire_facility_ledger ORDER BY sort_no LIMIT 20`，`name=facility_name`、`meta` 由
`location_name · facility_type` 拼接；`strengthCountFromLedger` 对该类返回台账实时计数（覆盖 V8 手填 42，使数字同源）。

### ADR-2：应急场所 / 医疗机构 新增运营可维护参考表
新增 `sys_emergency_strength_item(kind, name, meta, sort_no)`，与 `sys_emergency_strength` 同族（运营可维护参考数据）。
种子应急场所(6)/医疗机构(3)，贴近厂区实际（集结点/疏散点/医务室/协议医院）。`strengthItemsFromLedger` 对该两类
`SELECT ... WHERE kind=? ORDER BY sort_no LIMIT 20`，`name`/`meta` 直读。
> 该表仅服务 strength 预览，不参与救援资源台账（`/rescue-resources/*`），避免与 ledger 类别混淆。

### ADR-3：保留"不虚构"底线，仅放开三类
「装备车辆」仍返回 `null`（其明细由前端 `fac_rescue_equipment` 台账另路展示，不在此接口）。其余 ledger 类别行为不变。
契约原"不虚构明细"约束放宽为"消防设施取真实台账、应急场所/医疗机构取运营参考表"，本质仍是可溯源的真实/参考数据。

### ADR-4：`meta` 单字段承载辅助说明（沿用既有约定）
与 `StrengthItem.meta` 既有拼装规则一致（空值忽略、`·` 连接），不新增可空列，保持契约稳定。

## 数据影响

- 新增 `sys_emergency_strength_item`：`id`(自增) / `kind VARCHAR(32)` / `name VARCHAR(128)` / `meta VARCHAR(256)` / `sort_no INT`。
- 种子：应急场所 6 行、医疗机构 3 行（与 V8 `sys_emergency_strength` 计数为 6/3 保持一致）。
- 无存量数据迁移风险：纯新增表 + INSERT，不 ALTER 既有表。

## 风险

- 缓存：`strength()` 为 Caffeine 5min 读穿缓存，明细随之缓存；台账/参考表变更一致窗口为 TTL，与既有策略一致。
- 迁移：DM 不支持 `AUTO_INCREMENT`/多行 `VALUES`；建表用 `NUMBER(19) IDENTITY(1,1)`、种子逐条 `INSERT`（与 V8 一致）。
- 口径：`fac_fire_facility_ledger` 行数若与 V8 手填 42 不同，消防设施 `count` 将变为真实行数（更诚实，属预期行为）。

## 依赖

- 契约真源 `frontend-scaffold/docs/api/emergency.openapi.json`（先改契约，再改实现）。
- 方言一致性脚本 `scripts/check-dialect-migration-consistency.py`。
- 契约守门 `scripts/check-api-contract.mjs --strict`。
