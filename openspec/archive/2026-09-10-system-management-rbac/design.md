# 设计：系统管理域 + RBAC 模型升级

> 本文件是 `2026-09-10-system-management-rbac` 变更的**设计真源**（L4，待评审）。
> 现状事实以探查为准，均标注文件路径；一切 DDL / 端点 / 权限口径在人工确认前不落代码。
> 关联文档：`docs/architecture/auth-design.md`、`password-security.md`、`audit-log.md`、`data-masking.md`、`id-name-cache.md`、`AGENTS.md §6`。

---

## 0. 阅读指引

| 你要看什么 | 去哪节 |
| ---------- | ------ |
| 现状差在哪 | §1 |
| 为什么这么选 | §3 ADR（5 项关键决策） |
| 建哪些表、怎么迁移 | §5 |
| 开哪些接口 | §6 |
| 谁能访问什么、防自锁 | §7 |
| 密码怎么管 | §8 |
| 两库怎么改、契约怎么同步 | §10 / §11 |
| 哪些要你拍板 | §14 |

---

## 1. 现状与差距（事实基线）

| 维度 | 现状（事实） | 依据 | 差距 |
| ---- | ------------ | ---- | ---- |
| 用户表 | `sys_user`：`id/username/password_hash/real_name/role/status/created_at/updated_at/deleted`；`role` 为自由字符串列，默认 `'VIEWER'` | `entity/SysUser.java`；`db/migration/h2/V1__init_schema.sql` 行 9-19 | 无角色外键约束、无改密时间、无强制改密标记 |
| 菜单表 | `sys_menu`：`id/parent_id/name/code/path/icon/sort_order/status/deleted` + V7 追加 `allowed_roles VARCHAR(256) NOT NULL DEFAULT 'ADMIN,USER'` | V1 行 21-31；`V7__sys_menu.sql` 行 5 | 无菜单类型（目录/菜单/按钮）、无权限码列、授权靠逗号串 |
| 角色表 | **不存在** | 全库 `sys_role|SysRole` 零命中 | 需新建 |
| 用户-角色关联 | **不存在**（靠 `sys_user.role` 单列） | 同上 | 需明确单/多角色（见 ADR-1） |
| 角色-菜单关联 | **不存在**（靠 `sys_menu.allowed_roles` 逗号串） | 同上 | 需新建 |
| 权限码 | **无服务端权限码**；前端权限码硬编码在 `stores/auth.ts#ROLE_PERMS`（17 条，仅 `admin`） | `frontend-scaffold/src/stores/auth.ts` 行 16-37 | 权限码注册表缺失，两端口径分离 |
| 字典 | **不存在**（`sys_dict*` 零命中）；业务侧「字典」是各域自带的 options 常量 | 全库检索 | 需新建 |
| 端点门禁 | `@RequireAuth` 仅 `value` / `role` 两属性，单角色名忽略大小写比对 | `security/RequireAuth.java` 行 17-19；`security/RequireAuthInterceptor.java` 行 35-37 | 不支持权限码级细粒度 |
| 权限判定载体 | `JwtFilter` 从令牌 `role` claim 还原 `LoginUser.role` 写入 `UserContext`；**判定不查库** | `security/JwtFilter.java` 行 101-102；`security/UserContext.java` 行 34-36 | 角色/权限变更需等令牌过期（2h）才生效，或需引入解析层（见 ADR-3） |
| 登录刷新 | `AuthService.refresh()` 换发 access 时**硬编码 `"ADMIN"`** | `service/AuthService.java` 行 92 | **潜伏提权缺陷**，须修 |
| 默认管理员 | `AuthService.ensureAdmin()`：表空时种子 `admin/admin@2026`（ADMIN） | `auth-design.md §2` | 无强制首登改密 |
| 密码 | BCrypt（strength 10）存储；登录 `matches` 比对 | `password-security.md §1/§2` | 无改密/重置端点、无复杂度策略、无强制改密 |
| 审计 | `fac_audit_log`（append-only）由**前端经 `POST /uplink/audit` 上报**，无服务端自动写 | `docs/architecture/audit-log.md §1/§2`；`service/UplinkService.java` 行 41-56 | 系统管理写操作无服务端审计 |
| 系统管理前端 | 仅 `views/system/users.vue`（纯占位）+ `views/system/deviceCode.vue`（本地 `parseDeviceCode`，无后端） | 前端探查 | 页面与 service 全缺 |
| 缓存 | `IdNameCacheService`（Caffeine TTL 5min + 写时失效 `reloadMenus()`），提供 `allMenus()` | `common/cache/IdNameCacheService.java` 行 71-85 | 可复用为「角色→权限码」缓存范式 |

**一处既有文档偏差（顺带清理，见 §15）**：`AGENTS.md §6.3.3` 的白名单仍列 `/api/v1/auth/menus`、`/api/v1/auth/me`，与 `auth-design.md §2` 及 `JwtFilter.WHITELIST`（已移出）**不一致**。

---

## 2. 目标与非目标

