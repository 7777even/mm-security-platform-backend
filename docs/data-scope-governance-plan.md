# 数据权限（data_scope 行级 ABAC）治理分步方案

> 状态：**方案文档，待产品 / 架构拍板后再实施**。本文不写业务代码，只给出分步路径、事实依据与风险清单。
> 关联设计：`openspec/changes/2026-09-10-rbac-data-scope-abac/design.md`；实现：`security/DataScopeHelper.java`、`security/DataScopeResolver.java`、`service/RescueResourceService.java`、`db/migration/h2/V34__data_scope_abac.sql`。
> 日期：2026-09-12

---

## 0. 现状事实盘点（已核证）

数据权限机制本身已就绪，但**落地范围极窄、且存在编码错配隐患**：

| 维度 | 现状 | 证据 |
|---|---|---|
| 解析器 | `DataScopeResolver.resolveZones()`：ALL→`null`（看全部）；否则返回用户 `zone_codes` 解析集合；空集→零可见；匿名→`null` | `security/DataScopeResolver.java:57` |
| 注入工具 | `DataScopeHelper.apply(qw, col, zones)`：`null`→不加条件；空集→`1=0`；非空→`IN(zones)` | `security/DataScopeHelper.java:26` |
| **真实落地的域** | **仅 1 处**：`RescueResourceService.brigades()` 用 `qw.in(FacBrigadeTeam::getArea, zones)` | `service/RescueResourceService.java:171` |
| 可正确命中的列 | `fac_brigade_team.area`（英文，与 `sys_zone.zone_code` 对齐：LIANYOU/YIXI/GUANQU/CANGCUN/MATOU/FANGTING/TEQIN） | `V34` seed + `FacBrigadeTeam.area` |
| 有列但**编码错配** | `fac_device.zone`（中文）、`fac_workstation.zone`（中文） | 见下 |
| **无防区列** | `fac_alarm`、`fac_video_camera`、`fac_major_hazard`（仅有自由文本 `location`） | `V1`/`V14`/`V3` 建表 |
| 角色默认 scope | `sys_role.data_scope` 默认 `'SELF'`（V32） | `V32__system_rbac.sql:16` |

### 编码错配的具体证据（致命）

`sys_zone.zone_code` 是**英文枚举**，而业务表存的是**中文防区名**，两者值域不重叠：

- `fac_device.zone`（去重 5 种）：`危化仓库` `罐区A` `罐区B` `装卸区` `装置C`
- `fac_workstation.zone`（去重 8 种）：`乙烯区` `储运区` `全厂范围` `公用工程区` `化工区` `炼油区` `码头区` `罐区A`

即便中文名与 `sys_zone.zone_name`（炼油区/乙烯区/罐区/仓储区/码头区/芳烃区/特勤保障区）语义相近，
**`IN('LIANYOU','YIXI',...)` 套在中文列上必然 0 命中**；且存在子区（罐区A/B）、无对应英文的项（储运区/公用工程区/化工区/全厂范围/装卸区/装置C），并非干净 1:1。

### 当前真实行为（重要，避免误判）

目前 `fac_alarm` / `fac_video_camera` / `fac_major_hazard` / `fac_device` / `fac_workstation` 的列表查询**都没有调用** `DataScopeHelper.apply`，
即**所有角色都能看到全部数据**——这是**已知的数据隔离缺口**，但**不是"零可见 bug"**。
风险在于：若"顺手"给这些表加上 `apply`，会因编码错配 / 缺列立刻变成零可见或 SQL 报错。

---

## 1. 风险清单（拍板前必读）

- **R1 零可见线上事故（最高优先级）**：`data_scope` 默认 `SELF`。任一非 ALL 角色用户若 `zone_codes` 为空，`resolveZones()` 返回空集 → `1=0` → 列表全空。上线前必须为每个非 ALL 用户分配 `zone_codes`，否则会出现"看不见数据"的线上事故。
- **R2 编码错配零命中**：中文列直接 `IN` 英文 code → 0 行。必须先标准化编码（见步骤 2），禁止在现状下直接 apply。
- **R3 跨方言迁移**：达梦 / PG 镜像尚未激活（当前仅 h2 单方言）。新增 `zone_code` 列与回填须走 `V46+` 新增迁移，**严禁改 V1–V45**（Flyway 既存 V 不可变）；两库激活时需同步镜像。
- **R4 缓存一致性**：`DataScopeResolver` 用 Caffeine 5min TTL + `invalidateUser(username)`。`sys_user.zone_codes` 变更（SystemUserService）须显式调 `invalidateUser`，否则最长 5 分钟陈旧。
- **R5 性能**：`IN` 列表随用户防区数增长，但防区数通常 < 10，可忽略；达梦/PG 下 `IN` 仍安全（优于 `last("LIMIT")`）。

