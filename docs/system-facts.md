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
- 单测基线：standalone MockMvc + 纯 Mockito（**不起 Spring 上下文**）。当前 **434 单测全绿**（2026-09-10 含系统管理域 RBAC +73）；jacoco 行覆盖红线 **0.80**。
- 带 DB 的 `*IT` 在引入 Testcontainers 后启用；本机无 Docker 时如实报告未执行，**禁止用零 DB 通过冒充**。

## 2. 契约真源与四同步

- 机器可读契约**唯一真源**在 `frontend-scaffold/docs/api/*.openapi.json`，后端**不得**复制第二份主契约（禁止平行体系）。
- 改对外接口须四同步（同一次交付内）：
  1. openspec（两端各自 Change / spec）
  2. 前端契约 `docs/api/<domain>.openapi.json`（四条铁律：按域分组 / 接口有注释 / 字段有中文 description / 有 example）
  3. 后端实现 → 跑 `scripts/check-api-contract.mjs`（路由 + schema 双层级守门，`--strict` 进 CI）
  4. 通知前端 `npm run gen:api-types` 重新生成 TS 类型
- 后端 `node scripts/check-api-contract.mjs --strict`：路由差异 0 / schema 漂移 0（可比约 **197** schema）即达标；脚本默认 `--contracts ../frontend-scaffold/docs/api`。
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
  `GET /security/perimeter-alarms/{id}/snapshot` 字节端点；dev `PerimeterAlarmSnapshotSeeder` 生成占位 JPEG）；
  **V30 fac_node_phase_config**（应急流程节点联动配置：镜头锚点优先级 / 左右面板隐藏项 / 值班自动排班；
  列表字段以逗号串落库、`custom_center` 以 "lon,lat" 文本存储；9 节点种子取自前端 DEFAULT_NODE_PHASE_CONFIGS）；
  **V31 fac_emergency_phase / fac_emergency_response_mode / fac_emergency_process_stage / fac_emergency_node_guidance /
  fac_emergency_guidance_roster**（应急流程全景：5 阶段 + 4 响应模式显式列存，15 节点与 9 条处置指引的嵌套结构
  （动作/判据/升级规则/上报链路/岗位任务）序列化为 `detail_json LONGVARCHAR`，与 V27 同范式；
  种子由脚本从前端 `emergencyProcessData.ts` / `nodeGuidanceData.ts` 常量直出，避免手抄漂移）。
  **V32 sys_role / sys_role_menu / sys_dict_type / sys_dict_item**（系统管理域 RBAC 数据模型）＋ `sys_menu` 加
  `menu_type`(DIR/MENU/BUTTON) / `perm_code` / `visible`、`sys_user` 加 `pwd_updated_at` / `must_change_pwd`；
  **V33 种子**：6 条角色（ADMIN 内置 + 总指挥/值班调度/属地班长/内操/外操）＋ 5 条 fm-* 补权限码 ＋
  system 菜单权限子树（用户/角色/菜单/字典/设备编码 + 15 个 BUTTON 权限码）＋ ADMIN 全量授权、其余角色保留 fm-* 导航。
  **V34 data_scope 行级 ABAC**：`sys_zone`（防区字典）+ `sys_user.zone_codes VARCHAR(512)`（逗号串）；解析器 `DataScopeResolver` + `userZoneCache`(Caffeine TTL5min) 服务端解析，令牌仍只携 role（ADR-3）。通用 MyBatis 拦截器不可行（防区列名各异）→ 各 Service 显式调 `DataScopeHelper.apply(qw, zoneColumn, zones)`（ADR-4）；试点=救援队伍域 `FacBrigadeTeam.area`（值对齐 `BRIGADE_AREA` 字典与 `sys_zone.zone_name`）；`GET /system/zones` 登录可读供表单多选；匿名端点 `resolveZones` 返回 null 不过滤。
  **V35 大屏端剩余数据补接**（A3/A1/A2/B/C/D 收口，openspec `2026-09-10-screen-remaining-data-backend`）：新建 `fac_dispatch_personnel`（派单人员独立名册 10 条，**不复用脱敏的 sys_duty_member 或 375 人救援表**，替代前端 `AlarmDetailPanel` 硬编码 5 人名）＋ `fac_video_linkage_option`（预置点 9 / 业务对象 12 两类词表；相机名与相机类型由 `fac_video_camera` 派生不落此表）；回填 `fac_perimeter_alarm.dispatch_personnel`（id=1→杨恒朋、王钰、高策；id=2→李明辉、赵启明，原种子该列全空导致前端只能硬钉）；数据集加厚：`sys_duty_member` +8（生产调度部/消防救援部/安全环保部）、`fac_alarm` +7 闭环案例（`alarm_id` 取 `AE-2026-101~107` 避开 V2 种子 001-012 唯一约束）、`sys_knowledge_item` +6、`fac_monitoring_alarm` +9（ma-04~12）、`fac_system_message` +5、`fac_comm_device` +8（广播/电话/对讲）。三方言：`V35__screen_remaining_dataset.sql` 在 h2/dameng/postgresql 三目录齐备（达梦逐条 INSERT、pg 多行 VALUES，由 h2 派生脚本生成）；新表列名避 H2 保留字（`duty_role`/`person_name`/`sort_no`）。
  **V36 告警/巡检字典选项种子**：新增 `V36__dict_seed_alarm_patrol.sql`（h2/dameng/postgresql 三方言齐备，达梦逐条 INSERT、pg 多行 VALUES），向 `sys_dict_type`/`sys_dict_item` 插入 7 类字典（`fire_alarm_type`/`fire_alarm_source`/`fire_alarm_object_type`/`fire_alarm_object`/`fire_alarm_status`/`patrol_shift`/`patrol_status`，共 27 项），供 `GET /api/v1/system/dicts/{dictCode}` 读取（登录可读、仅启用项、按 `sort_order`；接口与 `SystemDictService.options` 无需改动）。item_value/item_label 与前端原常量逐一对齐（类型/来源/对象按显示串，状态用 code→中文），「全部X」哨兵由前端补、不入种子。
  **权限码口径**：业务域**沿用前端既有权限码**（`dashboard:view` / `fire-alarm:view` / `security:view` /
  `video:view` / `ops:view` / `fire-alarm:ack` / `mobile:field-report:view`），与 `src/router/menu.ts#MENU_ROUTE_SPECS`
  及 `SECONDARY_ROUTES` 的 `meta.perm` 逐一对齐——**自造新码会让前端路由守卫全量跳 404**。
  三方言同步：pg 与 h2 同构；达梦因 Oracle 兼容语法不支持多行 VALUES，角色种子拆为逐条 INSERT，
  且常量 SELECT 补 `FROM dual`（由一次性脚本从 h2 版本派生，见当日工作记忆）。
