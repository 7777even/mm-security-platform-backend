# Spec Delta: msds-domain（化学品 MSDS 域）

## ADDED Requirements

### Requirement: 化学品 MSDS 只读台账
系统 SHALL 提供危化品 MSDS 只读查询（列表 + 按 CAS 号详情），数据源为只读表 `fac_msds`。

#### Scenario: MSDS 列表
- **WHEN** 已登录用户请求 `GET /api/v1/msds`
- **THEN** 返回全部化学品（按 id 升序）与总数，每项含 `name`/`cas`/`classification`（`Result<MsdsList>`）

#### Scenario: MSDS 详情命中
- **WHEN** 请求 `GET /api/v1/msds/{cas}` 且 CAS 存在
- **THEN** 返回该化学品 MSDS 详情（`Result<MsdsDetail>`，含沸点/闪点/爆炸极限/储存/安全/应急处置）

#### Scenario: MSDS 详情未命中
- **WHEN** 请求 `GET /api/v1/msds/{cas}` 且 CAS 不存在
- **THEN** 返回业务码 404（HTTP 200 + B3 包络）

### Requirement: 详情按 CAS 查询
MSDS 详情 SHALL 以 CAS 号为主键口径（非自增 id），与移动端 `/msds/:cas` 路由一致。
