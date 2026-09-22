# 设计文档：流程填报记录写域（fac_form_record）

## 目标与约束

- 提供 `POST/PUT /api/v1/form-records`，与既有只读列表/详情组成完整 CRUD。
- `detail_json` 结构化存储（对齐 V27 `fac_emergency_cmd` 先例）；`formType` 固定枚举。

## 架构与方案

- 实体 `FacFormRecord.detailJson` 经 MyBatis-Plus 默认 camelCase→snake 映射 `detail_json`；`@Version` 乐观锁保留。
- Service 层 `VALID_FORM_TYPE` 集合强校验枚举；`detailJson` 非空即视为必填。
- Controller 类级 `@RequireAuth`；`POST` 放开（一线人员提交），`PUT` 加 `@RequireAuth(role="ADMIN")`（审核）。

## 决策记录（ADR）

- 决策 1：`detail_json` 而非 `content` 单文本 — 理由：向导多类型结构不同，需结构化。
- 决策 2：`POST` 去 ADMIN — 理由：一线人员需能自主填报，否则功能失效；审核收口在 `PUT` ADMIN。

## 风险与缓解

| 风险 | 影响 | 缓解 |
| --- | --- | --- |
| PG/DM 迁移未实跑 | 方言语法风险 | 按 V27 先例 `LONGVARCHAR`/`TEXT`，上环境复核 |
| 字段改名破坏契约对拍 | `check-api-contract` 失败 | 后端 DTO 字段名同步改 `detailJson` |

## 依赖

- 下游：前端 `form-wizard.vue` 调本端点。
- 契约：`frontend-scaffold/docs/api/form-records.openapi.json`。
