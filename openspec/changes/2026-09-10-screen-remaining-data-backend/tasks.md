# 任务：大屏端剩余数据补接后端

状态：✅ 已完成（A3 / A1 / A2 / B 类数据加厚 / C 类 WS+快照端到端验证 / D 类门禁）。

## 后端（backend-scaffold）

### 1. 迁移 V35
- [x] 新建 `V35__screen_remaining_dataset.sql`（h2）
  - [x] 建 `fac_dispatch_personnel`（派单人员名册）+ 10 条种子
  - [x] 建 `fac_video_linkage_option`（视频联动选项）+ 预置点 9 / 业务对象 12 种子
  - [x] UPDATE `fac_perimeter_alarm.dispatch_personnel` 回填（原实测为空数组）
  - [x] 加厚：`sys_duty_member` 部门(→4) / `fac_alarm` 闭环案例(→8) / `sys_knowledge_item`(→9) / `fac_monitoring_alarm`(→12) / `fac_system_message`(→7) / `fac_comm_device`(→11)
- [x] 派生 dameng / postgresql 方言版本（脚本派生 + 计数自检；主键 NUMBER IDENTITY / BIGSERIAL）
- ⚠️ 修正：V35 初版 4.2 段 `fac_alarm.alarm_id` 取 `AE-2026-006~012` 与 V2 种子唯一约束冲突 →
  改为 `AE-2026-101~107`（1xx 段避开 V2 的 001-012）。**教训：加厚表若目标表已有唯一键（如
  `uk_fac_alarm_alarm_id`），种子 ID 必须避让既有迁移。**

### 2. 派单人员（A3）
- [x] 实体 `FacDispatchPersonnel` + Mapper；DTO `DispatchPersonnel`（契约同名）
- [x] `EmergencyService.dispatchPersonnel()` + `GET /emergency/dispatch-personnel`（10 条真实名册）

### 3. 视频联动选项（A2）
- [x] 实体 `FacVideoLinkageOption` + Mapper；DTO `VideoLinkageOptions`（monitorNames / presetPoints /
  businessObjectCategories / businessObjects）
- [x] `VideoService.linkageOptions()`（相机名 / 相机类型由 `fac_video_camera` 派生 + 选项表读取）
  + `GET /video/linkage-options`（实测 monitorNames=27 / presetPoints=9 / categories=4 / objects=12）

### 4. 契约（前端真源）
- [x] `emergency.openapi.json` 增 `/emergency/dispatch-personnel` + `DispatchPersonnel` schema
- [x] `video.openapi.json` 增 `/video/linkage-options` + `VideoLinkageOptions` schema
- [x] `npm run gen:api-types` 重新生成

### 5. 测试与守门
- [x] `EmergencyServiceTest` 构造器补 `FacDispatchPersonnelMapper`（纯 Mockito 手动构造）
- [x] `mvn test` 全绿：446/446（BUILD SUCCESS）。V35 修复后 `EndToEndFlowTest` 上下文加载正常，
  原先的 MyBatis-Plus lambda-cache 级联错误随 Flyway 失败一并消失。
- [x] `check-api-contract.mjs --strict`：路由 0 差异 / schema 0 漂移
- [x] C 类端到端：WS `/ws/alarm` 收到 `alarm.push` 真实帧；`/video/cameras/1/snapshot` 与
  `/security/perimeter-alarms/1/snapshot` 均返回 `image/jpeg` 真实字节（~20KB）

## 前端（frontend-scaffold）

### 6. A3 派单人员接线
- [x] `services/emergency.ts` 加 `fetchDispatchPersonnel`（DEV 无 VITE_API_BASE 回落空数组，不灌假名）
- [x] `AlarmDetailPanel.vue` 选项改接后端（移除 `alarmDetailPersonnelOptions` 硬编码 5 人名）

### 7. A1 巡更联动接线（零后端改动，复用 `/security/patrol-cameras`）
- [x] `usePatrolLinkage.ts` 改接 `fetchPatrolCameras()`，按 zone 过滤派生 `PatrolLinkagePoint`
- [x] 删除已废弃 `lib/data/patrolLinkageMock.ts`（其 zone 与 `security.patrolZones` label 完全对齐）

### 8. A2 视频联动选项接线
- [x] `services/video.ts` 加 `fetchVideoLinkageOptions`
- [x] `VideoLinkageConfigDialog.vue` 四个下拉改接后端（移除 4 个本地选项常量）
- [x] 删除已废弃 `lib/data/videoLinkageOptions.ts`

### 9. 门禁
- [x] `vue-tsc -p tsconfig.app.json --noEmit` 0 错
- [x] `vitest run` 全绿：379/379
- [x] `validate-api-contracts.mjs` 通过（28 域）
- [x] `scripts/screen-local-data-gate.mjs` 大屏零本地业务数据门禁（聚焦回归守卫 + 透明审计，exit 0）
- [x] 两仓 `docs/system-facts.md` 同步

## 遗留 / 后续（超出本次范围）
- 大屏组件仍大量值引用 `lib/data/*Mock` 业务数据（审计：accidentRescueMock×16、mock×9、
  alarmDetailMock×9、typhoonEmergencyMock×5、planMatrixMock×4、drillRescueMock×4 等）。其中
  `accidentRescueMock.emergencyDispatchResources`（救援资源列表）、`drillRescueMock`、`preliminaryMock`
  等属应后端化的业务数据；而表单字段定义 / 路线几何 / 地图控件等按设计保留本地。
  **「全量零本地数据」是比 A3/A1/A2 更大的工程，需逐域评估后单独立项。**
