# 设计：大屏端剩余数据补接后端

## 1. 数据模型

### `fac_dispatch_personnel`（派单人员名册，A3）
| 列 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| person_name | VARCHAR(32) | 姓名（不脱敏，派单需要可辨识） |
| duty_role | VARCHAR(32) | 岗位：值班领导 / 消防队长 / 工艺处置组长 / 安全员 / 环保监测员 … |
| department | VARCHAR(64) | 所属部门 |
| phone | VARCHAR(32) | 联系电话 |
| sort_no | INT | 排序 |
| status | TINYINT | 1 启用 |
| deleted | TINYINT | 逻辑删除 |

> 列名避开 H2 / 达梦保留字：不用裸 `role`（→`duty_role`）、`name`（→`person_name`）、`order`（→`sort_no`）。

### `fac_video_linkage_option`（视频联动选项，A2）
| 列 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| option_type | VARCHAR(32) | `PRESET_POINT` / `BUSINESS_OBJECT` |
| option_label | VARCHAR(64) | 显示文本 |
| sort_no | INT | |

> 只存「无法从既有实体派生」的两类；相机名与相机类型从 `fac_video_camera` 派生。

## 2. 端点契约

### `GET /emergency/dispatch-personnel`
返回 `DispatchPersonnel[]`：`{ id, name, role, department, phone }`。
按 `sort_no` 升序；只返回 `status=1`。**无 `@RequireAuth(role="ADMIN")`**（派单人员名册是登录可见的共享基础数据，与 `/system/zones` 同口径）。

### `GET /video/linkage-options`
返回 `VideoLinkageOptions`：`{ monitorNames: string[], presetPoints: string[], businessObjectCategories: string[], businessObjects: string[] }`。
- `monitorNames`：派生自 `fac_video_camera.name`（27 台，按 id 升序）
- `businessObjectCategories`：派生自 `fac_video_camera.camera_type` 去重（云台 / 固定点机 / 枪机 / 球机）
- `presetPoints` / `businessObjects`：读 `fac_video_linkage_option` 按 type + sort_no

> 为什么合并成一个端点而不是四个：四个下拉同属一个弹窗，一次请求取回可避免竞态，且前端改动最小。

## 3. A1 派生方案（零后端改动）
`/security/patrol-cameras` 返回 `{ id, name, zone, status, longitude, latitude }`，zone 分布覆盖前端全部 5 个分区。前端 `usePatrolLinkage` 按 `activePatrolZoneLabel` 过滤后映射为 `PatrolLinkagePoint`（`camera` 字段即相机自身）。

## 4. 数据加厚（B 类）
目标量级按「大屏一屏可见条数」反推：列表类 ≥10、滚动消息类 ≥5、部门类 ≥4。只 INSERT，不 UPDATE 既有业务行（唯一例外是 `fac_perimeter_alarm.dispatch_personnel` 回填，因该字段全表为空属数据缺失）。

## 5. 前端接线范式（沿用既有约定）
「初始态预填本地 fixture + 远端 loader 覆盖 + 失败 `backendUnavailableWarn` 保留兜底」，保证无后端演示模式与空态都不白屏。
