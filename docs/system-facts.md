# 系统事实基线（后端）· System Facts

> **用途**：描述「系统现在是什么样」，是人维护、随代码演进的当前事实基线。**不是** AI 指令（约束写法的是 `AGENTS.md`）。AI 改动后端代码前应读本文确认现状；改动导致下列事实变化时**必须回来同步本文件**。
>
> 分库说明：原根目录「长期共识文档」已废弃（根不是 git 仓库，文件不会进版本控制）。本文件与 `frontend-scaffold/docs/system-facts.md` 为各自仓库提交的事实真源。AI 工作记忆（`.workbuddy/memory/MEMORY.md`）是本文件的镜像，以本文件为准。

## 1. 构建与测试

- **Maven 唯一可用命令**（本机）：
  `D:\apache-maven-3.9.11\apache-maven-3.9.11\bin\mvn.cmd -s ci-settings.xml test`，
  环境变量 `JAVA_HOME=D:\jdk-17_windows-x64_bin\jdk-17.0.4.1`。
  `mvn` / `mvnw` 均不可用，勿用。
- **CI 绝不能带 `ci-settings.xml`**：该文件硬编码本机 Windows `.m2` 路径，仅本地冒烟用。
- 单测基线：standalone MockMvc + 纯 Mockito（**不起 Spring 上下文**）。当前 **342 单测全绿**（2026-09-10 含预案行动卡写接口 +9、黑名单删除 +5）；jacoco 行覆盖红线 **0.80**。
- 带 DB 的 `*IT` 在引入 Testcontainers 后启用；本机无 Docker 时如实报告未执行，**禁止用零 DB 通过冒充**。

## 2. 契约真源与四同步

- 机器可读契约**唯一真源**在 `frontend-scaffold/docs/api/*.openapi.json`，后端**不得**复制第二份主契约（禁止平行体系）。
- 改对外接口须四同步（同一次交付内）：
  1. openspec（两端各自 Change / spec）
  2. 前端契约 `docs/api/<domain>.openapi.json`（四条铁律：按域分组 / 接口有注释 / 字段有中文 description / 有 example）
  3. 后端实现 → 跑 `scripts/check-api-contract.mjs`（路由 + schema 双层级守门，`--strict` 进 CI）
  4. 通知前端 `npm run gen:api-types` 重新生成 TS 类型
- 后端 `node scripts/check-api-contract.mjs --strict`：路由差异 0 / schema 漂移 0（可比约 158 schema）即达标；脚本默认 `--contracts ../frontend-scaffold/docs/api`。
- 守门输出格式 `字段:契约≠后端`（左契约右实现）。`number≠integer` 类漂移按**后端种子数据**裁决：种子全整数就改契约为 integer，别反过来动已跑通的后端。
- `oasFamily()` 已修：OpenAPI 3.1 可空联合 `"type":["string","null"]` 剥离 `'null'` 后取剩余类型，多类型联合遇 `null` 不比对（曾误判 90 处假漂移）。再遇批量 `object≠string` 先怀疑脚本而非契约。

## 3. 跨库顺序（已 CI 化）

- 前端仓 `contract-guard` job 稀疏检出后端 `main` 的 `scripts + src/main/java`，用后端守门脚本对本仓 `docs/api` 对拍。
- 后端仓 `contract-guard` 显式 `--contracts frontend-scaffold/docs/api`。
- 两 job 双向验证：任一侧先推、另一侧必红，直至两端契约一致。**「先推契约、再推后端实现」不再靠人记**。私有仓需 Settings → Secrets 配 `CONTRACT_REPO_TOKEN`（PAT），公开仓回退 `github.token`。

## 4. 提交与远端

- 按 scope 拆分提交、不 amend；两仓独立 git、独立推送。
- 后端分支 `main`、前端分支 `feature/scaffold-rebuild`。
- 远端均 HTTPS（SSH :22 被防火墙拒绝）：
  `https://github.com/7777even/mm-security-platform-backend` / `-scaffold`。
- 沙箱 Bash 推送：`GIT_TERMINAL_PROMPT=0 git -c credential.helper=wincred push -u origin <branch>`（用 Windows 内置 wincred 助手，无需交互）。

## 5. 数据库策略

