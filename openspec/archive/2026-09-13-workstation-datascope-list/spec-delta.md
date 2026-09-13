# 契约增量（spec-delta）：工作站域防区过滤列表

## 后端契约变更（影响 `frontend-scaffold/docs/api/dashboard.openapi.json` 真源，需四同步）

### 新增端点

- `GET /api/v1/workstations`
  - 描述：工作站/工位防区过滤分页列表（登录可读，复用 data_scope 行级 ABAC，与设备域同约定）。
  - 参数：`page`(默认1) / `size`(默认20) / `zone`(区域模糊) / `online`(是否在线) 可选。
  - 响应：`Result<WorkstationPageResult>`；`WorkstationPageResult { list: Workstation[], total, page, size }`。

### 新增 schema

- `WorkstationPageResult`：`list`(Workstation[] 复用既有 schema) / `total`(long) / `page`(long) / `size`(long)。

### 修改端点

- `GET /api/v1/dashboard/workstations`
  - 描述补注记："返回结果已套防区过滤（data_scope 行级 ABAC），非 ALL 角色仅见其 zone_codes 内工作站"；响应 schema `Workstation` 不变。

### 不受影响

- 既有 `Workstation` schema（沿用，list 元素）。
- 设备域 `DeviceService.page()`、救援队伍域 `brigades()` 既有过滤（本期仅新增工作站域）。
- 硬控名单、B3 包络、鉴权白名单、权限码（未新增）。
- `fac_workstation` 表结构（无迁移）。

## 守门

- `scripts/check-api-contract.mjs --strict`：路由 0 差异 / schema 0 漂移（新增 1 域路径 + 1 schema）。
- 前端 `npm run gen:api-types` 重新生成后 `WorkstationPageResult` 类型 diff 为 0。
