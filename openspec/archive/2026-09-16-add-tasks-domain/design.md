# Design: 处置任务域（tasks）

## 数据模型
- 单表 `fac_dispatch_task`（只读）：`id / task_code / title / task_level / source / area / deadline / status / description`。
- **列名用 `task_level` 而非 `level`**：达梦/Oracle 中 `LEVEL` 为保留字（同 H2 `value`/`command`/`type` 旧坑范式）；
  Java 字段仍为 `level`，由 `@TableField("task_level")` 映射，对外 JSON 字段名保持 `level`。

## 接口
- `GET /api/v1/tasks` → `Result<TaskList>`（`items` + `total`）。
- `GET /api/v1/tasks/{id}` → `Result<TaskItem>`；未命中抛 `BusinessException(ResultCode.NOT_FOUND)`。
- 纯只读，类级 `@RequireAuth`（登录可见）；**无写端点**，不触达下行红线 `HardControlPaths`。

## 分层
`TaskController` → `TaskService`（普通 `@Service` + `LambdaQueryWrapper`，非 `IService`）→ `FacDispatchTaskMapper extends BaseMapper`。
DTO `TaskItem` / `TaskList` 与契约 `components.schemas` 同名（守门按同名类逐字段对拍）。