- dev 唯一可实跑：H2 内存 + Flyway（`src/main/resources/db/migration/h2`），`spring.sql.init.enabled=false`。
- **Flyway 版本纪律**：已进入共享环境的 V-file **禁改/删**，新增只加 V17+。已落地：
  V6 `fac_field_report`；V7 `sys_menu` 加 `allowed_roles` + 重种 5 个 fm-* 顶部菜单；
  V8 应急力量/通讯录/知识库/值班表（硬编码迁 DB）；V13 production 域；V14 fac_video_*；V26 fac_video_camera 加 snapshot_bytes BLOB + GET /video/cameras/{id}/snapshot（dev VideoSnapshotSeeder 生成占位 JPEG）。
  V15 fac_tv_*；V16 fac_special_operation_*；V17 emergency_event / V18 emergency_plan /
  V19 rescue_resource / V20 fire_facility / V21 blacklist+fire_situation /
  V22 communication+weather；V23 duty 夜班种子；V24 大屏面板数据集（消防设备分类/系统消息/电视地图撒点/监控档案）；
  **V27 fac_emergency_cmd**（应急指挥指令，detail_json LONGVARCHAR 存派发对象+日志+媒体，种子 n1-n3/d1-d3/t1-t2）；
  **V28 fac_security_track + fac_security_track_meta**（巡更/通行轨迹时间轴 + 起止点标签）＋ fac_vehicle_search / fac_person_search 详情扩展列；
  **V29 fac_perimeter_alarm**（周界入侵告警，字段对齐前端 AlarmDetailItem 视图模型，含 snapshot_bytes BLOB +
  `GET /security/perimeter-alarms/{id}/snapshot` 字节端点；dev `PerimeterAlarmSnapshotSeeder` 生成占位 JPEG）。
- 达梦 DM8 / PG 暂缓（本机无实例、无 docker）；`application-dm.yml` 与 `db/migration/dameng` 保留作迁移资产。代码层 DB 无关（MyBatis-Plus 方言探测、不写方言函数）。
- **H2 保留字陷阱**：`value` / `command` 既不能作裸列名，也不能作 MyBatis-Plus 别名（`SELECT x AS value` 同样报错）。列名加后缀（value→value_name），**Java 属性名也避开保留字**再 `@TableField` 映射。已验证非保留字：`name/code/type/status/level/time/op_type/op_level/ticket_status/value_text/status_name`。

## 6. 安全加固

- CORS：`CorsConfig` 必须先于所有过滤器注册；非 dev profile 含 `*` 启动即抛异常。
- 鉴权失败直出 401/403 + `Result.fail` B3（UTF-8），**不冒泡 500**。
- 密钥纯 `${JWT_SECRET}` / `${SIGNATURE_SECRET}` 无默认；`SecurityBeans.validateSecrets()` 校验长度与占位符。
- 越权防护：`AuthorizationService.assertAdmin` / `assertSelfOrAdmin`；建改删警 `@RequireAuth(role="ADMIN")`。
- 刷新令牌 Cookie 化（`rt`，HttpOnly / SameSite=Lax / 7d），**绝不进 body**；`refresh` 用 `@CookieValue`，前端 `http.ts` 须 `withCredentials`。
- **JwtFilter 白名单（全局，覆盖所有 Controller）**：仅 `/auth/{login,refresh,logout}` 与 `/ws` 免鉴权。
  `/auth/me`、`/auth/menus` 已移出白名单（契约声明 401/403）；`/ws` 免鉴权是刻意设计（内网只读流，勿给 `ws.ts` 加 query 令牌）。
  ⚠️ 注意：Controller 不加 `@RequireAuth` ≠ 免鉴权，全局过滤器仍强制带 token（例如 `/tv/*` 无 token 返回 401）。
- 前端**无 401 静默刷新拦截器**：401 仅清内存令牌 + `onUnauthorized` 重登；续期须走 rt Cookie 调 `/auth/refresh` 并防重试死循环。

## 7. 联调与默认环境

- 后端端口 **8787**。前端 `VITE_API_BASE=http://localhost:8787/api/v1`、`VITE_USE_DEV_MOCK=false`、`VITE_ALARM_WS_URL=ws://localhost:8787/ws/alarm`。
- WS 包络：`{topic:'alarm.push', payload:AlarmItem}`。默认管理员账号 `admin / admin@2026`。`/auth/menus` 返 5 个顶部 fm-* 菜单。
- `gis.openapi.json` 为外部网关前瞻桩，**不实现**。

