# Spec: form-records（流程填报记录写域）

> 后端 `fac_form_record` 的读写能力，供 mgmt `/form` 流程填报向导使用。来源 Change：`openspec/changes/2026-09-22-add-form-records-write/`。

## Requirement: 流程填报记录读写

系统 SHALL 提供 `GET /api/v1/form-records`（列表分页）、`GET /api/v1/form-records/{id}`（详情）、`POST /api/v1/form-records`（新增）、`PUT /api/v1/form-records/{id}`（更新/审核流转）。

### Scenario: 一线人员新增填报

- **WHEN** 任意登录用户提交合法 `formType` + `reporter` + `detailJson`
- **THEN** 系统落库并返回完整记录（status 缺省 SUBMITTED）

### Scenario: 非法填报类型被拒

- **WHEN** 提交不在枚举内的 `formType`
- **THEN** 系统返回参数校验失败（B3 code≠0）

### Scenario: 管理员审核流转

- **WHEN** ADMIN 对记录 `PUT` status=REVIEWED
- **THEN** 系统更新状态；非 ADMIN 调用返回 403

## 约束

- `formType` SHALL 为固定枚举（隐患排查 / 设备巡检 / 值班交接 / 其他）。
- 结构化内容 SHALL 以 `detail_json` 存储。
- `PUT` SHALL 要求 ADMIN 角色。
- 详情未命中 SHALL 返回 B3 NOT_FOUND（HTTP 200 + code≠0）。
