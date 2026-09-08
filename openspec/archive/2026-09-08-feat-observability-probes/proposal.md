# Proposal: 可观测性探针 + 存量修复

## 背景
真实启动冒烟（dev/H2 + Bearer token）暴露两个存量问题：
1. `/actuator/health` 返回 500 —— 项目从未引入 `spring-boot-starter-actuator`，actuator 端点无映射，`DispatcherServlet` 抛 `NoHandlerFoundException`。K8s 的 liveness/readiness 探针因此完全不可用。
2. 启动日志持续告警 `FacWorkstation did not find @TableId` —— 实体主键未标注，MyBatis-Plus 退化为按字段名兜底，属噪声且隐患。

另：既有自定义 `/api/v1/health`（B3 包络）保留不动，与 actuator 探针互补。

## 目标
- 引入 actuator，暴露 `health`/`info` 及 `liveness`/`readiness` 子探针，供 K8s 探针与运维观测。
- 探针路径 `/actuator/**` 在 `JwtFilter`/`HmacFilter` 白名单放行，免 JWT、免 HMAC 签名（探针无法携带签名头）。
- `FacWorkstation` 补 `@TableId(type = IdType.INPUT)`（业务侧显式赋值，非自增），消除警告。
- `/actuator/info` 通过轻量 `InfoContributor` 暴露应用标识，而非空 `{}`。

## 非目标
- 不引入 Prometheus/Metrics 埋点（后续可观测性增强，不在本变更）。
- 不改动生产库选型（达梦 vs PostgreSQL 仍为延迟决策）。

## 风险
- `show-details=when_authorized`：匿名探针仅拿到 `UP/DOWN` 状态，已鉴权可看组件明细；不泄露数据源等内部细节。
- actuator 依赖为 Spring Boot 托管版本，无版本漂移风险。