- 达梦 DM8 / PG 暂缓（本机无实例、无 docker）；`application-dm.yml` 与 `db/migration/dameng` 保留作迁移资产。代码层 DB 无关（MyBatis-Plus 方言探测、不写方言函数）。
- **H2 保留字陷阱**：`value` / `command` 既不能作裸列名，也不能作 MyBatis-Plus 别名（`SELECT x AS value` 同样报错）。列名加后缀（value→value_name），**Java 属性名也避开保留字**再 `@TableField` 映射。已验证非保留字：`name/code/type/status/level/time/op_type/op_level/ticket_status/value_text/status_name`。

## 6. 安全加固

- CORS：`CorsConfig` 必须先于所有过滤器注册；非 dev profile 含 `*` 启动即抛异常。
- 鉴权失败直出 401/403 + `Result.fail` B3（UTF-8），**不冒泡 500**。
- 密钥纯 `${JWT_SECRET}` / `${SIGNATURE_SECRET}` 无默认；`SecurityBeans.validateSecrets()` 校验长度与占位符。
- 越权防护：`AuthorizationService.assertAdmin` / `assertSelfOrAdmin`；建改删警 `@RequireAuth(role="ADMIN")`。
- **RBAC 模型（V32 起）**：`sys_user.role` **单角色**引用 `sys_role.role_code`；菜单与权限码授权元组为
  `sys_role_menu`；权限码即 `sys_menu.perm_code`（`menu_type=BUTTON` 节点不参与导航、只贡献权限码）。
  **`sys_menu.allowed_roles` 已降级为只读兼容列**，不再参与任何判定。角色/权限解析由 `RoleAuthorityService`
  （Caffeine `role_code→{menuIds,perms}` TTL 5min + 写时失效 `reloadRolePerms()`）提供——**权限码不写入 access 令牌**，
  故角色授权变更**即时生效**、令牌不膨胀。
