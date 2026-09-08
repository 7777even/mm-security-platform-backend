# 分层与模块边界（Layering & Module Boundary）

> 后端包职责与越界红线。任何分层结构的调整属 **L2**（影响多文件结构），须先读本文与 `AGENTS.md §6.2 / §6.6`。本文是 AI 检索「某段逻辑该放哪层」的权威索引。

## 1. 决策（Decisions）

- **九包分层**（根包 `com.sinopec.mmsecurity`）：`controller` / `service` / `mapper` / `entity` / `dto` / `config` / `security` / `common` / `websocket`。业务模块在 `controller/service/...` 下按域再细分（如 `auth`、`alarm`、`uplink`、`emergency`）。
- **单向依赖**：Controller → Service → Mapper → Entity；DTO 在 Controller/Service 边界做转换；`common` 与 `security` 被各层依赖但**不反向**依赖业务包。
- **职责红线**（详细见 `AGENTS.md §6.2`）：

| 层 | 职责 | 禁止 |
| -- | ---- | ---- |
| `controller` | 接收 HTTP、参数校验（`@Valid`）、调 Service、包 `Result<T>` | ❌ 写业务逻辑、直接调 Mapper、拼 SQL |
| `service` | 业务编排、事务、调 Mapper、越权校验 | ❌ 感知 HTTP（`HttpServletRequest` 仅限必要透传）、返回实体裸对象 |
| `mapper` | MyBatis-Plus / XML 数据访问 | ❌ 写业务判断、调其他 Service |
| `entity` | 纯 POJO + MP 注解，映射表 | ❌ 含业务方法、注入 Spring Bean |
| `dto` | 契约边界对象（请求/响应/事件） | ❌ 含持久化注解、业务逻辑 |
| `config` | Bean 装配、拦截器/过滤器注册、插件 | ❌ 写业务 |
| `security` | 鉴权/授权/`UserContext`/JWT | ❌ 调业务 Service（保持纯安全） |
| `common` | `Result`/`ResultCode`/`BusinessException`/`TraceContext`/`DeviceCode` | ❌ 局部改写包络与错误码（跨端契约） |

## 2. 现状（Current State）

| 项 | 状态 |
| -- | ---- |
| 九包结构落地 | ✅ `src/main/java/com/sinopec/mmsecurity/*` |
| 逻辑删除手动 `deleted` 字段（非 `@TableLogic`） | ✅ 全库统一约定 |
| `UserContext` ThreadLocal 贯穿 Service | ✅ |
| 跨端包络 `Result` / `ResultCode` 集中 `common` | ✅ |

## 3. 约束（Constraints）

- Controller 返回**必须**包 `Result<T>`（`GlobalExceptionHandler` 兜底），不裸返实体。
- Service 方法若涉及越权，在入口调 `AuthorizationService.assertAdmin()` / `assertSelfOrAdmin()`，不把校验推到 Controller。
- Mapper 接口继承 `BaseMapper<T>`，复杂查询用 XML 或 `LambdaQueryWrapper`（**禁止在测试目录复制第二份 DDL**，schema 复用 `db/migration/**` 的 V 文件，见 P1-1）。
- 新增对外接口须同步前端库契约（AGENTS §11 四同步），后端不在本库复制 OpenAPI 主契约。
- 分层文件与 `AGENTS.md` 冲突时按 §1.3 仲裁：根 `AGENTS.md` 高于分层，分层只能加严、不得放宽。

## 4. 反模式（Anti-patterns）

- ❌ 在 Controller 里写 `if (status==1) {...}` 业务分支 —— 推到 Service。
- ❌ Service 直接 `return entity` 给前端 —— 应映射为 DTO，避免暴露 `passwordHash` 等（见 `data-masking.md`）。
- ❌ Mapper 里调另一个 Service 做关联补全 —— 关联在 Service 层做（或批量 `IN` + 内存 map，见 `id-name-cache.md`）。
- ❌ 在 `common` 里为某个业务特判加错误码 —— `ResultCode` 分段是跨端契约，新增码必须落段（AGENTS §3.1）。
- ❌ 业务方法里 `new ThreadLocal` 或手动管 `UserContext` —— 统一由 `JwtFilter` 写入、`finally` 清理。
