# 双库 CI 发布门禁对齐

> 后端 `main` / 前端 `feature/scaffold-rebuild` 两仓各自独立，但发布门禁须口径一致，避免"各自为政"。
> 本文是两仓 CI 门禁的真相源与对齐决策记录；前端仓 `docs/ci-gate-alignment.md` 为本文件的一致副本。

## 1. 门禁矩阵

| 门禁类别 | 后端 CI（`.github/workflows/ci.yml`） | 前端 CI（`.github/workflows/ci.yml`） | 对齐结论 |
| --- | --- | --- | --- |
| 编译 / 类型检查 | `mvn test` 隐含编译 | `npm run type-check`（vue-tsc） | ✅ 双端覆盖 |
| 单元测试 | `mvn -B -ntp test`（standalone MockMvc + Mockito，不起 Spring 上下文） | `npm run test`（vitest run） | ✅ 双端覆盖 |
| 覆盖率门禁 | JaCoCo 行覆盖 ≥ 0.80（`pom.xml` `jacoco.line.coverage.min`，跌破即失败） | ⚠️ 暂未启用（缺 `@vitest/coverage-v8` 与阈值，且未实测基线） | 🔲 待补（见 §3） |
| 契约守门 | `scripts/check-api-contract.mjs --strict`（实现 ↔ 契约漂移，路由 + schema 字段级） | `scripts/validate-api-contracts.mjs`（契约四铁律：summary / description / example / 字段 description + `$ref` 可解析） | ✅ 双端互补（作者侧 + 实现侧） |
| OpenSpec 卫生 | `scripts/check-openspec-hygiene.mjs` | `scripts/check-openspec-hygiene.mjs`（同款脚本） | ✅ 双端同款 |
| 构建产物可追溯 | 上传 JaCoCo 报告（`target/site/jacoco/`） | 上传 `dist/`（`actions/upload-artifact@v4`，`if: success()`） | ✅ 已对齐（前端补齐上传） |

## 2. 已落地的对齐动作

- 前端 CI 在 `npm run build` 后新增 `dist/` 构建产物上传，与后端上传 JaCoCo 报告形成对称的产物可追溯性。
- 契约守门双端互补：前端作为契约真源守"作者侧四铁律"，后端作为实现侧守"实现 ↔ 契约漂移"，任一漂移即失败。

## 3. 待补项与前置条件（不阻塞当前发布）

前端覆盖率门禁（对齐后端 JaCoCo 0.80）：

1. 安装 `@vitest/coverage-v8`，在 `vitest.config.ts` 配置 `test.coverage`（`provider: 'v8'`、`reportsDirectory`、`thresholds.lines`）。
2. **先本地 `vitest run --coverage` 实测当前行覆盖率基线**，据基线设 `thresholds.lines`（建议从当前基线扣 2~3 个百分点起步，逐步抬到 0.80）；禁止凭空设阈值导致 CI 红。
3. 将 `npm test` 改为 `vitest run --coverage`，并在 CI 上传 `coverage/` 报告。

## 4. 变更纪律

任一门禁阈值（覆盖率、契约严格度、type-check）的调整属 L3，须同步更新本文件与对端仓，并在 commit 中说明。
