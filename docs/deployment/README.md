# 端到端部署手册

> **定位**：把系统真正跑起来（演示 / 生产）。本地开发联调细节见 [`docs/integration/README.md`](../integration/README.md)，本手册不重复。
>
> **当前状态（2026-09-08）**
> - 容器化文件已补齐：后端 `Dockerfile`、前端 `Dockerfile` + `deploy/nginx.conf`、后端 `deploy/docker-compose.yml` + `.env.example`。
> - ⚠️ **本机未安装 Docker，以上 compose / 镜像均未实跑验证**，结构已通过语法校验；首次使用请在有 Docker 的环境执行并反馈问题。
> - ⚠️ 达梦 DM8 **已决策暂缓启用**（本机无实例 / 驱动 / Docker），配置与迁移脚本作为后期迁移资产保留，见 §5。

---

## 1. 部署拓扑

| 角色 | 端口 | 说明 |
| --- | --- | --- |
| 前端（nginx） | `8080`（compose 映射） | 托管 `dist`；反代 `/api/` 与 `/ws/` 到后端 |
| 后端（Spring Boot） | `8787` | 上下文 `/api/v1`；WS 告警推送 `/ws/alarm` |
| 数据库 | `5236`（达梦，可选） | dev 演示用 **H2 内存库**，不占端口、无需安装 |

compose 内部服务名：`frontend` / `backend` / `db-dm`。前端 nginx 通过服务名 `backend:8787` 反代。

---

## 2. 目录约定

两个仓库是**独立 git 仓库**，需同级 clone（compose 的前端 build context 依赖此约定）：

```
<root>/
├── backend-scaffold/     # 后端仓（本文档、Dockerfile、deploy/ 在此）
└── frontend-scaffold/    # 前端仓
```

---

## 3. 方式 A：docker compose（推荐，一键演示）

默认 profile 为 `dev`，后端用 **H2 内存库**，不需要任何数据库、也不需要配置密钥（dev profile 自带 dev 密钥）。

```bash
cd backend-scaffold/deploy
docker compose up --build
```

| 访问 | 地址 |
| --- | --- |
| 前端 | `http://localhost:8080` |
| 后端健康检查 | `http://localhost:8787/actuator/health` |
| WS 告警推送 | `ws://localhost:8787/ws/alarm` |

默认账号：**`admin` / `admin@2026`**（后端 `AuthService.ensureAdmin()` 种子写入）。

> 注意：dev 用 H2 内存库，**重启即丢数据**，仅适合演示/联调。

### 常见操作

```bash
docker compose up --build          # 构建并启动（dev/H2）
docker compose down                # 停止
docker compose down -v             # 停止并清库（达梦数据卷）
docker compose logs -f backend     # 看后端日志
docker compose --profile dm up     # 连达梦（见 §5，需自备镜像与驱动）
```

---

## 4. 方式 B：传统部署（无 Docker）

### 4.1 前端

```bash
cd frontend-scaffold
npm ci
npm run build           # 产出 dist/（vue-tsc -b && vite build）
```

将 `dist/` 交给 nginx 托管，配置可直接复用 `deploy/nginx.conf`（SPA 回退 + `/api/`、`/ws/` 反代 + 静态缓存）。

> ⚠️ `VITE_API_BASE` / `VITE_ALARM_WS_URL` 在**构建期**固化进产物，跨域或网关场景需改这两个变量后重新构建，或用 nginx 反代屏蔽差异。

### 4.2 后端

```bash
cd backend-scaffold
mvn -s ci-settings.xml package -DskipTests
java -jar target/mm-security-backend-1.0.0-SNAPSHOT.jar
```

### 4.3 nginx 关键片段

```nginx
location / {
    try_files $uri $uri/ /index.html;      # SPA history 回退
}
location /api/ { proxy_pass http://backend:8787; ... }
location /ws/  {                            # WebSocket 必须升级协议
    proxy_pass http://backend:8787;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_read_timeout 3600s;
}
```

---

## 5. 数据库与达梦迁移路径

| profile | 数据库 | 状态 |
| --- | --- | --- |
| `dev`（默认） | H2 内存库 | ✅ 唯一可实跑，演示/联调 |
| `prod` | PostgreSQL | 回退方案。⚠️ 本机未安装，需另备环境 |
| `dm` | 达梦 DM8 | ⏸️ **暂缓启用（后期迁移目标）** |

**达梦暂缓原因**：无 DM8 实例（5236 无监听）、无 `DmJdbcDriver18.jar`、未安装 Docker。同样的原因，**PostgreSQL 本机也不具备**，因此"不用达梦"之后本机仍只有 H2 可跑。

**达梦配置与迁移脚本保留勿删**：`application-dm.yml`、`db/migration/dameng/V1__init_schema.sql`、`V2__seed_data.sql`、pom 的 `dm` profile。

具备达梦环境后按此清单迁移：

