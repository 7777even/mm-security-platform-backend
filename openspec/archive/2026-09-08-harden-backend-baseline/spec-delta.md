# Spec Delta：后端安全配置与工程正确性加固

## ADDED Requirements

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

### Requirement: 后端单测基线

系统须提供不依赖 Spring 上下文、数据库与外部服务的单元测试，覆盖 Controller 参数透传、Service 委托、JWT 与硬控关键路径。

#### Scenario: 回归保护

- **WHEN** 修改 Controller 分页参数、JWT 签发逻辑或硬控名单
- **THEN** 对应单测失败并阻止合入

## MODIFIED Requirements

### Requirement: 设备分页查询出参

设备分页接口的 `data` 须返回与前端 `PageResult` 同构的 DTO，且不再包含任何模拟数据回落。

#### Scenario: 空库查询

- **WHEN** 库中无设备记录时调用 `GET /api/v1/devices`
- **THEN** 返回 `code=0` 且 `data` 为 `{list: [], total: 0, page: <当前页>, size: <每页条数>}`，不含 `mock` 字段

#### Scenario: 分页参数透传

- **WHEN** 以 `?page=2&size=5` 调用
- **THEN** Service 收到 page=2 / size=5，返回 `data.page=2`、`data.size=5`

## REMOVED Requirements

### Requirement: 设备列表 mock 回落

移除理由：`devFallbackList` 在空库时编造 12 条假数据并返回 `mock:true`，违反 AGENTS.md §1.3「Mock、桩服务不得作为业务能力已完成的依据」。演示数据改由 `data.sql` 种子数据提供。

## 关联 Spec

- 目标 spec：`openspec/specs/backend-security-baseline/spec.md`、`openspec/specs/backend-test-baseline/spec.md`（本变更新建 capability）。
- 涉及对外接口 `GET /api/v1/devices`，同步登记前端 `docs/api/device.openapi.json`（新建）。
