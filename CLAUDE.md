# Claude Code 项目规则 — 安全管控指挥系统后端脚手架

> 本文件是 Claude Code 在本仓库的入口。**完整约束以根目录 `AGENTS.md` 及其分层 `AGENTS.md` 为准**；本文件只补充 Claude 动手前必须在脑中建立的"优先级与红线"，不替代 `AGENTS.md`。
> 核心定位：**AI 是规格执行者，不是自由设计者**——所有设计 / 接口 / 流程决策都有唯一真相源，AI 必须读取并服从，不得自行创造平行体系。

## 1. 规则入口与层级

- 动手前**先读根 `AGENTS.md`**（含 §1 分级 L0–L4、§2 验证矩阵、§3 API 契约、§6 既有约束与红线、§8 L4 硬门禁、§11 跨库协作）。
- 改子目录时按 §6.6 读取链**向下读到对应层**：`docs/api/README.md`、`docs/architecture/README.md`、`docs/AGENTS.md`。
- 优先级（冲突时从高到低）：平台/人工指令 > 根 `AGENTS.md`（含 §3 契约、§6 红线、§11 跨库规则）+ 前端库契约真源 > 已确认 `openspec/changes|specs` > 当前 Change 的 `tasks.md` > Skill/插件方法。分层 `AGENTS.md` 只能加严、不得放宽。

## 2. 改动分级（先判级，再动手）

- **L0 问答/只读** → 直接完成；不建文件、不起子 Agent、不调 openspec。
- **L1 微小修改**（四门槛全满足：不改业务能力/接口契约/权限语义/数据结构、≤3 文件、5 分钟内可验证、不新增生产依赖）→ 直接改 + 最小验证。
- **L2 工程维护**（依赖/构建/配置/脚手架/非业务技术债，或 L1 门槛缺一）→ 说明方案与影响 → 执行 → 跑受影响目标验证。
- **L3 业务能力**（接口能力/业务规则/状态流转/权限语义/数据模型）→ openspec 提案 → 人工确认 → TDD 实施 → 验收 → 归档。
- **L4 高风险**（契约语义 / 权限模型 / 数据库结构 / 安全过滤器链 / 部署配置基线 / 框架升级）→ 按 L3 且**实施前取得人工确认**。
- L1/L2 **禁止**创建 openspec Change、计划文档或子 Agent；分级只决定流程重量，不豁免 §3 契约、§6 红线与 §2 验证矩阵。

## 3. 真相源（唯一，禁止平行体系）

- 业务规格与任务：`openspec/changes/<name>/`（proposal/design/tasks/spec-delta 四件套，`tasks.md` 是唯一任务真源，状态只回填勾选框）。
- 系统现状：`docs/`（长期共识）；短期过程记录：`engineering/`（qa/retro/plans），二者职责不混。
- **接口契约：机器可读真源在前端库 `frontend-scaffold/docs/api/*.openapi.json`**；本库禁止复制第二份契约，改接口必须同交付同步前端契约文件。
- 禁止在 `openspec/` 之外建第二套需求规格 / 任务清单 / issue 体系。

## 4. 绝对红线（任何改动）

1. **零下行控制**：后端不提供下行控制写接口；`HardControlInterceptor.HARD_CONTROL_PATHS` 名单内 POST/PUT/DELETE 一律 `code=503` 拒绝；WS 通道同样不得下发控制指令。
2. **B3 统一包络**：所有响应走 `common/Result<T>`（`code`/`message`/`data`/`traceId`），`code=0` 为成功；禁止自定义第二套响应外壳或裸返实体。
3. **20 位 MDM 设备编码**：设备对外主键固定 20 位，用 `@DeviceCode` 校验，禁止自创物理主键。
4. **防重放签名**：生产强制 `HmacFilter` 校验 `X-Timestamp`/`X-Nonce`/`X-Signature`；禁止在生产旁路签名。
5. **无状态令牌**：JWT 不落盘不写 Cookie；`jwt.secret` / `DB_PASSWORD` / `SIGNATURE_SECRET` 只从环境变量注入。
6. **错误码分段**：`1xx` 通用 / `2xx` 鉴权 / `3xx` 设备 / `5xx` 硬控；新增码同步 `ResultCode` + 前端契约 + 调用方。
7. **分层不越界**：Controller 不写业务、Service 不感知 HTTP、Mapper 不写业务判断、Entity 不直接做出参。
8. **数据库**：禁止操作生产库；DDL 由 Flyway 接管（双轨：`V1` 快照 + `V` 增量，三方言 `db/migration/{h2,postgresql,dameng}`），已发布 `V` 文件禁止改/删，结构变更属 L3/L4 须附回退方案（AGENTS §6.4）。

