# 测试策略（Test Strategy）

> 后端测试基线。改动代码后必须跑对应验证矩阵行（见 `AGENTS.md §2`）。CI 门禁：单测全绿 + JaCoCo 行覆盖 ≥ 0.80。

## 1. 基线事实

- **总规模**：**155 个单测全绿**（Maven `test` 阶段，`BUILD SUCCESS`）。
- **风格**：**standalone MockMvc + 纯 Mockito**，**不起 Spring 上下文**（无 `@SpringBootTest` 跑全量业务）——保证快、确定、无 DB 依赖。
- **覆盖率门禁**：JaCoCo `jacoco-maven-plugin` 绑 `test` 阶段，`check` 规则只卡**行覆盖率（LINE）0.80**（实测 87.6%）。**不卡指令 / 分支覆盖**——Lombok `@Data` 生成的 equals/hashCode/toString 字节码多、源码仅 1 行常未触达，卡指令/分支会开局即红。
- **契约守门**：`node scripts/check-api-contract.mjs --strict` 路由 0 漂移 / 具名 DTO schema 0 漂移（CI contract-guard job 跨仓拉前端 `docs/api`）。

## 2. 测试分层与代表

| 层 | 代表测试 | 覆盖点 |
| -- | -------- | ------ |
| 安全 | `CorsConfigTest`(7) / `SecurityBeansTest`(4) / `JwtFilterTest` / `AuthorizationServiceTest`(6) | CORS fail-fast（含空源退化）、密钥强度校验、令牌解析、RBAC 四层 |
| 鉴权 | `AuthControllerTest` / `AuthServiceTest` / `AuthServiceMenuContractTest` | login/refresh(Cookie)/logout、me 读 sys_user 真实数据、menus RBAC 过滤 5 个 fm-* |
| 报警 | `AlarmControllerTest` / `AlarmWebSocketHandlerTest`(2) | CRUD + ACK_FLOW、WS 推送 `{topic,payload}` 包络 |
| 应急 | `EmergencyServiceTest`(5 mapper) | 4 参考表由硬编码迁 DB 读真数据 |
| 回传 | `UplinkServiceTest`(4) | 落库 fac_field_report、reporter 服务端覆盖、越权守门 |
| 集成契约 | `IntegrationContractTest`(5) | 真实 CorsFilter→HmacFilter→JwtFilter 串接、401 带 CORS、白名单放行 |
| 端到端 | `EndToEndFlowTest`(7, `@SpringBootTest @ActiveProfiles("dev")`) | 登录→admin 受保护 200 / 缺 token 401+CORS / viewer 建警 403 / 伪报 reporter 403 / 凭 Cookie 续期 / 登出清 Cookie |

> 注：`EndToEndFlowTest` 是唯一起 Spring 上下文的测试（dev=H2 内存库），用于锁死跨层行为；其余全 standalone。

## 3. 运行

```bash
# 全量单测 + 覆盖率校验（CI 同款）
mvn -s ci-settings.xml test

# 单文件（验证矩阵对应行）
mvn -s ci-settings.xml test -Dtest=AlarmWebSocketHandlerTest

# 跨仓契约对拍（需前端仓 docs/api；CI 已显式 --contracts 指定）
node scripts/check-api-contract.mjs --contracts ../frontend-scaffold/docs/api --strict
```

## 4. 新增测试的纪律

- 默认走 standalone MockMvc + Mockito；确需跨层链路才用 `@SpringBootTest @ActiveProfiles("dev")`（仅 `EndToEndFlowTest` 先例）。
- 鉴权/越权行为变更，**必须**补或改对应测试锁死（如 refresh 改 Cookie 调用、reporter 服务端覆盖）。
- 删除被测试类（如 `HealthController`）须同步删除/改写引用它的测试，避免悬空断言。
- 覆盖率跌穿 0.80 即 CI 红；删未使用私有 helper 前确认其有 0 覆盖会拖累门禁（如 emergency 4 个旧 helper）。
