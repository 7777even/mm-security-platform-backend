# Retro — 后端安全与工程正确性加固（2026-09-07）

## 做了什么

按 `openspec/changes/harden-backend-baseline` 落地 P0/P1 加固 + 零依赖测试基线：
生产配置基线、CORS 收敛、过滤器顺序显式化、分页方言自动判定、设备出参 DTO 化去 mock、18 case 测试全绿。

## 做得好的

- **契约真源单一**：改动全程未复制第二份 OpenAPI，仅用脚本比对，避免双源漂移。
- **TDD 收口**：6 个测试类零依赖（standalone MockMvc / 纯 Mockito / 反射注入），CI 秒级、不连库，符合 AGENTS.md §2.2 起步基线。
- **白名单兜底断言**：`WebMvcConfig` 在白名单为空时主动抛 `IllegalStateException`，防止误提交导致 CORS 静默失效。
- **Windows 构建通路打通**：发现 `mvn`/`mvnw` 在沙箱均坏（MINGW 路径翻译 + 发行版缺失），改用 `mvn.cmd --%` + `ci-settings.xml` 离线/在线构建成功，已沉淀为可复用手法。

## 踩的坑

- **Maven 在 Windows 沙箱坏**：`mvn` 抛 `ClassNotFoundException`（MINGW 把 boot jar glob 翻译坏），`mvnw` 发行版缓存缺失。最终用 `D:\apache-maven-3.9.11\...\mvn.cmd` + PowerShell 风格 `--%` 解决。
- **离线模式拒缓存**：`-o` 下 `_remote.repositories` 标记来源 aliyunmaven 不可达而报 FATAL。改在线（`-s ci-settings.xml`）即解析通过——可见本机有网，离线反而更受限。
- **移除 @Component 后 @Value 失效**：HmacFilter/JwtFilter 去掉自动注册后，原 @Value 字段需改构造函数注入，由 `SecurityBeans` 装配。这是显式注册模式的必然后果，已正确处理。

## 改进项

- `AlarmController`/`AlarmService` 仍残留 `devFallbackList` mock，与本次 Device 去 mock 不一致，应纳入下一轮契约债清理。
- 测试的 `anyLong` 等未用 import 已清理；后续新增测试保持零依赖约束。
- 契约校验脚本默认仅报告（exit 0），建议在契约债消化后于 CI 开 `--strict`。

## 下一步

- 消化跨库契约技术债：新建 `device.openapi.json`，补齐 auth/login、auth/refresh、auth/me、dashboard/workstations 前端契约，明确 401/403 口径。
- 清理告警域 mock 回落。
