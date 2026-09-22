# Design: 事故救援「响应动态」按事件隔离

## 决策（ADR）

- **方案 A（选定）**：仅用 `fac_accident_dynamic.incident_id` 做事件隔离，复用既有 `fac_accident_incident` 作为事件主表。演练事件即 `fac_accident_incident` 中 `is_default=false` 的真实 drill 行；`incident_id` 指向它即可区分演练/真实动态。
- **否决方案 B**：不引入 `scenario`（`DRILL`/`REAL`）标签列——`incident_id` 已隐含场景归属，加标签属冗余且需双写维护。
- **种子风格**：演练专属动态按 V12 全局参考主数据风格编写（四类 `category`：`rescue`/`command`/`brief`/`awareness`，`tag` 用「【演练指令】/【快讯】/【快报】」前缀，`command_text`/`responder`/`reply`/`stage_label` 字段语义一致），仅文案以「演练」语境改写，保证视觉与既有动态一致。

## 数据模型

```
fac_accident_dynamic
  id (PK, AUTO)
  incident_id BIGINT  -- 新增：绑定 fac_accident_incident.id；NULL/未命中→默认事件
  category / title / tag / time / command_text / responder / reply / stage_label / sort_no (不变)

fac_accident_incident
  id (PK, AUTO)
  event_id / title / location / ... / is_default (不变)
  -- V63 新增一行：event_id=11 '储罐区消防演练' is_default=false
```

- 默认事件约定：`is_default=TRUE` 的行（V12 种子 `event_id=4`，本库 `id=1`）承载 19 条全局参考动态（`incident_id=1`）。
- 回退规则：事件自身 `dynamics` 为空时，取默认事件动态，防空屏（大屏不空屏铁律）。

## 迁移策略（V63 三方言）

- `ALTER TABLE fac_accident_dynamic ADD incident_id BIGINT;` + `CREATE INDEX ...(incident_id)`。
- `UPDATE fac_accident_dynamic SET incident_id = 1;` 存量归默认事件（一次性，幂等对同一空列）。
- 演练事件行 **不指定 `id`**，交由自增分配 —— 因 `V58__accident_rescue_events.sql` 已 seed `event_id=2` 占用了 `id=2`，硬编码 `id` 会触发 PK 23505 冲突导致 Flyway 失败、级联全部 `@SpringBootTest` 上下文加载失败。
- 演练动态 `incident_id` 用子查询 `(SELECT id FROM fac_accident_incident WHERE event_id = 11)` 引用刚插入行（确认 `event_id=11` 在 `fac_accident_incident` 中尚不存在，子查询结果唯一）。
- 达梦版本：达梦不支持多行 `VALUES`，10 条动态逐条 `INSERT`；`ADD incident_id BIGINT` 与 h2 同语法（DM `BIGINT` 即 `NUMBER(19)` 兼容）。Pg：`ADD COLUMN incident_id BIGINT;`。

## 服务改造

- `AccidentRescueService#buildAccidentRescueDto(id)`：
  - 调度资源 / 值班人员 / 辅助统计：维持 `auxStatMapper.selectList(allSorted())` 全局参考主数据不变。
  - 动态：`loadDynamicsByIncident(id)` 按 `incident_id` 查；为空且 `id != 默认事件 id` 时再取默认事件动态。
- `toDynamic(...)` 映射不变（`incidentId` 不入 `RescueDynamicEntry` DTO）。

## 测试策略

- 纯 Mockito 单测（不启 Spring 上下文，沿用基线）：
  - `incident_dynamicsOwned_returnsWithoutFallback`：`dynamicMapper.selectList` 第一次返回自身动态 → 直接返回，不回退。
  - `incident_dynamicsEmpty_fallsBackToDefaultIncident`：第一次返回空、第二次返回默认事件动态 → 回退成功。
- 验证：`mvn test` 全量 **661 绿，0 failure/0 error**；`scripts/check-api-contract.mjs --strict` 路由 0 差异 / schema 0 漂移（`incident_id` 不对外暴露）。

## 风险

- Flyway PK 冲突（初版硬编码 `id` 触发）：已通过「自增 + 子查询」修复并重跑全绿，详见上文迁移策略。
- 三方言仅 h2 实跑；pg/dm 静态对拍一致，须上环境复核（无 Docker / 无达梦实例）。
