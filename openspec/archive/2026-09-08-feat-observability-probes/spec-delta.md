# Spec Delta: 可观测性探针

> 本变更为运维/可观测性增强，**不新增业务 API 契约端点**，故无前端 OpenAPI 契约变更、无需跑 `check-api-contract`。

## 新增端点（actuator 托管，非业务路由）
| 路径 | 鉴权 | 说明 |
|---|---|---|
| `/actuator/health` | 免（白名单） | 聚合健康，含 groups: liveness/readiness |
| `/actuator/health/liveness` | 免 | K8s liveness 探针 |
| `/actuator/health/readiness` | 免 | K8s readiness 探针 |
| `/actuator/info` | 免 | 应用元数据（name/description/version） |

## 配置增量
- `pom.xml`：`spring-boot-starter-actuator`
- `application.yml`：`management.*` + `info.app.*`
- `JwtFilter` / `HmacFilter`：`/actuator` 放行

## 实体增量
- `FacWorkstation.workstationId`：`@TableId(type = IdType.INPUT)`
