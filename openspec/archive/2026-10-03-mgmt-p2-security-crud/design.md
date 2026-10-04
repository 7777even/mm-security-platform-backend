# Design: 安防人员·车辆检索与周界告警 CRUD

## 端点与权限矩阵

| 方法 | 路径 | 权限码 | 广播域 | 返回 |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/security/search/person` | `security:person-write` | `security.person-search` | `PersonSearchDetail` |
| PUT | `/api/v1/security/search/person/{id}` | `security:person-write` | `security.person-search` | `PersonSearchDetail` |
| DELETE | `/api/v1/security/search/person/{id}` | `security:person-write` | `security.person-search` | `null` |
| POST | `/api/v1/security/search/vehicle` | `security:vehicle-write` | `security.vehicle-search` | `VehicleSearchDetail` |
| PUT | `/api/v1/security/search/vehicle/{id}` | `security:vehicle-write` | `security.vehicle-search` | `VehicleSearchDetail` |
| DELETE | `/api/v1/security/search/vehicle/{id}` | `security:vehicle-write` | `security.vehicle-search` | `null` |
| DELETE | `/api/v1/security/perimeter-alarms/{id}` | `security:perimeter-delete` | `security.perimeter-alarm` | `null` |

权限由 `@RequireAuth(perm=...)` 切面拦截，未授权返回 403；V100 三方言种子已把三个按钮级菜单
（`fm-security-person-write` / `fm-security-vehicle-write` / `fm-security-perimeter-delete`，
sort_order 132/133/134）挂在 `fm-security` 父菜单下并授权 ADMIN 及岗位角色。

## 写请求 DTO 与校验

- `PersonSearchWriteRequest`（13 字段，全 `String`）：`name` 标 `@NotBlank(message="姓名不能为空")`，
  其余 `gate/status/date/gender/phone/company/idNumber/appointmentNo/appointmentTime/visitPurpose/
  specialOperation/operationArea` 可空。
- `VehicleSearchWriteRequest`（15 字段）：`plate` 标 `@NotBlank(message="车牌号不能为空")`；
  `confidence` 为 `Integer`（可空，识别失败无置信度），其余为 `String`。
- Controller 侧 `@Valid` 触发校验，失败由全局异常处理器转 B3 `code=400`，**不进入 Service**。

## 写语义

- **新增**：DTO → 实体全字段落库，`status` 不传留空由前端保证，`version` 由 JPA `@Version` 置 0；
  返回落库后的 `PersonSearchDetail` / `VehicleSearchDetail`（含服务端生成主键），供前端免重拉回填。
- **更新**：read-modify-write——先按 `id` 查出实体，再把 DTO 全字段覆盖（未传字段即置空，
  故前端编辑必须**先详情回显再提交**），`@Version` 冲突抛乐观锁异常转 409。
- **删除**：真删除（物理删除），不存在按 404 处理；`deletePerimeterAlarm` 与消防报警删除同范式。

## 实时广播

写方法（void 或返回实体）统一标注 `@RealtimeSync(domain=...)`：人员三写 `security.person-search`、
车辆三写 `security.vehicle-search`、周界删除 `security.perimeter-alarm`。
与既有 `fire-alarm.alarm` 同理，void 写方法（delete）也必须标注解——否则 WS 不广播、前端不刷新
（`FireFacilityServiceTest` 已对「void 写方法仍广播」做过回归锁定，本次沿用该断言范式）。

## 契约真源与守门

契约机器可读真源在前端 `docs/api/security.openapi.json`，**后端不复制第二份 OpenAPI**。
同 path 多 method 合并到同一个 path key（如 `/security/search/person/{id}` 下 get/put/delete），
新增两个 schema 与后端 DTO 同名同字段。守门：`node scripts/check-api-contract.mjs --strict`
比对「Controller (method, path)」与「具名 DTO 字段 + 类型族」，目标路由差异 0 / schema 漂移 0。

## 迁移与多方言

`V100__security_search_crud_perm.sql` 在 h2 / postgresql / dameng 三方言同名同号：
按钮菜单 upsert（按 `code` 幂等）+ `ALTER TABLE ... ADD COLUMN version BIGINT DEFAULT 0`。
达梦方言注意 `ALTER TABLE ... ADD` 不支持 `IF NOT EXISTS` 组合写法，按既有 V 脚本段落终结符规范收尾。
