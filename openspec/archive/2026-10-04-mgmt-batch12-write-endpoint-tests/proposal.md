# Proposal: 第 1/2 批写端点单测补齐与自然键更新语义修复

## 背景
第 3 批（hazard / hazard.point / special-operation）已闭环，并为写端点补了 19 例单测。回溯发现第 1/2 批（video.camera / communication.device / device / communication.record）共 12 个写端点**零单测覆盖**，且其中一个测试类 `CommRecordServiceTest` 根本不存在——写端点上线即未受测试保护。

补测过程中进一步发现：`apply*Fields` 私有填充方法存在「覆写定位键」的一致性问题——
`CommDeviceService` 会重写 `deviceCode`、`CommRecordService` 会重写 `recordNo`，而两者的 PUT 端点
正是以该字段作为路径定位键。这与第 3 批在 `HazardService` 监测点位上修掉的物理主键被覆写属于同一类缺陷。

## 目标
- 为四域 12 个写方法补齐单测，覆盖三类主线：主键分配（自增 Long 走 `LedgerIdSupport` 的 max+1；客户端给定主键走判重 409；业务自然键不作为物理主键）、删除口径（软删 vs 物理删须与读端点过滤一致）、缺失路径（未命中返回 null / ok=false 且不发删除）。
- 修复剩余两处「PUT 按自然键定位却被请求体改写该自然键」的问题（`CommDeviceService` 的 deviceCode、`CommRecordService` 的 recordNo），与第 3 批修掉的 `HazardService` 监测点位 id 统一同一个语义。

## 非目标
- 不改 HTTP 路径、不改请求/响应 schema（`check-api-contract` 差异维持基线 12）。
- 不动既有读端点逻辑与返回结构。
- 不补西侧前端 spec（另见后续 change）。
