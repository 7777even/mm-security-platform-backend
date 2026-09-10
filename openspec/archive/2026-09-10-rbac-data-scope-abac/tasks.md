# 任务清单：data_scope 行级 ABAC（MVP）

## 1. 数据库迁移（V34，三方言同步）
- [x] `V34__data_scope_abac.sql`（h2）：`CREATE TABLE sys_zone`（id/zone_code/zone_name/sort_order/status/deleted/时间戳）；种子 7 区（炼油区/乙烯区/罐区/仓储区/码头区/芳烃区/特勤保障区，zone_name 与 `BRIGADE_AREA` 一致）；`ALTER sys_user ADD COLUMN zone_codes VARCHAR(512)`。
- [x] postgresql / dameng 同步 V34（dameng 无多行 `VALUES`，逐条或 `FROM dual`；列类型用 `VARCHAR(512)`）。

## 2. 实体与映射
- [x] `SysZone` 实体 + `SysZoneMapper`（含 `selectByNameIn(List<String>)` 用于解析兜底/校验）。
- [x] `SysUser` 增加 `zoneCodes` 字段（`@TableField("zone_codes")`）；`SysUserMapper` 查询按 username。
- [x] `RoleAuthorityService` 暴露 `dataScopeOf(roleCode)`（复用现有缓存，返回 `ALL/DEPT/SELF`）。

## 3. 解析与注入（核心）
- [x] `DataScopeResolver`：`resolveZones(LoginUser) → Set<String> | null`
  - `role.dataScope == ALL` → `null`（不过滤）；
  - 否则解析 `sys_user.zone_codes` 逗号串 → `Set`；带 `userZoneCache`（Caffeine，TTL 5min + 用户 zone 变更时失效）。
- [x] `DataScopeHelper.apply(LambdaQueryWrapper<?> qw, SFunction<?,?> zoneColumn, Set<String> zones)`：
  - `zones == null` → 不加条件（ALL）；
  - `zones` 为空 → `qw.apply("1=0")`（无可见防区）；
  - 否则 → `qw.in(zoneColumn, zones)`。

## 4. 试点接入（救援队伍域）
- [x] 定位救援队伍列表查询 Service（调研指向 `RescueResourceService` / `FacBrigadeTeamMapper`），在构建 `LambdaQueryWrapper<FacBrigadeTeam>` 后注入 `DataScopeHelper.apply(qw, FacBrigadeTeam::getArea, resolver.resolveZones(loginUser))`。
- [x] 实现时验证 `FacBrigadeTeam.area` 取值确与 `sys_zone.zone_name` 对齐（已据种子确认：乙烯区/炼油区/罐区/仓储区/码头区/芳烃区/特勤保障区）。

## 5. 端点与契约（四同步）
- [x] 新增 `GET /system/zones`（防区下拉，登录可读）；`SystemUser` 保存 request 增加 `zoneCodes`。
- [x] 前端契约 `docs/api/system.openapi.json` 同步 + `npm run gen:api-types`。
- [x] 守门 `scripts/check-api-contract.mjs --strict` 路由 0 差异 / schema 0 漂移。

## 6. 前端（用户防区配置）
- [x] `services/system.ts`：增加 zone 类型与 `getZones()` 接口；`SystemUserSaveRequest` 增加 `zoneCodes`。
- [x] 用户表单（`views/system/users.vue`）：新增"可访问防区"多选（从 `/system/zones` 取），保存写入 `zoneCodes`。

## 7. 测试
- [x] `DataScopeResolverTest`：ALL→null；SELF + zoneCodes→集合；空 zoneCodes→空集（经 `userZoneCache`）。
- [x] `DataScopeHelperTest`：null→无附加条件；空→`1=0`；非空→`in`。
- [x] 救援队伍域集成：ADMIN 看全部 / 受限用户只看对应防区（MockMvc + Mockito）。

## 8. 门禁与交付
- [x] 后端 `mvn test` 全绿（基线 434 + 新增）；前端 `vitest run` / `vue-tsc` / `validate-api-contracts.mjs` 通过。
- [x] 提交拆分：后端 `feat(security)` + 前端 `feat(system)`；推送（后端 `main`、前端 `feature/scaffold-rebuild`）；归档 Change。
