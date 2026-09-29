# Proposal: tv-monitor-crud（工业电视监控点台账 CRUD + 防区归属编辑）

## 背景
`fac_tv_monitor`（工业电视监控点）此前只有只读端点 `GET /tv/monitors` 与 `GET /tv/monitors/{code}`，供大屏地图撒点与监控点详情使用。管理后台「设备管理」域尚无对监控点（摄像头台账）的**写**能力，操作员无法在前端新增/编辑/删除监控点，也无法将监控点归属到防区（V86 已建立 `sys_zone` 防区维度，`fac_tv_monitor.zone_code` 已落库但无写入入口）。

本变更补齐设备/防区管理写侧：新增 `POST /tv/monitors`、`PUT /tv/monitors/{code}`、`DELETE /tv/monitors/{code}` 三个写端点，配套权限码 `tv:monitor:create/update/delete`，并登记三方言种子（授权 ADMIN + 5 岗位角色）。

## 分级：L3
- 复用既有表 `fac_tv_monitor`，仅新增业务写端点与权限码，不改表结构、不引入新实体。
- 属 L3（新增业务读写端点 + 带权限码），按 L3 纪律走 openspec 四同步即可。
- 契约真源在 `frontend-scaffold/docs/api/tv.openapi.json`（前端仓），本仓只实现、不复制第二份主契约。

## 范围
1. 后端：新增权限码 `tv:monitor:create/update/delete`（V87 三方言种子，授权 ADMIN/COMMANDER/SCHEDULER/TEAM_LEADER/INNER_OPER/OUTER_OPER）；DTO `TvMonitorUpsertRequest`；`TvService.createMonitor/updateMonitor/deleteMonitor`（`@RealtimeSync(domain="tv.monitor")`）；`TvController` 三端点（`@RequireAuth`）。
2. 前端（见同名前端 Change）：扩展 `docs/api/tv.openapi.json`；生成类型；`services/tv.ts` 加 `createTvMonitor/updateTvMonitor/deleteTvMonitor`；新增 `apps/mgmt/views/monitor/TvMonitorMgmtView.vue` 台账 CRUD 页（设备/防区筛选 + 防区归属编辑，v-permission 按钮级显隐）+ 路由 + 菜单。
3. 契约四同步 + 双仓守门 + 按 scope 提交。

## 人工确认关卡（与既有范式一致）
- [x] **权限策略**：新增三个独立权限码，三方言种子登记并授权 ADMIN + 5 岗位角色（照抄 V83 `tv:snapshot:ack` 范式）。增/改/删权限解耦。
- [x] **更新语义**：`updateMonitor` 仅覆盖非空字段（read-modify-write），`online` 为 `Boolean` 包装类型——传 `false` 即置离线、`null` 即不更新，避免误清零。
- [x] **唯一约束**：`monitorCode` 全局唯一，重复后端返 `PARAM_INVALID`。
- [x] **实时广播**：三个写端点均标注 `@RealtimeSync(domain="tv.monitor")`，写后大屏地图撒点与管理页列表自动刷新。
- [ ] 端到端冒烟验证（隔离实例 curl 跑通创建/更新/删除 + 负例 401/403/重复编码）。
- [ ] 按 scope 拆分提交（backend: db / common / docs(openspec)）+ 推送。

## 不在范围
- 不引入新实体/表结构变更（V 文件已应用到 dev 文件库，禁止回改）。
- 不接入真实录像流（设备/防区管理页聚焦台账与防区归属；真实历史回放由抓拍快照时间轴满足，本期不做）。

## 待办 openspec（前端同名 Change 描述契约/页面部分）
- 前端契约 `tv.openapi.json` 已新增 `post /tv/monitors` 与 `put/delete /tv/monitors/{code}` + `TvMonitorUpsertRequest` schema。
- 管理页 `TvMonitorMgmtView.vue` 已落地。
