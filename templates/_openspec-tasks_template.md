# 任务清单：<变更名称>

> 适用：L3 / L4。任务须可勾选、单条 ≤2h；标注 [TDD] 的先写失败测试再实现（openspec/config.yaml）。
> 任务状态只回填此处，禁止在 engineering/ 另立第二套任务清单（AGENTS.md §4）。

## 1. <模块 / 阶段一>

- [ ] <任务描述（具体类 + 改动内容）。>
- [ ] [TDD] <先写期望测试（`src/test/java/.../XxxTest.java`），跑红，再实现；覆盖成功路径 + 存在性 / 唯一性 / 状态 / 权限护栏。>

## 2. <模块 / 阶段二>

- [ ] <...>
- [ ] [TDD] <...>

## 3. 数据层（如涉及）

- [ ] <更新 `src/main/resources/schema.sql` 快照 + 在 `resources/db/` 新增日期前缀增量 SQL。>
- [ ] <存量数据影响评估与回退方案已写入 proposal / design。>

## 4. 契约与守门验证

- [ ] <跨库四同步：同步前端 `docs/api/<domain>.openapi.json`（四条铁律），跑 `node scripts/check-api-contract.mjs`。>
- [ ] <按 AGENTS.md §2 验证矩阵对应行执行（`./mvnw -q compile` / `./mvnw test` / `smoke-test.ps1`），实际命令与结果记入 `engineering/qa/`。>

## 验收标准（Definition of Done）

- [ ] `tasks.md` 全部勾选，验收标准逐条满足。
- [ ] 受影响目标 `./mvnw test` 0 failure，接口链路冒烟通过（按 §2 矩阵对应行，不连跑三套）。
- [ ] 代码若改变契约 / 行为 / 数据结构，同步 `docs/` 与前端库 `docs/api/*.openapi.json`（短期记录不写进 docs/）。
- [ ] 提交按 scope 拆分：`type(scope): 描述`（scope ∈ auth/device/alarm/dashboard/security/common/db/config/docs/chore），单行成句、禁止 `- ` 分点列表；临时输出文件不入库。
- [ ] L3 / L4 完成后即刻写 `engineering/qa/` + `engineering/retro/`，不攒到最后补。