- **`@RequireAuth` 三级门禁**：`value=false` 放行 → 未登录 401 → `role` 不符 403 → `perm` 不持有 403（role 与 perm 为 AND）。
  `perm` 由 `RequireAuthInterceptor` 经 `RoleAuthorityService` 判定。系统管理域当前统一用 `role="ADMIN"`（ADR-5 第一步），
  权限码已全量登记，后续可逐端点切 `perm="system:user:create"` 等而不改路径。
- **强制首登改密（服务端兜底）**：`PasswordLifecycleInterceptor` 对**变更类请求**（非 GET/HEAD/OPTIONS）校验
  `must_change_pwd`，命中则 403；豁免 `/api/v1/auth/**`（否则改密路径自身被堵死）与 `/api/v1/uplink/audit`（审计旁路）。
  状态缓存 `PasswordStateCache`（TTL 5min + 改密/重置时 evict）。
  **dev 关闭种子标记**（`app.password.force-change-default-admin=false`，因 H2 内存库每次重启重建，强制改密会反复阻断联调）；
  **生产默认 true**（`admin@2026` 属已知弱口令）。
- **系统管理域硬防护**（服务端强制，不依赖前端禁用按钮）：禁删/禁停用/禁改自己角色（403）；
  **保护最后一个启用 ADMIN**（409）；内置角色与内置字典禁删禁停（403）；角色被用户引用、字典有字典项、
  菜单节点有子节点或已被授权时禁删（409）。
- **唯一索引 × 逻辑删除的语义鸿沟（2026-09-10 冒烟发现）**：`sys_user.username` / `sys_role.role_code` /
  `sys_dict_type.dict_code` 均有唯一索引，而删除是逻辑删除（行仍物理存在）。MyBatis-Plus 查询自动追加 `deleted=0`，
  故 `selectCount` **看不到已删行** → 预检查放行、DB 唯一键拒绝，只能抛笼统「数据冲突」。已在三个 Mapper 加
  `countXxxIncludingDeleted(value[, excludeId])`（`@Select` 原生 SQL 绕过逻辑删除过滤）使判定与约束一致，
  并明确**标识符不回收**（回收会让新旧记录在审计上无法区分）。