**目标**
1. 建立可配置 RBAC：角色表 + 角色-菜单/权限授权，权限判定由后端权威下发。
2. 系统管理域四组 CRUD（用户 / 角色 / 菜单权限 / 字典）端到端闭环，前端页面从占位变为可用。
3. 口令生命周期闭环（改密 / 重置 / 强制首登改密 / 复杂度）。
4. 权限判定链路可扩展（支持权限码级细粒度），且**不破坏无状态 JWT 前提**。
5. 服务端审计覆盖系统管理写操作。

**非目标**：见 `proposal.md`。核心是本期**不做** ABAC 行级防区过滤、账号锁定、多角色、SSO、审计查询 UI。

---

## 3. 关键决策（ADR）

### ADR-1：采用**单角色** RBAC，不引入用户-角色多对多

- **背景**：现模型 `sys_user.role` 单字符串；前端 `rbac-permission` spec 声明五类角色（总指挥/值班调度/属地班长/内操/外操），语义上是「一人一岗」。
- **推荐**：**保持一人一角色**。`sys_user.role` 保留为角色码引用（引用 `sys_role.role_code`），不建 `sys_user_role` 关联表。
  - 好处：**不动令牌结构、不动 `JwtFilter`、不动 `LoginUser`**——避开 L4 令牌链路最大风险面；改动量降低约 40%。
  - 代价：不支持一人多角色。对岗位制指挥场景可接受。
- **备选**：建 `sys_user_role` 多对多。需令牌携带 `roles[]` claim 或请求期查库，属令牌结构变更（L4 高风险），且与「无状态不查库」现状冲突。
- **演进**：若后续确需多角色，模型是**可加性**的——加 `sys_user_role` 表 + 令牌 `roles` claim + `UserContext.isAdmin()` 改集合判定，不推翻本设计。
- **待确认**：是否接受「一人一角色」？（§14 Q1）

### ADR-2：权限码统一由**菜单/权限树**承载（RuoYi 式），不建独立权限表

- **背景**：需要两类东西——①导航菜单（现 5 条 `fm-*`）；②按钮/接口级权限码（前端已用 `fire-alarm:ack`、`system:user:view` 等）。
- **推荐**：给 `sys_menu` 加 `menu_type`（`DIR` 目录 / `MENU` 菜单 / `BUTTON` 按钮）与 `perm_code`（权限标识）。按钮节点只承载 `perm_code`、不参与路由装配。
  - 好处：**单一授权轴**（角色 `→sys_role_menu→` 菜单/按钮），无「菜单授权」与「权限授权」两套口径漂移；复用既有 `sys_menu` 表与 `parent_id/icon/sort_order/status` 列。
  - 「权限管理 CRUD」= 菜单树 CRUD，语义自洽。
- **备选**：独立 `sys_permission` 注册表 + `sys_role_permission`。分离更「干净」，但引入第二授权轴，与 `sys_menu.allowed_roles` 双轨并存期间易漂移，表更多。
- **关键兼容点**：`GET /auth/menus` **返回结构不变**（仍是顶层 `fm-*` 的扁平 `MenuVO` 列表，供 `MENU_ROUTE_SPECS` 装配）——只把数据源从 `allowed_roles` 换成 `sys_role_menu`。全局权限码另由 `/auth/me` 的 `perms` 下发。
- **待确认**：是否接受「权限即菜单树节点」？（§14 Q2）

### ADR-3：权限码在**请求期由服务端缓存解析**，不塞进 access 令牌

- **背景**：`JwtFilter` 当前只认令牌内 `role`，不查库。若把权限码塞进令牌，则令牌体积增大、且角色/权限变更需等 2h TTL；若每请求查库，则违背无状态性能前提。
- **推荐**：`JwtFilter` 保持现状（令牌仍只带 `role`）；新增 `RoleAuthorityService`，用 **Caffeine 读穿缓存**（`role_code → Set<perm_code>`，TTL 5min）+ **写时失效**（角色/菜单写操作后 `reloadRolePerms()`），复用 `id-name-cache.md` 既有范式与 `IdNameCacheService` 结构。
  - `@RequireAuth(perm="system:user:create")` 由 `RequireAuthInterceptor` 调 `RoleAuthorityService.permsOf(UserContext.role())` 判定。
  - 写时失效 ⇒ 角色授权变更**立即生效**（无需等令牌过期、无需踢下线）。
- **备选**：(a) 权限码入令牌——变更滞后 + 令牌膨胀；(b) 每请求查库——N+1 且违背缓存纪律。
- **待确认**：接受服务端缓存解析？（§14 Q3）

### ADR-4：`@RequireAuth` **加属性**而非换注解

- **推荐**：`RequireAuth` 增加 `String perm() default ""`（多权限暂用 `String[]` 亦可，本期单值够用）。判定顺序：`value=false` 放行 → 未登录 401 → `role` 非空且不符 403 → `perm` 非空且不持有 403。
- **理由**：注解已用于类级与方法级、已有 9+ 处调用；加属性零回归。换新注解需全量替换且两注解并存期易混。
- **兼容**：`role="ADMIN"` 语义**不删**，系统管理域初期可 `role="ADMIN"` 粗粒度，细化时改 `perm=...`。二者可同时标注（AND 关系）。

