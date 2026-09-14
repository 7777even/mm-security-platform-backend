# Design: 分布式链路追踪（OpenTelemetry）

## ADR-1 规范 / 埋点方式：Micrometer Tracing 桥接 OTel（非 javaagent）
- 选 `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-otlp`，由 Spring Boot 3 自动配置托管。
- 理由：配置式（改 `application.yml` 即可）、自动把 `traceId/spanId` 注入 MDC（key 默认 `traceId`）、免运维改 `-javaagent` 启动参数；比 javaagent 在 filter 链与 MDC 时序上更可控。
- 备选 javaagent：零代码但 MDC 注入依赖 instrumentation 库、启动参数在运维侧，否决。

## ADR-2 接收端：Collector 中转 + Zipkin 起步 / Tempo 备选
- 应用只认 OTLP gRPC `:4317`（Collector），不直接连存储，切换存储零改应用。
- 本地/无栈：Collector `zipkin` exporter → `http://zipkin:9411`（Zipkin 自带 UI + 内存存储，最快看到东西）。
- 有 Grafana 栈：Collector `otlp` exporter → Tempo（配 Loki+Prometheus 做 logs/metrics/traces 关联）。

## ADR-3 traceId 对齐：弃用 `j-xxxx`
- 现 `JwtFilter` 在请求入口 `TraceContext.init()`（生成 `j+14位hex`）→ finally `clear()`，是"请求级 MDC 注入"模式。
- 改造：
  1. **新增 `TracingFilter`（`order=HIGHEST_PRECEDENCE`，与 `CorsFilter` 同优先级、确定性早于 `HmacFilter(+1)`/`JwtFilter(+10)`）**：起 root span + 注入 MDC + finally `clear()`；若当前已有活动 span（兜底）则直接复用，避免分裂成两条 trace。
  2. **移除 `JwtFilter` 的 `TraceContext.init()/clear()` 调用**，其日志/响应只 `TraceContext.get()` 读取。
  3. **`TraceContext` 改为纯读取封装**：`get()` 读 MDC `traceId`，有则返（标准 32 位 hex），无则 `j-none`（fallback，保留兼容）。
  4. `Result.java` 不动（仍 `TraceContext.get()`）；logback pattern `[%X{traceId:-j-none}]` 不变（值由 `j-xxxx` 变标准 hex）。
- 必须 TracingFilter 先于 JwtFilter，否则 JwtFilter 处 MDC 无 traceId，日志/响应与下游 span 不一致。

## ADR-4 采样：always 起步
- `management.tracing.sampling.probability=1.0`；注释说明量大切 `0.1` + 错误全采（tail-based 需 Collector `tail_sampling`，后续增强）。

## 依赖（pom.xml）
```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
```
（版本由 Spring Boot 父 POM 托管，无漂移风险；与现有 `io.micrometer` 坐标一致。）

## 配置（application.yml，common 段，dev/prod 共用）
```yaml
management:
  tracing:
    sampling:
      probability: 1.0          # C：起步总是采样；量大改 0.1
    otel:
      enabled: true
  otlp:
    tracing:
      endpoint: http://localhost:4317   # A：OTLP gRPC，app → Collector；生产改内网地址
otel:
  service.name: mm-security-backend
```

## Collector 配置（本地冒烟，不在仓库内）
`otel-collector-config.yaml` 片段：
```yaml
receivers:
  otlp:
    protocols: { grpc: { endpoint: 0.0.0.0:4317 } }
processors:
  batch: {}
exporters:
  zipkin:                       # 本地/无栈起步
    endpoint: http://zipkin:9411/api/v2/spans
  # tempo:                      # 有 Grafana 栈时切换
  #   endpoint: http://tempo:4317
service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [zipkin]       # 或 [tempo]
```

## 验证
- `mvn test` 0 failure。
- 本地起 `otel-collector`（`:4317`）+ `zipkin`（`openzipkin/zipkin` 容器或 jar）；启动后端（dev/H2），调 `GET /api/v1/...`：
  - Zipkin UI 见对应 trace（含 Web / JDBC / 下游 HTTP client span）。
  - 服务端日志 `[%X{traceId}]` 与 Zipkin 该 trace 的 traceId 一致；`Result.traceId` 同值。

## 风险
- **filter 顺序**：TracingFilter 须最高优先级，否则 JwtFilter 日志/响应 traceId 不一致（已在 ADR-3 约束）。
- **采样 1.0 存储**：明确"起步档、可切"，避免量上来后 Zipkin 内存爆。
- **运行时依赖不在仓库**：Collector/Zipkin 由运维按环境部署；endpoint 走配置，不硬编码。
- **前端感知**：`Result.traceId` 格式变化（j-14位 → 32位 hex），前端 UI/检索语义需同步（不属 OpenAPI schema 变更）。
