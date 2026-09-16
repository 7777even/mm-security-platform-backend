# Design: 化学品 MSDS 域（msds）

## 数据模型
- `fac_msds`（只读）：`id / name / cas / classification / state / boiling_point / flash_point / explosion_limit / storage / safety / emergency`。
- 对外详情主键口径为 **CAS 号**（非自增 id），与移动端 `/msds/:cas` 路由一致。

## 接口
- `GET /api/v1/msds` → `Result<MsdsList>`（`items` + `total`，列表项仅 4 字段）。
- `GET /api/v1/msds/{cas}` → `Result<MsdsDetail>`（11 字段）；未命中抛 `BusinessException(NOT_FOUND)`。
- 纯只读，类级 `@RequireAuth`（登录可见）。

## 分层
`MsdsController` → `MsdsService`（普通 `@Service` + `LambdaQueryWrapper`）→ `BaseMapper`。
`{cas}` 查询用 `selectList(...).stream().findFirst()`（避免 `selectOne` 在潜在重复 CAS 下抛 `TooManyResultsException`）。
DTO `MsdsItem` / `MsdsDetail` / `MsdsList` 与契约 `components.schemas` 同名，逐字段对拍。