### ADR-5：系统管理域端点的**鉴权粒度**分两步走

- **第一步（本 Change 落地）**：`/api/v1/system/**` 全部 `@RequireAuth(role="ADMIN")`——与既有写侧域（blacklist / emergency-plans / video-linkages）口径一致，风险最低。
- **第二步（同 Change 内提供能力，页面逐步接）**：为每组操作登记 `system:*` 权限码，允许后续把 `role="ADMIN"` 换为 `perm="system:user:create"` 等，**不改端点路径**。
- **理由**：一次性上细粒度权限码若无完整角色授权 UI 支撑，会导致「配不出可用的非管理员管理员」——反而锁死。先保可运行，再放粒度。

---

## 4. 权限模型总览

```
sys_user.role ──(role_code)──▶ sys_role ──┬─ role_name / status / data_scope(预留)
                                          │
                                          └──sys_role_menu──▶ sys_menu
                                                                ├─ menu_type=DIR/MENU → 参与 /auth/menus 导航装配
                                                                └─ menu_type=BUTTON  → 仅贡献 perm_code（按钮/接口级）

请求期：UserContext.role() ──▶ RoleAuthorityService(cache) ──▶ Set<perm_code>
        @RequireAuth(role=...) / (perm=...) 判定；/auth/me 下发 roles + perms 给前端
```

**权限码命名规范**：`域:资源:动作`，全小写，动作 `view|create|edit|delete|assign|export|...`。
例：`system:user:create`、`system:role:assign`、`system:dict:edit`、`system:menu:edit`；业务域沿用前端既有 `fire-alarm:ack`、`system:device-code:view`（**不重命名**，避免前端 `v-permission` 大面积改动）。

---

## 5. 表结构设计（DDL 草案）

> 归属迁移：**V32 起新增增量文件**（`V17+` 之后继续编号，禁改既有 V 文件）。
> 三方言（h2 / postgresql / dameng）须列定义一致；达梦按 Oracle 兼容方言写，且**不支持 `IF NOT EXISTS`**，其脚本单独处理。
> H2 保留字规避：不得用裸 `value` / `command` / `type` / `time` / `mode` / `seq` 作列名（本项目既有约定）。

### 5.1 新增表

```sql
-- V32__system_rbac.sql（h2 版；postgresql/dameng 同步）

CREATE TABLE IF NOT EXISTS sys_role (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code     VARCHAR(64)  NOT NULL,              -- 角色标识（ADMIN/COMMANDER/...），唯一
    role_name     VARCHAR(64)  NOT NULL,              -- 角色中文名
    description   VARCHAR(255),                       -- 角色说明
    data_scope    VARCHAR(16)  NOT NULL DEFAULT 'SELF', -- 预留数据范围 ALL/DEPT/SELF（本期不实现过滤）
    status        TINYINT      NOT NULL DEFAULT 1,    -- 1 启用 / 0 停用
    built_in      TINYINT      NOT NULL DEFAULT 0,    -- 1 内置角色（禁删）
    sort_order    INT          NOT NULL DEFAULT 0,    -- 排序
    deleted       TINYINT      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code ON sys_role(role_code);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id     BIGINT NOT NULL,                       -- 角色 id
    menu_id     BIGINT NOT NULL,                       -- 菜单/权限节点 id
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_menu ON sys_role_menu(role_id, menu_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_role ON sys_role_menu(role_id);

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_code    VARCHAR(64)  NOT NULL,                -- 字典标识（唯一），如 alarm_level
    dict_name    VARCHAR(64)  NOT NULL,                -- 字典名称
    description  VARCHAR(255),
    status       TINYINT      NOT NULL DEFAULT 1,
    built_in     TINYINT      NOT NULL DEFAULT 0,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dict_type_code ON sys_dict_type(dict_code);

CREATE TABLE IF NOT EXISTS sys_dict_item (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_code    VARCHAR(64)  NOT NULL,                -- 所属字典标识
    item_value   VARCHAR(128) NOT NULL,                -- 字典值
    item_label   VARCHAR(128) NOT NULL,                -- 字典显示名
    sort_order   INT          NOT NULL DEFAULT 0,
    status       TINYINT      NOT NULL DEFAULT 1,
    description  VARCHAR(255),
    deleted      TINYINT      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sys_dict_item_code ON sys_dict_item(dict_code);
```

> **⚠️ 与 `AGENTS.md §6.4.5` 的一处张力（待确认）**：规则要求「新表必须带 `deleted` 与审计字段」。`sys_role_menu` 是**纯关联表**（无业务身份），推荐**撤销授权时硬删**（否则 MyBatis-Plus 全局逻辑删除会干扰「删后再授」的唯一约束与查询）。上表已按规则给关联表保留最小字段但**不含 `deleted`**——请裁决：接受关联表硬删（推荐），还是强制加 `deleted`（需把唯一索引改为 `(role_id, menu_id, deleted)`）？（§14 Q4）

