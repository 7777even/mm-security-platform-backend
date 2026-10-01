# Design: 应急事件与流程填报补齐编辑/删除写端点

## 1. 为什么更新请求不接受分组维度字段

`EmergencyEventUpdateRequest` 刻意**不含** `scene / kind / eventCategory / groupCode / groupLabel`。
大屏侧栏是按 `group_code` 聚合的，若允许编辑时改分组，会出现两类问题：

1. 事件被挪到一个库内不存在的新分组 → 侧栏出现游离分组；
2. 组内事件被移走后原有分组变空 → 侧栏出现空分组。

分组维度在新增时由「事件类型」登记表（`EMERGENCY_EVENT_TYPE_DEFS`）一次性确定，之后不再变动。
这是**有意的收窄**，不是遗漏。

## 2. 局部更新 vs 全量覆盖

选局部更新（`null` = 不修改）而非全量覆盖，原因：

- 前端「编辑」弹窗只提交用户实际改动的字段，全量覆盖会让未渲染字段（如 `weatherMeta` 系列）被清空；
- 与既有 `BusinessWriteService` 四域（应急指令 / 值班签到 / 台风调度 / 巡更执行）口径一致。

代价：无法把某字段显式置空。当前无此需求，出现时再引入空值哨兵。

## 3. status 与 statusLabel 的联动规则

| 传入 | 结果 |
| --- | --- |
| 只传 `status` | 按枚举推导中文标签（pending→未处置 / processing→处置中 / done→已处置） |
| 都传 | 以传入为准（允许自定义态势文案） |
| 只传 `statusLabel` | 只改标签，不动 status 枚举 |

`status` 非法直接 `PARAM_INVALID`，不做静默回落——静默回落会让调用方误以为状态已生效。

## 4. 删除的级联顺序

```
fac_accident_detail_field  (按 incident_id 删)
        ↓
fac_accident_incident      (按 event_id 查，逐条删)
        ↓
fac_emergency_event        (按 id 删)
```

反序会留下「有详情无事件」的孤儿行，让 `/accident/rescue-incident` 聚合出现空面板。
单测用 `InOrder` 固定该顺序，防止后续重构无意调换。

## 5. 权限口径分层（本 Change 唯一有争议的取舍）

既有三个写端点（create / report / start-response）原本是裸 `@RequireAuth`（仅登录态），
大屏 `useFireEmergencyEventList` 与 `AccidentEmergencyRescue` 都在调用。
若统一收 `emergency:event:write`，值守岗（非 ADMIN/COMMANDER/SCHEDULER）会立刻 403，
属于**打断既有业务**的破坏性变更。

故：**新增的 update / delete 收权限码，既有自助端点维持原状**。
代价是权限粒度不一致，已在 Controller 类注释里写明两类口径，避免后人误读为遗漏。

## 6. 流程填报主键：为什么不再依赖自增序列

V66 种子用 `INSERT ... (id, ...) VALUES (1, ...), (2, ...)` **显式指定 id**。
H2 / PostgreSQL / 达梦的自增序列（identity / serial / IDENTITY）都只在不指定 id 时推进，
显式插入不会同步序列，于是后续新增从 `id=1` 起跳 → 撞主键 → `DataIntegrityViolationException`
→ 前端看到 409「数据冲突」，dev 环境「新建填报」完全不可用（真机复现三次均失败）。

候选修法对比：

| 方案 | 评价 |
| --- | --- |
| 新增迁移对齐序列 | 三方言语法各异（H2 `ALTER COLUMN ... RESTART WITH` / PG `setval` / 达梦 IDENTITY 重置），且需动态取 `max(id)+1`，静态 SQL 难写、易撞号 |
| 改种子不显式插 id | 已执行的迁移不可改（Flyway checksum），对既有库无效 |
| **代码显式分配 `max(id)+1`** | 三方言行为一致、零迁移、对既有多态库都生效 |

选第三种。代价：并发插入可能算出同一个 id 而撞主键——填报是低频人工操作，
且失败会返回明确 409 提示重试，优于「每次新增都必失败」的现状。

## 7. 广播域补挂

`emergency.event` 与 `form.record` 是**新增域**，此前无人订阅。本 Change 在后端把
create / update / delete / report / start-response 全部挂上 `@RealtimeSync`，
前端 mgmt 侧用 `useDomainAutoRefresh` 接线。大屏侧接入属下一批（大屏应急事件列表有本地草稿
回落机制，接入订阅需先评估与草稿的冲突，不在本 Change 内草率接线）。
