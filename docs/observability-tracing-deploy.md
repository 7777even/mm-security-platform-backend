# 链路追踪部署与验证指南（OpenTelemetry）

> 配套 openspec change：`2026-09-14-feat-observability-tracing`。
> 本仓库**只含应用侧代码 + OTLP 配置**；Collector / 后端存储(Zipkin / Tempo)由运维按环境部署，
> 应用只认 OTLP gRPC `:4317`，切换存储**零改应用**。

## 1. 架构

```
应用(Spring Boot, mm-security-backend)
   │  OTLP gRPC :4317  (management.otlp.tracing.endpoint)
   ▼
OTel Collector  (批处理 / 采样 / 导出，运维部署)
   │
   ├── zipkin exporter → Zipkin (本地/无栈起步, 自带 UI + 内存存储)
   └── tempo  exporter → Tempo  (有 Grafana 栈时, 配 Loki+Prometheus 做 logs/metrics/traces)
```

- 应用侧已落地：`TracingFilter`（order=HIGHEST_PRECEDENCE，先于 JwtFilter）起 root span + 注入 MDC `traceId`
  （标准 32 位 hex，对齐 OTel，弃用旧 `j-xxxx`）；`management.tracing.sampling.probability=1.0`（起步总是采）。
- 切换后端存储只需改 Collector 的 `exporters`，**不改应用**。

## 2. 本地/无栈起步：Collector + Zipkin（docker-compose）

已抽出为本仓库 `deploy/docker-compose.tracing.yml` 与 `deploy/otel-collector-config.yaml`（均经 YAML 校验，不入构建，供运维 `docker compose` 直接拉起）：

`deploy/docker-compose.tracing.yml`：

```yaml
version: "3.8"
services:
  otel-collector:
    image: otel/opentelemetry-collector:0.102.1
    command: ["--config=/etc/otel/config.yaml"]
    volumes:
      - ./otel-collector-config.yaml:/etc/otel/config.yaml:ro
    ports:
      - "4317:4317"     # OTLP gRPC（应用导出目标）
    depends_on:
      - zipkin

  zipkin:
    image: openzipkin/zipkin:3.4.1
    ports:
      - "9411:9411"     # Zipkin UI: http://localhost:9411
```

`deploy/otel-collector-config.yaml`：

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
processors:
  batch: {}
exporters:
  # 本地/无栈起步：Zipkin 自带 UI + 内存存储
  zipkin:
    endpoint: http://zipkin:9411/api/v2/spans
  # 有 Grafana 栈时切换为 Tempo（注释掉 zipkin 即可）：
  # tempo:
  #   endpoint: http://tempo:4317
service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [zipkin]   # 或 [tempo]
```

启动：`docker compose -f deploy/docker-compose.tracing.yml up -d`（compose 文件同目录已含 `otel-collector-config.yaml`，相对挂载生效）

## 3. 有 Grafana 栈：Collector + Tempo

- Collector `exporters.tempo.endpoint: http://tempo:4317`（OTLP 或 Jaeger 协议均可）。
- Tempo 与现有 Loki(日志) / Prometheus(指标) 同置，Grafana 内做 trace↔log↔metric 关联。
- 应用侧**无需改动**。

## 4. 验证（端到端）

前置：Collector(:4317) + Zipkin(:9411) 已起；应用以 dev/H2 启动（`spring.profiles.active=dev`）。

1. 调一个需鉴权的端点（JwtFilter 会带 traceId 进响应）：
   ```bash
   curl -s -m4 -H "Authorization: Bearer <access_token>" http://localhost:8080/api/v1/devices \
     | python -c "import sys,json; print('Result.traceId =', json.load(sys.stdin).get('traceId'))"
   ```
   预期：`Result.traceId` 为 32 位 hex（如 `4bf92f3577b34da6a3ce929d0e0e4736`），不再是 `j-none`/`j-xxxx`。
2. 打开 Zipkin UI `http://localhost:9411` → 按 service=`mm-security-backend` 查最近 trace，
   应能看到含 **Web(MVC)** / **JDBC** / 下游 HTTP client 的 span 树。
3. 对齐校验：同一请求的**服务端日志** `[%X{traceId}]` 值 == `Result.traceId` == Zipkin 该 trace 的 traceId。
   三者一致即证明「日志 / 响应 / 追踪系统」链路打通。

## 5. 常见问题

- **`Result.traceId` 仍是 `j-none`**：说明 TracingFilter 未注入 MDC。检查
  (a) `management.tracing.enabled=true` 且 `micrometer-tracing-bridge-otel` 依赖在 classpath；
  (b) TracingFilter 顺序确实早于 JwtFilter（`SecurityBeans.tracingFilterRegistration` order=HIGHEST_PRECEDENCE）。
- **Zipkin 看不到 trace**：Collector 是否起、`:4317` 是否通、`exporters` 是否挂到 zipkin；
  应用日志应无 `OTLP` 导出报错（偶发 `UNAVAILABLE` 说明 Collector 未就绪，属瞬时）。
- **采样量太大**：把 `management.tracing.sampling.probability` 从 `1.0` 调到 `0.1`；
  需「错误全采」时由 Collector `tail_sampling` processor 实现（后续增强，不在本 change）。
- **生产 endpoint**：`management.otlp.tracing.endpoint` 改为内网 Collector 地址（如 `http://otel-collector.monitor:4317`），
  不要直连公网。

## 6. 前端感知

`Result.traceId` 字段仍是 `string`，仅**值格式变化**（`j-14位` → 32 位 hex），不属 OpenAPI schema 变更。
前端若对 traceId 做了格式假设/检索，需同步：统一按 32 位 hex 处理（或兼容 `j-none` 回落）。