### 5.2 既有表扩列

```sql
-- sys_menu：承载菜单类型与权限码
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS menu_type VARCHAR(16) NOT NULL DEFAULT 'MENU'; -- DIR/MENU/BUTTON
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS perm_code VARCHAR(128);                        -- 权限标识，如 system:user:create
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS visible   TINYINT NOT NULL DEFAULT 1;          -- 1 显示 / 0 隐藏

-- sys_user：口令生命周期
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS pwd_updated_at  TIMESTAMP;                     -- 最近改密时间
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS must_change_pwd TINYINT NOT NULL DEFAULT 0;    -- 1 下次登录强制改密
```

### 5.3 种子（仅 h2 dev；生产由运维初始化）

```sql
-- 角色种子（6 条，ADMIN 内置）
INSERT INTO sys_role(role_code, role_name, data_scope, status, built_in, sort_order) VALUES
  ('ADMIN',        '系统管理员', 'ALL',  1, 1, 0),
  ('COMMANDER',    '总指挥',     'ALL',  1, 0, 10),
  ('SCHEDULER',    '值班调度',   'DEPT', 1, 0, 20),
  ('TEAM_LEADER',  '属地班长',   'DEPT', 1, 0, 30),
  ('INNER_OPER',   '内操',       'SELF', 1, 0, 40),
  ('OUTER_OPER',   '外操',       'SELF', 1, 0, 50);
```

> `sys_user_role` 不建；**admin 用户 → ADMIN 角色的绑定在 `AuthService.ensureAdmin()` 里落**（因为 admin 用户是运行时代码种子，不是 SQL 种子——见 §1 现状）。
> `sys_role_menu` 种子：ADMIN 授予全部菜单/权限节点；其余角色按 §7 矩阵授予。建议放入迁移或 `ensureAdmin()` 同级的初始化代码。

### 5.4 `sys_menu.allowed_roles` 的处置（存量数据迁移）

| 方案 | 说明 | 推荐 |
| ---- | ---- | ---- |
| **A. 迁移后降级为只读兼容列** | 用脚本把 `allowed_roles` 展开为 `sys_role_menu` 行；`AuthService.menus()` 改读 `sys_role_menu`；列**保留**但不再写，下一版本删除 | ✅ 推荐：可回退、零停机、契约无感 |
| B. 一次性删除列 | 迁移后 `ALTER TABLE sys_menu DROP COLUMN allowed_roles` | ❌ 不可回退，且 `SysMenu` 实体需同步改 |

> 迁移映射：现有 5 条 `fm-*` 菜单 `allowed_roles='ADMIN,USER'` → 为 `ADMIN` 建行；`USER` 角色不存在于 `sys_role` 则**跳过并记警告**（当前系统实际只有 admin，`USER` 为历史遗留）。

---

### 5.5 标识唯一性与逻辑删除（实施期发现的既有语义鸿沟）

`sys_user.username` / `sys_role.role_code` / `sys_dict_type.dict_code` 均带**唯一索引**，而删除是**逻辑删除**（行仍物理存在）。MyBatis-Plus 的查询会自动追加 `deleted=0`，因此 `selectCount` **看不到已删行** → 预检查放行、数据库唯一键拒绝，只能抛出笼统的「数据冲突：请检查唯一键或必填字段」，且用户无法判断原因。

**处置**：三个 Mapper 增加 `countXxxIncludingDeleted(value[, excludeId])`（`@Select` 原生 SQL 绕过逻辑删除过滤），使应用层判重与数据库约束口径一致，返回准确提示「不可复用（含历史已删除账号/角色/字典）」。**标识符不回收是有意设计**——回收会让新旧记录在审计上无法区分。

> 冒烟首轮正是因此暴露（`smoke_system_rbac.py` 复跑时创建同名用户返回笼统 409），非脚本缺陷而是真实产品缺陷。

---

## 6. 端点清单

> 前缀 `/api/v1`；B3 包络 `Result<T>`；分页返回 `XxxPageResult{list,total,page,size}`（与前端 `PageResult` 同构）。
> 门禁列：`ADMIN` = `@RequireAuth(role="ADMIN")`；`perm:xxx` = 目标态权限码（本 Change 先按 ADMIN，权限码同批登记）。
> **所有写端点必须服务端审计**（§9）。

### 6.1 用户管理

