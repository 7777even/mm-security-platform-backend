# 需求交付基线 · 交付范围追溯清单（后端）

> 本文是后端**已交付能力**的内部追溯基线，由代码与契约真源（`frontend-scaffold/docs/api/*.openapi.json`）派生。
> 若甲方提供独立《功能项清单 / 需求规格说明书》，其应作为**上游真源**，本文各条目须回链到对应功能项编号。
> 任何新增端点必须在此登记一行，并标注其归属 capability spec 与前端消费模块，否则视为未闭环。

## 0. 口径与生成方式

> **2026-10-06 重大修订**：修订前本文只手工登记 **37 条**（34 REST + 1 WS），而后端实际有 **37 个 Controller / 288 个端点映射** —— 追溯矩阵失真约 87%，按本文自身规则「任何新增端点必须在此登记」即等于 251 条未闭环。根因是**靠人工维护清单**：业务域纵深阶段（阶段 6）批量新增端点时未回写本文。故本次改为**由脚本从代码与契约反推**，人工只维护判断性章节（§3 / §4）。

- **端点计数（脚本反推，可复现）**：后端 **37 个 Controller / 288 个端点映射**，读 **140** / 写 **148**，覆盖 **31 个契约域**；`gis.openapi.json` 为外部网关前瞻桩（不实现，不计入）。
- **三项交叉校验（均 0 缺口）**：
  1. 契约未登记端点 **0** —— 288 条全部能在 `frontend-scaffold/docs/api/*.openapi.json` 找到同 method + path（与 `scripts/check-api-contract.mjs --strict` 同口径）；
  2. 前端零引用端点 **0** —— 288 条全部在 `frontend-scaffold/src|apps` 有消费方（已排除 `*.spec.ts` 与 `src/mocks/`）；
  3. 写端点授权 **148/148** 具备 `role`/`perm` 约束或显式豁免并写明理由（`scripts/check-endpoint-authz.mjs`，其中豁免 11 条）。
- **列含义**
  - **能力域 spec**：后端 `openspec/specs/<capability>/spec.md` 中**提及该端点**的文本证据；`⚠️ 未归属` = spec 未提及（缺口见 §3，**不代表无实现**）。
  - **鉴权**：`公开` = JwtFilter 白名单（login/refresh/logout）；`登录` = JwtFilter 强制令牌 + 裸（或无）`@RequireAuth`；`role:ADMIN` / `perm:<code>` = `@RequireAuth` 显式约束。
  - **前端消费模块**：`src/services/*` 优先，其次 `screen/`（大屏）、`mgmt/`（管理端）、`mobile/`（移动端）；最多列 2 项。
- **重新生成**：`python scripts/gen-scope-inventory.py`（无第三方依赖，只读不写；`--matrix` 只输出 §2）。端点有增删后**必须重跑并回填 §1 / §2**，禁止手抄。

## 1. 交付规模总览（按契约域）

| 契约域 | 端点 | 读 | 写 | 控制器 | 前端主 service |
| ------ | ---: | -: | -: | ------ | -------------- |
| emergency | 31 | 15 | 16 | Emergency | services/backendFallback.ts、services/businessWrite.ts |
| system | 31 | 11 | 20 | SystemDict、SystemMenu、SystemRole、SystemUser、SystemZone | services/system.ts |
| security | 29 | 14 | 15 | Security | services/security.ts、services/securityEventStore.ts |
| rescue-resource | 20 | 8 | 12 | RescueResource | services/rescueResource.ts |
| emergency-plan | 16 | 6 | 10 | EmergencyPlan | services/emergencyPlan.ts |
| fire-facility | 15 | 5 | 10 | FireFacility | services/fireFacility.ts、services/fireFacilityMonitoringMock.ts |
| tv | 15 | 10 | 5 | Tv | services/tv.ts |
| video | 14 | 8 | 6 | Video | services/fireImages.ts、services/video.ts |
| fire-monitoring | 12 | 6 | 6 | FireMonitoring | services/businessWrite.ts、services/fireMonitoring.ts |
| hazard | 11 | 5 | 6 | Hazard | services/hazard.ts |
| communication | 9 | 3 | 6 | CommDevice、CommRecord | services/communication.ts |
| production | 9 | 8 | 1 | Production | services/production.ts、services/tv.ts |
| auth | 7 | 2 | 5 | Auth | services/auth.ts、services/menu.ts |
| dashboard | 7 | 7 | 0 | Dashboard、Workstation | services/alarm.ts、services/dashboard.ts |
| emergency-event | 7 | 2 | 5 | EmergencyEvent | services/emergencyEvent.ts、services/fireEmergencyMock.ts |
| typhoon-emergency | 7 | 4 | 3 | TyphoonEmergency | services/businessWrite.ts、services/typhoonEmergency.ts |
| device | 5 | 2 | 3 | Device | services/communication.ts、services/device.ts |
| form-records | 5 | 2 | 3 | FormRecord | services/formRecords.ts |
| mgmt-ledger | 5 | 2 | 3 | MgmtLedger | services/mgmtLedger.ts |
| special-operation | 5 | 2 | 3 | SpecialOperation | services/fireMonitoring.ts、services/specialOperation.ts |
| alarm | 4 | 1 | 3 | Alarm | services/alarm.ts、services/fireFacility.ts |
| fire-alarm | 4 | 1 | 3 | FireAlarm | services/alarm.ts |
| security-blacklist | 3 | 1 | 2 | Blacklist | services/securityBlacklist.ts |
| fire-situation | 3 | 3 | 0 | FireSituation | services/fireSituation.ts |
| map | 3 | 3 | 0 | Map | services/map.ts |
| uplink | 3 | 1 | 2 | Uplink | services/audit.ts、services/offlineOutbox.ts |
| drills | 2 | 2 | 0 | Drill | services/drill.ts |
| msds | 2 | 2 | 0 | Msds | services/msds.ts |
| tasks | 2 | 2 | 0 | Task | services/task.ts |
| accident-rescue | 1 | 1 | 0 | AccidentRescue | services/accidentRescue.ts |
| weather | 1 | 1 | 0 | Weather | services/weather.ts |
| **合计** | **288** | **140** | **148** | **37** | — |

## 2. 全量端点追溯矩阵（按契约域）

### accident-rescue（1）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 1 | GET | `/api/v1/accident/rescue-incident` | AccidentRescue | emergency-event | 登录 | services/accidentRescue.ts / screen/components/panels/accident-rescue/RescueDynamicsPanel.vue、screen/lib/composables/useFireEmergencyEventList.ts… |

### alarm（4）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 2 | GET | `/api/v1/alarms` | Alarm | alarm-domain、map-geojson… | 登录 | services/alarm.ts、services/fireFacility.ts… / screen/components/common/AlarmDetailPanel.vue、screen/components/map/CenterMap.vue… |
| 3 | POST | `/api/v1/alarms` | Alarm | alarm-domain、map-geojson… | role:ADMIN | services/alarm.ts、services/fireFacility.ts… / screen/components/common/AlarmDetailPanel.vue、screen/components/map/CenterMap.vue… |
| 4 | DELETE | `/api/v1/alarms/{alarmId}` | Alarm | alarm-domain、production-alarm | role:ADMIN | services/alarm.ts、services/fireFacility.ts… / screen/components/common/AlarmDetailPanel.vue、mobile/views/alarms.vue |
| 5 | PUT | `/api/v1/alarms/{alarmId}` | Alarm | alarm-domain、production-alarm | role:ADMIN | services/alarm.ts、services/fireFacility.ts… / screen/components/common/AlarmDetailPanel.vue、mobile/views/alarms.vue |

### auth（7）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 6 | POST | `/api/v1/auth/login` | Auth | auth-rbac | 公开 | services/auth.ts / src/main.ts、src/stores/auth.ts |
| 7 | POST | `/api/v1/auth/logout` | Auth | auth-rbac | 公开 | services/auth.ts |
| 8 | GET | `/api/v1/auth/me` | Auth | auth-rbac、password-lifecycle… | 登录 | services/auth.ts、services/menu.ts / src/main.ts、src/router/index.ts… |
| 9 | GET | `/api/v1/auth/menus` | Auth | auth-rbac | 登录 | services/menu.ts / src/main.ts、src/router/index.ts… |
| 10 | POST | `/api/v1/auth/password` | Auth | password-lifecycle | 登录 | services/auth.ts |
| 11 | PUT | `/api/v1/auth/profile` | Auth | ⚠️ 未归属 | 登录 | services/auth.ts |
| 12 | POST | `/api/v1/auth/refresh` | Auth | auth-rbac | 公开 | services/auth.ts |

