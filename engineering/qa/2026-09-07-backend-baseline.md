# QA — 后端安全与工程正确性加固（2026-09-07）

> 关联 openspec change：`openspec/changes/harden-backend-baseline`
> 执行人：scaffold-bot ｜ 环境：Windows 11 / JDK 17.0.4.1 / Maven 3.9.11（mvn.cmd）

## 守门命令与结果

### 1. 单元测试（零依赖，不起 Spring 上下文）

```powershell
$env:JAVA_HOME='D:\jdk-17_windows-x64_bin\jdk-17.0.4.1'
cd D:\gkproject\mm-security-platform\backend-scaffold
& 'D:\apache-maven-3.9.11\apache-maven-3.9.11\bin\mvn.cmd' -B test -s ci-settings.xml
```

结果：**BUILD SUCCESS**，18 case 全绿。

| 测试类                       | case | 覆盖要点                                       |
| ---------------------------- | ---- | ---------------------------------------------- |
| ResultEnvelopeTest           | 4    | B3 包络 code/message/traceId、空上下文兜底       |
| AuthControllerTest           | 2    | 登录成功返 token / 口令错返业务码 401            |
| DeviceControllerTest         | 2    | 分页 DTO 字段正确 / 空库返回空列表               |
| HardControlInterceptorTest   | 3    | 硬控 POST 拒绝 / GET 放行 / 普通路径写放行       |
| JwtUtilTest                  | 4    | 签发解析 / 过期 / 篡改 / 密钥不符               |
| DeviceServiceTest            | 3    | 分页委托 Mapper / code 非法 / 设备不存在        |

### 2. 跨库契约比对

```bash
node scripts/check-api-contract.mjs
```

结果：实现 11 / 契约 18 / 已对齐 4 / 差异 20（exit 0，仅报告）。
本次改动（移除 devices 的 mock 回落、新增 DTO）**未引入任何新端点漂移**。
20 项差异为加固前已存在的技术债，归入 Change 之外单独跟踪（见任务：消化跨库契约技术债）。

### 3. 编译

`mvn test` 已包含 `test-compile`，主代码与测试代码均编译通过，无警告阻断。

## 验证结论

- [x] P0：生产 `application-prod.yml` 强制 `signature.enabled=true`、JWT 去默认密钥、关闭 H2 Console 与 SQL 日志。
- [x] P0：CORS 由通配 `*` 收敛为 `app.cors.allowed-origins` 白名单。
- [x] P1：HmacFilter / JwtFilter 改 `FilterRegistrationBean` 显式顺序（HIGHEST_PRECEDENCE / +10）。
- [x] P1：分页方言不再写死 PostgreSQL（按 JDBC 元数据自动判定）。
- [x] P1：DeviceController 出参 DTO 化，移除 `devFallbackList` mock 回落，data.sql 补 8 条种子设备。
- [x] 测试基线 18 case 全绿。

## 遗留（不在本 Change 范围）

- AlarmController / AlarmService 仍有 `devFallbackList` mock 回落 —— 属告警域契约债，待契约补齐时一并处理。
- 401/403 当前一律 HTTP 200 + 业务 code，与前端契约声明的 `Unauthorized`/`Forbidden` 响应不一致 —— 留待契约对齐确认。
- 25 项跨库契约差异（含 device 域契约缺失）待任务「消化跨库契约技术债」处理。
