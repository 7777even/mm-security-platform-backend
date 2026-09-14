# 提案：视频墙导航聚合端点（大屏「全后端化」P0-①）

> **状态：`approved`** —— 用户于 2026-09-11 确认按缺口清单顺序补齐（「做」），本提案为清单第一项。
> 分级 **L3**（新增只读端点 + 新增 1 张表，不触及鉴权 / 权限模型 / 既有表结构）。

## 背景

第三轮审计确认：大屏唯一「数据完全来自本地、无任何后端」的视图是视频墙
`screen/components/video-wall/videoWallStore.ts` —— `targetTree`/`videoTree`/
`cameraTargetMap`/`defaultHighAltitudeCameras` 由**模块加载期的代码生成逻辑**产出
（6 分类 × 20 目标 = 120 监测目标；每目标 4~12 路摄像头通道共 954 路，按 `floor((i-1)/24)`
分桶进 5 个厂区；1:1 通道→目标映射；4 路默认高空AR相机）。消费方 11 处，
核心渲染方为 `VideoWallSidebar.vue`。

既有结论「可直连 `GET /video/navigation` + `/video/cameras`，无需新端点」经逐字段复核**不成立**：
`/video/navigation` 是视频控制左导的单一 `tree`（应急演练/事件/巡检/重点监控），
与视频墙「目标树 + 相机目录 + 映射」三件套结构不等价，`fac_video_camera`（27 路）也不承载
通道目录语义。

## 目标

| 编号 | 内容 |
| --- | --- |
| A1 | 新增 `GET /video/wall-navigation`：一次返回 `targetTree` / `videoTree` / `cameraTargetMap` / `defaultHighAltitudeCameras` |
| A2 | 新增表 `fac_video_wall_node`（V37 三方言）承载 CATEGORY(6) / AREA(5) / HIGH_AR(4) / TARGET(120) 共 135 行 |
| A3 | 前端 `videoWallStore.ts` 四份数据改三态加载；`savedModes`/`savedPlans` 保留为本地 UI 偏好 |

## ADR

- **ADR-1 能落库的业务对象落库，能派生的占位数据派生**：目标/分类/厂区/高空AR是业务对象 → 落库；
  954 路摄像头通道是目标的占位衍生（编码 `v-{i}-{j}`、命名 `CAM-装置#{i:003}-通道{j}`），
  若物化建表需多写 954×2 行种子且无独立维护价值 → 由服务端按 TARGET 行 `cam_count`
  确定性派生，接真实 MDM 设备后改读设备表。
- **ADR-2 单表 + kind 判别**：四类节点共用 `fac_video_wall_node`（`node_kind` 判别），
  不建 4 张同构小表；TARGET 行内嵌 `parent_code`(分类) + `area_code`(厂区) + `cam_count`。
- **ADR-3 种子由脚本生成**：与前端原生成逻辑逐位对齐（保证 UI 数据零变化），
  用一次性 Node 脚本直出三方言 SQL，禁手抄。
- **ADR-4 契约四同步同一次交付**：openspec → `video.openapi.json`（新增 path + 3 schema）→
  后端实现（`check-api-contract.mjs --strict` 守门）→ `npm run gen:api-types`。

## 非目标（本期不做）

- 不为视频墙建写端点（`savedModes`/`savedPlans`/`savedPlans` 轮巡是前端交互状态，保留本地）。
- 不接入真实 MDM 设备表（占位通道命名维持现状，UI 数据零变化）。
- 不动 `videoWallStore.zones`（消防装置区态势，属 P0-④ 面板化改造，另行立项）。

## 风险

- `VideoWallSidebar` 的 `expandedTargetGroups` 在组件 setup 时按空树初始化 → 加载后默认全收起，
  可接受（用户点开即展开）。
- `videoWallStore` 其余 10 处消费方只调用 `replaceWallWithCameras`/`prepareAutoFillCameras` 等
  API，不直接读树，行为不变。
