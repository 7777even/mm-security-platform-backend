# Design: 消防设施报警处置端点

## 1. 报警 id 与故障的反查规则（核心）

报警 id 由 `toAlarm` 派生：`id = "AL-" + faultCode.replace("FLT-","").replace("-","")`。
反向定位不能靠字符串拼回去（故障编号可能是 `FLT-0001` 也可能是 `F-20260317-001`，连字符位置不可逆）。

采用**与派生算法互逆的 SQL 表达式匹配**，对任意编号格式都成立：

```java
String digits = alarmId.replaceAll("[^0-9]", "");   // 仅留数字，杜绝注入
faultMapper.selectOne(new LambdaQueryWrapper<FacFireFacilityFault>()
        .apply("REPLACE(REPLACE(fault_code,'FLT-',''),'-','') = {0}", digits));
```

`digits` 为空（如 `AL-abc`）直接 B3 `PARAM_INVALID`；查不到故障 → B3 `NOT_FOUND`。

## 2. 复用而非复制：抽 `doUpdateFault`

既有 `updateFault(String faultId, req)` 已覆盖状态校验、字段局部更新、时间线追加、返回带时间线的条目，
且带 `@RealtimeSync(domain="fire-facility.fault")`。

⚠️ 关键约束：`RealtimeSyncAspect` 是 **Spring 代理型 `@Aspect`**，同类内 `this.updateFault(...)`
属自调用，**不会**走代理、广播不触发。若 `updateAlarm` 直接调 `updateFault`，前端收不到刷新通知。

因此把写逻辑抽成 `private doUpdateFault(FacFireFacilityFault e, req)`：

- `updateFault(faultId, req)` —— `@RealtimeSync`，按主键定位后调 `doUpdateFault`（既有故障端点不变）
- `updateAlarm(alarmId, req)` —— `@RealtimeSync`，按报警 id 反查后调 `doUpdateFault`

两者都是经代理调用的公开入口，各自广播一次；核心逻辑零复制。

## 3. 权限与包络

- `@RequireAuth(perm = "fire-facility:handle")`：复用 V68 已登记并授权的按钮级权限码，满足
  `check-endpoint-authz.mjs` 对写端点必须带 `perm=`/`role=` 的硬门禁。
- 校验失败沿用 B3 包络（HTTP 200 + `code!=0`），单测断言 `ResultCode.PARAM_INVALID` / `NOT_FOUND`。

## 4. 契约

前端契约唯一真源 `docs/api/fire-facility.openapi.json` 新增 `/fire-facility/alarms/{alarmId}` 的 `put`，
请求/响应**复用既有 schema** `FireFacilityFaultUpdateRequest` / `FireFacilityFaultItem`（不新增 schema，
守门按同名类逐字段对拍，零漂移）。
