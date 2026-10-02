# Design: 应急通讯录台账写端点

## 1. 为什么写请求用 DTO 字段名而不是实体字段名

实体 `SysEmergencyPhone` 是 `name / number / category`，只读 DTO `EmergencyPhone`
同样是这套字段（id 为 String 仅展示层差异）。两者字段名天然对齐，写请求 `PhoneWriteRequest`
直接复用同名 name/number/category，前端可把列表行直接灌进表单、原样回传，
视图层不必做任何字段映射——与救援资源四台账、知识库同一套路。

代价是 service 内要做一次实体↔DTO 映射（`row.setName(req.getName().trim())`），只写一次。

## 2. 为什么必填校验放 service 而不是 `@NotBlank`

- 与既有 `BusinessWriteService`（四域写端点）保持同风格，降低认知成本；
- create 与 update **共用一个 DTO**，Bean Validation 无法表达「新增必填、编辑可不传」；
- 非法值直接抛 `PARAM_INVALID`，不做静默回落——静默回落会让调用方误以为已生效。

## 3. 新增主键为什么取 max+1

`sys_emergency_phone` 在 V8 种子里**显式指定了 id**（如 `INSERT ... (id, ...) VALUES (1, ...)`），
而 H2 / PostgreSQL / 达梦的自增序列只在「不指定 id」时推进，显式插入**不会**同步序列。
于是后续新增行仍从 id=1 起跳、撞主键 → `DataIntegrityViolationException` → 前端看到 409「数据冲突」。

统一走 `LedgerIdSupport.nextId(mapper, idRef, getter)`（max(id)+1，空表从 1 起），
三方言行为一致、零迁移、对既有多态库都生效。

## 4. 为什么写后 invalidateAll 而非仅写库

`phones()` 方法带 Caffeine 读穿缓存（TTL 5min），大屏高频轮询入口每次刷新都走缓存。
若不 `invalidateAll()`，写后最多 5min 才在大屏生效，与「实时广播」语义冲突。
写侧在 insert/update/delete 后统一 `phoneCache.invalidateAll()`，配合 `@RealtimeSync`
广播 `emergency.phone` 域，三端（管理端/大屏/移动端）秒级一致。

## 5. 删除为什么是物理删除

`sys_emergency_phone` 无 `deleted` 列，无法软删。删除为物理删除；不存在（含重复删除）
返回 B3 `NOT_FOUND`，不静默成功。
