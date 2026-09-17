# Capability: Realtime Broadcast

## ADDED Requirements

### Requirement: 数据变更事件与通用广播
系统 SHALL 提供统一的「数据变更事件 → 多 topic WebSocket 广播」机制，使任意写操作可被三端实时感知。

#### Scenario: 事件发布
- **WHEN** 标注 `@RealtimeSync(domain)` 的写方法成功返回
- **THEN** 系统发布 `EntityChangedEvent(domain, action, id?, data?)`，并由 `RealtimePublisher` 经既有 `/ws/alarm` 连接向所有会话广播 `{topic:"<domain>.changed", payload:{domain, action, id?, data?}}`

#### Scenario: 端点复用
- **WHEN** 客户端已连接 `/ws/alarm`
- **THEN** 既可接收既有 `alarm.push`，也可接收任意 `<domain>.changed`，无需建立第二条连接

### Requirement: 写入口显式广播覆盖
系统 SHALL 通过 `@RealtimeSync(domain)` 注解在写方法上显式声明广播域，保证全部数据改动可被发现与审计。

#### Scenario: 覆盖审计
- **WHEN** 检视服务层写方法
- **THEN** 每个对外写方法均标注 `@RealtimeSync(domain)`，未标注者视为不广播且可被评审 / CI 识别

### Requirement: 零下行控制红线不变
实时广播 SHALL 仅下发「刷新通知」（`*.changed`），客户端据此重新拉取只读数据，不含任何硬控下行写指令。

#### Scenario: 只通知不控制
- **WHEN** 检视实时广播相关代码
- **THEN** 不存在向设备 / 系统下发消防泵 / 应急广播 / 逃生门禁等生命安全类硬控写端点的路径
