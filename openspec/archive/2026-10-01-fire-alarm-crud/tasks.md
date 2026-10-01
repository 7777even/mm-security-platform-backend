# Tasks: fire-alarm-crud（后端）

## 权限种子
- [x] 三方言 `V90__fire_alarm_write_perm.sql`：在 `fm-fire` 下登记按钮级菜单 `fire-alarm:create`/`fire-alarm:delete`，授权 ADMIN+5 岗位角色（h2/postgresql/dameng 同名同号）
- [x] 跑 `python scripts/check-dialect-migration-consistency.py --dialects h2,dameng,postgresql` 校验无撞号、三方言集合一致

## DTO
- [x] 新增 `dto/FireAlarmCreateRequest`（title/time 必填，其余可选，status 默认 ACTIVE）
- [x] `dto/FireAlarmUpdateRequest` 扩为 19 字段（全可选，局部更新语义，向后兼容大屏仅传 6 字段）

## Service / Controller
- [x] `FireAlarmService.create(req)`：生成 `FA-` 前缀 alarmId、@RealtimeSync 广播 `fire-alarm.alarm`
- [x] `FireAlarmService.delete(alarmId)`：不存在抛 `BusinessException(NOT_FOUND)`，否则真删除 + 广播
- [x] `FireAlarmService.update` 的 read-modify-write 块从 6 字段扩到 19 字段（status/falseAlarm 枚举校验、@Version 乐观锁）
- [x] `FireAlarmController` 新增 `POST /fire-alarms`（`fire-alarm:create`）+ `DELETE /fire-alarms/{alarmId}`（`fire-alarm:delete`）

## 测试 / 守门
- [x] `FireAlarmControllerTest`/`FireAlarmServiceTest` 覆盖 create/delete/全字段 update + 权限拒绝 + 枚举非法 + 不存在删除
- [x] 跑 `node scripts/check-api-contract.mjs --strict`：路由差异 0 / schema 漂移 0（PASS）

## 契约四同步
- [x] 前端 `docs/api/fire-alarm.openapi.json` 扩展 POST/DELETE + 全字段 PUT（Task 6 已提交）

## 收尾（跨端）
- [x] 双仓推送 `feature/fire-alarm-crud` / `feature/scaffold-rebuild` 与三端联调验证（Task 12，待前端 8–11 完成后一并执行）