### communication（9）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 13 | GET | `/api/v1/communication/devices` | CommDevice | ⚠️ 未归属 | 登录 | services/communication.ts / screen/lib/composables/useCommunicationDevices.ts、mgmt/views/monitor/BroadcastDeviceView.vue… |
| 14 | POST | `/api/v1/communication/devices` | CommDevice | ⚠️ 未归属 | perm:communication:device-write | services/communication.ts / screen/lib/composables/useCommunicationDevices.ts、mgmt/views/monitor/BroadcastDeviceView.vue… |
| 15 | DELETE | `/api/v1/communication/devices/{id}` | CommDevice | ⚠️ 未归属 | perm:communication:device-write | services/communication.ts |
| 16 | GET | `/api/v1/communication/devices/{id}` | CommDevice | ⚠️ 未归属 | 登录 | services/communication.ts |
| 17 | PUT | `/api/v1/communication/devices/{id}` | CommDevice | ⚠️ 未归属 | perm:communication:device-write | services/communication.ts |
| 18 | GET | `/api/v1/communication/records` | CommRecord | communication | 登录 | services/communication.ts / mgmt/router.ts、mgmt/views/comm/CommRecordView.vue |
| 19 | POST | `/api/v1/communication/records` | CommRecord | communication | perm:communication:record-write | services/communication.ts / mgmt/router.ts、mgmt/views/comm/CommRecordView.vue |
| 20 | DELETE | `/api/v1/communication/records/{recordNo}` | CommRecord | ⚠️ 未归属 | perm:communication:record-write | services/communication.ts |
| 21 | PUT | `/api/v1/communication/records/{recordNo}` | CommRecord | ⚠️ 未归属 | perm:communication:record-write | services/communication.ts |

### dashboard（7）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 22 | GET | `/api/v1/dashboard/alarm-trend` | Dashboard | dashboard-analytics | 登录 | services/alarm.ts、services/dashboard.ts / screen/components/panels/security/AlarmTrendPanel.vue |
| 23 | GET | `/api/v1/dashboard/messages` | Dashboard | ⚠️ 未归属 | 登录 | services/dashboard.ts / screen/components/layout/SystemMessageBar.vue |
| 24 | GET | `/api/v1/dashboard/overview` | Dashboard | ⚠️ 未归属 | 登录 | services/alarm.ts、services/dashboard.ts / mgmt/views/workbench.vue |
| 25 | GET | `/api/v1/dashboard/risk-heatmap` | Dashboard | dashboard-analytics | 登录 | services/dashboard.ts、services/map.ts |
| 26 | GET | `/api/v1/dashboard/workstations` | Dashboard | dashboard-analytics | 登录 | services/dashboard.ts / screen/components/panels/production/ProductionWorkstationPanel.vue |
| 27 | GET | `/api/v1/dashboard/workstations/{id}` | Dashboard | ⚠️ 未归属 | 登录 | services/dashboard.ts / screen/components/panels/production/ProductionWorkstationPanel.vue |
| 28 | GET | `/api/v1/workstations` | Workstation | dashboard-analytics | 登录 | services/dashboard.ts / screen/components/panels/production/ProductionWorkstationPanel.vue |

### device（5）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 29 | GET | `/api/v1/devices` | Device | backend-security-baseline、map-geojson | 登录 | services/communication.ts、services/device.ts… / screen/components/map/SecurityMap.vue、screen/components/panels/production/ProductionDeviceLedgerPanel.vue… |
| 30 | POST | `/api/v1/devices` | Device | backend-security-baseline、map-geojson | perm:device:write | services/communication.ts、services/device.ts… / screen/components/map/SecurityMap.vue、screen/components/panels/production/ProductionDeviceLedgerPanel.vue… |
| 31 | DELETE | `/api/v1/devices/{code}` | Device | ⚠️ 未归属 | perm:device:write | services/communication.ts、services/device.ts / screen/components/panels/production/ProductionDeviceLedgerPanel.vue |
| 32 | GET | `/api/v1/devices/{code}` | Device | ⚠️ 未归属 | 登录 | services/communication.ts、services/device.ts / screen/components/panels/production/ProductionDeviceLedgerPanel.vue |
| 33 | PUT | `/api/v1/devices/{code}` | Device | ⚠️ 未归属 | perm:device:write | services/communication.ts、services/device.ts / screen/components/panels/production/ProductionDeviceLedgerPanel.vue |

### drills（2）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 34 | GET | `/api/v1/drills` | Drill | drills-domain | 登录 | services/drill.ts / mobile/router.ts、mobile/views/drill-detail.vue… |
| 35 | GET | `/api/v1/drills/{id}` | Drill | drills-domain | 登录 | services/drill.ts / mobile/views/drill-detail.vue、mobile/views/drills.vue |

### emergency（31）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 36 | GET | `/api/v1/emergency/assist-stats` | Emergency | ⚠️ 未归属 | 登录 | services/emergency.ts |
| 37 | GET | `/api/v1/emergency/cases` | Emergency | ⚠️ 未归属 | 登录 | services/emergencyCase.ts / mgmt/views/emergency/CaseLibView.vue |
| 38 | POST | `/api/v1/emergency/cases` | Emergency | ⚠️ 未归属 | perm:emergency:case:write | services/emergencyCase.ts / mgmt/views/emergency/CaseLibView.vue |
| 39 | DELETE | `/api/v1/emergency/cases/{id}` | Emergency | ⚠️ 未归属 | perm:emergency:case:write | services/emergencyCase.ts |
| 40 | PUT | `/api/v1/emergency/cases/{id}` | Emergency | ⚠️ 未归属 | perm:emergency:case:write | services/emergencyCase.ts |
| 41 | GET | `/api/v1/emergency/closed-cases` | Emergency | emergency-reference | 登录 | services/backendFallback.ts、services/closedCases.ts |
| 42 | GET | `/api/v1/emergency/command-records` | Emergency | ⚠️ 未归属 | 登录 | services/businessWrite.ts / screen/components/panels/accident-rescue/EmergencyResponseCommandPanel.vue、mgmt/views/emergency/EmergencyCommandView.vue… |
| 43 | POST | `/api/v1/emergency/command-records` | Emergency | ⚠️ 未归属 | perm:emergency:command:write | services/businessWrite.ts / screen/components/panels/accident-rescue/EmergencyResponseCommandPanel.vue、mgmt/views/emergency/EmergencyCommandView.vue… |
| 44 | DELETE | `/api/v1/emergency/command-records/{id}` | Emergency | ⚠️ 未归属 | perm:emergency:command:write | services/businessWrite.ts |
| 45 | PUT | `/api/v1/emergency/command-records/{id}` | Emergency | ⚠️ 未归属 | perm:emergency:command:write | services/businessWrite.ts |
| 46 | GET | `/api/v1/emergency/commands` | Emergency | ⚠️ 未归属 | 登录 | services/emergency.ts / mobile/views/events.vue |
| 47 | GET | `/api/v1/emergency/commands/{commandId}` | Emergency | ⚠️ 未归属 | 登录 | services/emergency.ts |
| 48 | GET | `/api/v1/emergency/dispatch-personnel` | Emergency | ⚠️ 未归属 | 登录 | services/emergency.ts / screen/components/common/AlarmDetailPanel.vue |
| 49 | GET | `/api/v1/emergency/duty` | Emergency | emergency-reference | 登录 | services/businessWrite.ts、services/duty.ts… / screen/components/panels/DutyInfoPanel.vue、screen/components/panels/accident-rescue/RescueDutyPanel.vue… |
| 50 | GET | `/api/v1/emergency/duty-sign-ins` | Emergency | ⚠️ 未归属 | 登录 | services/businessWrite.ts / mgmt/views/emergency/DutySignInView.vue、mobile/views/duty.vue |
| 51 | POST | `/api/v1/emergency/duty-sign-ins` | Emergency | ⚠️ 未归属 | perm:emergency:duty:write | services/businessWrite.ts / mgmt/views/emergency/DutySignInView.vue、mobile/views/duty.vue |
| 52 | DELETE | `/api/v1/emergency/duty-sign-ins/{id}` | Emergency | ⚠️ 未归属 | perm:emergency:duty:write | services/businessWrite.ts |
| 53 | PUT | `/api/v1/emergency/duty-sign-ins/{id}` | Emergency | ⚠️ 未归属 | perm:emergency:duty:write | services/businessWrite.ts |
| 54 | GET | `/api/v1/emergency/knowledge` | Emergency | emergency-reference | 登录 | services/knowledge.ts / screen/components/panels/accident-rescue/RescueAuxiliaryPanel.vue、screen/components/panels/preliminary/SafetyKnowledgePanel.vue… |
| 55 | POST | `/api/v1/emergency/knowledge` | Emergency | emergency-reference | perm:emergency:knowledge:write | services/knowledge.ts / screen/components/panels/accident-rescue/RescueAuxiliaryPanel.vue、screen/components/panels/preliminary/SafetyKnowledgePanel.vue… |
| 56 | DELETE | `/api/v1/emergency/knowledge/{id}` | Emergency | ⚠️ 未归属 | perm:emergency:knowledge:write | services/knowledge.ts |
| 57 | PUT | `/api/v1/emergency/knowledge/{id}` | Emergency | ⚠️ 未归属 | perm:emergency:knowledge:write | services/knowledge.ts |
| 58 | GET | `/api/v1/emergency/phones` | Emergency | emergency-reference | 登录 | services/emergencyPhone.ts / screen/components/panels/accident-rescue/EmergencyAddressBookDialog.vue、mgmt/views/emergency/ContactsView.vue… |
| 59 | POST | `/api/v1/emergency/phones` | Emergency | emergency-reference | perm:emergency:phone:write | services/emergencyPhone.ts / screen/components/panels/accident-rescue/EmergencyAddressBookDialog.vue、mgmt/views/emergency/ContactsView.vue… |
| 60 | DELETE | `/api/v1/emergency/phones/{id}` | Emergency | ⚠️ 未归属 | perm:emergency:phone:write | services/emergencyPhone.ts |
| 61 | PUT | `/api/v1/emergency/phones/{id}` | Emergency | ⚠️ 未归属 | perm:emergency:phone:write | services/emergencyPhone.ts |
| 62 | GET | `/api/v1/emergency/process/guidances` | Emergency | ⚠️ 未归属 | 登录 | services/emergencyProcess.ts |
| 63 | GET | `/api/v1/emergency/process/node-configs` | Emergency | ⚠️ 未归属 | 登录 | services/emergencyProcess.ts / screen/lib/composables/useEmergencyProcess.ts |
| 64 | PUT | `/api/v1/emergency/process/node-configs` | Emergency | ⚠️ 未归属 | role:ADMIN | services/emergencyProcess.ts / screen/lib/composables/useEmergencyProcess.ts |
| 65 | GET | `/api/v1/emergency/process/panorama` | Emergency | ⚠️ 未归属 | 登录 | services/emergencyProcess.ts / screen/lib/composables/useEmergencyProcess.ts |
| 66 | GET | `/api/v1/emergency/strength` | Emergency | emergency-reference、rescue-resource | 登录 | services/backendFallback.ts、services/emergency.ts / screen/components/panels/preliminary/EmergencyRescuePanel.vue、screen/lib/composables/useRescueStrengthView.ts |

