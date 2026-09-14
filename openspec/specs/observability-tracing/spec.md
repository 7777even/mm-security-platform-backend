# observability-tracing Specification

## Purpose

分布式链路追踪（OpenTelemetry）。由 Change `feat-observability-tracing`（进行中）回填。建立 Web / JDBC / HTTP client 调用链的 span 自动埋点与 Collector 上报，并统一 traceId 使日志 MDC、响应包络、追踪后端三者一致。

## Requirements

### Requirement: 分布式追踪埋点与上报

系统须通过 OpenTelemetry（Micrometer Tracing 桥接）自动埋点 Web / JDBC / HTTP client span，并经 OTLP gRPC 上报 OTel Collector。

#### Scenario: 自动埋点

- **WHEN** 处理一次 HTTP 请求或发起下游 HTTP/JDBC 调用
- **THEN** 自动生成 span（含 traceId/spanId/duration/operation），无需业务代码埋点

#### Scenario: OTLP 上报

- **WHEN** span 结束
- **THEN** 经 `management.otlp.tracing.endpoint`（默认 `http://localhost:4317`）上报 Collector；存储由 Collector 导出（本地 Zipkin / 有栈 Tempo），应用不直连存储

### Requirement: traceId 全链路一致

系统须统一使用 OTel 标准 traceId，弃用自定义 `j-xxxx`，使日志 MDC、响应包络、追踪后端三者一致。

#### Scenario: 日志与响应一致

- **WHEN** 请求处理中记录日志或构造 `Result`
- **THEN** 日志 `[%X{traceId}]` 与 `Result.traceId` 均为同一标准 32 位 hex，与 Zipkin/Tempo 中 traceId 一致

#### Scenario: 无上下文 fallback

- **WHEN** 非请求上下文（如启动期、定时任务外）
- **THEN** `TraceContext.get()` 返回 `j-none`，不抛 NPE

### Requirement: 不引入业务契约

链路追踪为内部可观测性，不属于 `/api/v1` 业务路由，不进入前端 OpenAPI 契约。

#### Scenario: 契约边界

- **WHEN** 新增或调整 tracing 埋点/上报
- **THEN** 无需变更 `frontend-scaffold/docs/api/*.openapi.json`，也无需跑 `check-api-contract`
