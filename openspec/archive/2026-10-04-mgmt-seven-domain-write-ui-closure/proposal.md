# Proposal: 七域写端点权限码落地与前端写 UI 闭合

## 背景

第 1/2/3 批为监测 / 通信 / 生产共 7 个域补了 POST / PUT / DELETE 写端点，但当时为求快速落地，
统一用了临时授权 `@RequireAuth(role = "ADMIN")`，前端也只做了「订阅 → 重拉」半环——
管理端没有新增 / 编辑 / 删除的入口，写能力实际只对直连 API 的 ADMIN 可用。

## 目标

- **后端**：以 V106 迁移登记 7 个按钮级权限码并播种给角色，7 个 Controller 的写端点由 `role="ADMIN"` 换成正式 `perm`。
- **前端**：为 7 个视图补 `MgmtRecordEditDialog` 写弹窗、新增按钮与操作列，权限按钮用 `v-permission` 挂载；
  5 个 service 补齐 create / update / delete 方法。

## 非目标

- 不改请求 / 响应 schema，不改 HTTP 路径（`check-api-contract` 差异维持基线 12）。
- 不开嵌套明细写：重大危险源 7 类 JSON 明细列、特殊作业票三张子表仍不经写端点编辑。
- 不给「设备管理 / 通讯通知管理」新建可见菜单节点（见 design.md 的父菜单取舍）。