- 新增对外接口须同步契约（§2），系统管理域契约文件为 `frontend-scaffold/docs/api/system.openapi.json`（tags: system）。
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
- 2026-09-10（写侧第 3 块·视频联动配置保存/删除）：`VideoController` 新增 `POST /api/v1/video/linkages`、`PUT/DELETE /api/v1/video/linkages/{configCode}`（`@RequireAuth(role="ADMIN")`，复用 V14 `fac_video_linkage` + `fac_video_linkage_rule`，无新迁移）；新增入参 DTO `VideoLinkageSaveRequest`/`VideoLinkageRuleInput`；`VideoService.saveLinkage(configCode 可空＝新建/更新，规则行整表替换，linkageCount/businessObjects 由 rules 推导)` + `deleteLinkage`（configCode 形如 `lk-NNN`，新建按现有最大数字后缀 +1；更新未命中返回 null、删除未命中 ok=false）。前端契约 +2 schema（`VideoLinkageSaveRequest`/`VideoLinkageRuleInput`）+ 复用本地 `DeleteResult`；`useVideoLinkageConfig.saveLinkageEdit(payload)` 改 async 落库、新增 `removeLinkageConfig`，`VideoLinkageConfigDialog` 提交/删除改接。门禁：mvn 356 绿、守门 strict 0/0（可比 163 / 路由 0 差异）、vue-tsc 0、vitest 378。
- 2026-09-10（写侧第 4 块·组态节点联动配置读+写）：新增 **V30 fac_node_phase_config**（9 节点：alarmJudgement/1min/3min/5min/plantArea/companyLevel/govLevel/handling/archive；列表字段 `camera_anchors`/`right_hidden_tabs`/`left_hidden_panels` 逗号串落库，`custom_center` 存 "lon,lat"）；新增实体 `FacNodePhaseConfig` + `FacNodePhaseConfigMapper` + DTO `NodePhaseConfig`/`NodePhaseMapCamera`/`NodePhaseDuty`；`EmergencyController` 新增 `GET /api/v1/emergency/process/node-configs`、`PUT /api/v1/emergency/process/node-configs`（`@RequireAuth(role="ADMIN")`，按 nodeId 整体 upsert、返回全量列表）；`EmergencyService.nodePhaseConfigs/saveNodePhaseConfigs` 负责逗号串↔数组互转。前端新增 `services/emergencyProcess.ts`，`nodeConfigData.ts` 契约类型上收至 service 并新增 `mergeNodeConfigs/cloneDefaultNodeConfigs`（保留默认值 + localStorage 离线缓存），`useEmergencyProcess` 的 `openNodeConfig` 触发远程拉取、`saveNodeConfig/resetNodeConfig` 改 async 落库（失败不假成功）。门禁：mvn 356 绿、守门 strict 0/0、vue-tsc 0、vitest 378。
- 2026-09-10（③类最后一项·系统管理域 + RBAC 模型）：L4 变更，四件套在
  `openspec/changes/2026-09-10-system-management-rbac/`（proposal/design/spec-delta/tasks，已人工确认后实施）。
  **数据模型**：V32/V33（见 §5）。**鉴权链路**：`RequireAuth` 加 `perm` 属性 + `RoleAuthorityService`（缓存解析）+
  `PasswordLifecycleInterceptor` / `PasswordStateCache`；`AuthService.menus()` 数据源由 `allowed_roles` 切到 `sys_role_menu`
  （返回结构不变，仍仅 5 个 fm-* 顶层）；`AuthService.me()` 由 `Map` 改强类型 `MeResult` 并增补 `roles`/`perms`/`mustChangePwd`；
  **修复 `AuthService.refresh()` 硬编码角色 `"ADMIN"` 的潜伏提权缺陷**（改为回查库中真实角色，账号不存在/已停用则拒绝续期）。
  **端点**：`/api/v1/system/{users,roles,menus,dict-types,dict-items,permissions,dicts}` 四组 CRUD（统一 `role="ADMIN"`，
  唯一例外 `GET /system/dicts/{dictCode}` 登录即可读）；`/auth/password`（本人改密）、`/auth/profile`（本人资料）。
  **口令生命周期**：`PasswordPolicy`（配置化：`app.password.{enabled,min-length,require-categories,force-change-default-admin}`）、
  管理员重置口令（随机临时口令 + `must_change_pwd=1`）。**服务端审计**：`SystemAuditHelper` 落 `fac_audit_log`（`module=system`，
  尽力而为、不含口令）。**启动种子**：原 `AuthService.ensureAdmin()` 拆出为 `RbacBootstrapService`（角色幂等 upsert +
  默认管理员 + ADMIN 授权兜底）。门禁：mvn **434** 绿 + jacoco 过、守门 strict 0/0（可比 **197**）、契约校验 28 域通过、
  vue-tsc 0、vitest **379**；8801 冒烟 **51 项 PASS**（含三条硬防护、强制改密兜底、字典两级与业务只读端点）。
  前端：`services/system.ts` 新增；`stores/auth.ts` 退役硬编码 `ROLE_PERMS`，改为 `/auth/me` 下发；新增
  `views/system/{roles,menus,dict}.vue` 与 `users.vue` 落地。
