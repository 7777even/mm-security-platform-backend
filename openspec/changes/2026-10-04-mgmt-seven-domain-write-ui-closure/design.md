# Design: 权限码落地与写 UI 范型

## 决策

### 1. 权限码命名沿用 `域:对象-write`
| 域 | 权限码 | 菜单 code | 端点 |
| --- | --- | --- | --- |
| video.camera | `video:camera-write` | `fm-video-camera-write` | POST `/video/cameras`、PUT/DELETE `/video/cameras/{id}` |
| communication.device | `communication:device-write` | `fm-communication-device-write` | POST `/communication/devices`、PUT/DELETE `/communication/devices/{id}` |
| communication.record | `communication:record-write` | `fm-communication-record-write` | POST `/communication/records`、PUT/DELETE `/communication/records/{recordNo}` |
| device | `device:write` | `fm-device-write` | POST `/devices`、PUT/DELETE `/devices/{code}` |
| hazard | `hazard:write` | `fm-hazard-write` | POST `/hazards`、PUT/DELETE `/hazards/{id}` |
| hazard.point | `hazard:point-write` | `fm-hazard-point-write` | POST `/monitoring/points`、PUT/DELETE `/monitoring/points/{id}` |
| special-operation | `special-operation:write` | `fm-special-operation-write` | POST `/special-operations`、PUT/DELETE `/special-operations/{id}` |

角色播种沿用 V101 的集合：ADMIN / COMMANDER / SCHEDULER / TEAM_LEADER / INNER_OPER / OUTER_OPER。

### 2. 父菜单归属的取舍
7 个按钮均为 `visible = 0`（仅作 RBAC 授权载体，不进菜单树）。`sys_menu` 现有一级节点只有
fm-emergency / fm-fire / fm-production / fm-security / fm-tv，**没有**「设备管理」「通讯通知管理」节点。
为避免为隐藏按钮新建可见菜单节点（会影响菜单树展示），统一挂到既有的 `fm-production` 下，
与 V74 登记 `production:ack` 的既有做法一致。后续若要精确菜单树归属，另行补节点再迁移 `parent_id`。

### 3. 定位键在编辑态必须不可改
`deviceCode` / `recordNo` / 监测点位 `id` 是各 PUT 端点的路径定位键。后端已在修复中明确
「不以请求体覆盖定位键」，前端相应把这些字段的 `FieldDef` 设 `disabledOnEdit: true`，
避免用户改掉编码后资源从原 URL 消失。数字自增 id 域（hazard / special-operation / video.camera）无此约束。

### 4. 前端统一照 GateView 范型
`MgmtRecordEditDialog` + `FIELDS` + `openCreate/openEdit/onSave/onDelete`，
删除必须 `ElMessageBox.confirm` 二次确认；操作列 `fixed="right"`；按钮与新增入口挂 `v-permission`。
字符串定位键的视图（CommRecordView / DeviceView / MonitorPointView）用页面内 `editKey` 判定新增 / 更新，
不改动 `MgmtRecordEditDialog` 的 `(payload, id: number | null)` 签名。

### 5. 读逻辑与订阅一行不改
改造只增不改：`load()`、筛选、分页、tab、`useDomainAutoRefresh` 订阅全部保持原样，
避免把已验证的「订阅 → 重拉」能力一起碰坏。
