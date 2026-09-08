# 乐观锁与并发护栏（Optimistic Locking）

> 并发写冲突的防护设计真源。当前为**待落地设计**，本文作为实现约束；改动并发策略、实体结构属 **L3/L4**，须 proposal + 评审。本文与 `AGENTS.md §6.4 数据库变更规则` 配合（加字段需走 Flyway V 文件）。

## 1. 决策（Decisions）

- **乐观锁覆盖可变实体**：对会被并发更新的实体（`fac_alarm`、`fac_security_event`、`fac_fire_alarm`、`fac_field_report` 等状态/数值可变表）引入 `version` 字段 + MyBatis-Plus `@Version`，由 `OptimisticLockerInnerInterceptor` 在 `update` 时自动加 `WHERE version = ?` 并自增。
- **冲突即 409**：版本不匹配抛出 `ObjectOptimisticLockingFailureException` → 映射为 `ResultCode.CONFLICT`（409）返回前端，由前端提示「数据已被他人修改，请刷新重试」。**不**静默覆盖、不重试到成功（避免活锁）。
- **逻辑删除沿用现有约定**：本库逻辑删除为手动 `deleted` 整数字段（如 `AlarmService` 置 `deleted=1`），**不引入 `@TableLogic` 注解**以免与手写 wrapper 冲突——新增表沿用同一手动约定，保持一致性。
- **只读路径不取锁**：列表 / 详情查询走无锁读，乐观锁只作用于写。

## 2. 现状（Current State）

| 项 | 状态 |
| -- | ---- |
| `OptimisticLockerInnerInterceptor` 装配 | ⚠️ **未实现**（`MybatisPlusConfig` 仅有 `PaginationInnerInterceptor`） |
| 实体 `@Version version` 字段 | ⚠️ 未实现 |
| 逻辑删除 | ✅ 手动 `deleted` 整数字段（非 `@TableLogic`） |
| 冲突错误码 `CONFLICT` | ⚠️ 需确认 `ResultCode` 是否已含 409 段 |

> 现状下并发写（如两人同时处置同一报警）后写覆盖先写，无冲突检测——本文旨在补该护栏。

## 3. 约束（Constraints）

- `version` 字段**不暴露为客户端可编辑**（不在 DTO 接收体开放写），仅随读取返回、随更新回传用于比对（可选 `If-Match`）。
- 新增 `version` 列必须走 Flyway V 文件（**共享环境后禁止改/删已有 V-file**，新增只加 V*+**）；三方言（h2/postgresql/dameng）同步加列。
- 乐观锁与逻辑删除共存：MyBatis-Plus 在乐观锁 update 时会同时带 `deleted=0` 条件，需验证三方言下生成 SQL 正确。
- 批量更新（如 `updateBatchById`）的乐观锁行为需显式测试，避免部分成功部分失败难定位。

## 4. 反模式（Anti-patterns）

- ❌ 用悲观锁（`select for update`）当默认 —— 安全指挥系统写冲突低频，悲观锁拖累并发，乐观锁足够。
- ❌ 冲突后自动重试到成功 —— 制造活锁，且可能覆盖他人合理修改。
- ❌ 把 `version` 当业务版本号对外展示 / 写入审计 —— 它是并发控制内部量。
- ❌ 引入 `@TableLogic` 与现有手动 `deleted` 混用 —— 行为不一致，统一走手动 wrapper。