- 2026-09-10（③类·应急流程全景 后端化）：新增 **V31** 五表（`fac_emergency_phase` 5 阶段 / `fac_emergency_response_mode` 4 模式 / `fac_emergency_process_stage` 15 节点 / `fac_emergency_node_guidance` 9 指引 / `fac_emergency_guidance_roster` 1 值班表）；**种子由脚本经 esbuild 从前端 `emergencyProcessData.ts` / `nodeGuidanceData.ts` 常量直出**（避免手抄 25KB 嵌套 JSON），节点与指引的嵌套结构（处置动作 / 完成判据 / 升级规则 / 上报链路 / 岗位任务）序列化为 `detail_json LONGVARCHAR`（与 V27 同范式），阶段与响应模式用显式列（`start_stage`/`end_stage`/`mode_code`/`mode_label` 回避保留字）。新增 5 实体 + 5 Mapper + **14 个契约同名 DTO**（`EmergencyPhase`/`ResponseModeOption`/`ProcessStage`/`ProcessAction`/`CriteriaChecklistItem`/`SubStageItem`/`StageEscalationRule`/`StageEscalationDetails`/`EmergencyProcessPanorama`/`NodeGuidance`/`NodeGuidanceReportingStep`/`NodeGuidanceRoleTask`/`GuidanceDutyRoster`/`EmergencyProcessGuidance`）；`EmergencyController` 加 `GET /api/v1/emergency/process/panorama`、`/process/guidances`；`EmergencyService` 加 `processPanorama()/processGuidances()` + `readJson()` 防御式反序列化（解析失败返回空实例，避免列表出现 null）。前端 `services/emergencyProcess.ts` 收口该域全部契约类型并加 2 个 fetch；`emergencyProcessData.ts`/`nodeGuidanceData.ts` 降级为「契约类型 re-export + 离线兜底默认值」；`useEmergencyProcess` 把 phases/响应模式/stages/指引/值班表改 ref + 新增 `loadEmergencyProcessRemote()`（成功覆盖、失败告警保留默认值），由 `PlanPanoramaModule` onMounted 与 `openGuidance()` 触发；`PlanPanoramaModule`（2308 行）不再直引 `EMERGENCY_PHASES`，改用组件 computed `phaseList`/`roster`。门禁：mvn **361** 绿、守门 strict 0/0（可比 177）、vue-tsc 0、vitest 378、eslint 0；8801 冒烟 **34 项全 PASS**（5 阶段 / 4 模式 / 15 节点连续性 / 模板生成节点 / 9 指引字段与值班表逐项核对）。
- 2026-09-10（大屏端剩余数据补接·V35）：消除大屏端直读本地 mock 常量的最后几处展示数据。**A3 派单人员**：新增 `fac_dispatch_personnel`（独立名册 10 条，实体/DTO/Mapper + `EmergencyService.dispatchPersonnel()` + `GET /api/v1/emergency/dispatch-personnel`，`AlarmDetailPanel` 改为 watch 弹窗打开时拉取填充 select，移除硬编码 5 人名）。**A1 巡更联动零后端改动**：`usePatrolLinkage.ts` 完全重写，改引 `@/services/security` 的 `fetchPatrolCameras` 按 `activePatrolZoneLabel` 过滤派生 `PatrolLinkagePoint[]`，删除 `patrolLinkageMock.ts` 依赖。**A2 视频联动选项**：新增 `fac_video_linkage_option` + `VideoService.linkageOptions()`（相机名/类型由 `fac_video_camera` 派生，预置点/业务对象读词表）+ `GET /api/v1/video/linkage-options`；`VideoLinkageConfigDialog` 四 select 改绑远程选项，删除 `videoLinkageOptions.ts` 常量。**B 数据加厚**：V35 回填 dispatch_personnel + 6 张表加厚（值班部门/闭环案例/知识库/监测告警/系统消息/通讯设备）。**C WS/快照验证**：实证 `/ws/alarm` 收到 `alarm.push` 帧、两个 snapshot 端点返回真实 image/jpeg 字节（~20KB）。**D 门禁**：`scripts/screen-local-data-gate.mjs`（聚焦回归守卫——3 处已后端化组件不得值引用本地业务常量 + 其余 lib/data 值引用透明审计 exit 0）。契约四同步：前端 `emergency.openapi.json`/`video.openapi.json` 各增端点 + schema，`npm run gen:api-types` 重新生成 TS 类型。门禁：mvn **446** 绿（含 `EmergencyServiceTest` 补 FacDispatchPersonnelMapper）、守门 strict 0/0、vue-tsc 0、vitest 379；8787 实证 `/emergency/dispatch-personnel`=10 条、`/video/linkage-options`=monitorNames27/presetPoints9/categories4/objects12、闭环案例 8（原 1）、周界告警 dispatchPersonnel=['杨恒朋、王钰、高策']。**遗留**：大屏组件仍大量值引用 lib/data/*Mock（accidentRescueMock×16 等），"全量零本地数据" 属更大工程，本次收口为聚焦回归守卫 + 透明审计，未做全量拔除。
