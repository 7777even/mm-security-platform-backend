# Design: 应急力量 / 知识库明细接口富化

## 架构决策（ADR）

### ADR-1：明细取自既有台账，不新增明细表
`strength` 的 4 个 ledger 源类别已与 `GET /rescue-resources/*` 同源（`EmergencyService.strengthCountFromLedger` 注释即此口径）。
明细直接复用这些 mapper（`fac_rescue_personnel` / `fac_rescue_equipment` / `fac_rescue_vehicle` / `fac_brigade_team`），
**不新增明细表**，保证「数量」与「明细」同源、不出现两套数字。

### ADR-2：明细预览上限 20 条
台账可达数百条（部队 375 人），弹窗不宜全量。各 ledger 查询追加 `LIMIT 20`（h2/pg/dm 均支持），
`count` 仍为全量计数。前端弹窗展示该 20 项，并在 `count` 中体现全量。

### ADR-3：`description` 走 DB 列 + 种子回填，而非后端常量
知识分类说明应可运营维护。为 `sys_knowledge_item` 增加可空 `description` 列，V59 回填 3 个既有种子分类的文案；
后端 `knowledge()` 直接透传 `r.getDescription()`，不再有硬编码。

### ADR-4：`meta` 单字段承载辅助说明
不同类别辅助字段不同（岗位 / 规格 / 车型 / 区域），统一拼成单个 `meta` 字符串（`·` 连接、空值忽略），
避免 `StrengthItem` 出现 N 个可空列，保持契约稳定。

## 数据影响
- `sys_knowledge_item`：新增列 `description VARCHAR(512)`（h2/pg）/ `VARCHAR2(512 CHAR)`（dm），可空。
- 无存量数据迁移风险：新列默认 NULL，UPDATE 仅按 `title` 命中既有 3 行种子。

## 风险
- 缓存：`strength()`/`knowledge()` 均为 Caffeine 5min 读穿缓存，明细随之缓存；台账变更一致窗口为 TTL，与既有策略一致。
- 迁移：DM 不支持多行 `VALUES`/`ADD COLUMN IF NOT EXISTS`；本次仅 `ALTER ADD` + 逐条 `UPDATE`，规避。

## 依赖
- 契约真源 `frontend-scaffold/docs/api/emergency.openapi.json`（先改契约，再改实现）。
- 方言一致性脚本 `scripts/check-dialect-migration-consistency.py`。