### emergency-event（7）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 67 | GET | `/api/v1/emergency-events` | EmergencyEvent | emergency-event | 登录 | services/emergencyEvent.ts、services/fireEmergencyMock.ts… / screen/lib/composables/useFireEmergencyEventList.ts、screen/lib/composables/usePreliminaryEventList.ts… |
| 68 | POST | `/api/v1/emergency-events` | EmergencyEvent | emergency-event | 登录 | services/emergencyEvent.ts、services/fireEmergencyMock.ts… / screen/lib/composables/useFireEmergencyEventList.ts、screen/lib/composables/usePreliminaryEventList.ts… |
| 69 | GET | `/api/v1/emergency-events/evacuation-people` | EmergencyEvent | ⚠️ 未归属 | 登录 | services/emergencyEvent.ts |
| 70 | DELETE | `/api/v1/emergency-events/{id}` | EmergencyEvent | emergency-event | perm:emergency:event:write | services/emergencyEvent.ts / screen/views/AccidentEmergencyRescue.vue |
| 71 | PUT | `/api/v1/emergency-events/{id}` | EmergencyEvent | emergency-event | perm:emergency:event:write | services/emergencyEvent.ts / screen/views/AccidentEmergencyRescue.vue |
| 72 | POST | `/api/v1/emergency-events/{id}/report` | EmergencyEvent | emergency-event | 登录 | services/emergencyEvent.ts / screen/views/AccidentEmergencyRescue.vue |
| 73 | POST | `/api/v1/emergency-events/{id}/start-response` | EmergencyEvent | emergency-event | 登录 | services/emergencyEvent.ts / screen/views/AccidentEmergencyRescue.vue |

### emergency-plan（16）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 74 | GET | `/api/v1/emergency-plans` | EmergencyPlan | ⚠️ 未归属 | 登录 | services/emergencyPlan.ts / screen/components/panels/accident-rescue/EmergencyPlanPanel.vue、screen/lib/composables/usePlanMatrix.ts… |
| 75 | POST | `/api/v1/emergency-plans` | EmergencyPlan | ⚠️ 未归属 | perm:emergency:plan:write | services/emergencyPlan.ts / screen/components/panels/accident-rescue/EmergencyPlanPanel.vue、screen/lib/composables/usePlanMatrix.ts… |
| 76 | GET | `/api/v1/emergency-plans/catalog` | EmergencyPlan | ⚠️ 未归属 | 登录 | services/emergencyPlan.ts / screen/components/panels/accident-rescue/EmergencyPlanPanel.vue、mgmt/views/emergency/PlanCatalogView.vue… |
| 77 | GET | `/api/v1/emergency-plans/catalog-detail` | EmergencyPlan | ⚠️ 未归属 | 登录 | services/emergencyPlan.ts / screen/components/panels/accident-rescue/EmergencyPlanPanel.vue、mobile/views/plan-detail.vue |
| 78 | GET | `/api/v1/emergency-plans/catalog-items` | EmergencyPlan | ⚠️ 未归属 | 登录 | services/emergencyPlan.ts / mgmt/views/emergency/PlanCatalogView.vue |
| 79 | POST | `/api/v1/emergency-plans/catalog-items` | EmergencyPlan | ⚠️ 未归属 | perm:emergency:plan-catalog:write | services/emergencyPlan.ts / mgmt/views/emergency/PlanCatalogView.vue |
| 80 | DELETE | `/api/v1/emergency-plans/catalog-items/{id}` | EmergencyPlan | ⚠️ 未归属 | perm:emergency:plan-catalog:write | services/emergencyPlan.ts |
| 81 | PUT | `/api/v1/emergency-plans/catalog-items/{id}` | EmergencyPlan | ⚠️ 未归属 | perm:emergency:plan-catalog:write | services/emergencyPlan.ts |
| 82 | GET | `/api/v1/emergency-plans/matrix` | EmergencyPlan | ⚠️ 未归属 | 登录 | services/emergencyPlan.ts |
| 83 | GET | `/api/v1/emergency-plans/options` | EmergencyPlan | ⚠️ 未归属 | 登录 | services/emergencyPlan.ts |
| 84 | DELETE | `/api/v1/emergency-plans/{id}` | EmergencyPlan | ⚠️ 未归属 | perm:emergency:plan:write | services/emergencyPlan.ts / screen/components/panels/accident-rescue/EmergencyPlanPanel.vue、screen/lib/composables/usePlanMatrix.ts… |
| 85 | PUT | `/api/v1/emergency-plans/{id}` | EmergencyPlan | ⚠️ 未归属 | perm:emergency:plan:write | services/emergencyPlan.ts / screen/components/panels/accident-rescue/EmergencyPlanPanel.vue、screen/lib/composables/usePlanMatrix.ts… |
| 86 | POST | `/api/v1/emergency-plans/{id}/invoke` | EmergencyPlan | ⚠️ 未归属 | role:ADMIN | services/emergencyPlan.ts |
| 87 | POST | `/api/v1/emergency-plans/{planId}/action-cards` | EmergencyPlan | ⚠️ 未归属 | role:ADMIN | services/emergencyPlan.ts / screen/lib/composables/usePlanMatrix.ts |
| 88 | DELETE | `/api/v1/emergency-plans/{planId}/action-cards/{cardId}` | EmergencyPlan | ⚠️ 未归属 | role:ADMIN | screen/lib/composables/usePlanMatrix.ts |
| 89 | PUT | `/api/v1/emergency-plans/{planId}/action-cards/{cardId}` | EmergencyPlan | ⚠️ 未归属 | role:ADMIN | screen/lib/composables/usePlanMatrix.ts |

### fire-alarm（4）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 90 | GET | `/api/v1/fire-alarms` | FireAlarm | ⚠️ 未归属 | 登录 | services/alarm.ts / screen/components/panels/SafetyAlarmPanel.vue、screen/lib/adapters/alarmAdapter.ts… |
| 91 | POST | `/api/v1/fire-alarms` | FireAlarm | ⚠️ 未归属 | perm:fire-alarm:create | services/alarm.ts / screen/components/panels/SafetyAlarmPanel.vue、screen/lib/adapters/alarmAdapter.ts… |
| 92 | DELETE | `/api/v1/fire-alarms/{alarmId}` | FireAlarm | ⚠️ 未归属 | perm:fire-alarm:delete | services/alarm.ts |
| 93 | PUT | `/api/v1/fire-alarms/{alarmId}` | FireAlarm | ⚠️ 未归属 | perm:fire-alarm:ack | services/alarm.ts |

