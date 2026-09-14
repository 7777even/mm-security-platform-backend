# Tasks: 分布式链路追踪（OpenTelemetry）

- [x] pom.xml 引入 `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-otlp`（版本由 Spring Boot 3.2.5 父 POM 托管）
- [x] application.yml 增加 `management.tracing.enabled=true` + `management.tracing.sampling.probability=1.0` + `management.otlp.tracing.endpoint=http://localhost:4317`（3.2.x 用 `management.otlp.tracing.endpoint`；service.name 取 `spring.application.name`）
- [x] 新增 `TracingFilter`（`order=HIGHEST_PRECEDENCE`，与 CorsFilter 同优先级、确定性早于 HmacFilter(+1)/JwtFilter(+10)）：起 root span + MDC 注入 + finally 清理；已有活动 span 则复用防双 trace
- [x] 移除 `JwtFilter` 的 `TraceContext.init()` / `clear()` 调用；`TraceContext` 改为纯读取封装（`get()` 读 MDC `traceId`，fallback `j-none`）；`Result.java` 不动
- [x] [TDD] 新增 `TraceContextTest`：有 MDC `traceId` 时 `get()` 返回之、无 MDC 返回 `j-none`
- [x] mvn test 0 failure（599 通过，jacoco 达标，BUILD SUCCESS @2026-09-14）
- [x] 本地端到端冒烟：沙箱无 docker，无法起 `otel-collector`+`zipkin`；改为交付部署文档 `backend-scaffold/docs/observability-tracing-deploy.md`（含 docker-compose + 验证步骤），端到端由运维/流水线在有容器环境完成
- [x] 更新 `docs/system-facts.md` 链路追踪段（决策 A/B/C + endpoint 配置约定）

> 冒烟说明：沙箱无 docker，无法在本地起 otel-collector + zipkin 做端到端验证。代码改动 + `mvn test` 门禁已可执行；
> 端到端（Zipkin UI 见 trace、日志/Result.traceId 与 Zipkin 一致）留待有 docker/容器的环境由运维或部署流水线完成，
> 部署配置见 `docs/observability-tracing-deploy.md`（含 docker-compose + 验证步骤）。前端 `Result.traceId` 字段仍为 `string`，仅值格式变化
> （`j-14位` → 32 位 hex），不属 OpenAPI schema 变更，但前端 UI/检索语义需同步。
