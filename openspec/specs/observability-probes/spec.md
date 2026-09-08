# observability-probes Specification

## Purpose

可观测性探针（Actuator）。由 Change `feat-observability-probes`（已归档）回填。

## Requirements

### Requirement: 健康与元数据探针

系统须通过 Actuator 提供健康探针与应用元数据，且探针路径免鉴权（`JwtFilter` / `HmacFilter` 对 `/actuator` 放行）。

#### Scenario: 聚合健康探针

- **WHEN** `GET /actuator/health`
- **THEN** 返回聚合健康状态，包含 `liveness` / `readiness` groups

#### Scenario: K8s 探针

- **WHEN** `GET /actuator/health/liveness` 或 `GET /actuator/health/readiness`
- **THEN** 分别返回 liveness / readiness 状态，免鉴权

#### Scenario: 应用元数据

- **WHEN** `GET /actuator/info`
- **THEN** 返回应用 name / description / version 元数据

### Requirement: 探针不引入业务契约

探针为 actuator 托管端点，不属于 `/api/v1` 业务路由，不进入前端 OpenAPI 契约。

#### Scenario: 契约边界

- **WHEN** 新增或调整 actuator 探针
- **THEN** 无需变更 `frontend-scaffold/docs/api/*.openapi.json`，也无需跑 `check-api-contract`

> 注：`/actuator/prometheus` 指标端点系 Change 之外交付（feat-observability-probes 明确排除），尚无对应 capability spec，登记于进度台账待回填。
