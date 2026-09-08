# backend-security-baseline Specification

## Purpose

后端安全配置与工程正确性基线。由 Change `harden-backend-baseline`（已归档）回填。

## Requirements

### Requirement: 生产配置基线

系统须在生产 profile 下强制启用防重放签名、关闭 H2 Console、并在缺少 JWT 密钥环境变量时启动失败。

#### Scenario: 生产 profile 启动

- **WHEN** 以 `SPRING_PROFILES_ACTIVE=prod` 启动且未显式覆盖 `signature.enabled`
- **THEN** 防重放签名校验处于开启状态，缺失 `X-Timestamp` / `X-Nonce` / `X-Signature` 的请求被拒

#### Scenario: 生产缺少 JWT 密钥

- **WHEN** 生产 profile 下未注入 `JWT_SECRET` 环境变量
- **THEN** 应用启动失败，不得回退到内置占位密钥

### Requirement: CORS 来源白名单

系统须只接受白名单来源的跨域请求，不得在生产对任意来源开放并同时允许携带凭证。

#### Scenario: 白名单外来源

- **WHEN** 跨域请求来自 `app.cors.allowed-origins` 之外的站点
- **THEN** 浏览器侧请求被 CORS 策略拒绝

### Requirement: 安全过滤器顺序确定

系统须以显式装配固定 `HmacFilter` → `JwtFilter` 的执行顺序，不得依赖 Spring Boot 自动注册的隐式顺序。

#### Scenario: 签名与鉴权次序

- **WHEN** 一个请求同时缺少签名头与令牌
- **THEN** 先因签名校验失败被拒（`SIGNATURE_INVALID`），而非先进入鉴权流程

### Requirement: 设备分页查询出参

设备分页接口的 `data` 须返回与前端 `PageResult` 同构的 DTO，且不包含任何模拟数据回落。

#### Scenario: 空库查询

- **WHEN** 库中无设备记录时调用 `GET /api/v1/devices`
- **THEN** 返回 `code=0` 且 `data` 为 `{list: [], total: 0, page: <当前页>, size: <每页条数>}`，不含 `mock` 字段

#### Scenario: 分页参数透传

- **WHEN** 以 `?page=2&size=5` 调用
- **THEN** Service 收到 page=2 / size=5，返回 `data.page=2`、`data.size=5`

### Requirement: 无设备列表 mock 回落（已移除）

设备接口不得在空库时编造演示数据；演示数据由种子数据提供，接口层禁止 mock 回落。

#### Scenario: 检查出参

- **WHEN** 检视设备分页实现
- **THEN** 不存在 `devFallbackList` 等空库编造数据的回落路径，也不返回 `mock:true`