## 8. Windows 本地坑

- WorkBuddy 注入 `SERVER__PORT` 会覆盖 8787 → 启动须 `mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8787"`（只写 `-Dserver.port` 因 fork 不生效）。
- curl 发中文 body 踩 GBK 乱码 → 一律用 Python（utf-8）冒烟。

## 9. 里程碑速记

- 2026-09-08：两仓 CI/CD + 跨库契约守门；Dockerfile/compose（本地无 docker 未实跑）；jacoco 0.80；openspec 回填；Prometheus `/actuator/prometheus`。
- 2026-09-09：生产应急域全栈接线（V13）；video/tv/special-operation 三域（V14–V16）+ 前端契约/services/面板改接；大屏去 mock 收尾（V24 + 4 域端点）。
- 2026-09-10：续验大屏四端点冒烟全 `code=0`；前端 `vue-tsc` 全绿；契约守门 0 漂移；本系统事实基线分库落地。
- 2026-09-10（B4/B5/B6 大屏去 mock 收尾）：B4 应急指挥指令（`/emergency/commands` + `/{id}`，V27）；B5 巡更/通行轨迹（`/security/track/{timeline,summary}`）与车辆·人员检索详情（`/security/search/{vehicle,person}/{id}`，V28）；B6 周界入侵告警（`/security/perimeter-alarms/latest`、`/{id}`、`/{id}/snapshot`，V29），前端 `perimeterAlarmToDetail` 适配器把 DTO 映射为 30+ 字段 `AlarmDetailItem`，`SecurityStatusPanel` 弃用 `resolveDemoAlarmDetailById('demo-intrusion-1')`（两份 `alarmDetailMock.ts` 副本的 `demoAlarmDetails` 死常量一并删除）。
- 2026-09-10（video 流媒体递延项·静态图后端化）：video 静态图改由后端传——fac_video_camera 加 `snapshot_bytes` BLOB（V26），`GET /video/cameras/{id}/snapshot` 返回 `image/jpeg`（无则 404）；dev 启动 `VideoSnapshotSeeder`（`CommandLineRunner` + `@Profile dev`）用 `BufferedImage`+`ImageIO` 生成带名称/位置/REC 角标占位 JPEG 写回 BLOB（27/27 张）。前端 `VideoControlGrid` 用 `blob`→`objectURL` 的 `<img>` 替换原雪碧图占位（规避 `<img>` 无法带 JWT 的 401 坑）。门禁：mvn test 309 绿、vue-tsc 0 错、契约守门 0 漂移；运行时冒烟 camera id=1 → 200 image/jpeg 20144 字节。
- 2026-09-10（写侧后端化第 1 块·预案行动卡 CRUD）：`EmergencyPlanController` 新增 `POST /api/v1/emergency-plans/{planId}/action-cards`、`PUT/DELETE .../{cardId}`（`@RequireAuth(role="ADMIN")`，复用 V18 `fac_plan_action_card` 表，无新迁移）；新增入参 DTO `PlanActionCardCreate`/`PlanActionCardUpdate`；`EmergencyPlanService` 加 `createActionCard/updateActionCard/deleteActionCard`（card_code `ac-`+UUID12，缺省 status=pending）。前端契约 + `usePlanMatrix` 三写函数已同步接线（无 `VITE_API_BASE` 保持本地演示改）。门禁：mvn 337 绿、守门 strict 0/0（可比 158）、vue-tsc 0、vitest 378；8801 运行时冒烟建改删全通。
- 2026-09-10（写侧第 2 块·黑名单删除）：`BlacklistController` 新增 `DELETE /api/v1/security/blacklist/vehicles/{id}`、`/persons/{id}`（`@RequireAuth(role="ADMIN")`，复用 V21 `fac_blacklist_entry` 表，无新迁移）；`BlacklistService` 加 `removeVehicle/removePerson`（按 id+entryKind 双重校验，未命中返回 `{ok:false}` 不抛异常）。前端契约 + `securityBlacklist.ts` + `BlacklistDialog.vue` 同步接线。门禁：mvn 342 绿、守门 strict 0/0、vue-tsc 0、vitest 378；8801 冒烟删车/删人后 3→2，重复删/错类型/不存在均 ok=false。
