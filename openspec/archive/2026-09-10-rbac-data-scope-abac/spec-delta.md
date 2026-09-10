# 契约增量（spec-delta）：data_scope 行级 ABAC

## 后端契约变更（影响 `docs/api/system.openapi.json` 真源，需四同步）

### 新增端点
- `GET /api/v1/system/zones`
  - 描述：防区下拉列表（登录可读，无需 ADMIN）。
  - 响应：`Result<ZoneItem[]>`；`ZoneItem { id, zoneCode, zoneName, sortOrder, status }`。

### 修改端点
- `POST /api/v1/system/users`（创建）
- `PUT /api/v1/system/users/{id}`（更新）
  - 请求体 `SystemUserSaveRequest` 增加字段：
    - `zoneCodes: string`（可选，逗号串，如 `炼油区,罐区`；空表示未分派，配合 `data_scope≠ALL` 时看不到任何行）。

### 不受影响
- 既有 `sys_role` 的 `data_scope` 字段（已是枚举，本期不改契约，仅后端解析生效）。
- 其余业务域端点（本期仅救援队伍域接入行级过滤，接口契约不变）。

## 守门
- `scripts/check-api-contract.mjs --strict`：路由 0 差异 / schema 0 漂移（可比 197）。
- 前端 `docs/api/system.openapi.json` 与后端实现经 `npm run gen:api-types` 重新生成后类型 diff 为 0。
