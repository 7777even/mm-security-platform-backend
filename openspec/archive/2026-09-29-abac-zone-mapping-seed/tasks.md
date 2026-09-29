# Tasks

## 1. 配置注入
- [x] `application-dev.yml` 追加 `abac.zone-mapping.location-to-zones` 示例映射（6 项，防区名对齐 V34 种子）

## 2. 闭环验证（TDD）
- [x] 新增 `websocket.RealtimeBroadcastServiceTest`：restrictedUserOnlyReceivesIntersectingZone / unmappedEventZoneFailsOpenToAllAuthenticated / noIntersectionExcludesRestrictedUser / sessionCountReflectsRegisteredSessions
- [x] 后端单测全绿（`mvn.cmd -s ci-settings.xml test -Dtest=RealtimeBroadcastServiceTest`）

## 3. 归档
- [x] `git mv` 到 `openspec/archive/2026-09-29-abac-zone-mapping-seed`
- [x] `node scripts/check-openspec-hygiene.mjs` 通过
