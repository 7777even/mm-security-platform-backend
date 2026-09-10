# 提案：RBAC 数据范围（data_scope）行级 ABAC 落地（MVP）

> **状态：`draft` —— L4 安全变更，待人工确认后实现。**
> 用户已于 2026-09-10 通过两次 AskUserQuestion 确认：① 做 data_scope 行级 ABAC；② MVP 边界＝建防区主数据 + 用户↔防区绑定，试点 1~2 域。本提案据此细化。

## 背景与约束（调研结论 2026-09-10）
- `sys_role.data_scope`（值 `ALL/DEPT/SELF`，默认 `SELF`）自 V32 起已登记，但**全仓无任何查询引用它**——即"只登记、不过滤"（代码注释明载）。
- 当前**缺少统一防区主数据**：`FacProductionAreaZone` 仅是"装置内 A/B/C 分区"，`BRIGADE_AREA` 字典仅被救援域引用，彼此无外键。
- `sys_user` **无任何区域归属字段**，也无 `sys_user_zone` 关系表；"当前用户可访问哪些区域"无数据来源。
- 17+ 张业务表的区域字段命名/词表彼此不互通：`area`/`zone`/`areaName`/`areaCode`/`zoneCode`/`a·b·c`/`炼油区`/`乙烯装置区`…，且无外键。
- 取 `dataScope` 的天然入口是 `RoleAuthorityService`（已是 Caffeine 缓存 + 写时失效），无需动 JWT。

> 因此**无法干净做"全业务行级过滤"**。本变更采用 MVP 边界：先补两块地基（防区主数据 + 用户↔防区绑定），再对**唯一对齐的域（救援队伍）**做端到端试点。

## 目标
1. 建立规范防区主数据 `sys_zone`（词表取自现有 `BRIGADE_AREA` 字典的 7 个分区）。
2. `sys_user` 增加 `zone_codes`（可访问防区列表，逗号串）。
3. 服务端按 `role.data_scope` + `user.zone_codes` 解析"当前用户可访问防区集合"，在业务查询注入 `IN (...)`。
4. 对**救援队伍域**（`FacBrigadeTeam.area`）接入行级过滤，作为可演示试点。
5. 系统管理前端：用户表单增加"可访问防区"多选配置入口。

## 非目标（本期不做）
- 不对全部 17+ 业务域接入（其余域 area 词表不互通，强行接入会数据失配，比不过滤更糟）。
- 不引入"部门"概念（`DEPT` 本期等同 `SELF`，待未来有部门实体再区分）。
- 不修改 JWT 载荷（`data_scope`/zone 由服务端经 `RoleAuthorityService` + 用户缓存解析，遵循 ADR-3 权限码不写令牌）。

## ADR
- **ADR-1 防区真源**：新建 `sys_zone`，种子取 `BRIGADE_AREA` 的 7 区（炼油区/乙烯区/罐区/仓储区/码头区/芳烃区/特勤保障区）；业务表 `area` 值与之对齐者方可接入。
- **ADR-2 用户绑定**：防区绑定放 `sys_user.zone_codes`（逗号串），不另建关系表（防区数量小、单用户多防区场景有限，逗号串足够且零额外 join）。
- **ADR-3 解析位置**：`data_scope` 解析走服务端缓存（`RoleAuthorityService` 现有 Caffeine + 新增 `userZoneCache`），**不写令牌**，令牌仍只携 `role`。
- **ADR-4 注入方式**：过滤走 `DataScopeHelper.apply(qw, zoneColumn, zones)` 显式调用——各实体 zone 列名不一致，通用 MyBatis 数据权限拦截器不可行；逐域在 Service 列表查询处调用。
- **ADR-5 试点域锁定**：救援队伍域（`FacBrigadeTeam.area` 值＝乙烯区/炼油区/罐区/仓储区/码头区/芳烃区/特勤保障区，与 `BRIGADE_AREA` **完全对齐**）。其余域列入后续路线图。

## 风险
- 救援队伍域是唯一对齐域；未来接入其他域须先将其 `area` 词表归一到 `sys_zone`。
- 现有用户（admin 等）须保证 `data_scope=ALL` 以看全部；新增受限用户须配 `zone_codes`。
- 空 `zone_codes` + `data_scope≠ALL` → 看不到任何行（`1=0` 兜底），符合最小权限原则（无配置即无可见数据）。
- `sys_role.data_scope` 现有种子值（ALL/DEPT/SELF）保持不变；本期 `DEPT` 与 `SELF` 等价处理。

## 与既有 perm 的关系
- 两层叠加：**perm**（菜单/接口级，控制"能不能进救援队伍页/调接口"）＋ **data_scope**（数据行级，控制"看哪些防区的数据"）。
- ADMIN（`data_scope=ALL`）看全部；受限角色用户只看自己 `zone_codes` 内的防区。
