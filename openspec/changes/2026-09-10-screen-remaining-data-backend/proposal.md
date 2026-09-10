# 提案：大屏端剩余数据补接后端（派单人员 / 视频联动选项 / 数据集加厚）

> **状态：`approved`** —— 用户于 2026-09-10 审阅《大屏端数据后端化缺口盘点》后指示「按顺序改」，即对本提案范围予以确认。
> 分级 **L3**（新增业务读端点 + 新增表，不触及鉴权 / 权限模型 / 既有表结构）。

## 背景（2026-09-10 实测）

起后端 8787 全量冒烟后的三个事实：

1. 前端调用 URL **0 条越界契约**，100 个 GET 端点 **92 个 `code=0`** → 接线层面已基本完成。
2. 剩余「前端仍直读本地常量」仅 4 处，其中 3 处需要后端配合。
3. 「后端数据稀疏」是一类此前未被识别的缺口：接口通、但返回条数撑不满大屏。

## 目标

| 编号 | 缺口 | 处置 |
| --- | --- | --- |
| A1 | `usePatrolLinkage.ts` 巡更联动点位纯本地 | **零后端改动**：`/security/patrol-cameras` 已含 `zone` 字段且 5 个分区全覆盖（路网 8 / 门禁卡口 5 / 核心区 4 / 周界 4 / 外围 4），前端按 zone 过滤派生即可 |
| A2 | `VideoLinkageConfigDialog.vue` 4 个下拉选项本地常量 | 新增 `GET /video/linkage-options`：相机名与相机类型由 `fac_video_camera` **派生**，预置点与业务对象**落库** |
| A3 | `AlarmDetailPanel.vue` 派单人员 5 个硬编码人名 | 新增 `fac_dispatch_personnel` 名册 + `GET /emergency/dispatch-personnel`；并回填 `fac_perimeter_alarm.dispatch_personnel`（实测该字段全表为空） |
| B | 多端点数据稀疏 | V35 一次性加厚：`departments`、闭环案例、知识库、监测告警、系统消息、通讯设备 |

## 非目标（本期不做）

- 不修改既有表结构（只 UPDATE 数据 + 新建 2 张表）。
- 不接入字典体系（`sys_dict_*` 目前是空壳，V32 建表无种子；强行接入会先要为字典补种子，范围外溢）。
- 不为派单增加写端点（本期只读；派单动作是否落库属独立的写侧 Change）。
- 不动 `/ws/alarm`（单独做端到端验证，不作为接口变更）。

## ADR

- **ADR-1 能派生就不建表**：A1 的联动点位与巡逻相机是同一实体（「该分区下可调阅的相机」），`/security/patrol-cameras` 已提供 `zone/name/status/lng/lat`，前端派生即可，**拒绝为它新增冗余表**。
- **ADR-2 选项数据分两类来源**：可从既有实体派生的（相机名、相机类型）走派生；属于设备能力 / 业务词表的（预置点、业务对象）落库 `fac_video_linkage_option`，便于后续维护。
- **ADR-3 派单人员独立成名册**：值班人员（`sys_duty_member`）出参经 `@Masked` 脱敏（杨\* / 李\*），救援人员（`fac_rescue_personnel`）375 人过多，均不适合做派单下拉。故新建 `fac_dispatch_personnel`（指挥层可指派的责任人，10 人量级）。
- **ADR-4 数据加厚走新迁移**：已进共享环境的 V1–V34 **禁改禁删**，一律新增 V35；只 INSERT/UPDATE 数据，不改结构。

## 风险

- 加厚数据可能影响既有单测断言（如条数、首条内容）→ 全量 `mvn test` 验证，红了就改断言而非回退数据。
- `fac_perimeter_alarm.dispatch_personnel` 由空变为有值 → 检查 `SecurityServiceTest`/`SecurityControllerTest` 是否断言空。
- 前端 A1 派生后的点位 `name` 取自相机名（如「北环路1#」），与本地 fixture 的「北环路1」略有差异 → 属可接受，且由后端真源决定。

## 人工确认关卡

- [x] 用户已审阅缺口盘点并指示「按顺序改」
- [x] A1 采用零后端改动的派生方案
- [x] A3 新建派单人员名册（而非复用脱敏的值班人员）
- [x] 数据加厚一律走新增 V35，不改历史 V-file