1. 安装驱动（受授权限制，不随镜像分发）：
   ```bash
   mvn install:install-file -Dfile=DmJdbcDriver18.jar -DgroupId=com.dameng \
       -DartifactId=DmJdbcDriver18 -Dversion=8.1.3 -Dpackaging=jar
   ```
2. 构建含驱动的镜像：`docker build --build-arg BUILD_PROFILE=dm ...`
3. 配置 `deploy/.env`（从 `.env.example` 复制），激活 `SPRING_PROFILES_ACTIVE=dm`
4. 启动：`docker compose --profile dm up --build`
5. 验收：Flyway 迁移成功 → 应用启动 → 种子数据校验 → 接口冒烟（§7）

> 数据源变量是**自定义名** `DB_HOST / DB_PORT / DB_NAME / DB_USERNAME / DB_PASSWORD`，不是标准的 `SPRING_DATASOURCE_*`。
> 另：Flyway 社区版无达梦官方 database 模块，迁移 SQL 需在真实实例上复核（见 `application-dm.yml` 注释）。

---

## 6. 生产环境变量清单

dev profile 自带密钥与 `CORS=*`，**无需注入**；切到 `prod` / `dm` 时以下为**必填**（缺失即启动失败，fail-fast）：

| 变量 | 必填 | 说明 |
| --- | --- | --- |
| `JWT_SECRET` | ✅ | JWT 密钥，≥ 32 字节。base yml 为纯 `${JWT_SECRET}` 无默认值 |
| `SIGNATURE_SECRET` | ✅ | 防重放签名密钥，≥ 16 字节 |
| `CORS_ALLOWED_ORIGINS` | ✅ | 前端域名白名单（逗号分隔，**不含 `*`**；非 dev profile 含 `*` 会启动抛异常） |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | ✅（dm/prod） | 数据源，达梦默认 `localhost:5236/mm_security` |
| `DB_USERNAME` / `DB_PASSWORD` | ✅（dm/prod） | 数据库凭证 |
| `COOKIE_SECURE` | 建议 | 刷新 Cookie(`rt`) 的 `Secure` 标记；生产 HTTPS 必须 `true` |
| `SERVER_PORT` | 可选 | 默认 `8787` |

**compose 注入方式**：敏感项写在 `deploy/.env`（`env_file` 注入）。刻意**不**放在 `environment:` 里——环境变量优先级高于 `application-dev.yml`，空字符串会覆盖 dev 自带密钥导致启动即失败。

---

## 7. 健康检查与冒烟

```bash
# 后端存活
curl -s http://localhost:8787/actuator/health      # 期望 {"status":"UP"}
curl -s http://localhost:8787/actuator/info

# Prometheus 指标抓取端点（Micrometer 暴露，供监控系统拉取 JVM/HTTP/业务指标）
# 该端点免鉴权，生产务必限制在内网或监控网段，勿直接暴露公网。
curl -s http://localhost:8787/actuator/prometheus | head -8

# 端到端最小路径
# 1) 登录拿 accessToken（refresh 令牌由 Set-Cookie 下发，不进 body）
# 2) GET /api/v1/auth/menus        -> 200，5 个顶部 fm-* 菜单
# 3) GET /api/v1/devices           -> 200
# 4) GET /api/v1/dashboard/overview-> 200
# 5) ws://localhost:8787/ws/alarm  -> 收到 {topic:'alarm.push', payload:{...}}
```

中文冒烟请用 Python(utf-8) 发起，**勿裸 `curl` 带中文 body**（Windows 控制台 GBK 会乱码）。另可用 `scripts/smoke-test.ps1`。

---

## 8. CI/CD

两仓各自接入 GitHub Actions（`.github/workflows/ci.yml`）：

| 仓库 | 门禁内容 |
| --- | --- |
| 后端 `main` | Maven 单测 + **jacoco 覆盖率门禁**（行覆盖率红线 `0.80`，实测基线 87.60%）→ 覆盖率报告以 artifact 留存；跨库 API 契约守门（稀疏拉前端 `docs/api` 后 `--strict` 对拍） |
| 前端 `feature/scaffold-rebuild` | `npm ci` → `type-check` → `vitest run`（338 用例）→ 契约四铁律校验 → 生产构建 |

> CI 里**不要**加 `-s ci-settings.xml`：该文件把 `localRepository` 硬编码为 Windows 路径 `C:/Users/7even/.m2/repository`，Linux runner 上不可用。

---

## 9. 已知限制

1. **Docker 相关未实跑**：本机无 Docker，compose / Dockerfile 仅通过结构与语法校验。
2. **达梦未验证**：见 §5，属后期迁移项。
3. **CSP 默认关闭**：`deploy/csp.conf` 的 nonce 是占位符（`REPLACE_WITH_GATEWAY_NONCE`），需入口网关/OpenResty 注入真实 nonce 后才能启用，否则内联脚本被拦导致白屏。
4. **分支覆盖率偏低**（约 11%）：仅以行覆盖率做门禁；提升分支覆盖是后续可改进项。