### fire-facility（15）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 94 | GET | `/api/v1/fire-facility/alarms` | FireFacility | ⚠️ 未归属 | 登录 | services/fireFacility.ts、services/fireFacilityMonitoringMock.ts / mgmt/views/fire/FireFacilityAlarmView.vue |
| 95 | PUT | `/api/v1/fire-facility/alarms/{alarmId}` | FireFacility | ⚠️ 未归属 | perm:fire-facility:handle | services/fireFacility.ts |
| 96 | GET | `/api/v1/fire-facility/faults` | FireFacility | ⚠️ 未归属 | 登录 | services/fireFacility.ts、services/fireFacilityMonitoringMock.ts / mgmt/components/FireFacilityFaultEditDialog.vue、mgmt/views/fire/FaultMgmtView.vue |
| 97 | POST | `/api/v1/fire-facility/faults` | FireFacility | ⚠️ 未归属 | perm:fire-facility:fault-create | services/fireFacility.ts、services/fireFacilityMonitoringMock.ts / mgmt/components/FireFacilityFaultEditDialog.vue、mgmt/views/fire/FaultMgmtView.vue |
| 98 | DELETE | `/api/v1/fire-facility/faults/{faultId}` | FireFacility | ⚠️ 未归属 | perm:fire-facility:fault-delete | services/fireFacility.ts / mgmt/components/FireFacilityFaultEditDialog.vue |
| 99 | PUT | `/api/v1/fire-facility/faults/{faultId}` | FireFacility | ⚠️ 未归属 | perm:fire-facility:handle | services/fireFacility.ts / mgmt/components/FireFacilityFaultEditDialog.vue |
| 100 | GET | `/api/v1/fire-facility/ledger` | FireFacility | ⚠️ 未归属 | 登录 | services/fireFacility.ts、services/fireFacilityMonitoringMock.ts / mgmt/views/fire/FireFacilityLedgerView.vue、mgmt/views/fire/FireFacilityMaintenanceView.vue |
| 101 | POST | `/api/v1/fire-facility/ledger` | FireFacility | ⚠️ 未归属 | perm:fire-facility:ledger:write | services/fireFacility.ts、services/fireFacilityMonitoringMock.ts / mgmt/views/fire/FireFacilityLedgerView.vue、mgmt/views/fire/FireFacilityMaintenanceView.vue |
| 102 | DELETE | `/api/v1/fire-facility/ledger/{id}` | FireFacility | ⚠️ 未归属 | perm:fire-facility:ledger:write | services/fireFacility.ts / mgmt/views/fire/FireFacilityMaintenanceView.vue |
| 103 | PUT | `/api/v1/fire-facility/ledger/{id}` | FireFacility | ⚠️ 未归属 | perm:fire-facility:ledger:write | services/fireFacility.ts / mgmt/views/fire/FireFacilityMaintenanceView.vue |
| 104 | POST | `/api/v1/fire-facility/ledger/{ledgerId}/maintenance` | FireFacility | ⚠️ 未归属 | perm:fire-facility:ledger:write | services/fireFacility.ts / mgmt/views/fire/FireFacilityMaintenanceView.vue |
| 105 | DELETE | `/api/v1/fire-facility/maintenance/{recordId}` | FireFacility | ⚠️ 未归属 | perm:fire-facility:ledger:write | services/fireFacility.ts / mgmt/views/fire/FireFacilityMaintenanceView.vue |
| 106 | GET | `/api/v1/fire-facility/monitors` | FireFacility | ⚠️ 未归属 | 登录 | services/fireFacility.ts、services/fireFacilityMonitoringMock.ts |
| 107 | POST | `/api/v1/fire-facility/monitors/report` | FireFacility | ⚠️ 未归属 | perm:fire-facility:handle | services/fireFacility.ts |
| 108 | GET | `/api/v1/fire-facility/work-orders` | FireFacility | ⚠️ 未归属 | 登录 | services/fireFacility.ts、services/fireFacilityMonitoringMock.ts / mobile/views/ops-board.vue、mobile/views/order-detail.vue… |

### fire-monitoring（12）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 109 | GET | `/api/v1/fire/equipment` | FireMonitoring | ⚠️ 未归属 | 登录 | services/fireMonitoring.ts / screen/components/panels/EquipmentMonitoring.vue |
| 110 | GET | `/api/v1/fire/equipment-status` | FireMonitoring | ⚠️ 未归属 | 登录 | services/fireMonitoring.ts / screen/components/panels/EquipmentMonitoring.vue |
| 111 | GET | `/api/v1/fire/patrol-executions` | FireMonitoring | ⚠️ 未归属 | 登录 | services/businessWrite.ts / mgmt/views/fire/PatrolExecutionView.vue、mobile/views/patrol-exec.vue |
| 112 | POST | `/api/v1/fire/patrol-executions` | FireMonitoring | ⚠️ 未归属 | perm:fire-alarm:patrol:write | services/businessWrite.ts / mgmt/views/fire/PatrolExecutionView.vue、mobile/views/patrol-exec.vue |
| 113 | DELETE | `/api/v1/fire/patrol-executions/{id}` | FireMonitoring | ⚠️ 未归属 | perm:fire-alarm:patrol:write | services/businessWrite.ts |
| 114 | PUT | `/api/v1/fire/patrol-executions/{id}` | FireMonitoring | ⚠️ 未归属 | perm:fire-alarm:patrol:write | services/businessWrite.ts |
| 115 | GET | `/api/v1/fire/patrols` | FireMonitoring | ⚠️ 未归属 | 登录 | services/fireMonitoring.ts / screen/components/common/FirePatrolDialog.vue、screen/components/panels/EquipmentMonitoring.vue… |
| 116 | POST | `/api/v1/fire/patrols` | FireMonitoring | ⚠️ 未归属 | perm:fire:patrol-write | services/fireMonitoring.ts / screen/components/common/FirePatrolDialog.vue、screen/components/panels/EquipmentMonitoring.vue… |
| 117 | DELETE | `/api/v1/fire/patrols/{id}` | FireMonitoring | ⚠️ 未归属 | perm:fire:patrol-write | services/fireMonitoring.ts |
| 118 | PUT | `/api/v1/fire/patrols/{id}` | FireMonitoring | ⚠️ 未归属 | perm:fire:patrol-write | services/fireMonitoring.ts |
| 119 | GET | `/api/v1/fire/rescue-forces` | FireMonitoring | ⚠️ 未归属 | 登录 | services/fireMonitoring.ts / screen/components/panels/DutyInfoPanel.vue |
| 120 | GET | `/api/v1/fire/special-operations` | FireMonitoring | ⚠️ 未归属 | 登录 | services/fireMonitoring.ts、services/specialOperation.ts / screen/components/panels/SpecialOperationsPanel.vue |

### fire-situation（3）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 121 | GET | `/api/v1/fire-situation/areas` | FireSituation | ⚠️ 未归属 | 登录 | services/fireSituation.ts / screen/components/panels/SafetyAlarmPanel.vue |
| 122 | GET | `/api/v1/fire-situation/markers` | FireSituation | ⚠️ 未归属 | 登录 | services/fireSituation.ts / screen/components/map/CenterMap.vue |
| 123 | GET | `/api/v1/fire-situation/monitored-objects` | FireSituation | ⚠️ 未归属 | 登录 | services/fireSituation.ts |

### form-records（5）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 124 | GET | `/api/v1/form-records` | FormRecord | form-records | 登录 | services/formRecords.ts / mgmt/router.ts |
| 125 | POST | `/api/v1/form-records` | FormRecord | form-records | 登录 | services/formRecords.ts / mgmt/router.ts |
| 126 | DELETE | `/api/v1/form-records/{id}` | FormRecord | form-records | role:ADMIN | services/formRecords.ts |
| 127 | GET | `/api/v1/form-records/{id}` | FormRecord | form-records | 登录 | services/formRecords.ts |
| 128 | PUT | `/api/v1/form-records/{id}` | FormRecord | form-records | role:ADMIN | services/formRecords.ts |

### hazard（11）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 129 | GET | `/api/v1/facilities/detail` | Hazard | ⚠️ 未归属 | 登录 | services/hazard.ts / screen/views/AccidentEmergencyRescue.vue |
| 130 | GET | `/api/v1/hazards` | Hazard | ⚠️ 未归属 | 登录 | services/hazard.ts / src/router/index.ts、screen/lib/composables/useScreenHazardData.ts… |
| 131 | POST | `/api/v1/hazards` | Hazard | ⚠️ 未归属 | perm:hazard:write | services/hazard.ts / src/router/index.ts、screen/lib/composables/useScreenHazardData.ts… |
| 132 | DELETE | `/api/v1/hazards/{id}` | Hazard | ⚠️ 未归属 | perm:hazard:write | services/hazard.ts |
| 133 | GET | `/api/v1/hazards/{id}` | Hazard | ⚠️ 未归属 | 登录 | services/hazard.ts |
| 134 | PUT | `/api/v1/hazards/{id}` | Hazard | ⚠️ 未归属 | perm:hazard:write | services/hazard.ts |
| 135 | GET | `/api/v1/monitoring/alarms` | Hazard | ⚠️ 未归属 | 登录 | services/hazard.ts |
| 136 | GET | `/api/v1/monitoring/points` | Hazard | ⚠️ 未归属 | 登录 | services/hazard.ts / mgmt/views/monitor/MonitorPointView.vue |
| 137 | POST | `/api/v1/monitoring/points` | Hazard | ⚠️ 未归属 | perm:hazard:point-write | services/hazard.ts / mgmt/views/monitor/MonitorPointView.vue |
| 138 | DELETE | `/api/v1/monitoring/points/{id}` | Hazard | ⚠️ 未归属 | perm:hazard:point-write | services/hazard.ts |
| 139 | PUT | `/api/v1/monitoring/points/{id}` | Hazard | ⚠️ 未归属 | perm:hazard:point-write | services/hazard.ts |