| 方法 | 路径 | 说明 | 门禁 |
| ---- | ---- | ---- | ---- |
| GET | `/system/users` | 分页查询（`keyword`/`status`/`roleCode` 过滤） | ADMIN |
| GET | `/system/users/{id}` | 用户详情 | ADMIN |
| POST | `/system/users` | 新增用户（用户名唯一；初始密码；分配单角色） | ADMIN |
| PUT | `/system/users/{id}` | 修改资料（姓名 / 角色 / 状态） | ADMIN |
| DELETE | `/system/users/{id}` | 逻辑删除（禁删自己 / 禁删内置 admin / 禁删最后一个 ADMIN） | ADMIN |
| PUT | `/system/users/{id}/status` | 启用 / 停用（禁停用自己 / 最后一个 ADMIN） | ADMIN |
| PUT | `/system/users/{id}/role` | 调整角色（禁改自己 / 最后一个 ADMIN） | ADMIN |
| POST | `/system/users/{id}/password/reset` | 重置密码（随机临时密码 + `must_change_pwd=1`） | ADMIN |
| GET | `/system/users/{id}/roles`（可选） | 目标态：用户可授角色集合（单角色期可省） | ADMIN |

**响应字段（用户列表项，须脱敏，见 §10.4）**：`id`、`username`、`realName`（按 `@Masked` 策略）、`roleCode`、`roleName`、`status`、`mustChangePwd`、`createdAt`、`updatedAt`。**绝不含 `passwordHash`**。

### 6.2 角色管理

| 方法 | 路径 | 说明 | 门禁 |
| ---- | ---- | ---- | ---- |
| GET | `/system/roles` | 角色列表（分页或全量，用于用户表单下拉） | ADMIN（列表可放宽为登录即可，供表单用） |
| GET | `/system/roles/{id}` | 角色详情 | ADMIN |
| POST | `/system/roles` | 新增角色（`roleCode` 唯一，禁与内置冲突） | ADMIN |
| PUT | `/system/roles/{id}` | 修改角色（内置角色禁改 `roleCode`、禁停用） | ADMIN |
| DELETE | `/system/roles/{id}` | 删除角色（内置禁删；**有用户引用时拒绝**，返回冲突） | ADMIN |
| PUT | `/system/roles/{id}/status` | 启用 / 停用（内置 ADMIN 禁停用） | ADMIN |
| GET | `/system/roles/{id}/menus` | 该角色已授权节点 id 集合 | ADMIN |
| PUT | `/system/roles/{id}/menus` | 整体覆盖授权（写后 `reloadRolePerms()`） | ADMIN |

### 6.3 菜单 / 权限管理

| 方法 | 路径 | 说明 | 门禁 |
| ---- | ---- | ---- | ---- |
| GET | `/system/menus` | 菜单权限树（含 DIR/MENU/BUTTON 全量，供角色授权树渲染） | ADMIN |
| GET | `/system/menus/nav` | 当前用户可见导航（等价 `/auth/menus`，二选一保留） | 登录 |
| POST | `/system/menus` | 新增节点（目录/菜单/按钮） | ADMIN |
| PUT | `/system/menus/{id}` | 修改节点 | ADMIN |
| DELETE | `/system/menus/{id}` | 删除节点（有子节点或已被角色授权时拒绝或级联，需裁决 §14 Q5） | ADMIN |
| GET | `/system/permissions` | **权限码字典**（从 `sys_menu.perm_code` 聚合去重，供前端展示与登记核对） | ADMIN |

### 6.4 数据字典

| 方法 | 路径 | 说明 | 门禁 |
| ---- | ---- | ---- | ---- |
| GET | `/system/dict-types` | 字典类型分页列表 | ADMIN |
| POST/PUT/DELETE | `/system/dict-types[/{id}]` | 字典类型增改删（`built_in` 禁删） | ADMIN |
| GET | `/system/dict-items` | 字典项列表（`dictCode` 必填过滤，分页） | ADMIN |
| POST/PUT/DELETE | `/system/dict-items[/{id}]` | 字典项增改删 | ADMIN |
| GET | `/system/dicts/{dictCode}` | **业务只读**：按字典标识取启用项（供业务下拉，缓存） | 登录 |

### 6.5 个人中心（本人，非 ADMIN）

| 方法 | 路径 | 说明 | 门禁 |
| ---- | ---- | ---- | ---- |
| GET | `/auth/me` | **扩展**：增补 `roles: string[]`、`perms: string[]`、`mustChangePwd` | 登录 |
| PUT | `/auth/profile` | 改本人资料（真实姓名等；禁改自身角色/状态） | 登录（self） |
| POST | `/auth/password` | 改本人密码（**须校验旧密码**；成功后清 `must_change_pwd`、更新 `pwd_updated_at`） | 登录（self） |

> `/auth/me` 与 `/auth/password` 均在**非白名单**内（须有效 access 令牌），符合 `auth-rbac` spec 与 `JwtFilter` 现状。

---

## 7. 权限边界矩阵

### 7.1 域 × 角色

| 能力 | ADMIN | 非管理员角色 | 未登录 |
| ---- | ----- | ------------ | ------ |
| 系统管理域全部读写（用户/角色/菜单/字典） | ✅ | ❌ 403 | ❌ 401 |
| 本人资料 / 本人改密 | ✅ | ✅ | ❌ 401 |
| 本人信息查看（`/auth/me`） | ✅ | ✅ | ❌ 401 |
| 业务域读（按各域既有口径） | ✅ | 按 `sys_role_menu` 授权 | ❌ 401 |
| 业务域写（blacklist / plans / video…） | ✅ | 按权限码（目标态） | ❌ 401 |

