# 变更提案：后端安全配置与工程正确性加固

> 适用：L3 / L4 改动（高风险：生产配置基线、安全过滤器链）。

## Why

`AGENTS.md` 落地后对后端做了一轮体检，发现规范宣称的红线在代码里并未真正成立：

1. **生产默认关闭防重放签名**——只有 `application.yml` 与 `application-dev.yml`，`signature.enabled` 默认 `${SIGNATURE_ENABLED:false}`。按 README 的 `SPRING_PROFILES_ACTIVE=prod` 启动会带签名关闭上线，直接违反 §3.4。
2. **CORS 全开且带凭证**——`allowedOriginPatterns("*")` + `allowCredentials(true)`，等价于任意站点可带 Cookie 调 `/api/**`，违反 §6.3 安全红线。
3. **过滤器顺序不受控**——`HmacFilter` / `JwtFilter` 仅靠 `@Component` 自动注册，无 `@Order`、无 `FilterRegistrationBean`；§6.1 宣称的 1→2→3→4 事实上只有 3/4 显式注册。
4. **分页插件方言写死 PostgreSQL**——dev 跑 H2 时方言错配。
5. **出参用 `Map<String,Object>` 且空库回落 mock**——无类型契约，且属 §1.3 禁止的"桩数据不得算完成"。
6. **零测试**——`src/test` 为空目录，§2.2 起步基线未落。

不做会怎样：生产环境在"看起来已按规范实现"的错觉下带着已知漏洞上线，后续任何契约改动都无回归保护。

## What Changes

- 新增 `src/main/resources/application-prod.yml`：显式 `signature.enabled=true`、去除开发占位、开启 H2 Console 禁用。
- 改 `config/WebMvcConfig.java`：CORS 收敛为可配置白名单（`app.cors.allowed-origins`），`*` 仅保留给 dev。
- 改 `config/SecurityBeans.java`：新增 `FilterRegistrationBean`，显式固定 `HmacFilter`(1) → `JwtFilter`(2) 顺序。
- 改 `config/MybatisPlusConfig.java`：分页方言按实际数据源决定，不再写死 `POSTGRE_SQL`。
- 改 `controller/DeviceController.java` + 新增 `dto/DevicePageResult.java`：出参 DTO 化，移除 `devFallbackList` mock 回落。
- 新增 `src/test/java/**`：Controller / Service / 安全层三层单测。

## Capabilities

### Added Capabilities

- `backend-security-baseline`：生产配置基线（签名开启、CORS 白名单、过滤器顺序显式化）。
- `backend-test-baseline`：后端零依赖单测起步基线（Controller / Service / 安全层）。

### Modified Capabilities

- `device-query`：设备分页与详情查询的出参形态由无类型 Map 改为 `DevicePageResult` DTO，移除 mock 回落。

## Impact

- **受影响范围**：`config/`（WebMvc、SecurityBeans、MybatisPlus）、`controller/DeviceController`、`dto/`、`resources/application-prod.yml`（新增）、`src/test/java`（新增）。
- **契约同步**：`DeviceController` 的 `data` 结构从 `{list,total,page,size,mock?}` 变为 `{list,total,page,size}`（`mock` 字段移除），需同步新建 `frontend-scaffold/docs/api/device.openapi.json`。
- **数据影响**：不涉及 DDL。移除 mock 回落意味着空库时返回空列表（不再编造 12 条假数据），前端需能正确处理空列表。
- **不触碰的边界**：不动 `Result` 包络、不动 `ResultCode` 错误码分段、不动硬控名单、不动 JWT 签发逻辑、不动 WebSocket 推送协议。
- **安全语义**：签名开关默认值变化（prod 强制开启）、CORS 来源收敛、过滤器顺序固定——均属 §8 L4 门禁项，已取得人工确认。
- **回归面**：`scripts/smoke-test.ps1` 第 4 步断言 `total`，空库时由 mock 的 12 条变为 0 条，需同步调整冒烟断言；`check-api-contract.mjs` 不受影响。

## 人工确认关卡（L3 须过 / L4 实施前须过）

- [x] 提案范围与用户确认一致（用户已在多选确认中选择 P0 安全硬伤 / P1 工程正确性 / 补测试基线 / 消化跨库契约债）。
- [x] API 契约（AGENTS.md §3）未违反：零下行控制 / B3 包络 / 20 位 MDM / 防重放签名 / 无状态 JWT / 错误码分段。
- [x] 跨库四同步已排定：device 域契约新建，auth / dashboard 端点补录。
- [x] 数据变更影响与回退方案已确认：不涉及 DDL；mock 移除的回退方式是恢复 `devFallbackList` 调用（单行改动）。
- [x] 高风险项（L4：契约语义 / 权限模型 / 数据库结构 / 安全过滤器链 / 部署配置基线）已明确并取得人工确认。