### map（3）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 140 | GET | `/api/v1/map/alarms` | Map | map-geojson | 登录 | services/map.ts / screen/components/map/CenterMap.vue、screen/components/map/ProductionMap.vue… |
| 141 | GET | `/api/v1/map/devices` | Map | map-geojson | 登录 | services/map.ts / screen/components/map/SecurityMap.vue |
| 142 | GET | `/api/v1/map/zone-signs` | Map | map-geojson | 登录 | services/map.ts / screen/components/map/MaomingPetroCesiumMap.vue、screen/components/map/maomingPetroMapConstants.ts |

### mgmt-ledger（5）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 143 | GET | `/api/v1/mgmt-ledger/{domain}` | MgmtLedger | mgmt-ledger | 登录 | services/mgmtLedger.ts / mgmt/router.ts、mgmt/views/MgmtLedgerView.vue |
| 144 | GET | `/api/v1/mgmt-ledger/{domain}/meta` | MgmtLedger | mgmt-ledger | 登录 | services/mgmtLedger.ts |
| 145 | POST | `/api/v1/mgmt-ledger/{domain}/rows` | MgmtLedger | ⚠️ 未归属 | perm:mgmt-ledger:write | services/mgmtLedger.ts |
| 146 | DELETE | `/api/v1/mgmt-ledger/{domain}/rows/{rowId}` | MgmtLedger | ⚠️ 未归属 | perm:mgmt-ledger:write | services/mgmtLedger.ts |
| 147 | PUT | `/api/v1/mgmt-ledger/{domain}/rows/{rowId}` | MgmtLedger | ⚠️ 未归属 | perm:mgmt-ledger:write | services/mgmtLedger.ts |

### msds（2）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 148 | GET | `/api/v1/msds` | Msds | msds-domain | 登录 | services/msds.ts / mobile/router.ts、mobile/views/home.vue… |
| 149 | GET | `/api/v1/msds/{cas}` | Msds | msds-domain | 登录 | services/msds.ts / mobile/views/msds-detail.vue、mobile/views/msds.vue |

### production（9）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 150 | GET | `/api/v1/production/alarms` | Production | production-alarm | 登录 | services/production.ts、services/tv.ts / screen/components/common/AlarmDetailPanel.vue、screen/components/panels/production/ProductionAlarmPanel.vue |
| 151 | PUT | `/api/v1/production/alarms/{id}` | Production | production-alarm | perm:production:ack | services/production.ts、services/tv.ts / screen/components/common/AlarmDetailPanel.vue |
| 152 | GET | `/api/v1/production/alarms/{id}/snapshots` | Production | ⚠️ 未归属 | 登录 | services/tv.ts / screen/components/common/AlarmDetailPanel.vue |
| 153 | GET | `/api/v1/production/areas/{facilityId}` | Production | ⚠️ 未归属 | 登录 | services/production.ts |
| 154 | GET | `/api/v1/production/devices` | Production | ⚠️ 未归属 | 登录 | services/production.ts / screen/lib/composables/useProductionDeviceListView.ts |
| 155 | GET | `/api/v1/production/overview` | Production | ⚠️ 未归属 | 登录 | services/production.ts |
| 156 | GET | `/api/v1/production/personnel` | Production | ⚠️ 未归属 | 登录 | services/production.ts / screen/components/map/ProductionMap.vue |
| 157 | GET | `/api/v1/production/risk-warnings` | Production | ⚠️ 未归属 | 登录 | services/production.ts / screen/components/panels/production/RiskControlPanel.vue |
| 158 | GET | `/api/v1/production/risk-warnings/{id}` | Production | ⚠️ 未归属 | 登录 | services/production.ts / screen/components/panels/production/RiskControlPanel.vue |

### rescue-resource（20）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 159 | GET | `/api/v1/rescue-resources/brigades` | RescueResource | rescue-resource | 登录 | services/rescueResource.ts / screen/lib/composables/useFireBrigadeView.ts、mgmt/views/emergency/EmergencyTeamView.vue |
| 160 | POST | `/api/v1/rescue-resources/brigades` | RescueResource | rescue-resource | perm:rescue:brigade:write | services/rescueResource.ts / screen/lib/composables/useFireBrigadeView.ts、mgmt/views/emergency/EmergencyTeamView.vue |
| 161 | DELETE | `/api/v1/rescue-resources/brigades/{id}` | RescueResource | ⚠️ 未归属 | perm:rescue:brigade:write | services/rescueResource.ts |
| 162 | GET | `/api/v1/rescue-resources/brigades/{id}` | RescueResource | ⚠️ 未归属 | 登录 | services/rescueResource.ts |
| 163 | PUT | `/api/v1/rescue-resources/brigades/{id}` | RescueResource | ⚠️ 未归属 | perm:rescue:brigade:write | services/rescueResource.ts |
| 164 | GET | `/api/v1/rescue-resources/equipment` | RescueResource | rescue-resource | 登录 | services/rescueResource.ts / screen/lib/composables/useRescueEquipmentView.ts、mgmt/views/emergency/ResourceView.vue |
| 165 | POST | `/api/v1/rescue-resources/equipment` | RescueResource | rescue-resource | perm:rescue:equipment:write | services/rescueResource.ts / screen/lib/composables/useRescueEquipmentView.ts、mgmt/views/emergency/ResourceView.vue |
| 166 | DELETE | `/api/v1/rescue-resources/equipment/{id}` | RescueResource | ⚠️ 未归属 | perm:rescue:equipment:write | services/rescueResource.ts |
| 167 | GET | `/api/v1/rescue-resources/equipment/{id}` | RescueResource | ⚠️ 未归属 | 登录 | services/rescueResource.ts |
| 168 | PUT | `/api/v1/rescue-resources/equipment/{id}` | RescueResource | ⚠️ 未归属 | perm:rescue:equipment:write | services/rescueResource.ts |
| 169 | GET | `/api/v1/rescue-resources/personnel` | RescueResource | rescue-resource | 登录 | services/rescueResource.ts / screen/lib/composables/useRescuePersonnelView.ts、mgmt/views/emergency/EmergencyExpertView.vue |
| 170 | POST | `/api/v1/rescue-resources/personnel` | RescueResource | rescue-resource | perm:rescue:personnel:write | services/rescueResource.ts / screen/lib/composables/useRescuePersonnelView.ts、mgmt/views/emergency/EmergencyExpertView.vue |
| 171 | DELETE | `/api/v1/rescue-resources/personnel/{id}` | RescueResource | ⚠️ 未归属 | perm:rescue:personnel:write | services/rescueResource.ts |
| 172 | GET | `/api/v1/rescue-resources/personnel/{id}` | RescueResource | ⚠️ 未归属 | 登录 | services/rescueResource.ts |
| 173 | PUT | `/api/v1/rescue-resources/personnel/{id}` | RescueResource | ⚠️ 未归属 | perm:rescue:personnel:write | services/rescueResource.ts |
| 174 | GET | `/api/v1/rescue-resources/vehicles` | RescueResource | ⚠️ 未归属 | 登录 | services/rescueResource.ts / screen/lib/composables/useRescueVehicleView.ts、mgmt/views/emergency/EmergencyVehicleView.vue |
| 175 | POST | `/api/v1/rescue-resources/vehicles` | RescueResource | ⚠️ 未归属 | perm:rescue:vehicle:write | services/rescueResource.ts / screen/lib/composables/useRescueVehicleView.ts、mgmt/views/emergency/EmergencyVehicleView.vue |
| 176 | DELETE | `/api/v1/rescue-resources/vehicles/{id}` | RescueResource | ⚠️ 未归属 | perm:rescue:vehicle:write | services/rescueResource.ts |
| 177 | GET | `/api/v1/rescue-resources/vehicles/{id}` | RescueResource | ⚠️ 未归属 | 登录 | services/rescueResource.ts |
| 178 | PUT | `/api/v1/rescue-resources/vehicles/{id}` | RescueResource | ⚠️ 未归属 | perm:rescue:vehicle:write | services/rescueResource.ts |