---

## 2. 分步方案（待拍板后实施）

### 步骤 0 · 冻结并书面确认现状
- 明确"仅 brigade_team 域隔离、其余域全角色可见"是**当前预期行为**（缺口），不视为 bug。
- 在需求/设计文档标注：数据权限治理为独立专项，不在性能优化专项内顺手做。

### 步骤 1 · 补全 `sys_zone` 防区主数据（需产品给权威防区树）
- 现有 7 个 English code 不足以覆盖中文业务值（储运区/公用工程区/化工区/装卸区/装置区/全厂范围 等无对应）。
- 决策 **D2a**：是新增 English code（推荐，规范化），还是把子区/跨区归并到现有 7 个？
- 明确 `全厂范围` 语义 = 需要"跨防区"权限（特殊角色），而非某单一 code。

### 步骤 2 · 统一防区编码（核心，决策 D1）
- **方案 A（推荐）**：在 `fac_device` / `fac_workstation` 等表**新增 `zone_code` 列**（规范化，与 `sys_zone.zone_code` 同域），回填脚本由"中文 zone → English zone_code"映射驱动。优点：列统一后，可进一步做**通用 MyBatis 拦截器**（见 D5），长期可维护。
- **方案 B**：新增 `zone_code ↔ 中文zone` 映射表，查询时翻译。改动小但每次查询需翻译/关联，且未解决缺列表。

### 步骤 3 · 为缺列核心表补 `zone_code` 列 + 回填（决策 D2b）
- `fac_alarm` / `fac_video_camera` / `fac_major_hazard` 无防区列。决策：
  - 按 facility / 设备归属回填（需产品给归属规则）；或
  - 暂按 ALL 可见（先不开启这些域的隔离，保持现状），避免错误赋防区导致数据丢失可见性。

### 步骤 4 · 注入数据权限
- 在目标域 Service 列表查询**仿 `brigades()` 显式 `apply`**（最稳，逐域可控）；或
- 统一列后评估**通用拦截器**（D5）：基于统一 `zone_code` 列 + 约定/注解，自动注入，减少逐 Service 改动。

### 步骤 5 · 上线前置（对应 R1）
- 为所有非 ALL 角色用户分配 `zone_codes`；
- 加校验：用户保存时若 `data_scope≠ALL` 且 `zone_codes` 为空，告警/阻断；
- `SystemUserService` 改 `zone_codes` 时调 `DataScopeResolver.invalidateUser`。

### 步骤 6 · 回归测试
- 参照 `RescueResourceServiceTest` 思路补单测，覆盖三态：`null`(ALL 不过滤) / 空集(`1=0`) / 非空(`IN` 命中)；
- 跑完整基线（477 用例门禁）确认无回归。

---

## 3. 待拍板的决策点（D 系列）

| ID | 决策 | 选项 | 影响 |
|---|---|---|---|
| **D1** | 防区编码统一策略 | A) 新增 `zone_code` 列并回填（推荐）/ B) 中文↔英文映射表 | 决定数据模型与长期可维护性 |
| **D2a** | `sys_zone` 是否扩容 English code | 新增缺失防区 / 归并到现有 7 个 | 防区主数据权威性 |
| **D2b** | 缺列核心表（alarm/camera/hazard）如何赋防区 | 按归属回填 / 暂 ALL 可见 | 是否真正隔离这三类数据 |
| **D3** | `全厂范围` 等跨区值映射 | 视为特殊跨区角色 / 拆到具体 code | 跨区可见性语义 |
| **D4** | 上线前用户 `zone_codes` 分配 | 批量初始化脚本 / 手工 | 直接决定 R1 是否爆雷 |
| **D5** | 是否做通用拦截器 | 逐 Service 显式 apply（稳）/ 统一列后通用拦截器（省事） | 代码量与健壮性权衡 |

---

## 4. 结论

数据权限**不是性能问题，是真实业务缺口**，且当前"局部生效 + 编码错配 + 缺列"的组合意味着**绝不能顺手给更多表加 `apply`**。
正确路径是：先由产品给出权威防区树（D2a）→ 统一编码（D1）→ 补列回填（D2b）→ 再注入 → 再上线前置（D4）。
属数据治理专项，需排期与架构拍板；在拍板前，本文档即为其实施蓝图。
