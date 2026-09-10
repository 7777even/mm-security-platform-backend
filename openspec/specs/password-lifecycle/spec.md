# password-lifecycle Specification

## Purpose

口令生命周期能力：本人改密、管理员重置口令、强制首登改密（含服务端兜底拦截）与口令复杂度策略。哈希方式（BCrypt）与密钥策略见 `docs/architecture/password-security.md`；本 spec 只覆盖「生命周期与策略」语义。

## Requirements

### Requirement: 本人修改密码

系统须提供 `POST /api/v1/auth/password`，校验旧口令与新口令复杂度后方可修改；成功后更新 `pwd_updated_at`、清除 `must_change_pwd`。

#### Scenario: 旧密码错误

- **WHEN** 携带错误的旧密码调用 `POST /api/v1/auth/password`
- **THEN** 返回失败（`code=100`），密码未被修改

#### Scenario: 不满足复杂度

- **WHEN** 新口令不满足复杂度策略（长度或字符类别）
- **THEN** 返回失败并说明策略，密码未被修改

### Requirement: 口令复杂度策略

系统须以配置化策略校验新口令：最短长度（`app.password.min-length`，默认 8）、须覆盖的字符类别数（`app.password.require-categories`，默认 3，类别为大写 / 小写 / 数字 / 符号）、不得包含用户名、不得与旧口令相同。策略可经 `app.password.enabled=false` 应急关停（正式环境必须开启）。

#### Scenario: 长度不足

- **WHEN** 提交短于最小长度的新口令
- **THEN** 返回失败并提示长度要求（`code=100`）

#### Scenario: 包含用户名

- **WHEN** 新口令包含该账号的用户名（忽略大小写）
- **THEN** 返回失败，密码未被修改

### Requirement: 管理员重置密码

系统须提供 `POST /api/v1/system/users/{id}/password/reset`，由服务端生成随机临时口令（不得为固定值），置 `must_change_pwd=1`；响应不返回口令哈希，临时口令仅此一次可见。

#### Scenario: 重置后强制改密

- **WHEN** 管理员重置某用户密码
- **THEN** 该用户下次登录响应 `/auth/me` 的 `mustChangePwd=true`，且在完成改密前不得访问业务写端点

### Requirement: 强制首登改密的服务端兜底

`must_change_pwd=1` 的账号在**变更类请求**（非 GET / HEAD / OPTIONS）上须被服务端拒绝（403）；豁免 `/api/v1/auth/**`（否则改密路径自身被堵死）与 `/api/v1/uplink/audit`（审计旁路）。前端引导跳改密页仅为辅助，**不得**作为唯一防线。

#### Scenario: 未改密时调用业务写端点

- **WHEN** `mustChangePwd=true` 的用户携带有效令牌调用业务写端点
- **THEN** 返回 403，业务逻辑不执行

#### Scenario: 未改密时仍可改密

- **WHEN** `mustChangePwd=true` 的用户调用 `POST /api/v1/auth/password`
- **THEN** 请求被放行（不落入兜底拦截）

#### Scenario: 默认账号首登

- **WHEN** 以种子默认账号在**生产**配置下首次登录
- **THEN** 登录后标记需改密；未改密前业务变更类请求被拒绝