### security（29）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 179 | GET | `/api/v1/security/bollards` | Security | ⚠️ 未归属 | 登录 | services/security.ts / mgmt/views/security/BollardView.vue |
| 180 | POST | `/api/v1/security/bollards` | Security | ⚠️ 未归属 | perm:security:bollard-write | services/security.ts / mgmt/views/security/BollardView.vue |
| 181 | DELETE | `/api/v1/security/bollards/{id}` | Security | ⚠️ 未归属 | perm:security:bollard-write | services/security.ts |
| 182 | PUT | `/api/v1/security/bollards/{id}` | Security | ⚠️ 未归属 | perm:security:bollard-write | services/security.ts |
| 183 | GET | `/api/v1/security/events` | Security | ⚠️ 未归属 | 登录 | services/securityEventStore.ts / screen/components/panels/security/EntryExitStatsPanel.vue、screen/lib/composables/useEntryCaptureListView.ts |
| 184 | GET | `/api/v1/security/gate-controls` | Security | ⚠️ 未归属 | 登录 | services/security.ts / mgmt/router.ts、mgmt/views/security/BarrierView.vue… |
| 185 | POST | `/api/v1/security/gate-controls` | Security | ⚠️ 未归属 | perm:security:gate-write | services/security.ts / mgmt/router.ts、mgmt/views/security/BarrierView.vue… |
| 186 | DELETE | `/api/v1/security/gate-controls/{id}` | Security | ⚠️ 未归属 | perm:security:gate-write | services/security.ts |
| 187 | PUT | `/api/v1/security/gate-controls/{id}` | Security | ⚠️ 未归属 | perm:security:gate-write | services/security.ts |
| 188 | GET | `/api/v1/security/patrol-cameras` | Security | ⚠️ 未归属 | 登录 | services/security.ts / screen/components/panels/security/PerimeterAlarmCreateDialog.vue、screen/lib/composables/usePatrolLinkage.ts |
| 189 | GET | `/api/v1/security/perimeter-alarms` | Security | perimeter-alarm | 登录 | services/security.ts / screen/components/panels/security/SecurityStatusPanel.vue、mgmt/views/security/PerimeterAlarmView.vue |
| 190 | POST | `/api/v1/security/perimeter-alarms` | Security | perimeter-alarm | perm:security:perimeter-create | services/security.ts / screen/components/panels/security/SecurityStatusPanel.vue、mgmt/views/security/PerimeterAlarmView.vue |
| 191 | GET | `/api/v1/security/perimeter-alarms/latest` | Security | perimeter-alarm | 登录 | services/security.ts / screen/components/panels/security/SecurityStatusPanel.vue |
| 192 | DELETE | `/api/v1/security/perimeter-alarms/{id}` | Security | perimeter-alarm | perm:security:perimeter-delete | services/security.ts / screen/components/panels/security/SecurityStatusPanel.vue |
| 193 | GET | `/api/v1/security/perimeter-alarms/{id}` | Security | perimeter-alarm | 登录 | services/security.ts / screen/components/panels/security/SecurityStatusPanel.vue |
| 194 | PUT | `/api/v1/security/perimeter-alarms/{id}` | Security | perimeter-alarm | perm:security:perimeter-ack | services/security.ts / screen/components/panels/security/SecurityStatusPanel.vue |
| 195 | GET | `/api/v1/security/perimeter-alarms/{id}/snapshot` | Security | perimeter-alarm | 登录 | services/security.ts |
| 196 | GET | `/api/v1/security/search/person` | Security | ⚠️ 未归属 | 登录 | services/security.ts / mgmt/views/security/PersonnelRegView.vue |
| 197 | POST | `/api/v1/security/search/person` | Security | ⚠️ 未归属 | perm:security:person-write | services/security.ts / mgmt/views/security/PersonnelRegView.vue |
| 198 | DELETE | `/api/v1/security/search/person/{id}` | Security | ⚠️ 未归属 | perm:security:person-write | services/security.ts |
| 199 | GET | `/api/v1/security/search/person/{id}` | Security | ⚠️ 未归属 | 登录 | services/security.ts |
| 200 | PUT | `/api/v1/security/search/person/{id}` | Security | ⚠️ 未归属 | perm:security:person-write | services/security.ts |
| 201 | GET | `/api/v1/security/search/vehicle` | Security | ⚠️ 未归属 | 登录 | services/security.ts / mgmt/views/security/VehicleRegView.vue |
| 202 | POST | `/api/v1/security/search/vehicle` | Security | ⚠️ 未归属 | perm:security:vehicle-write | services/security.ts / mgmt/views/security/VehicleRegView.vue |
| 203 | DELETE | `/api/v1/security/search/vehicle/{id}` | Security | ⚠️ 未归属 | perm:security:vehicle-write | services/security.ts |
| 204 | GET | `/api/v1/security/search/vehicle/{id}` | Security | ⚠️ 未归属 | 登录 | services/security.ts |
| 205 | PUT | `/api/v1/security/search/vehicle/{id}` | Security | ⚠️ 未归属 | perm:security:vehicle-write | services/security.ts |
| 206 | GET | `/api/v1/security/track/summary` | Security | ⚠️ 未归属 | 登录 | services/security.ts |
| 207 | GET | `/api/v1/security/track/timeline` | Security | ⚠️ 未归属 | 登录 | services/security.ts |

### security-blacklist（3）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 208 | GET | `/api/v1/security/blacklist` | Blacklist | ⚠️ 未归属 | 登录 | services/securityBlacklist.ts / screen/components/panels/security/BlacklistDialog.vue |
| 209 | DELETE | `/api/v1/security/blacklist/persons/{id}` | Blacklist | ⚠️ 未归属 | role:ADMIN | services/securityBlacklist.ts / screen/components/panels/security/BlacklistDialog.vue |
| 210 | DELETE | `/api/v1/security/blacklist/vehicles/{id}` | Blacklist | ⚠️ 未归属 | role:ADMIN | services/securityBlacklist.ts / screen/components/panels/security/BlacklistDialog.vue |

### special-operation（5）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 211 | GET | `/api/v1/special-operations` | SpecialOperation | ⚠️ 未归属 | 登录 | services/fireMonitoring.ts、services/specialOperation.ts / screen/components/panels/SpecialOperationsPanel.vue、screen/lib/composables/useSpecialOperationView.ts… |
| 212 | POST | `/api/v1/special-operations` | SpecialOperation | ⚠️ 未归属 | perm:special-operation:write | services/fireMonitoring.ts、services/specialOperation.ts / screen/components/panels/SpecialOperationsPanel.vue、screen/lib/composables/useSpecialOperationView.ts… |
| 213 | DELETE | `/api/v1/special-operations/{id}` | SpecialOperation | ⚠️ 未归属 | perm:special-operation:write | services/specialOperation.ts / screen/lib/composables/useSpecialOperationView.ts |
| 214 | GET | `/api/v1/special-operations/{id}` | SpecialOperation | ⚠️ 未归属 | 登录 | services/specialOperation.ts / screen/lib/composables/useSpecialOperationView.ts |
| 215 | PUT | `/api/v1/special-operations/{id}` | SpecialOperation | ⚠️ 未归属 | perm:special-operation:write | services/specialOperation.ts / screen/lib/composables/useSpecialOperationView.ts |

