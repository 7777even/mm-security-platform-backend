# 双库 CI 发布门禁对齐

> 后端 `main` / 前端 `feature/scaffold-rebuild` 两仓各自独立，但发布门禁须口径一致，避免"各自为政"。
> 本文是两仓 CI 门禁的真相源与对齐决策记录；前端仓 `docs/ci-gate-alignment.md` 为本文件的一致副本。
>
> ⚠️ **维护要求**：门禁的增删改**必须同步本文两份副本**。本文曾长期停留在「前端覆盖率门禁暂未启用」的旧状态（实际早已启用），属**失真**——改门禁时请顺手核对本表与两份 `ci.yml` 是否一致。

## 1. 门禁矩阵（2026-09-16 按两份 `ci.yml` 实测重写）

| 门禁类别 | 后端 CI（`.github/workflows/ci.yml`） | 前端 CI（`.github/workflows/ci.yml`） | 对齐结论 |
| --- | --- | --- | --- |
| 编译 / 类型检查 | `mvn -B -ntp test` 隐含编译 | `npm run type-check`（`vue-tsc`） | ✅ 双端覆盖 |
| 静态检查 | ❌ 无等价步骤（无 checkstyle / spotbugs） | `npm run lint`（ESLint，**0 error** 为准） | 🟡 后端待补（见 §3） |
| 单元测试 | `mvn -B -ntp test`（standalone MockMvc + Mockito，不起 Spring 上下文；**619 例基线**） | `npm run test:coverage`（vitest，**316 例 / 52 文件基线**） | ✅ 双端覆盖 |
| 覆盖率门禁 | JaCoCo 行覆盖 ≥ **0.80**（`pom.xml` `jacoco.line.coverage.min`；**无 excludes**，全类参与，实测分析 520 类） | vitest v8 阈值：stmts/lines ≥ 80、branch ≥ 70、funcs ≥ 65（`vite.config.ts`） | ✅ 双端已启用，**但强度不同**：前端 `coverage.all:false` + `include` 白名单 → 未被测试加载的文件不插桩，**只防存量回退、不防新增零测试**（见 §3） |
| 契约守门（实现侧） | `scripts/check-api-contract.mjs --strict`（契约 ↔ 后端实现，路由 + schema 字段级；可比 249 schema） | 同脚本（稀疏检出后端 `scripts + src/main/java` 后对拍） | ✅ 双端互补（作者侧 + 实现侧） |
| 契约守门（作者侧） | — | `scripts/validate-api-contracts.mjs`（四铁律：`summary` / `description` / example / 字段中文 `description` + `$ref` 可解析） | ✅ 前端单侧（契约真源在前端） |
| OpenSpec 卫生 | `scripts/check-openspec-hygiene.mjs` | 同款脚本 | ✅ 双端同款 |
| **写端点授权覆盖** | **`scripts/check-endpoint-authz.mjs`**（写端点须带 `role=`/`perm=` 的 `@RequireAuth`，或进脚本 ALLOWLIST 并写理由；含名单腐烂检查） | —（前端只管显隐，授权一律由后端强制） | ✅ 后端单侧即可（2026-09-16 新增） |
| 领域专项门禁 | — | `npm run gate:screen`（大屏本地数据零回退）+ `npm run build:subapps && npm run gate:subapp-assets`（子应用产物悬空引用） | 🟡 前端单侧（后端无对应领域） |
| 构建 | `mvn -B -ntp test`（不单独打包） | `npm run build`（`vue-tsc -b && vite build`） | ✅ |
| 构建产物可追溯 | 上传 JaCoCo 报告（`target/site/jacoco/`） | 上传 `dist/` + `coverage/` | ✅ 已对齐 |
| 跨仓契约竞态 | 接收 `repository_dispatch(contract-updated)` 重跑 `contract-guard` | 契约变更且 `contract-guard` 绿后发 `repository_dispatch`（`notify-backend-contract`，需 `fetch-depth: 2`） | ✅ 已根治时序红 |
| 安全扫描 / 发布 | `trivy.yml` + `cd.yml` | — | 🟡 后端单侧 |

## 2. 已落地的对齐动作

- 前端 CI 在 `npm run build` 后上传 `dist/`，与后端上传 JaCoCo 报告形成对称的构建产物可追溯。
- 契约守门双端互补：前端守「作者侧四铁律」，后端守「实现 ↔ 契约漂移」，任一漂移即失败。
- 前端补齐覆盖率门禁（v8 + 阈值）与 `lint` 步骤，**已不再是"待补"状态**（本文旧版本此处失真，已订正）。
- 前端新增 `gate:screen` / `gate:subapp-assets` 两个领域专项门禁；后端新增 `check-endpoint-authz.mjs` 写端点授权门禁。
- 跨仓竞态由 `repository_dispatch` + 对端 `contract-guard` 重跑根治，不再依赖人工推送顺序。

## 3. 待补项与前置条件（不阻塞当前发布）

1. **后端 DB 集成测试层（`*IT` / Testcontainers）** —— 当前仅 1 例 H2 版 `DbLayerIntegrationIT`，三方言（h2/pg/dm）无真实容器验证。**前置：可用 Docker 环境**。
2. **前端 E2E** —— 现状靠 vitest + 视觉走查，无端到端自动化。前置：确定 E2E 框架与可运行的联调环境。
3. **前端覆盖率门禁强度** —— `all: true` 会全量插桩 OOM（`vite.config.ts` 注释已说明），故无法简单翻开关。可行方向：对**新增文件**做「必须有引用它的 `.spec.ts`」增量校验。
4. **后端 Java 静态检查** —— 与前端 ESLint 对齐，补齐 checkstyle / spotbugs 之类；属 L3。

## 4. 变更纪律

任一门禁阈值（覆盖率、契约严格度、type-check）或**门禁清单本身**的调整属 L3，须同步更新本文件**两份副本**与两端 `ci.yml`，并在 commit 中说明。
