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

### Requirement: Prometheus 指标端点

系统须通过 Actuator 暴露 `/actuator/prometheus` 指标端点，供监控系统抓取 JVM / HTTP / 业务指标；该端点免 JWT/免签名（`JwtFilter`/`HmacFilter` 对 `/actuator/**` 放行），生产须限制在内网/监控网段访问，不得直接暴露公网。

#### Scenario: 指标抓取

- **WHEN** 监控系统 `GET /actuator/prometheus`
- **THEN** 返回 Prometheus 文本格式指标（含 JVM / HTTP / 业务指标），无需携带令牌

#### Scenario: 公网隔离

- **WHEN** 从公网直连 `/actuator/prometheus`
- **THEN** 由网关/网络安全策略拒绝（端点本身仅做鉴权豁免，不负责公网暴露）

> 注：`/actuator/prometheus` 由 `management.endpoints.web.exposure.include` 暴露，鉴权豁免与内网访问限制见 `src/main/resources/application.yml`；该端点不属于 `/api/v1` 业务路由，不进入前端 OpenAPI 契约。