### 7.2 破坏性与自我保护规则（硬约束，服务端强制）

| 规则 | 说明 | 违反返回 |
| ---- | ---- | -------- |
| **禁删/禁停用自己** | 操作目标 `id` == `UserContext` 当前用户 → 拒绝 | 403 `不可对自己的账号执行该操作` |
| **禁改自己角色** | 防自我提权 / 自锁 | 403 |
| **保护最后一个 ADMIN** | 使 ADMIN 角色启用用户数归零的删/停/改角色请求 → 拒绝 | 409 `必须保留至少一个启用状态的管理员` |
| **内置对象不可删** | `built_in=1` 的角色/用户/字典类型禁删；内置角色禁改 `roleCode`、禁停用 | 403 |
| **角色被引用不可删** | 删除存在用户的角色 → 拒绝 | 409 `该角色下仍有用户` |
| **非 ADMIN 不得进系统管理域** | 域内端点统一门禁 | 403 |
| **改密必须验旧密码** | `/auth/password` 不得跳过旧密码 | 400 |
| **重置密码不得固定值** | 服务端随机生成，响应一次性返回（或走安全信道），置 `must_change_pwd=1` | — |
| **用户名唯一** | 新增/改名校验，冲突返回 | 409 |

> ⚠️ **最后管理员保护是本设计的核心安全兜底**——「全员锁死」比「越权」更难恢复。实现须在 `SystemUserService` 事务内以「统计 ADMIN 启用用户数」为准，不能仅靠前端禁用按钮。

---

## 8. 密码策略（`password-lifecycle`）

| 项 | 决策 |
| ---- | ---- |
| 存储 | 沿用 BCrypt（`BCryptPasswordEncoder`，strength 10）——**不改**（`password-security.md` 属 L4，无变更必要） |
| 本人改密 | `POST /auth/password`：校验旧密码 → 校验策略 → 写新哈希 → 更新 `pwd_updated_at` → 清 `must_change_pwd`；**失效当前 access 令牌由前端主动登出重登**（无状态无黑名单，不做服务端失效） |
| 管理员重置 | `POST /system/users/{id}/password/reset`：随机临时密码（12 位含大小写数字符号）→ 置 `must_change_pwd=1` → 返回一次性临时密码 |
| 强制首登改密 | 登录成功后若 `must_change_pwd=1`，响应 `LoginResult` 带 `mustChangePwd=true`；前端强制跳改密页，改密前不放行到业务路由（**前端拦截 + 后端在业务写端点拒绝**双保险） |
| 默认账号 | `admin/admin@2026` 种子时置 `must_change_pwd=1`（`password-security.md` 反模式已点名「`admin@2026` 不得当生产凭证」）。**实施期细化**：该标记由 `app.password.force-change-default-admin` 控制——**生产默认 true，dev 置 false**（H2 内存库每次重启重建，强制改密会反复阻断联调）；`PasswordLifecycleInterceptor` 本身在**任何 profile 都启用**，故管理员手动重置口令产生的强制改密在 dev 同样被拦，行为由 `PasswordLifecycleInterceptorTest` 固化 |
| 复杂度 | 长度 ≥ 8；至少含 大写 / 小写 / 数字 / 符号 中 3 类；不含用户名；不得与旧密码相同。**配置化**（`app.password.*`），默认开启 |
| 失败锁定 | **本期不做**（§14 Q6） |

---

## 9. 审计（服务端主动写）

- 系统管理域**每个写操作**成功后，服务端向 `fac_audit_log` 落一条：`action`（如 `system.user.create` / `system.role.assign`）、`module='system'`、`detail_json`（**脱敏后**：记录目标 id、变更字段、**不记密码**）、`event_at`。
- 实现方式（推荐）：`SystemAuditHelper.record(...)` 由各 `SystemXxxService` 显式调用（**不引入 AOP 切面**，避免与既有「无切面」现状冲突，且显式更易测）。
- 写入**尽力而为**：审计失败仅 `warn`，不阻断主流程（对齐 `audit-log.md §3`）。
- `fac_audit_log` **append-only**：不改不删（对齐 `audit-log.md §1`）。
- 与既有前端上报**并存**：本 Change 是**增补**服务端写入，不替换 `POST /uplink/audit`。

---

## 10. 后端实现要点

### 10.1 分层与命名（对齐既有惯例）

