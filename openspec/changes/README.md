# openspec/changes/ — 进行中的变更提案

本目录**只存放进行中（未完结）的 OpenSpec Change**，每个 Change 为四件套：

- `proposal.md`（Why / What / Capabilities / Impact + 人工确认关卡）
- `design.md`（架构、决策 ADR、风险、依赖、数据影响）
- `tasks.md`（≤2h 可勾选任务；**唯一任务真源**，状态只回填勾选框）
- `spec-delta.md`（新增 / 修改 / 移除 三段，与 `openspec/specs/<capability>/spec.md` 同构）

## 闭环纪律（AGENTS.md §7.1）

1. `tasks.md` 全部勾选后，**同一次交付内**必须完成：
   - 将 `spec-delta.md` 内容合入 `openspec/specs/<capability>/spec.md`（新建或扩充 capability）；
   - `git mv` 本 Change 到 `openspec/archive/<YYYY-MM-DD>-<name>/`。
2. 已完结 Change 不允许滞留本目录；CI 以 `node scripts/check-openspec-hygiene.mjs` 守门（全勾未归档即失败）。
3. 禁止在本目录之外建立第二套需求规格或任务清单。
