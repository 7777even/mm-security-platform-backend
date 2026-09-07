# QA · 告警字段对齐（2026-09-07）

## Change
`openspec/changes/align-alarm-contract-fields`（对齐 FacAlarm ↔ 前端 AlarmItem）

## 改动
- `schema.sql`：`fac_alarm` 加 `alarm_id` / `location` / `category` / `warned` / `plan_id`
- `FacAlarm` 实体：加对应字段（`alarmId`/`location`/`category`/`warned`/`planId`）；物理主键 `id` 保留
- 新增 `AlarmItem` DTO：与前端 `AlarmItem` 字节级对齐
- 新增 `AlarmAssembler`：纯转换（status int→string 枚举、content→description、occurredAt→ts）
- `AlarmPageResult.list` 由 `List<FacAlarm>` 改为 `List<AlarmItem>`
- `AlarmController.page()`：装配前调 assembler 转换
- `data.sql` 种子：补新列；`type` 统一为前端枚举（FIRE/GAS/TEMP/CCTV/SOS，去掉 FLOOD/INTRUSION）

## 验证
- `mvn test`：**26 case 全绿**（新增 `AlarmAssemblerTest` 3 + `AlarmControllerTest` 2）
- `scripts/check-api-contract.mjs`：实现有/契约无 = 0，**无新增 path 漂移**（path 维度不变）

## 设计要点
- 用「实体保留物理主键 + 新增业务 ID 列 + DTO 转换层」模式，避免实体直接序列化暴露内部字段（id/content/occurred_at/deleted）
- `AlarmAssembler` 零依赖、纯函数，单测极简
- 种子 `type` 枚举前后端必须统一，否则前端 `AlarmType` 校验报错