### system（31）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 216 | GET | `/api/v1/system/dict-items` | SystemDict | ⚠️ 未归属 | role:ADMIN | services/system.ts / mgmt/views/system/DictView.vue |
| 217 | POST | `/api/v1/system/dict-items` | SystemDict | ⚠️ 未归属 | role:ADMIN | services/system.ts / mgmt/views/system/DictView.vue |
| 218 | DELETE | `/api/v1/system/dict-items/{id}` | SystemDict | ⚠️ 未归属 | role:ADMIN | services/system.ts |
| 219 | PUT | `/api/v1/system/dict-items/{id}` | SystemDict | ⚠️ 未归属 | role:ADMIN | services/system.ts |
| 220 | GET | `/api/v1/system/dict-types` | SystemDict | ⚠️ 未归属 | role:ADMIN | services/system.ts / mgmt/views/system/DictView.vue |
| 221 | POST | `/api/v1/system/dict-types` | SystemDict | ⚠️ 未归属 | role:ADMIN | services/system.ts / mgmt/views/system/DictView.vue |
| 222 | DELETE | `/api/v1/system/dict-types/{id}` | SystemDict | ⚠️ 未归属 | role:ADMIN | services/system.ts |
| 223 | PUT | `/api/v1/system/dict-types/{id}` | SystemDict | ⚠️ 未归属 | role:ADMIN | services/system.ts |
| 224 | GET | `/api/v1/system/dicts/{dictCode}` | SystemDict | system-management | 登录 | services/system.ts / screen/components/common/FireAlarmListDialog.vue、screen/components/common/FirePatrolDialog.vue… |
| 225 | GET | `/api/v1/system/menus` | SystemMenu | ⚠️ 未归属 | role:ADMIN | services/system.ts / src/router/index.ts、mgmt/views/system/RoleView.vue |
| 226 | POST | `/api/v1/system/menus` | SystemMenu | ⚠️ 未归属 | role:ADMIN | services/system.ts / src/router/index.ts、mgmt/views/system/RoleView.vue |
| 227 | DELETE | `/api/v1/system/menus/{id}` | SystemMenu | ⚠️ 未归属 | role:ADMIN | services/system.ts |
| 228 | PUT | `/api/v1/system/menus/{id}` | SystemMenu | ⚠️ 未归属 | role:ADMIN | services/system.ts |
| 229 | GET | `/api/v1/system/permissions` | SystemMenu | ⚠️ 未归属 | role:ADMIN | services/system.ts |
| 230 | GET | `/api/v1/system/roles` | SystemRole | auth-rbac、system-management | role:ADMIN | services/system.ts / src/router/index.ts、mgmt/views/system/RoleView.vue |
| 231 | POST | `/api/v1/system/roles` | SystemRole | auth-rbac、system-management | role:ADMIN | services/system.ts / src/router/index.ts、mgmt/views/system/RoleView.vue |
| 232 | DELETE | `/api/v1/system/roles/{id}` | SystemRole | auth-rbac、system-management | role:ADMIN | services/system.ts / mgmt/views/system/RoleView.vue |
| 233 | GET | `/api/v1/system/roles/{id}` | SystemRole | auth-rbac、system-management | role:ADMIN | services/system.ts / mgmt/views/system/RoleView.vue |
| 234 | PUT | `/api/v1/system/roles/{id}` | SystemRole | auth-rbac、system-management | role:ADMIN | services/system.ts / mgmt/views/system/RoleView.vue |
| 235 | GET | `/api/v1/system/roles/{id}/menus` | SystemRole | auth-rbac、system-management | role:ADMIN | services/system.ts / mgmt/views/system/RoleView.vue |
| 236 | PUT | `/api/v1/system/roles/{id}/menus` | SystemRole | auth-rbac、system-management | role:ADMIN | services/system.ts / mgmt/views/system/RoleView.vue |
| 237 | PUT | `/api/v1/system/roles/{id}/status` | SystemRole | ⚠️ 未归属 | role:ADMIN | services/system.ts |
| 238 | GET | `/api/v1/system/users` | SystemUser | password-lifecycle、system-management | role:ADMIN | services/system.ts / src/router/index.ts、src/views/system/menus.vue… |
| 239 | POST | `/api/v1/system/users` | SystemUser | password-lifecycle、system-management | role:ADMIN | services/system.ts / src/router/index.ts、src/views/system/menus.vue… |
| 240 | DELETE | `/api/v1/system/users/{id}` | SystemUser | password-lifecycle、system-management | role:ADMIN | services/system.ts |
| 241 | GET | `/api/v1/system/users/{id}` | SystemUser | password-lifecycle、system-management | role:ADMIN | services/system.ts |
| 242 | PUT | `/api/v1/system/users/{id}` | SystemUser | password-lifecycle、system-management | role:ADMIN | services/system.ts |
| 243 | POST | `/api/v1/system/users/{id}/password/reset` | SystemUser | password-lifecycle | role:ADMIN | services/system.ts |
| 244 | PUT | `/api/v1/system/users/{id}/role` | SystemUser | system-management | role:ADMIN | services/system.ts |
| 245 | PUT | `/api/v1/system/users/{id}/status` | SystemUser | ⚠️ 未归属 | role:ADMIN | services/system.ts |
| 246 | GET | `/api/v1/system/zones` | SystemZone | system-management | 登录 | services/system.ts / src/views/tv/playback.vue、mgmt/views/system/AreaView.vue |

### tasks（2）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 247 | GET | `/api/v1/tasks` | Task | tasks-domain | 登录 | services/task.ts / mobile/router.ts、mobile/components/MobileHeader.vue… |
| 248 | GET | `/api/v1/tasks/{id}` | Task | tasks-domain | 登录 | services/task.ts / mobile/views/task-detail.vue、mobile/views/tasks.vue |

### tv（15）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 249 | GET | `/api/v1/tv/inspections` | Tv | ⚠️ 未归属 | 登录 | services/tv.ts / screen/components/panels/tv/PlantInspectionPanel.vue、screen/lib/data/tvMock.ts |
| 250 | GET | `/api/v1/tv/maintenance-orders` | Tv | ⚠️ 未归属 | 登录 | services/tv.ts |
| 251 | GET | `/api/v1/tv/maintenance-orders/{id}` | Tv | ⚠️ 未归属 | 登录 | services/tv.ts |
| 252 | GET | `/api/v1/tv/map-points` | Tv | ⚠️ 未归属 | 登录 | services/tv.ts / screen/components/map/TvMap.vue |
| 253 | GET | `/api/v1/tv/monitors` | Tv | tv | 登录 | services/tv.ts / screen/components/panels/tv/VideoAnalysisPanel.vue、screen/lib/composables/useTvVideoDetail.ts… |
| 254 | POST | `/api/v1/tv/monitors` | Tv | tv | perm:tv:monitor:create | services/tv.ts / screen/components/panels/tv/VideoAnalysisPanel.vue、screen/lib/composables/useTvVideoDetail.ts… |
| 255 | DELETE | `/api/v1/tv/monitors/{code}` | Tv | tv | perm:tv:monitor:delete | services/tv.ts / screen/components/panels/tv/VideoAnalysisPanel.vue、screen/lib/composables/useTvVideoDetail.ts… |
| 256 | GET | `/api/v1/tv/monitors/{code}` | Tv | tv | 登录 | services/tv.ts / screen/components/panels/tv/VideoAnalysisPanel.vue、screen/lib/composables/useTvVideoDetail.ts… |
| 257 | PUT | `/api/v1/tv/monitors/{code}` | Tv | tv | perm:tv:monitor:update | services/tv.ts / screen/components/panels/tv/VideoAnalysisPanel.vue、screen/lib/composables/useTvVideoDetail.ts… |
| 258 | GET | `/api/v1/tv/monitors/{code}/snapshots` | Tv | tv | 登录 | services/tv.ts / src/views/tv/playback.vue |
| 259 | GET | `/api/v1/tv/overview` | Tv | ⚠️ 未归属 | 登录 | services/tv.ts / screen/components/panels/tv/EventAnalysisPanel.vue、screen/components/panels/tv/MaintenanceOrderPanel.vue… |
| 260 | GET | `/api/v1/tv/snapshots` | Tv | tv | 登录 | services/tv.ts / screen/components/panels/tv/TvSnapshotFeedPanel.vue、src/views/tv/playback.vue |
| 261 | POST | `/api/v1/tv/snapshots` | Tv | tv | perm:video:snapshot:create | services/tv.ts / screen/components/panels/tv/TvSnapshotFeedPanel.vue、src/views/tv/playback.vue |
| 262 | POST | `/api/v1/tv/snapshots/{id}/ack` | Tv | tv | perm:video:snapshot:ack | services/tv.ts / screen/components/panels/tv/TvSnapshotFeedPanel.vue |
| 263 | GET | `/api/v1/tv/snapshots/{id}/snapshot` | Tv | tv | 登录 | services/tv.ts / src/views/tv/playback.vue |

### typhoon-emergency（7）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 264 | GET | `/api/v1/typhoon/dispatch-orders` | TyphoonEmergency | ⚠️ 未归属 | 登录 | services/businessWrite.ts / screen/views/TyphoonEmergencyDetailV3.vue、mgmt/views/typhoon/TyphoonDispatchView.vue |
| 265 | POST | `/api/v1/typhoon/dispatch-orders` | TyphoonEmergency | ⚠️ 未归属 | perm:typhoon:dispatch:write | services/businessWrite.ts / screen/views/TyphoonEmergencyDetailV3.vue、mgmt/views/typhoon/TyphoonDispatchView.vue |
| 266 | DELETE | `/api/v1/typhoon/dispatch-orders/{id}` | TyphoonEmergency | ⚠️ 未归属 | perm:typhoon:dispatch:write | services/businessWrite.ts |
| 267 | PUT | `/api/v1/typhoon/dispatch-orders/{id}` | TyphoonEmergency | ⚠️ 未归属 | perm:typhoon:dispatch:write | services/businessWrite.ts |
| 268 | GET | `/api/v1/typhoon/dispatch-resources` | TyphoonEmergency | ⚠️ 未归属 | 登录 | services/typhoonEmergency.ts |
| 269 | GET | `/api/v1/typhoon/incident` | TyphoonEmergency | ⚠️ 未归属 | 登录 | services/typhoonEmergency.ts |
| 270 | GET | `/api/v1/typhoon/response-board` | TyphoonEmergency | ⚠️ 未归属 | 登录 | services/typhoonEmergency.ts |

