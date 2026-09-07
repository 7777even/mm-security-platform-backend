# docs/api/ — 跨库契约同步纪律（后端视角）

## 契约真源不在本库（重要）

接口**机器可读契约的唯一真源在前端库**：

```
frontend-scaffold/docs/api/
├── _shared.json            # 跨域共享组件（安全方案 / 分页参数 / B3 包络 / 错误响应）
├── auth.openapi.json
├── alarm.openapi.json
├── dashboard.openapi.json
├── device.openapi.json     # 设备台账（已与 DeviceController 对齐）
├── map.openapi.json
├── emergency.openapi.json
├── uplink.openapi.json
├── realtime.openapi.json   # /ws/alarm 推送（仅订阅）
└── gis.openapi.json        # 前瞻性桩
```

**本目录不复制第二份 OpenAPI 主契约**——两处副本必然漂移，违反根 `AGENTS.md` §1.4「禁止平行体系」。本目录只维护：实现映射、同步纪律、校验方式。

> 为什么真源在前端：前端按契约生成 TS 类型（`npm run gen:api-types`），是 API Contract First 的既有落点；后端作为实现方对齐契约。

## 实现映射表（Controller ↔ 契约域）

| Controller              | 基础路径            | 已实现端点                                          | 前端契约文件                    | 状态                                   |
| ----------------------- | ------------------- | --------------------------------------------------- | ------------------------------- | -------------------------------------- |
| `HealthController`      | `/api/v1`           | `GET /health`                                       | （健康检查不入契约，已豁免）     | 免鉴权                                 |
| `AuthController`        | `/api/v1/auth`      | `POST /login`、`POST /refresh`、`GET /me`、`GET /menus` | `auth.openapi.json`         | 已对齐（login/refresh/me 已补入契约） |
| `DashboardController`   | `/api/v1/dashboard` | `GET /overview`、`GET /workstations`                 | `dashboard.openapi.json`        | 已对齐（workstations 已补入契约） |
| `DeviceController`      | `/api/v1/devices`   | `GET`（分页）、`GET /{code}`                         | `device.openapi.json`            | 已对齐（契约文件已建）              |
| `AlarmController`       | `/api/v1/alarms`    | `GET`（分页）                                        | `alarm.openapi.json`            | 查询已对齐；契约的 POST/PUT/DELETE 未实现 |
| `AlarmWebSocketHandler` | `/ws/alarm`         | 订阅推送（dev 每 12s 模拟一条）                      | `realtime.openapi.json`         | 已对齐                                 |
| `gis.openapi.json`      | —                   | 外部网关（`https://gateway.example.com/gis`）         | 前瞻性桩                        | **不由本服务实现**                     |
| `map` / `emergency` / `uplink` | —            | 尚未实现                                            | 真实契约                        | 待实现（技术债）                       |

> 契约域与实现的差异（契约有 / 实现无）由 `scripts/check-api-contract.mjs` 检出。截至 2026-09-07，「实现有 / 契约无」已归零；剩余差异均为前端超前、后端尚未实现的前瞻桩（`alarm` 的 POST/PUT/DELETE、`dashboard/alarm-trend`、`risk-heatmap`、`emergency/*`、`map/*`、`uplink/*` 及 `gis` 外部网关），属**待实现技术债**，须在 openspec Change 中消化，不得静默。

## 同步纪律（改接口必走四同步）

1. **OpenSpec**：后端业务变更先建 `openspec/changes/<name>/`，proposal 中写明「本次触及的契约文件与端点」。
2. **前端契约**：同一次交付更新 `frontend-scaffold/docs/api/<domain>.openapi.json`，遵守四条铁律：
   - 按域分组（`tags` 用域 / Controller 名）；
   - 每个接口有 `summary` + `description`；
   - 每个字段有中文 `description`；
   - 每个接口有成功响应 `example`（B3 包络 `code=0` 取 `data`）。
3. **后端实现**：Controller / DTO 与契约对齐，跑 `node scripts/check-api-contract.mjs` 确认端点不漂移。
4. **前端类型**：通知前端重跑 `npm run gen:api-types`；后端在 `engineering/qa/` 记录中注明「已通知前端重生成类型」。

**禁止**：只改后端代码不改前端契约；只改契约不通知前端；用 Markdown 接口清单替代 OpenAPI；在本库复制契约文件。

## 后端必须满足的契约语义

| 契约                 | 后端落点                                              |
| -------------------- | ----------------------------------------------------- |
| B3 包络              | `common/Result<T>`：`code` / `message` / `data` / `traceId`，`code=0` 成功 |
| 零下行控制           | `security/HardControlInterceptor`（名单内 POST/PUT/DELETE → `503`） |
| 20 位 MDM 设备编码   | `common/DeviceCode` + `DeviceCodeValidator`（违反 → `301`） |
| 防重放签名           | `security/HmacFilter`（`X-Timestamp` / `X-Nonce` / `X-Signature`） |
| 无状态令牌           | `security/JwtUtil` 签发，服务端不落盘、不写 Cookie      |
| 错误码分段           | `common/ResultCode`：`1xx` 通用 / `2xx` 鉴权 / `3xx` 设备 / `5xx` 硬控 |

## 校验工具

```bash
# 默认读 ../frontend-scaffold/docs/api；可用 --contracts 指定其它目录
node scripts/check-api-contract.mjs
node scripts/check-api-contract.mjs --contracts ../frontend-scaffold/docs/api
node scripts/check-api-contract.mjs --strict        # 有差异时退出码 1（CI / 守门用）
```

脚本扫描 `src/main/java/**/controller/**` 的 `@RequestMapping` / `@GetMapping` 等注解（外加 `WebSocketConfig` 的 `addHandler` 注册），还原端点清单，与契约 `paths`（按 `servers[0].url` 补全前缀）比对，输出：

- **契约有 / 实现无**（missing-impl）
- **实现有 / 契约无**（missing-contract）
- **外部网关契约，不由本服务实现**（server 为绝对 URL，如 `gis.openapi.json`）
- **已对齐**（matched）

豁免：`/api/v1/health`、`/actuator/health` 属运维端点，不入户契约。

退出码：**默认 `0`（仅报告）**；`--strict` 且存在差异时为 `1`。当前两端存在历史技术债差异，CI 接入前先在 openspec Change 中消化，再开 `--strict`。
