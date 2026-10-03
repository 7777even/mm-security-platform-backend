# Design: 消防设施台账与防火巡查 CRUD（P2 批次 3）

## 端点与权限矩阵

| 方法 | 路径 | 权限码 | 广播域 | 返回 |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/fire-facility/ledger` | `fire-facility:ledger:write` | `fire-facility.ledger` | `FireFacilityLedgerItem` |
| PUT | `/api/v1/fire-facility/ledger/{id}` | `fire-facility:ledger:write` | `fire-facility.ledger` | `FireFacilityLedgerItem` |
| DELETE | `/api/v1/fire-facility/ledger/{id}` | `fire-facility:ledger:write` | `fire-facility.ledger` | `null` |
| POST | `/api/v1/fire/patrols` | `fire:patrol-write` | `fire.patrol-record` | `FirePatrolRecord` |
| PUT | `/api/v1/fire/patrols/{id}` | `fire:patrol-write` | `fire.patrol-record` | `FirePatrolRecord` |
| DELETE | `/api/v1/fire/patrols/{id}` | `fire:patrol-write` | `fire.patrol-record` | `null` |

权限由 `@RequireAuth(perm=...)` 切面拦截，未授权返回 403；V99/V102 三方言种子已把两个按钮级菜单（`fm-fire-ledger-write` / `fm-fire-patrol-record-write`）挂在 `fm-fire` 父菜单下并授权 ADMIN、COMMANDER、SCHEDULER、TEAM_LEADER、INNER_OPER、OUTER_OPER。

## 写请求 DTO 与校验（零下行控制）

- `FireFacilityLedgerWriteRequest`：`facilityCode`/`facilityName`/`facilityType` 在 Service 层判必填（重复编码→B3 CONFLICT）；`location`/`device`/`maintainerName`/`maintainerPhone`/`enabled` 可选。**DTO 不含 `status` 字段**。
- `FirePatrolWriteRequest`：`patrolDate` `@NotBlank(message="巡查日期不能为空")`；`shift`/`dutyPerson`/`patrolCount`/`locations`(List<String>)/`completed`/`workOrderNo` 可选。**DTO 不含 `status` 字段，不含检查项结果**。
- Controller 侧 `@Valid` 触发校验，失败由全局异常处理器转 B3 `code=400`，不进入 Service。

## 写语义

- **新增台账**：DTO → 实体全字段落库（不含 status），`sort_no` 接续现有最大值（`LedgerIdSupport.nextSortNo` 范式，规避序列滞后）；返回落库后的 `FireFacilityLedgerItem`（含服务端生成主键）。
- **更新台账**：read-modify-write——先按 `id` 查出实体，仅覆盖 DTO 非空字段（status 不在白名单），返回更新后条目。
- **删除台账**：级联清理 `fac_fire_facility_maintenance.ledger_id` 后物理删除，不存在按 404 处理。
- **巡查新增/更新/删除**：同理；`fac_fire_patrol` 走 `@Version` 乐观锁，并发冲突返回 409。

## 实时广播

写方法统一标注 `@RealtimeSync(domain=...)`：台账三写 `fire-facility.ledger`、巡查三写 `fire.patrol-record`。void 写方法（delete）也必须标注——否则 WS 不广播、前端不刷新（沿用 `FireFacilityServiceTest` 的「void 写方法仍广播」断言范式）。

## 契约真源与守门

契约机器可读真源在前端 `docs/api/fire-facility.openapi.json` / `docs/api/fire-monitoring.openapi.json`，后端不复制第二份 OpenAPI。同 path 多 method 合并到同一 path key；新增 `FireFacilityLedgerWriteRequest` / `FirePatrolWriteRequest` 两个 schema 与后端 DTO 同名同字段（`status` 不在 schema 内）。守门：`node scripts/check-api-contract.mjs --strict` 比对「Controller (method, path)」与「具名 DTO 字段 + 类型族」，schema 漂移 0。

## 迁移与多方言

`V99__fire_facility_ledger_crud_perm.sql` 在 h2 / postgresql / dameng 三方言同名同号：按钮菜单 upsert（按 `code` 幂等）+ `sys_role_menu` 授权。达梦方言 `version` 用 `NUMBER(19)`；`V102` 另含 `ALTER TABLE fac_fire_patrol ADD COLUMN version BIGINT DEFAULT 0`。