| 层 | 新增 |
| -- | ---- |
| `entity/` | `SysRole`、`SysRoleMenu`、`SysDictType`、`SysDictItem`；`SysMenu` / `SysUser` 扩字段 |
| `mapper/` | `SysRoleMapper`、`SysRoleMenuMapper`、`SysDictTypeMapper`、`SysDictItemMapper`（`extends BaseMapper`） |
| `dto/` | `SystemUserItem`、`SystemUserCreate`、`SystemUserUpdate`、`SystemUserPageResult`、`SystemRoleItem`、`SystemRoleSaveRequest`、`SystemMenuNode`、`SystemMenuSaveRequest`、`DictTypeItem`、`DictItemItem`、`PasswordChangeRequest`、`PasswordResetResult`、`ProfileUpdateRequest`、`MeResult`（`/auth/me` 出参，替换现 `Map<String,Object>`） |
| `service/` | `SystemUserService`、`SystemRoleService`、`SystemMenuService`、`SystemDictService`、`PasswordPolicy`、`SystemAuditHelper`、`RoleAuthorityService` |
| `controller/` | `SystemUserController`、`SystemRoleController`、`SystemMenuController`、`SystemDictController`；`AuthController` 扩 3 端点 |
| `security/` | `RequireAuth.perm` 属性；`RequireAuthInterceptor` 增 `perm` 判定分支（属 L4 门禁区，最小改动） |

### 10.2 缓存与失效

- `RoleAuthorityService`：Caffeine `role_code → Set<perm_code>`，TTL 5min；写入口（角色授权 / 菜单改 `perm_code`）调用 `reloadRolePerms()`。
- `IdNameCacheService`：`sys_user` 增改后须 `evict` 对应 `id→realName`（`id-name-cache.md §3` 明确要求「覆盖所有写入口」）。
- `menus()` 数据源改 `sys_role_menu` 后，菜单写操作须 `reloadMenus()`。

### 10.3 迁移与初始化顺序

`ensureAdmin()` → 确保 `admin` 用户存在 → 确保 `ADMIN` 角色存在 → 绑定 `admin→ADMIN` → 确保 `ADMIN` 拥有全部 `sys_role_menu`。`sys_role` 的种子可由 SQL 落（§5.3），也可由代码幂等 upsert（更抗空库/重跑）。**建议代码幂等**，与 `ensureAdmin` 同源。

### 10.4 出口脱敏

- 用户列表 / 详情 `realName` **不脱敏，原样返回**（实施期细化）：系统管理端点仅 `ADMIN` 可访问，属 `data-masking.md` 明确允许的「独立权限层下可看明文」出口——用户管理场景若掩名（张*）将无法辨识与核对，反而不满足等保审计可读性。`@Masked` 仍用于通用/非管理出口。
- `passwordHash` **永不**入任何 DTO；禁止用 `Map.of` 直接回实体（`data-masking.md` 反模式）。
- 审计 `detail_json` 写入前脱敏。

---

## 11. 契约与四同步

| 步 | 动作 |
| -- | ---- |
| 1 | 本 Change（后端）四件套 + 前端镜像 Change |
| 2 | **新增** `docs/api/system.openapi.json`（tags: `system`；users/roles/menus/dicts 四组）；**修改** `docs/api/auth.openapi.json`（`/auth/me` 增 `roles`/`perms`/`mustChangePwd`、增 `/auth/password`、`/auth/profile`） |
| 3 | 后端实现后跑 `node scripts/check-api-contract.mjs --strict`（路由 0 差异 / schema 0 漂移） |
| 4 | 前端 `npm run gen:api-types` 重生成 `src/types/generated/system.ts` + `auth.ts` 更新 |

**契约四条铁律**：按域分组 / 接口有中文 `summary`+`description` / 字段有中文 `description` / 成功响应有 `example`（B3 包络）。
**schema 命名必须与后端 DTO 同名**（`check-api-contract.mjs` schema 层强制逐字段对拍）——即上表 `dto/` 每个响应 DTO 都要在契约里有同名 schema。

---

## 12. 前端改造点

| 项 | 现状 | 改法 |
| -- | ---- | ---- |
| `views/system/users.vue` | 占位 | 落地：用户列表（分页/筛选）+ 新增/编辑弹窗 + 角色下拉 + 启停用 + 重置密码 + 删除确认 |
| `views/system/roles.vue`（新） | 无 | 角色列表 + 角色表单 + **授权树**（`GET /system/menus` 树 + 已授权 id 勾选 + 保存） |
| `views/system/menus.vue`（新） | 无 | 菜单/权限树维护（目录/菜单/按钮 + `perm_code`） |
| `views/system/dict.vue`（新） | 无 | 字典类型 + 字典项两级维护 |
| `views/system/deviceCode.vue` | 纯前端解析 | **保持**（本地工具，无需后端；不改） |
| `stores/auth.ts` | `ROLE_PERMS` 硬编码、`RoleId='admin'` | 改为从 `GET /auth/me` 取 `roles`/`perms` 填充；`RoleId` 放宽为 `string`；`ROLE_PERMS` 退役为兜底空集 |
| `router/index.ts` | `SECONDARY_ROUTES` 静态注册 2 页 | 增 3 页（roles/menus/dict），`meta.perm` 用 `system:*`；补 `meta.adminOnly` 兜底 |
| `router/menu.ts` | `MENU_ROUTE_SPECS` 仅 `fm-*` | 可选：加 `system` 分组壳；否则维持隐藏二级页 |
| `services/system.ts`（新） | 无 | 用户/角色/菜单/字典 REST 封装，`request()` + 类型（对齐 `emergencyPlan.ts` 惯例） |
| `services/auth.ts` | 无 me 类型 | 增 `fetchMe()` / `changePassword()` / `updateProfile()` |
| 启动时序 | `main.ts#installMenus` | 需在路由解析前完成 `/auth/me`（取 perms）+ `/auth/menus`；否则守卫 `hasPerm` 拿到空集误跳 404 |

