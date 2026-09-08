# 术语表（后端）

> 跨库、跨端一致引用的术语真源。前端侧的枚举文字/色阶/状态映射以 `frontend-scaffold/docs/glossary.md` 为 canonical，本文仅作后端实现口径的镜像与补充，**禁止自造术语或色阶**。
> 新增状态/术语必须先提案入对应规范（OpenSpec L3/L4），再回写两端 glossary。

## A. 与前端对齐的契约术语（canonical 见前端 glossary）

| 术语 | 后端实现口径 | 前端 canonical |
| ---- | ----------- | ------------- |
| **B3 统一包络** | `common/Result<T>{code,message,data,traceId}`：`code=0` 成功，非 0 走 `GlobalExceptionHandler` 包成业务错误 | `ApiResponse{code,message,data,traceId}`，`unwrapBody<T>` 解包 |
| **ACK_FLOW** | `FacAlarm.status` 状态机 `ACTIVE→ACKED→DISPATCHED→CLOSED`，`AlarmController` 的 ack 仅前进一格 | 同上，前端 `ACK_FLOW` 常量 |
| **AlarmLevel** | `FacAlarm.level` ∈ `{1,2,3,4}`（一级/二级/三级/四级） | 大屏色阶 `#ff5a4a/#ff9a3c/#f0c429/#b07aff`；后台 tag 色 `--color-alarm-1..4` |
| **AlarmStatus** | `ACTIVE` 待处理 / `ACKED` 已确认 / `DISPATCHED` 已派单 / `CLOSED` 已闭环 | 同语义，标签类 `tag-danger/-warning/-info/-success` |
| **RBAC 四层权限** | ④ 数据级＝服务端 `AuthorizationService.assertAdmin` / `assertSelfOrAdmin`（`@RequireAuth(role=ADMIN)` + 菜单 `allowed_roles` 过滤） | ① 按钮 `v-permission` ② 路由 `meta.perm` ③ 菜单 `/auth/menus` ④ 数据级服务端过滤 |
| **20 位 MDM 编码** | `FacDevice.deviceCode` 等中石化物理主键，固定 20 位；`/system/device-code` 解析入口 | 同，解析入口一致 |
| **wujie 微前端** | 后端仅提供 REST 数据与动态菜单；子应用装载由前端 `WujieHost` 完成，后端不感知 | `wujie-vue3` 壳内嵌 18 子应用 |
| **零下行控制** | `HardControlInterceptor.HARD_CONTROL_PATHS` 唯一名单；POST/PUT/DELETE 命中 → 503；WS 只推不控 | `guardHardControl` 前端拦截；`ack` 不触发设备硬控 |
| **防重放签名** | 生产 `HmacFilter` 校验 `X-Timestamp/X-Nonce/X-Signature`（HMAC-SHA256，容忍 300s）；dev `signature.enabled=false` | 生产挂签；Dev 可 `gateway-bypass` |
| **令牌内存态 + 刷新 Cookie** | access 令牌前端内存态；refresh 经 `ResponseCookie` 下发 **HttpOnly** Cookie（`name=rt`、`SameSite=Lax`、`Max-Age=7d`、`secure` 按 profile）；`/auth/logout` 清 Cookie | `getAccessToken/setAccessToken` 内存态；刷新凭 Cookie，JS 不可读 |
| **WS 推送包络** | `AlarmWebSocketHandler` 推送 `{topic:'alarm.push', payload:AlarmItem}`；吞掉客户端 15s `{type:'ping'}` 心跳 | `RealtimeClient` 按 `topic` 分发，`alarm.push→alarm store` |
| **契约四同步** | 改接口四同步：openspec → 前端契约 → 后端实现（跑 `check-api-contract.mjs`）→ 通知前端 `gen:api-types` | 前端消费侧同步 |

## B. 后端专属术语

| 术语 | 含义 |
| ---- | ---- |
| **UserContext** | `security/UserContext` 线程级上下文，由 `JwtFilter` 注入（username / role / userId）；`/auth/me`、`/auth/menus` 已移出白名单，必须带令牌才注入 |
| **JwtFilter** | 解析 Bearer Token、`WHITELIST` 内路径跳过；顺序 `HIGHEST_PRECEDENCE+10`（CorsFilter 最前、HmacFilter 次之） |
| **HmacFilter** | 生产开启的防重放签名校验；dev 关闭 |
| **HardControlInterceptor** | 零下行控制硬控拦截器，名单外写操作 → 503 `HARD_CONTROL_BLOCKED` |
| **RequireAuthInterceptor** | `@RequireAuth(role=ADMIN)` 角色鉴权 + 数据级 `assertSelfOrAdmin` |
| **ResultCode** | 错误码分段：`1xx` 通用 / `2xx` 鉴权 / `3xx` 设备 / `5xx` 硬控（详见架构 README §4） |
| **双轨迁移（Flyway）** | V1 全量快照 + V 增量；已进共享环境的 V 文件禁改/删；dev=H2，达梦/PG 为生产目标/回退 |
| **逻辑删除** | 全表 `deleted`（`0/1`），MyBatis-Plus 全局生效，禁止物理删业务行 |
| **OpenSpec** | 业务变更规格工具；`openspec/changes/` 为唯一规格源，完结回填 `specs/` 并归档 `archive/`（带 `YYYY-MM-DD-` 前缀） |
| **L0–L4** | 分级工作流：L0 只读/文档 → L1 小改 → L2 技术债 → L3 业务能力 → L4 高风险（过滤器顺序/安全放宽等） |
| **验证矩阵** | 最小验证对应表（`AGENTS.md §2`），改完只跑对应一行 |
| **契约真源** | `frontend-scaffold/docs/api/*.openapi.json` 是机器可读契约唯一真源，后端不复制第二份 |

## C. 维护

- 枚举文字/状态语义/色阶变更：**先改前端 glossary（canonical）→ 同步本文 A 表 → 同步代码与契约**。
- 后端新增拦截器/过滤器/错误码：在 B 表与架构 README §3/§4 同步登记。
- 本文与 `frontend-scaffold/docs/glossary.md` 互为镜像；冲突以**前端 glossary** 为权威（其含设计 token 与色阶定义）。
