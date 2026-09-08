# Design: implement-remaining-contracts

## 架构与分层

严格沿用九包分层（controller/service/dto/entity/mapper）+ `common.Result` B3 包络。新建域走独立第一级包内细分：
- `controller/EmergencyController`、`controller/MapController`、`controller/UplinkController`
- `service/EmergencyService`、`service/MapService`、`service/UplinkService`
- `dto/` 平铺（与既有 AlarmItem/DashboardOverview 风格一致）

## 关键决策（ADR）

### ADR-1 应急资源参考数据（emergency）
`strength / duty / phones / knowledge` 属应急资源**静态参考配置**（通讯录、值班表、知识库、力量统计），
其本质是企业配置而非运行时遥测——与 `#16` 移除的 dashboard 随机值性质不同。实现为 `EmergencyService`
内**显式注释的参考列表**，不造表、不写随机。
`closed-cases` 则从 `fac_alarm(status=3 CLOSED)` **真实聚合**（caseId=alarmId / title / location / closedAt=occurred_at / handler 默认「系统归档」）。

### ADR-2 地图点位坐标来源（map）
`fac_alarm` 实体无经纬度，点位坐标经 `device_code` 关联 `fac_device(lat,lon)` 取得；
无坐标的报警点位跳过（保证 GeoJSON 几何有效）。设备点位直接用自身 `lat/lon`。统一 WGS84 `[lon,lat]`。

### ADR-3 uplink 落库与 204 语义
- `POST /audit/log`：B3 包络（`Result.ok()`），批量落 `fac_audit_log`（新增表 + 实体 + Mapper）。
- `POST /field-reports`：前端契约明确「不走 http.ts B3 包络、仅以 HTTP 状态判断成功」，故返回 `204 No Content`（`ResponseEntity` 裸返，非 `Result` 包络），`@RequireAuth` 仍需鉴权（契约 401）。现场回传接收即确认，预处理落库为后续 Change（本期仅受理 + 日志）。

### ADR-4 业务 ID 寻址与校验
- `fac_alarm` 物理主键 `id` 自增；`alarm_id` 为业务展示 ID。`create/update/delete` 以 `alarm_id` 寻址。
- `create` 生成 `AE-{yyyy}-{seq}`（seq = 现有 `AE-yyyy-NNN` 最大后缀 +1，Java 侧解析，DB 无关）。
- `device_code` 强制 20 位 MDM 校验（非 20 位抛 `DEVICE_CODE_INVALID`），与既有 `AlarmService.page` 一致。

### ADR-5 status 枚举映射
后端 int（0=ACTIVE/1=ACKED/2=DISPATCHED/3=CLOSED）↔ 前端 string 枚举，由 `AlarmAssembler.mapStatus` 已有
int→string 方向；service 侧补 string→int（`ACTIVE=0` 缺省）。

## 数据影响

- `schema.sql` 新增 `fac_audit_log`（dev H2 快照）；逻辑删除不适用（审计不可变），带 `created_at`。
- 不改动既有表结构；生产达梦迁移脚本待 Flyway 引入（P3）。

## 风险

- 报警点位依赖 `fac_device` 坐标完整性；种子数据设备均带坐标，联调可用；缺坐标报警不渲染（非报错）。
- emergency 参考数据为静态配置，后续若需动态化，单独 Change 引入参考表。
