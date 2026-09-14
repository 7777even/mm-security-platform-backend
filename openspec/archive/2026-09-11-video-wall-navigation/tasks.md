# 任务：视频墙导航聚合端点

## 后端

- [x] T1 V37 三方言迁移：`fac_video_wall_node` 建表 + 135 行种子（脚本生成，h2/postgresql 多行 VALUES、dameng 逐条 INSERT）
- [x] T2 实体 `FacVideoWallNode` + `FacVideoWallNodeMapper`
- [x] T3 DTO `VideoWallNavigation` / `VideoWallGroupNode` / `VideoWallCamera`
- [x] T4 `VideoService.wallNavigation()`（目标树 / 厂区视频目录派生通道 / 映射 / 高空AR）
- [x] T5 `VideoController` `GET /wall-navigation`（登录可读，同 `/video/*` 既有口径）
- [x] T6 `VideoServiceTest` 补 `wallNavigation` 单测（树形/派生/映射/数量断言 + 空表空结构）
- [x] T7 全量 `mvn -s ci-settings.xml test` 全绿（448 passed，jacoco 覆盖率达标）

## 契约

- [x] T8 `frontend-scaffold/docs/api/video.openapi.json` 新增 `/video/wall-navigation` + 3 schema（中文 description + example）
- [x] T9 `check-api-contract.mjs --strict` 路由 0 差异 / schema 0 漂移
- [x] T10 `validate-api-contracts.mjs` 28 域通过

## 前端

- [x] T11 `services/video.ts` 新增 `fetchVideoWallNavigation()`（三态：live/demo/offline）
- [x] T12 `videoWallStore.ts` 四份数据改空态 + `loadVideoWallNavigation()`；删除模块期代码生成；默认高空AR模式改为加载后构建（m2 仍由前 2 目标各前 2 通道派生，口径不变）
- [x] T13 `VideoWallView.vue` 挂载调用 loader
- [x] T14 门禁 `screen-local-data-gate.mjs`：`REGRESSION_GUARDS` 锁 `videoWallStore.ts` 禁再出现生成逻辑 + `REQUIRED_GUARDS` 必须引用 backendFallback
- [x] T15 前端三绿：`gate:screen` PASS / `vue-tsc` 0 错 / `vitest run` 389 passed；实机冒烟 `smoke_wall_navigation.py` PASS（6 分类/120 目标/5 厂区/954 通道/4 高空AR/映射口径逐位对齐）
