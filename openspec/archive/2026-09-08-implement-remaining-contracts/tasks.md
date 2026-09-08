# Tasks: implement-remaining-contracts

## 实现步骤

### alarm 域（POST/PUT/DELETE）
- [x] 1. 新建 `dto/EmergencyEventPayload.java`：level(@NotNull Integer) / type(@NotBlank) / status(String,可空,缺省 ACTIVE) / deviceCode(@NotBlank) / location(@NotBlank) / description(@NotBlank)；含中文注释；加 `@Valid` 触发 400
- [x] 2. 新建 `dto/DeleteResult.java`：ok(Boolean)
- [x] 3. `AlarmService` 增加 `create / update / delete / nextAlarmId`：20 位 MDM 校验、string→int status 映射、alarm_id 寻址、逻辑删除返回布尔；复用 `AlarmAssembler.toItem`
- [x] 4. `AlarmController` 增加 `@PostMapping` / `@PutMapping("/{alarmId}")` / `@DeleteMapping("/{alarmId}")`
- [x] 5. 测试：`AlarmServiceTest`(create 生成 ID/校验/逻辑删除) + `AlarmControllerTest`(POST 201 形态 / PUT 不存在返回 null / DELETE ok)

### dashboard 域（risk-heatmap）
- [x] 6. 新建 `dto/RiskHeatItem.java`：zone(String) / score(Number)
- [x] 7. `DashboardService.riskHeatmap()`：分区真实聚合（设备离线/告警 + 活动报警按 zone），返回 `List<RiskHeatItem>`
- [x] 8. `DashboardController` 增加 `@GetMapping("/risk-heatmap")`
- [x] 9. 测试：`DashboardServiceTest.riskHeatmap` + `DashboardControllerTest.riskHeatmap`

### emergency 域（5 端点）
- [x] 10. 新建 9 个 DTO：EmergencyStrength/EmergencyResource/ClosedCaseList/ClosedCase/DutyRoster/DutyMember/EmergencyPhoneBook/EmergencyPhone/KnowledgeList/KnowledgeItem
- [x] 11. 新建 `EmergencyService`：strength/duty/phones/knowledge 静态参考配置（注释）；closedCases 从 fac_alarm CLOSED 聚合
- [x] 12. 新建 `EmergencyController`（`@RequireAuth`，5 个 GET）
- [x] 13. 测试：`EmergencyServiceTest` + `EmergencyControllerTest`

### map 域（2 GeoJSON 端点）
- [x] 14. 新建 GeoJSON DTO：GeoJsonFeatureCollection / GeoJsonFeature / GeoJsonGeometry
- [x] 15. 新建 `MapService`：alarmPoints 按 deviceCode 关联坐标；devicePoints 用自身坐标，status 映射 ONLINE/OFFLINE/ALARM
- [x] 16. 新建 `MapController`（`@RequireAuth`，2 个 GET）
- [x] 17. 测试：`MapServiceTest` + `MapControllerTest`

### uplink 域（2 上行端点）
- [x] 18. 新建 DTO：AuditEventBatch / AuditEvent / FieldReportItem
- [x] 19. `schema.sql` 新增 `fac_audit_log`；新建 `entity/FacAuditLog.java` + `mapper/AuditLogMapper.java`
- [x] 20. 新建 `UplinkService`：reportAudit 批量落库；submitFieldReport 受理返回 204
- [x] 21. 新建 `UplinkController`：`/audit/log`(B3) + `/field-reports`(204 裸返)
- [x] 22. 测试：`UplinkServiceTest` + `UplinkControllerTest`

### 验证与归档
- [x] 23. `node scripts/check-api-contract.mjs`：差异 13→0（实现有/契约无 = 0）
- [x] 24. `mvn test` 全绿
- [x] 25. 提交按 scope 拆分（alarm/dashboard/emergency/map/uplink/db/test/docs）；更新 MEMORY + 日志

## 验收标准（DoD）

- [x] 13 项契约端点全部进入 check-api-contract「已对齐」，实现有/契约无 = 0，差异 13→0
- [x] `POST/PUT/DELETE /alarms` 行为正确：create 生成 AE-YYYY-NNN 且默认 ACTIVE；device_code 非 20 位 400/301；update 不存在返回 data=null；delete 逻辑删除返回 ok
- [x] `GET /dashboard/risk-heatmap` 返回分区 score（真实聚合，无随机）
- [x] emergency 5 端点字段与契约对齐；closed-cases 来自 fac_alarm CLOSED
- [x] map 两点位返回合法 GeoJSON FeatureCollection（WGS84 [lon,lat]，无坐标报警不渲染）
- [x] uplink：`/audit/log` 落 fac_audit_log 且 B3；`/field-reports` 返回 204 且需鉴权
- [x] `mvn test` 0 failure

## 验证记录（2026-09-07 收尾）

- `node scripts/check-api-contract.mjs` → **差异 0**，24 端点全对齐，实现有/契约无 = 0。
- `mvn test` → **71 用例全绿**（原 38 + 新增 33），0 failure / 0 error。
- 真实启动冒烟（H2，dev profile):
  - 13 端点经 Bearer token 实测：GET 类 200；写类 `alarm POST` 200(生成 AE-2026-010)、
    `audit/log POST` 200(落 fac_audit_log)、`field-reports POST` **204 空体**(契约 bypass B3)。
  - 发现并修复存量 bug：`data.sql` 以 UTF-8 落盘，但 Spring `sql.init` 在中文 Windows 下按 JVM 默认字符集(GBK)
    读取，致库内中文全乱码(`瑁呭嵏鍖` 之类)。已在 `application-dev.yml` 的 `spring.sql.init` 显式加
    `encoding: UTF-8`，重启后 `risk-heatmap` 分区名恢复正确(罐区A/装卸区/装置C)。
  - 已知非本次引入项：`/actuator/health` 返回 500(`NoHandlerFoundException`，actuator 未暴露，pre-existing)；
    `FacWorkstation` 实体缺 `@TableId`(MyBatis-Plus 警告，pre-existing，不影响功能)。
- 临时冒烟脚本 `smoke-write.py` / `verify-data.py` 已删除，未入库。
