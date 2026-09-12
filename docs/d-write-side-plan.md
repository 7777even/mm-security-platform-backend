# D 类写侧（4 块业务写能力）分步方案

> 状态：**方案文档，待产品 / 架构拍板后再实施**。不写业务代码，只给分步路径、事实依据与决策点。
> 关联：`security/HardControlInterceptor.java`、`frontend-scaffold/src/services/hardControlGuard.ts`、`WebMvcConfig.java`。
> 日期：2026-09-12

---

## 0. 先厘清两个不同概念（避免混淆）

本方案涉及的"写"有两类，**红线约束完全不同**：

### (A) 零下行硬控红线（物理设备控制）—— 不可放开
- **后端**：`HardControlInterceptor` 拒绝以下前缀的 POST/PUT/DELETE（返回 503 HARD_CONTROL_BLOCKED）：
  `/api/v1/devices/cmd`、`/devices/control`、`/fire/release`、`/fire/suppress`、`/doors/lock`、`/doors/unlock`、`/broadcast/issue`、`/emergency/trigger`。
- **前端**：`hardControlGuard.ts` 的 `HARD_CONTROL_PATTERNS` 在请求发出前抛 `HardControlViolation`：
  `fire-pump`、`broadcast/(force|cut|power)`、`door-lock/(power|lock|unlock)`、`evacuation/trigger`、`sprinkler/control`。
- **性质**：消防泵、广播强切、门禁断电、疏散触发、喷淋控制——直接作用于物理安全设备，**只监不控**，属安全/合规红线，无论产品是否同意都**必须保持拦截**。

### (B) 4 块业务写能力（业务记录）—— 当前只读，可评估放开
- 这 4 类**不在**硬控清单内，是业务域内的"记录/状态变更"，不是物理下行。
- **现状：后端全部 GET 只读，前端无对应写 API**（即"未实现写"，而非"被红线拦"）。

| 业务写域 | 对应只读端点（现状） | 若要启用写，涉及 |
|---|---|---|
| 应急指令下发 / 状态推进 | `GET /emergency/commands`、`GET /emergency/commands/{id}`（仅读指令记录） | 新增 `POST /emergency/commands`、`PUT /emergency/commands/{id}` 推进状态 |
| 台风资源调度 | `GET /typhoon/dispatch-resources`（只读资源） | 新增调度单写端点（指派/确认） |
| 巡更 | `GET /fire/patrols`、`GET /security/patrol-cameras`（只读） | 新增巡更记录/打卡写端点 |
| 值班签到 | `GET /emergency/duty`（只读排班） | 新增签到写端点 |

> 注：当前唯一已存在的写端点是 `PUT /emergency/process/node-configs`（流程节点配置），非硬控、已放行——说明系统**已有**放开业务写的先例与机制，并非全禁写。

---

## 1. 已核证的事实

- 4 个写侧域 controller 经映射扫描：**除 `PUT /emergency/process/node-configs` 外，全部为 `@GetMapping`**，即后端对这些域只提供读。
- `HardControlInterceptor` 与 `hardControlGuard.ts` 的红线清单**不完全一致**（后端按显式前缀、前端按正则；fire/release vs fire-pump、broadcast/issue vs broadcast/(force|cut|power) 等表述不同）——存在两端定义漂移风险。
- 物理下行路径（devices/cmd、fire/release、doors、broadcast/issue、emergency/trigger 等）后端已硬拦，前端也拦，双重兜底。

---

## 2. 风险清单