## 5. 工程约定

- **Git 提交**：`type(scope): 描述`，scope 固定枚举 `auth/device/alarm/dashboard/security/common/db/config/docs/chore`，禁止自造；横切层（`common`/`security`）先行、业务域跟随；提交信息单行成句、禁止分点列表；禁止提交临时输出（`mvn-out.txt`、`nohup.out`）。
- **验证矩阵**（改动后只跑对应一行，禁止 L1/L2 后连跑 compile+test+package 三套）：文档 `git diff --check`；单类 `./mvnw -q compile`；横切层 `./mvnw test`；数据层 `./mvnw test` + `scripts/smoke-test.ps1`；构建配置 `./mvnw clean package -DskipTests`；对外接口 `node scripts/check-api-contract.mjs` + `./mvnw test`；L3/L4 按 tasks 验收全量。
- **Windows 工程**：用 `./mvnw` / `mvnw.cmd`，禁止裸 `mvn`；清理用 `./mvnw clean`，禁止 `rm -rf target`；杀 Java 用 `taskkill /PID <winpid> /F /T`（Git-Bash `kill` 无效）。
- **Review 结论三选一**：通过 / 需修改 / 需人工决策（禁止"基本可以"等模糊结论）。

## 6. 范围边界（跨库定位）

- 本仓库是**后端服务库**，与 `frontend-scaffold` 平级双库、非 monorepo；AI 不得改动前端源码，也不得在本库复制第二份 OpenAPI 契约。前后端接口以 §3 契约真源 + 已确认 openspec 为准。
- AI 负责需求分析/技术设计/编码/测试/Review/文档；需求确认、架构决策、权限确认、数据库结构确认、最终合并由人负责。AI 禁止操作生产、改生产数据、自造业务规则、自建权限模型、未经批准改 DB 结构、未确认引依赖。

## 7. OpenSpec 可执行命令（可选）

本仓库已启用 openspec（`schema: spec-driven`）。Cursor 侧可用 `openspec` CLI，见 `.cursor/commands/opsx-*.md`（需本地安装 `openspec` CLI）。WorkBuddy/Claude 侧直接按 `AGENTS.md` §7 四件套执行。

## 8. 开工前必读（路线图 / 进度台账 / 归档纪律）

L3 / L4 开工前，按序读取：

1. **跨库路线图**：`docs/architecture/roadmap.md`（能力依赖顺序与阶段完成判据）。
2. **进度台账**：`engineering/plans/end-to-end-development-progress-tracker.md`（各能力域状态与证据）。
3. **当前已确认 Change**：`openspec/changes/<name>/`——其 `tasks.md` 是唯一实施依据；路线图只规定依赖顺序，不授权跳过已确认范围。

收尾纪律：

- `tasks.md` 全勾后**同一次交付内**完成 spec 回填（→ `openspec/specs/<capability>/`）并归档到 `openspec/archive/<YYYY-MM-DD>-<name>/`（详见 AGENTS §7.1；CI 由 `scripts/check-openspec-hygiene.mjs` 守门）。
- 更新进度台账：状态、Change、日期、验收证据（QA 文件链接）、阻塞项、行更新时间。
- 判据：**Mock、桩服务、单层代码、建表/枚举完成，不得作为能力「已完成」的依据**；只有端到端验收 + 回归全绿 + 文档同步通过才可关闭。
