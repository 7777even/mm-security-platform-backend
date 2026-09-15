# Proposal: ④-C 管理台账扩展 6 个生产必需域

## Why
④ 业务域剩余闭环要求把原型的若干静态域真后端化。其中 6 个生产必需域
（key-location / incident-archive / drill-script / linkage-unit / emergency-pool / ef-medium）
复用已落地的 mgmt-ledger 通用只读能力，仅需种子化数据 + 前端路由登记，
无需新建 Controller，避免重复造轮子。另 5 个 comm-* 域（短信/呼叫/广播/推送/对讲）
依赖产品确认通信网关能力，按红线冻结（见 docs/frozen-prototype-domains.md）。

## What Changes
- Flyway V52（h2 / dameng / postgresql 三方言）向 mgmt_ledger_meta/row/cell 种子化
  6 个域的元数据与示例数据；列定义沿用前端 mgmtMenus 原型页约定。
- 前端 router.ts 将 6 个路径加入 SERVICE_PATHS 与 MGMT_LEDGER_PATHS，
  复用 MgmtLedgerView.vue 渲染。
- 前端 protoPages.ts 移除这 6 个 slug（已非原型嵌入页）。

## Capabilities
- 后台管理端 6 个域现在通过既有 GET /api/v1/mgmt-ledger/{domain} 返回真实数据，
  复用既有 mgmt-ledger 契约（docs/api/mgmt-ledger.openapi.json），无新增对外接口。

## Impact
- 仅新增只读种子数据，无表结构变更、无对外接口 schema 变更、无存量数据影响。
- 回退：删除 V52 迁移或清空 6 个域数据即可，风险极低。
- 达梦/PostgreSQL 方言 V52 沿 V51 的「未实跑」状态，待 DM8/PG 实例 flyway migrate 校验。
