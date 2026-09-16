# Design: 应急演练域（drills）

## 数据模型
- `fac_drill`（只读）：`id / drill_code / name / drill_type / form / time_range / place / status / departments`。
- `fac_drill_task`（只读，隶属）：`id / drill_id / name / status`。
- 列表行附 `taskCount`（按 `drill_id` 分组计数）；详情返回 `tasks[]` 子项。

## 接口
- `GET /api/v1/drills` → `Result<DrillList>`（`items` + `total`）。
- `GET /api/v1/drills/{id}` → `Result<DrillDetail>`（含 `tasks`）；未命中抛 `BusinessException(NOT_FOUND)`。
- 纯只读，类级 `@RequireAuth`（登录可见）。

## 分层
`DrillController` → `DrillService`（普通 `@Service` + `LambdaQueryWrapper`）→ `BaseMapper`。
DTO `DrillItem` / `DrillDetail` / `DrillTaskItem` / `DrillList` 与契约 `components.schemas` 同名，逐字段对拍。
