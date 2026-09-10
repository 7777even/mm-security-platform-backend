# 设计：data_scope 行级 ABAC（MVP）

## 1. 数据模型

### 1.1 防区主数据 `sys_zone`（新增）
```sql
CREATE TABLE sys_zone (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    zone_code   VARCHAR(64)  NOT NULL,   -- 防区编码（如 'LIAN_YOU'）
    zone_name   VARCHAR(64)  NOT NULL,   -- 防区名（与业务表 area 值一致：'炼油区'/'乙烯区'/...）
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_sys_zone_code ON sys_zone(zone_code);
```
种子（zone_name 严格对齐 `BRIGADE_AREA` 字典项，已验证救援队伍域 area 取值一致）：
炼油区 / 乙烯区 / 罐区 / 仓储区 / 码头区 / 芳烃区 / 特勤保障区。

### 1.2 `sys_user` 扩展
```sql
ALTER TABLE sys_user ADD COLUMN zone_codes VARCHAR(512);  -- 逗号串，如 '炼油区,罐区'；空=未分派
```
- ADMIN 等全量账号：`data_scope` 保持 `ALL`、`zone_codes` 留空（解析为不过滤）。
- 受限账号：`zone_codes` 填其可访问防区名集合。

### 1.3 角色 `data_scope` 语义（沿用现有枚举，不改列）
| data_scope | 行为 |
|---|---|
| `ALL`  | 不过滤（解析返回 `null`） |
| `DEPT` | 本期等同 `SELF`：按 `user.zone_codes` 过滤 |
| `SELF` | 按 `user.zone_codes` 过滤 |

## 2. 解析流程

```
请求进入 → UserContext 持有 LoginUser(role)
  │
  ├─ DataScopeResolver.resolveZones(loginUser)
  │    1. scope = roleAuthorityService.dataScopeOf(role)        // Caffeine 缓存
  │    2. scope == ALL  → return null   (调用方不加 WHERE)
  │    3. else：zones = userZoneCache.get(username)            // Caffeine，TTL5min+写失效
  │           → 查 sys_user.zone_codes，按逗号解析为 Set<String>
  │           → return zones   (空 Set 表示无可见防区)
  │
  └─ Service 列表查询：
        qw = new LambdaQueryWrapper<>();
        ... 既有条件 ...
        DataScopeHelper.apply(qw, FacBrigadeTeam::getArea, zones);
        mapper.selectPage(page, qw);
```

### DataScopeHelper.apply 语义
| zones 参数 | 注入 |
|---|---|
| `null` | 不加任何条件（ALL） |
| 空 `Set` | `qw.apply("1=0")`（最小权限：无任何防区授权→看不到任何行） |
| 非空 | `qw.in(zoneColumn, zones)` |

## 3. 试点接入点（救援队伍域）
- 调研定位：救援队伍查询经 `RescueResourceService`（或同名 Service）+ `FacBrigadeTeamMapper`。
- 注入位置：该 Service 的队伍列表/分页查询构建 `LambdaQueryWrapper<FacBrigadeTeam>` 之后、`selectPage/selectList` 之前。
- 列对齐：`FacBrigadeTeam.getArea()` 的域值（乙烯区/炼油区/罐区/仓储区/码头区/芳烃区/特勤保障区）与 `sys_zone.zone_name` 完全一致 → `qw.in(FacBrigadeTeam::getArea, zones)` 可直接命中。
- **不接入**的域（本期）：监测告警（area＝乙烯装置区/乙烯罐区）、生产装置设备（area＝炼油区/化工区）等词表不互通，待其 area 归一到 `sys_zone` 后再接入。

## 4. 前端配置入口
- 新增 `GET /system/zones`（登录可读，返回 `sys_zone` 列表）供用户表单下拉。
- 用户表单新增"可访问防区"多选：`options = zones`，`v-model` 绑定 `zoneCodes`（逗号串），保存随 `SystemUserSaveRequest.zoneCodes` 提交。
- 角色表单 `data_scope` 下拉（ALL/DEPT/SELF）如已存在则不改；如缺失则补。

## 5. 与 perm 的叠加
- 接口层：`@RequireAuth(perm="system:user:view")` 等已落地的细粒度 perm 控制"能否进入/调用"。
- 数据层：本变更的 `data_scope` 控制"进入后能看到哪些防区行"。
- 二者正交叠加，互不替代。ADMIN 因 `data_scope=ALL` 自动看全部。

## 6. 缓存失效
- `userZoneCache`：用户 `zone_codes` 变更（`SystemUserService` 更新/创建用户）时 `invalidate(username)`。
- `RoleAuthorityService` 缓存：角色 `data_scope` 变更时沿用现有 `reloadRolePerms()` 失效逻辑（实现时在 `SystemRoleService` 对应方法补失效调用）。
