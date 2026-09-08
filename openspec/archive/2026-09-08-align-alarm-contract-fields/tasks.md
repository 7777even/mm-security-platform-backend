# Tasks: align-alarm-contract-fields

## 实现步骤

- [x] 1. `schema.sql`：`fac_alarm` 加 `alarm_id`(VARCHAR32) / `location`(VARCHAR128) / `category`(VARCHAR32) / `warned`(BOOLEAN DEFAULT FALSE) / `plan_id`(VARCHAR64)
- [x] 2. `FacAlarm` 实体：加 `alarmId` / `location` / `category` / `warned`(Boolean) / `planId` 字段
- [x] 3. 新建 `dto/AlarmItem.java`：全字段对齐前端 AlarmItem
- [x] 4. 新建 `service/AlarmAssembler.java`：`FacAlarm → AlarmItem`（status int→string 映射、content→description、occurredAt→ts）
- [x] 5. `AlarmPageResult.list` 改为 `List<AlarmItem>`
- [x] 6. `AlarmController.page()`：装配分页前调 assembler 转 `AlarmItem`
- [x] 7. `data.sql` 种子：补新列，`type` 用前端枚举（FIRE/GAS/TEMP/CCTV/SOS）
- [x] 8. 测试：`AlarmControllerTest` / `AlarmAssemblerTest` 断言 `AlarmItem` 字段
- [x] 9. `mvn test` 全绿（26 case）
- [x] 10. 提交：按 scope `db` / `alarm` / `test` / `docs` 拆分

## 验收标准（DoD）

- [x] `GET /api/v1/alarms` 返回的 `data.list[]` 每个元素含 `alarmId`/`status`(string 枚举)/`ts`/`location`/`category`/`warned`/`planId`/`description`，无 `undefined`
- [x] `mvn test` 0 failure
- [x] `scripts/check-api-contract.mjs` 无新增 path 漂移（实现有/契约无 = 0）
- [x] QA + Retro 写入 `engineering/`
