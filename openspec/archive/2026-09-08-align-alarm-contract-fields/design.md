# Design: align-alarm-contract-fields

## 字段映射决策表

| 前端 AlarmItem 字段 | 类型 | 后端来源 | 处理方式 |
|---|---|---|---|
| `alarmId` | string `AE-2026-NNN` | **新增列 `alarm_id`** | 加列；种子用 `AE-2026-001`..`008` |
| `level` | int 1-4 | `FacAlarm.level` | 透传 |
| `type` | enum FIRE/GAS/TEMP/CCTV/SOS | `FacAlarm.type` | **枚举统一为前端值**；种子去掉 FLOOD/INTRUSION，改用前端枚举 |
| `status` | string ACTIVE/ACKED/DISPATCHED/CLOSED | `FacAlarm.status`(int) | **int→string 映射**：0→ACTIVE / 1→ACKED / 2→DISPATCHED / 3→CLOSED |
| `deviceCode` | string 20位 | `FacAlarm.deviceCode` | 透传 |
| `location` | string | **新增列 `location`** | 加列+种子 |
| `ts` | date-time ISO8601 | `FacAlarm.occurredAt` | DTO 别名映射（`content`→`ts` 由 Jackson 序列化为 ISO） |
| `description` | string | `FacAlarm.content` | DTO 别名映射 |
| `category` | enum WEATHER/FIRE_PHONE/STORAGE_FIRE/OTHER | **新增列 `category`** | 加列+种子 |
| `warned` | bool | **新增列 `warned`** | 加列+种子（默认 false） |
| `title` | string | `FacAlarm.title` | 透传 |
| `planId` | string | **新增列 `plan_id`** | 加列+种子（可空） |

## 架构

- **不改 `FacAlarm` 物理主键**：`id BIGINT AUTO_INCREMENT` 保留；`alarm_id` 是新增的业务展示 ID（非主键）。
- **新增 `AlarmItem` DTO**（`dto/` 包）：携带全部前端字段，与 `AlarmItem` 契约 1:1。
- **新增 `AlarmAssembler`**（`service/` 或 `dto/`）：`FacAlarm → AlarmItem` 纯转换（int status 映射、字段改名、类型枚举保留）。
- **`AlarmPageResult.list` 由 `List<FacAlarm>` 改为 `List<AlarmItem>`**；`AlarmController.page()` 在装配分页结果前调用 assembler。
- **`data.sql` 种子**补 `alarm_id`/`location`/`category`/`warned`/`plan_id`，`type` 用前端枚举值。

## 迁移策略（dev 用 H2）
- `schema.sql` 的 `CREATE TABLE fac_alarm` 直接含新列（重建库即可生效，dev 用 `spring.sql.init.mode=always` 全量重建）。
- 不写 ALTER（dev 库每次启动重建，无需在线迁移；生产迁移另议）。

## 契约同步
- 后端字段对齐后，`frontend-scaffold/docs/api/alarm.openapi.json` 已是真实契约，无需改 path；仅在后端 README 映射表标注「已对齐」。
- 跑 `scripts/check-api-contract.mjs` 确认无新增漂移（path 维度不变）。
