# Proposal: 流程填报记录写域（fac_form_record）

## Why

解冻 mgmt `/form` 需后端填报存储端点。V66 已建 `fac_form_record` 表与只读列表/详情；本变更补齐新增（`POST`，一线人员可提交）与更新（`PUT`，ADMIN 审核），并将 `content` 升级为 `detail_json` 结构化存储、`formType` 固定枚举，落实四同步中的后端实现闭环。

## What Changes

- V66 三方言迁移：`content VARCHAR(2048)` → `detail_json`（h2/dm8 `LONGVARCHAR`、pg `TEXT`），种子改为 JSON。
- `FacFormRecord` 实体、`FormRecord*` DTO：`content` → `detailJson`。
- `FormRecordService`：`detailJson` 非空校验 + `formType` 枚举校验（隐患排查/设备巡检/值班交接/其他）。
- `FormRecordController`：`POST` 去 `ADMIN`（一线可提交），`PUT` 保留 `ADMIN`。

## Capabilities

### Added Capabilities

- `form-records`：流程填报记录写回能力（新增/审核流转）。

## Impact

- 新域写端点；权限语义：`POST` 任意登录、`PUT` ADMIN。
- 契约真源 `frontend-scaffold/docs/api/form-records.openapi.json` 同步（content→detailJson、formType 枚举、权限描述）。
- `check-api-contract --strict` 据此校验 DTO 字段对拍。
