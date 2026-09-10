# ID↔名称缓存一致性（ID-Name Cache）

> 引用数据（用户 id→姓名、设备 id→名称、菜单 key→名称）解析的缓存设计真源。已于 2026-09-08 落地实现（见 `common/cache/IdNameCacheService`，Caffeine 读穿 + TTL 5min + 写时失效），本文为实现约束与变更记录；改动缓存策略属 **L3**，须 proposal + 评审。

## 1. 决策（Decisions）

- **稳定引用数据走读穿缓存**：`sys_user(id→realName)`、`sys_menu(key→name/path)` 等**极少变更**的引用数据，用本地读穿缓存（Caffeine，进程内、带 TTL + 写时失效）避免列表接口 N+1 查询。
- **失效策略**：引用数据更新（如改真实姓名、菜单重排）时**主动 evict** 对应 key；缓存 TTL 作为兜底（如 5min），保证最终一致。
- **可变业务数据不缓存**：报警、事件等高频变更实体**不进此缓存**，每次读库保证实时。
- **批量解析优于逐行查**：列表组装阶段对收集到的 id 集合做 `IN` 批量查，再本地 map 映射；仅在无法批量（跨服务）时才用缓存。

## 2. 现状（Current State）

| 项 | 状态 |
| -- | ---- |
| 统一 `id→name` 缓存组件（Caffeine） | ✅ 已实现（`common/cache/IdNameCacheService`：sys_user id→realName 读穿 + sys_menu 全量列表缓存，TTL 5min + evict/reload 主动失效） |
| `AuthService.me()` 每次查 `sys_user` | ✅ 单点查询，量小可接受（缓存 API 已就绪供列表解析复用） |
| 列表接口 id→name 解析 | ✅ `IdNameCacheService.userName(id)` 提供读穿，规避 N+1 / 不一致 |
| `menus()` 全表加载 + 内存过滤 | ✅ 已走 `idNameCache.allMenus()`，带 TTL 5min 失效，菜单变更后 `reloadMenus()` 主动失效 |
| 角色 → 授权（menuIds + perms） | ✅ `security/RoleAuthorityService`（2026-09-10/V32）：Caffeine `role_code→RoleGrant`，TTL 5min + `reloadRolePerms()`；角色/菜单写入口必须调用 |
| 用户 → 是否需强制改密 | ✅ `common/cache/PasswordStateCache`（2026-09-10）：Caffeine `username→must_change_pwd`，TTL 5min + 改密/重置口令时 `evict(username)` |
| 字典项（业务只读 options） | ✅ `service/SystemDictService` 内置 Caffeine `dictCode→启用项列表`，TTL 5min + 字典项写操作整表失效 |

> 现状下菜单靠 `sysMenuMapper.selectList(null)` 全量内存过滤（无失效机制），用户姓名解析未集中——本文旨在统一为带失效的缓存层。

## 2.1 写时失效登记表（新增缓存必须登记，否则出现「改了但不变」）

| 缓存 | 失效入口 | 触发写操作 |
| ---- | -------- | ---------- |
| `IdNameCacheService.userRealNameCache` | `evictUser(id)` | 用户改名（`SystemUserService.update` / `AccountService.updateProfile`） |
| `IdNameCacheService.menuListCache` | `reloadMenus()` | 菜单任意增删改（`SystemMenuService`） |
| `RoleAuthorityService` | `reloadRolePerms()` | 角色增删改/启停、角色授权保存、菜单增删改（权限码或状态变化） |
| `PasswordStateCache` | `evict(username)` | 本人改密、管理员重置口令、用户状态/角色变更 |
| `SystemDictService.optionsCache` | 内部 `invalidateOptions()` | 字典类型/字典项任意增删改 |

## 3. 约束（Constraints）

- 缓存**只放稳定引用数据**；任何含 PII 的解析结果（如真实姓名）若要缓存，须遵守 `data-masking.md` 出口脱敏，且**不缓存明文越权可见范围外**的数据。
- 写时失效必须覆盖所有写入口（admin 改姓名、菜单管理接口等），否则出现「改了但列表不变」的一致性 bug。
- **角色授权缓存是安全相关缓存**：授权变更必须即时生效（不能等 5min TTL），故 `reloadRolePerms()` 为强制项；同理 `PasswordStateCache.evict` 遗漏会造成「已改密仍被拦」或反向漏放。
- 缓存不替代权限判定：`AuthorizationService` 的归属校验仍基于实时 `UserContext`，不受缓存影响。
- 多实例部署时进程内缓存 TTL 即最终一致窗口，须被接受；如需强一致改用共享缓存（Redis）——当前阶段不引入。

## 4. 反模式（Anti-patterns）

- ❌ 把高频变更业务实体塞进读穿缓存 —— 一致性窗口引发「看到旧数据」。
- ❌ 只设 TTL 不写时失效 —— 改了姓名列表却要等 TTL 过期才更新，体验差。
- ❌ 用缓存结果做权限判定 —— 缓存是性能手段，不是信任边界。
- ❌ 为「避免 N+1」而在循环里查库且不缓存 —— 既慢又无收益，应批量 `IN` 或缓存二选一。
