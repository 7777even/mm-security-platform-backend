# Design: perimeter-alarm-create

## 1. 权限码（V76，三方言）
照抄 V70 的 `security:perimeter-ack` 种子范式，新增：
```sql
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '周界告警录入', 'fm-security-perimeter-create', NULL, NULL, 131, 1, 0, 'BUTTON', 'security:perimeter-create', 0
FROM sys_menu p WHERE p.code = 'fm-security';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN','COMMANDER','SCHEDULER','TEAM_LEADER','INNER_OPER','OUTER_OPER')
  AND m.code = 'fm-security-perimeter-create';
```
- `sort_order=131`（避开 ack 的 130）。三方言文件逐行一致（INSERT...SELECT 形式，DM8/PG 通用）。

## 2. 请求体 DTO `PerimeterAlarmCreateRequest`
字段（与「最小可用」对齐，schema 名须与契约同名以便守门脚本对拍）：
| 字段 | 类型 | 必填 | 默认/说明 |
|---|---|---|---|
| title | String | 是 | 告警标题，非空校验 |
| alarmType | String | 否 | 默认 `周界入侵告警` |
| levelCode | String | 否 | 等级（如 一级/二级/三级/四级），允许任意字符串 |
| location | String | 否 | 位置 |
| alarmTime | String | 否 | 发生时间 `yyyy-MM-dd HH:mm:ss`，默认当前时刻 |
| description | String | 否 | 说明 |
| objectName | String | 否 | 入侵对象名 |
| objectType | String | 否 | 入侵对象类型 |
| intrusionPosition | String | 否 | 入侵位置 |
| intrusionMethod | String | 否 | 入侵方式 |
| relatedCamera | String | 否 | 关联摄像机 |

> 为保持「最小可用」，仅 `title` 做非空校验；其余为空时后端填充合理默认值，不抛错。

## 3. `SecurityService.createPerimeterAlarm`
- 标注 `@RealtimeSync(domain = "security.perimeter-alarm")`。
- 构造 `FacPerimeterAlarm`：
  - `alarmCode = "PA-" + yyyyMMdd + "-" + HHmmss`（LocalDateTime 格式化）。
  - `status = "未确认"`、`falseAlarm = "未核实"`、`source = "人工录入"`、`version = 0`。
  - `title` 必填（空则 `BusinessException(PARAM_INVALID)`）。
  - `alarmTime` 缺省取 `LocalDateTime.now()` 格式化 `yyyy-MM-dd HH:mm:ss`。
  - `alarmType` 缺省 `周界入侵告警`。
  - 其余字段透传请求体（可为 null）。
- `perimeterAlarmMapper.insert(e)`。
- 返回 `toPerimeterAlarmDetail(e)`（复用既有映射，含 snapshotPath 空串）。

## 4. Controller
```java
@PostMapping("/security/perimeter-alarms")
@RequireAuth(perm = "security:perimeter-create")
public Result<PerimeterAlarmDetail> createPerimeterAlarm(@RequestBody PerimeterAlarmCreateRequest req) {
    return Result.ok(securityService.createPerimeterAlarm(req));
}
```
- 校验失败（@Valid 或 GlobalExceptionHandler）收敛为 **B3 包络（HTTP 200 + code!=0）**，非 400。

## 5. 前端接线
- 契约 `security.openapi.json` 新增 `POST /security/perimeter-alarms`，requestBody `#/components/schemas/PerimeterAlarmCreateRequest`，responses 200 `allOf [ApiResponse, {data: PerimeterAlarmDetail}]`。
- `services/security.ts` 加 `PerimeterAlarmCreatePayload` + `createPerimeterAlarm(payload)`（范式对齐 `updatePerimeterAlarm`）。
- `SecurityStatusPanel.vue`：顶栏加「新增治安报警」按钮（`v-permission="'security:perimeter-create'"`）+ `el-dialog`/`Dialog` 表单（最小可用字段）。提交成功：`touchPerimeterAlarmChanged()` 即时重拉；广播由既有订阅兜底。

## 6. 守门 / 验证
- 后端 `check-api-contract.mjs --strict`（0 差异）、`check-endpoint-authz.mjs`（新端点带 perm，0 违规）。
- 前端 `validate-api-contracts.mjs`、`vue-tsc`、`vitest`、`gate:screen`。
- 隔离实例（:8899 + mem 库）curl 验证：创建成功 200+data、未鉴权 401、无权限 403。
