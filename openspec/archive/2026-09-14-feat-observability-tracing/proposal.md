# Proposal: 分布式链路追踪（OpenTelemetry）

## Why
现状仅有自定义 `common/TraceContext` 用 `UUID` 注入日志 MDC（`[%X{traceId:-j-none}]`）：无 span、无跨服务传播、无上报。排障只能靠单请求日志号，无法下钻 Web→JDBC→HTTP client 调用链。这是**从零建设**，不是"接个接收端"。

## What Changes
引入 OpenTelemetry（Micrometer Tracing 桥接 + OTLP 导出）建立分布式追踪，决策按建议收敛：
- **A 接收端**：OTel Collector 中转；本地/无监控栈用 **Zipkin 单机起步**（自带 UI、零外部依赖），已有 Grafana 栈则切 **Tempo**。应用经 OTLP gRPC `:4317` 上报 Collector。
- **B 对齐**：弃用自定义 `j-xxxx`，统一用 OTel 标准 traceId（MDC 与 `Result.traceId` 一致）；日志格式基本不变。
- **C 采样**：起步**总是采样**（`probability=1.0`），量大切 10%+错误全采（tail-based 需 Collector，后续增强）。

## Capabilities
新增 `observability-tracing` capability：span 自动埋点（Web / JDBC / HTTP client）、Collector 上报、traceId 全链路一致。

## Impact
- 纯后端内部可观测性增强，**不新增业务 API 契约端点**，无需变更前端 OpenAPI、无需跑 `check-api-contract`（同 `feat-observability-probes` 先例）。
- 前端需感知 `Result.traceId` 值格式由 `j-+14位` 变为标准 32 位 hex（字段仍为 `string`，schema 不变）；UI/检索语义同步。
- Collector / Zipkin 为运行时依赖，不在仓库；上报 endpoint 走配置，不硬编码。