---

## 13. 测试与守门

- **后端**：`SystemUserControllerTest` / `SystemRoleControllerTest` / `SystemMenuControllerTest` / `SystemDictControllerTest`（standalone MockMvc + `GlobalExceptionHandler`）；`SystemUserServiceTest` / `PasswordPolicyTest` / `RoleAuthorityServiceTest` / `RequireAuthPermTest`（含**最后管理员保护**、**自锁防护**、**越权 403** 三类负例）。基线 361 → 预计 +50±。
- **前端**：`users.vue`/`roles.vue` 相关组件测试；`stores/auth.spec.ts` 更新（perms 来源）；`services/system.spec.ts`。
- **守门**：`check-api-contract.mjs --strict`；`validate-api-contracts.mjs`（新域 27→28）；`node scripts/check-openspec-hygiene.mjs`。
- **手工冒烟**：Python(utf-8) 脚本（curl 中文 body 踩 GBK）——admin 登录 → 建角色 → 建用户 → 赋角色 → 重置密码 → 用新用户登录验证权限边界。

---

## 14. 风险、回退与未决问题

### 风险与回退

| 风险 | 影响 | 缓解 / 回退 |
| ---- | ---- | ----------- |
| 全员锁死（无可用 ADMIN） | 系统不可登录管理 | 「最后管理员保护」硬约束 + 迁移脚本保证 admin→ADMIN 绑定；回退：DB 直改 `sys_user.role='ADMIN'` |
| 权限判定改动引入越权 | 数据泄露 | 判定仅在 `RequireAuthInterceptor` 单一入口；负例单测；`--strict` 契约守门 |
| `menus()` 数据源切换导致导航丢失 | 前端白屏/菜单空 | 迁移先建 `sys_role_menu`（ADMIN 全量）再切数据源；`DEFAULT_MENUS` 兜底已有 |
| 存量 `sys_user.role='VIEWER'`（若有）不匹配 `sys_role` | 该用户无权限 | 迁移脚本把未知 role 归到 `OUTER_OPER`（最小权限）并记警告 |
| 三方言 DDL 漂移 | 生产迁移失败 | h2 实跑验证 + PG/DM 脚本人工复核（达梦须实测，属阶段 7） |
| `refresh` 硬编码缺陷被忽略 | 提权 | 本 Change 必修，列为确认关卡 |

### 未决问题（已于 2026-09-10 裁决：全部按推荐结论采纳）

| # | 问题 | 裁决 |
| - | ---- | ---- |
| Q1 | 单角色（一人一岗）还是多角色？ | ✅ **单角色**（ADR-1，避开令牌变更） |
| Q2 | 权限码由菜单树承载还是独立权限表？ | ✅ **菜单树**（ADR-2） |
| Q3 | 权限码经服务端缓存解析（不入令牌）？ | ✅ **是**（ADR-3） |
| Q4 | `sys_role_menu` 硬删还是带 `deleted`？ | ✅ **硬删**（关联表无业务身份） |
| Q5 | 删除菜单节点时：有子节点/被授权 → 拒绝 or 级联？ | ✅ **拒绝**（更安全，显式先解绑） |
| Q6 | 本期是否做账号登录失败锁定？ | ✅ **不做**（登记后续，避免与登录链路耦合） |
| Q7 | 系统管理页归属主壳还是 `fm-mgmt` 子应用？ | ✅ 维持**主壳** `SECONDARY_ROUTES`（现状） |
| 附加 | `AuthService.refresh` 角色硬编码缺陷是否随本 Change 修？ | ✅ **修**（回归库中真实角色，附回归用例） |

---

## 15. 顺带清理的既有偏差（非本 Change 主体，建议一并修）

1. **`AGENTS.md §6.3.3` 白名单过期**：仍列 `/api/v1/auth/menus`、`/api/v1/auth/me`、`/api/v1/health`，与 `auth-design.md §2` / `JwtFilter.WHITELIST`（已移出 `me`/`menus`）不一致 → 同步为实际白名单。
2. **`AuthService.refresh` 角色硬编码 `"ADMIN"`**（行 92）→ 本 Change 修复（应回归库中 `sys_user.role`）。
3. **前端 `RoleId = 'admin'` 字面量类型 + `ROLE_PERMS` 硬编码** → 随权限来源后端化退役（§12）。
4. **`AuthService.me()` 现返回 `Map<String,Object>`** → 建议改强类型 `MeResult` DTO，便于契约 schema 对拍（`check-api-contract.mjs` 需要同名 DTO）。
