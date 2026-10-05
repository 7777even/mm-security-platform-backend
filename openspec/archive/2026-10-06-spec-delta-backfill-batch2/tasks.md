# Tasks: 第二批 spec-delta 回填（缺口清零）

- [x] 用 `--gaps` 取出剩余 66 条未归属端点清单并按域分组
- [x] 逐域在 `openspec/archive/` 检索 delta 原文，区分「有 delta」与「需反推」
- [x] 新建 `device` capability spec（3 端点）
- [x] 补充 `system-management`（15：字典类型/字典项/菜单/权限码四组 CRUD + 启停用）
- [x] 补充 `rescue-resource`（14：四台账 CRUD + 按 id 读）
- [x] 补充 `communication`（7：devices CRUD + records PUT/DELETE）
- [x] 补充 `emergency-reference`（7：指令/签到 PUT/DELETE + process.* 三端点）
- [x] 补充 `production-alarm`（7：生产域只读端点 + 报警抓拍关联）
- [x] 补充 `tv`（5：inspections / maintenance-orders / map-points / overview）
- [x] 补充 `dashboard-analytics`（3：overview / workstations/{id} / messages）
- [x] 补充 `mgmt-ledger`（3：rows 写端点）
- [x] 补充 `auth-rbac`（1：`PUT /auth/profile`）
- [x] 补充 `emergency-event`（1：`GET /emergency-events/evacuation-people`）
- [x] 重跑 `--gaps` 确认缺口 0 / 288、门禁复跑后归档提交推送