- **R1 红线误放开**：若把"应急指令下发"实现为直接触发物理动作（如调用 /emergency/trigger），会撞硬控红线被拦；若语义不清，可能被误判为可放行。**必须先把"指令下发"的语义（系统内部指令记录 vs 物理触发）界定清楚**。
- **R2 前后端红线漂移**：两端清单不一致，新增端点时易只在一端登记，留下未兜底路径。须统一为单一真源。
- **R3 审计缺失**：业务写（尤其应急指令、调度）若不加审计，事后无法追溯谁改了什么，合规风险高。
- **R4 权限越权**：写端点须配独立权限码（如 `patrol:write`、`duty:sign`、`typhoon:dispatch`、`emergency:command`），不能复用只读权限。
- **R5 跨方言迁移**：写侧新表须走 `V46+` 新增迁移，禁改 V1–V45；达梦/PG 镜像激活时同步。

---

## 3. 分步方案（待拍板后实施）

### 步骤 0 · 产品界定 4 类写的范围与合规边界
- 逐类确认是否启用写（应急指令/状态推进最敏感，建议单列评审）。
- 明确"应急指令下发"**不**得触发物理设备（物理触发仍走红线禁止）；仅允许系统内部指令记录与状态推进。

### 步骤 1 · 统一零下行红线为单一真源（决策 D5，对应 R2）
- 将前端 `HARD_CONTROL_PATTERNS` 与后端 `HARD_CONTROL_PATHS` 收敛为**同一份清单**（建议后端暴露契约/常量，前端引用，或双方都从 openspec 契约生成）。
- 新增任何下行端点必须在此清单显式登记并评审（已在两端注释约定，但无强制同步机制）。

### 步骤 2 · 数据建模（决策 D2）
- 为 4 类写侧新增业务表（如 巡更记录、值班签到、台风调度单、应急指令单）走 `V46+` 迁移；禁改既有 V。
- 写侧表含：操作人、操作时间、前/后值、审计字段。

### 步骤 3 · 后端写端点 + 权限 + 审计（决策 D3/D4）
- 新增 `@PostMapping`/`@PutMapping` 写端点（**避开**硬控前缀，避免被 `HardControlInterceptor` 误伤）。
- 配独立权限码（沿用 `router/menu.ts` + `meta.perm` 对齐约定）。
- 复用或新增审计拦截器（参考 `PasswordLifecycleInterceptor` 风格，对变更类请求落审计日志）。

### 步骤 4 · 前端写 API + 交互
- 在 `services/` 定义写接口，表单/交互确认**不命中** `guardHardControl`（路径不进入硬控正则）。
- 写操作走三态取数（live/demo/offline）已有兜底，无需重写。

### 步骤 5 · 回归与契约
- 契约四同步（openspec → 前端契约 → 后端实现 → `check-api-contract --strict` → `gen:api-types`）。
- 前端 `vitest` + 后端 477 基线 + `gate:screen` 全过。

---

## 4. 待拍板的决策点（D 系列）

| ID | 决策 | 选项 | 影响 |
|---|---|---|---|
| **D1** | 4 类业务写是否启用 | 全启用 / 仅启用低敏感（巡更、值班签到）/ 全保持只读 | 决定本专项是否落地 |
| **D2** | 写侧数据模型 | 新增独立业务表（推荐）/ 复用现有读表加列 | 数据模型与迁移 |
| **D3** | 审计与权限 | 强制审计 + 独立权限码（推荐）/ 暂不审计 | 合规与越权风险 |
| **D4** | "应急指令下发"语义 | 仅系统内部指令记录（可放行）/ 含物理触发（红线禁止） | 直接决定该域能否写 |
| **D5** | 红线单一真源 | 后端常量 + 前端引用（推荐）/ 双端各维护并加同步校验 | 防 R2 漂移 |

---

## 5. 结论

D 类写侧**不是被红线拦死，而是当前未实现写**（后端 GET 只读、前端无写 API）。
其中物理下行控制（devices/cmd、fire/release、doors、broadcast/issue、emergency/trigger 等）属**不可放开的红线**；
而 4 块业务写域是业务记录，可由产品决策是否启用，但须先界定语义（尤其应急指令）、统一红线清单、补审计与权限。
属需产品/架构拍板的专项，本文档即其实施蓝图；在拍板前不应擅自为这 4 域添加写端点。
