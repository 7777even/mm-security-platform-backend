# 数据权限三核心表防区列治理（A1 剩余项 ②）

> 状态：schema 预备完成（V50），回填与 ABAC 注入**待产品定规则**，本文件为治理说明与待办清单。
> 关联：V34 数据权限 ABAC 框架、V46 fac_device.zone、V49 工作站域防区词、A1 ① 工作站域已套过滤。

## 1. 现状

| 表 | 现有位置信息 | 防区列 | 数据权限过滤 |
|---|---|---|---|
| `fac_alarm` | `location` VARCHAR(128) 自由文本 | ❌（V50 已加 `zone`） | 未套 |
| `fac_video_camera` | `location` VARCHAR(128) 自由文本 | ❌（V50 已加 `zone`） | 未套 |
| `fac_major_hazard` | 仅 `longitude`/`latitude`/`enterprise`/`category`，无 location | ❌（V50 已加 `zone`） | 未套 |

数据权限模型（V34 约定）：`sys_user.zone_codes` 存**中文 zone_name**，`DataScopeHelper.apply`
用 `qw.in(zone, zones)` 按中文字符串命中，不经 `zone_code` 翻译。`sys_zone.zone_name` 现有 16 词
（V34 七个救援区 + V46 五个装置区 + V49 四个工作站区）。

## 2. 已落地（本次）

- **V50**（`db/migration/h2`）：为三表加可空 `zone VARCHAR(64)`。**仅加列，不改行、不注入过滤。**
- 回填映射草稿：`docs/sql/data-scope-three-tables-backfill.draft.sql`（**不进 Flyway 目录**，不会自动执行）。

## 3. 关键决策：先回填、后注入（不可反序）

`DataScopeHelper.apply` 在 `zones` 为空时拼 `1=0` → 非 ALL 角色**零可见**（R1 上线风险）。
若先注入过滤、回填尚未完成，非 ALL 用户登录后看不到任何报警/摄像头/危化品数据，比当前「越权可见」更严重。

⇒ 顺序铁律：**① 回填 zone（location/坐标 → 中文防区词）→ ② 校验全部命中 sys_zone → ③ 才在 Service 注入 `apply`**。

## 4. 回填映射（DRAFT，待产品确认）

见 `docs/sql/data-scope-three-tables-backfill.draft.sql`：

- `fac_alarm` / `fac_video_camera`：按 `location` 关键词启发式映射（罐区/炼油/化工/乙烯/储运/公用工程/码头 →
  对应中文防区词，兜底 `全厂范围`）。此为**建议默认映射**，需产品核对关键词覆盖度。
- `fac_major_hazard`：**无 location**，需产品定「企业名/类别/经纬度网格 → 防区」规则（当前 SQL 中仅为结构占位，未启用）。

约束：回填产出的 zone 值必须落在 `sys_zone.zone_name` 已有词条内，禁止自造未登记词条。

## 5. 待办清单（顺序执行）

1. 产品确认 `location → 防区` 关键词映射（alarm/camera）与 `危化品 → 防区` 规则（major_hazard）。
2. 按确认规则执行 `data-scope-three-tables-backfill.draft.sql`（或转写正式 V51 迁移）。
3. 跑校验 SQL：三表 `zone` 无 NULL 且全部命中 `sys_zone.zone_name`。
4. 在三表对应 Service 列表查询注入 `DataScopeResolver` + `DataScopeHelper.apply(qw, Xxx::getZone, zones)`，
   仿 `DeviceService.page()` / `WorkstationService.page()` 模式。
5. 补单测（纯 Mockito / standalone MockMvc）覆盖 zone 过滤分支。
6. 达梦 / PG 镜像 V50（待两库激活，R5）。

## 6. 注意

- V50 仅 H2 方言，与 V46–V49 一致；达梦 / PG 镜像待两库激活。
- 回填是**数据任务**，非代码缺陷；上线前须由运维保证非 ALL 用户 `zone_codes` 已分配（否则仍零可见）。
