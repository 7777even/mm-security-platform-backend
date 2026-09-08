# Design: 可观测性探针

## 配置（application.yml，common 段，dev/prod 共用）
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
  info:
    env:
      enabled: false
info:
  app:
    name: mm-security-backend
    description: 石化园区安全风险管控平台后端服务
```

## 安全放行
- `JwtFilter.WHITELIST` 增加 `"/actuator"`（`startsWith` 匹配，覆盖 `/actuator/health`、`/actuator/health/liveness`、`/actuator/health/readiness`、`/actuator/info`）。
- `HmacFilter.shouldNotFilter` 增加 `uri.startsWith("/actuator")` —— 即使生产 `signature.enabled=true`，探针也免签名（K8s 探针无法携带 HMAC 头）。

## Info 贡献器
`AppInfoContributor implements InfoContributor`（`@Component`），从 `info.app.*` 注入 name/description/version 到 `/actuator/info`。
> 说明：Spring Boot 默认不暴露 application.yml 中随意写的 `info.*` 键，必须借 `InfoContributor` 显式贡献，否则 `/actuator/info` 返回 `{}`。

## 实体修复
`FacWorkstation.workstationId` 增加 `@TableId(type = IdType.INPUT)`，与 `FacDevice` 主键声明风格一致。

## 验证
- `mvn test` 0 failure（71 case）。
- 真实启动（dev/H2，端口 8099）免鉴权打探针：
  - `GET /actuator/health` → 200 `{"status":"UP","groups":["liveness","readiness"]}`
  - `GET /actuator/health/readiness` → 200 `{"status":"UP"}`
  - `GET /actuator/health/liveness` → 200 `{"status":"UP"}`
  - `GET /actuator/info` → 200 `{"name":"mm-security-backend","description":"...","version":"dev"}`
  - 启动日志无 `FacWorkstation ... @TableId` 警告。