### uplink（3）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 271 | GET | `/api/v1/audit/log` | Uplink | uplink-audit | 登录 | services/audit.ts / src/router/index.ts、mgmt/views/system/AuditView.vue |
| 272 | POST | `/api/v1/audit/log` | Uplink | uplink-audit | 登录 | services/audit.ts / src/router/index.ts、mgmt/views/system/AuditView.vue |
| 273 | POST | `/api/v1/field-reports` | Uplink | uplink-audit | 登录 | services/offlineOutbox.ts |

### video（14）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 274 | GET | `/api/v1/video/cameras` | Video | ⚠️ 未归属 | 登录 | services/fireImages.ts、services/video.ts / screen/components/video-control/VideoControlBottomBar.vue、screen/components/video-control/VideoControlGrid.vue… |
| 275 | POST | `/api/v1/video/cameras` | Video | ⚠️ 未归属 | perm:video:camera-write | services/fireImages.ts、services/video.ts / screen/components/video-control/VideoControlBottomBar.vue、screen/components/video-control/VideoControlGrid.vue… |
| 276 | DELETE | `/api/v1/video/cameras/{id}` | Video | ⚠️ 未归属 | perm:video:camera-write | services/video.ts |
| 277 | PUT | `/api/v1/video/cameras/{id}` | Video | ⚠️ 未归属 | perm:video:camera-write | services/video.ts |
| 278 | GET | `/api/v1/video/cameras/{id}/snapshot` | Video | ⚠️ 未归属 | 登录 | services/video.ts |
| 279 | GET | `/api/v1/video/important-groups` | Video | ⚠️ 未归属 | 登录 | services/video.ts / screen/components/panels/tv/ImportantVideoPanel.vue |
| 280 | GET | `/api/v1/video/linkage-options` | Video | ⚠️ 未归属 | 登录 | services/video.ts / screen/components/video-wall/VideoLinkageConfigDialog.vue、mgmt/components/VideoLinkageEditDialog.vue |
| 281 | GET | `/api/v1/video/linkages` | Video | ⚠️ 未归属 | 登录 | services/video.ts / screen/lib/composables/useVideoLinkageConfig.ts、mgmt/router.ts… |
| 282 | POST | `/api/v1/video/linkages` | Video | ⚠️ 未归属 | role:ADMIN | services/video.ts / screen/lib/composables/useVideoLinkageConfig.ts、mgmt/router.ts… |
| 283 | DELETE | `/api/v1/video/linkages/{configCode}` | Video | ⚠️ 未归属 | role:ADMIN | services/video.ts / screen/lib/composables/useVideoLinkageConfig.ts |
| 284 | PUT | `/api/v1/video/linkages/{configCode}` | Video | ⚠️ 未归属 | role:ADMIN | services/video.ts / screen/lib/composables/useVideoLinkageConfig.ts |
| 285 | GET | `/api/v1/video/linkages/{configCode}/rules` | Video | ⚠️ 未归属 | 登录 | services/video.ts / screen/lib/composables/useVideoLinkageConfig.ts |
| 286 | GET | `/api/v1/video/navigation` | Video | ⚠️ 未归属 | 登录 | services/video.ts / screen/components/video-control/VideoControlSidebar.vue |
| 287 | GET | `/api/v1/video/wall-navigation` | Video | ⚠️ 未归属 | 登录 | services/video.ts / screen/components/video-wall/videoWallStore.ts |

### weather（1）

| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |
| - | ---- | ---- | ------ | ----------- | ---- | ------------ |
| 288 | GET | `/api/v1/weather/overview` | Weather | ⚠️ 未归属 | 登录 | services/weather.ts |

## 3. 能力域 spec 覆盖缺口（2026-10-06 量化）

> 判据：§2「能力域 spec」列为 `⚠️ 未归属` 的端点数 = 后端 `openspec/specs/*/spec.md` 全文中**未出现该端点路径**的端点数。

| 契约域 | 端点 | 无 spec 引用 | 缺口率 |
| ------ | ---: | ----------: | -----: |
| emergency | 31 | 24 | 77% |
| security | 29 | 22 | 75% |
| emergency-plan | 16 | 16 | 100% |
| fire-facility | 15 | 15 | 100% |
| system | 31 | 15 | 48% |
| rescue-resource | 20 | 14 | 70% |
| video | 14 | 14 | 100% |
| fire-monitoring | 12 | 12 | 100% |
| hazard | 11 | 11 | 100% |
| communication | 9 | 7 | 77% |
| production | 9 | 7 | 77% |
| typhoon-emergency | 7 | 7 | 100% |
| special-operation | 5 | 5 | 100% |
| tv | 15 | 5 | 33% |
| fire-alarm | 4 | 4 | 100% |
| security-blacklist | 3 | 3 | 100% |
| dashboard | 7 | 3 | 42% |
| device | 5 | 3 | 60% |
| fire-situation | 3 | 3 | 100% |
| mgmt-ledger | 5 | 3 | 60% |
| auth | 7 | 1 | 14% |
| emergency-event | 7 | 1 | 14% |
| weather | 1 | 1 | 100% |
| **合计** | **288** | **196** | **68%** |

**怎么读这张表（避免误判为「这些功能没做」）**

1. capability spec 是**需求规约**（Requirement / Scenario），不是端点清单；端点已实现且有契约、有前端消费、有授权守门（三项校验均 0 缺口），**功能本身是闭环的**。
2. 但本文 §5 维护规则要求「新增端点：先建/归属 capability spec → 写契约 → 在此表追加一行」。**196 条（68%）缺归属留痕，是真实的流程债务**——甲方若按「功能项 → 端点 → spec → 测试」四层追溯，这 196 条在第 3 层会断链。
3. **100% 缺口的 11 个域共 91 条**（emergency-plan / fire-facility / video / fire-monitoring / hazard / typhoon-emergency / special-operation / fire-alarm / security-blacklist / fire-situation / weather）建议优先补；已完整覆盖的 8 个域（accident-rescue / alarm / drills / form-records / map / msds / tasks / uplink）可作范本。
4. **不主张为凑数而建 spec**：补的方式优先是「在既有 spec 增 Requirement」（如 `map-geojson` 吸收 hazard/device），只有确实构成独立能力时才新建 spec 目录；新建须走 openspec L3/L4 流程，不在本文自行决定。

## 4. 已知债务

- ✅ **已解决（本次订正，勿再当缺口）**
  - 「认证/RBAC 域无独立 capability spec」→ `openspec/specs/auth-rbac/spec.md` 已建（8 条 Requirement）。
  - 「Prometheus 指标端点未入 spec」→ 已入 `observability-probes` spec 的 `### Requirement: Prometheus 指标端点`。
  - 「追溯清单只到 34 端点」→ 本次 §1 / §2 已由脚本全量重建为 288 条，并固化重跑方式（§0）。
- ⬜ **甲方《功能项清单》缺失（阻塞中）**：本文为内部派生基线；一旦甲方提供需求文档，须逐条回链编号，建立「功能项 → 端点 → spec → 测试」四层可追溯。
- ⬜ **spec 覆盖缺口 196 条（本次新登记）**：见 §3，按域分批补，不阻塞交付但阻塞甲方追溯。
- ⬜ **（`scripts/check-endpoint-authz.mjs` 既有标注）**：`Uplink#reportAudit`（`POST /audit/log`）现状登录即可提交，任意登录账号可伪造审计记录，待产品/安全确认是否收紧——**不是加 `role=ADMIN`**（会挡死普通用户上报），应在 Service 侧校验「仅允许上报与自己相关的动作」。

## 5. 维护规则

1. **新增端点**：先建/归属 capability spec（L3/L4）→ 写契约（前端 `docs/api/*.openapi.json`）→ **重跑 `python scripts/gen-scope-inventory.py` 回填 §1 / §2** → 补单测 → `node scripts/check-api-contract.mjs --strict` 通过 → 同步 §3 缺口表。
2. **端点下线**：spec 标 Deprecated → 契约移除 → 前端同步 → 重跑脚本刷新本文。
3. **禁止手抄 §1 / §2**：手抄必然再次失真（2026-10-06 修订前的 37 vs 288 即教训）。脚本只读代码与契约，不做任何写入，可安全反复执行。
4. 本文与 `openspec/specs/*`、`frontend-scaffold/docs/requirement/README.md`（业务域与权限模型）互为补充：本文是「后端交付了什么」，前端文档是「业务怎么跑」。

## 6. 修订记录

| 日期 | 修订 |
| ---- | ---- |
| 2026-09-16 | 初版：手工登记 37 条（34 REST + 1 WS）+ 9 个能力域交付状态。 |
| 2026-10-06 | 全量重建：§0 改为脚本反推口径；§1 / §2 覆盖 37 Controller / 288 端点 / 31 契约域；新增 §3 spec 覆盖缺口量化（196/288）；§4 订正已解决债务 3 项、新增缺口 1 项；§5 固化「禁止手抄」规则。新增 `scripts/gen-scope-inventory.py`。 |
