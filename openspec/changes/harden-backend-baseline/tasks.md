# 任务清单：后端安全配置与工程正确性加固

> 状态只回填此处，禁止在 engineering/ 另立第二套任务清单（AGENTS.md §4）。

## 1. P0 — 生产配置基线

- [x] 新增 `src/main/resources/application-prod.yml`：`signature.enabled=true`、关闭 H2 Console、`jwt.secret=${JWT_SECRET}` 无默认值、关闭 SQL 日志。
- [x] 改 `config/WebMvcConfig.java`：CORS 由 `allowedOriginPatterns("*")` 改为 `app.cors.allowed-origins` 白名单，列表含 `*` 时才走 pattern 模式。

## 2. P1 — 过滤器顺序

- [x] 去掉 `HmacFilter` / `JwtFilter` 的 `@Component` 自动注册语义，改由 `FilterRegistrationBean` 装配。
- [x] `config/SecurityBeans.java` 新增 `hmacFilterRegistration`（order 最高）与 `jwtFilterRegistration`（order 最高 +10）。
- [x] [TDD] `HardControlInterceptorTest` 覆盖：硬控路径 POST 命中即抛 `HARD_CONTROL_BLOCKED`、非硬控路径放行、GET 一律放行。

## 3. P1 — 分页方言

- [x] 改 `config/MybatisPlusConfig.java`：分页拦截器不再写死 `DbType.POSTGRE_SQL`，改自动判定。

## 4. P1 — 出参 DTO 化与去 mock

- [x] 新增 `dto/DevicePageResult.java`（`list` / `total` / `page` / `size`，与前端 `PageResult` 同构）。
- [x] 改 `controller/DeviceController.java`：返回 `Result<DevicePageResult>`，移除 `devFallbackList` mock 回落分支。
- [x] 在 `data.sql` 补设备种子数据，替代 mock 的演示作用。
- [x] 同步调整 `scripts/smoke-test.ps1` 第 4 步断言（不再假定 12 条假数据）。
- [x] [TDD] `DeviceControllerTest`：分页参数透传、DTO 字段正确、空库返回空列表。

## 5. 测试基线（零依赖单测）

- [x] [TDD] `JwtUtilTest`：签发可解析、过期抛错、篡改签名抛错。
- [x] [TDD] `AuthControllerTest`：登录成功返回 token、口令错误返回业务错误码。
- [x] [TDD] `DeviceServiceTest`：分页委托 Mapper、按 code 查询委托 Mapper。
- [x] `ResultEnvelopeTest`：`Result.ok` / `Result.fail` 包络字段与 traceId。

## 6. 守门验证

- [x] `./mvnw test` 全绿。
- [x] `node scripts/check-api-contract.mjs` 端点无新增漂移。
- [x] 实际命令与结果记入 `engineering/qa/`。

## 验收标准（Definition of Done）

- [x] `tasks.md` 全部勾选，验收标准逐条满足。
- [x] `./mvnw test` 0 failure。
- [x] 契约同步：新建 `frontend-scaffold/docs/api/device.openapi.json` 并反映 `mock` 字段移除。
- [x] 提交按 scope 拆分：`security` / `config` / `device` / `test`。
- [x] QA + Retro 即刻写入 `engineering/`。
