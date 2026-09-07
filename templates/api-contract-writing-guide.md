# API 契约编写与同步指南（后端视角）

> 适用：所有 `controller/`、`dto/`、对外接口与跨库契约同步。
> 契约**机器可读真源在前端库** `frontend-scaffold/docs/api/*.openapi.json`；本库不复制第二份，只做实现对齐与同步。
> 完整背景见根 `AGENTS.md` §3（契约规则）与 §11（跨库协作）、`docs/api/README.md`（实现映射）。

## 一、后端实现侧六条红线

1. **零下行控制**：不提供下行控制写接口。`HardControlInterceptor.HARD_CONTROL_PATHS` 名单内 POST/PUT/DELETE → `code=503`；新增名单外下行能力须人工评审；WS 通道同样不得下发控制指令。
2. **B3 统一包络**：业务响应统一 `common/Result<T>`（`code`/`message`/`data`/`traceId`），`code=0` 成功；非 0 由 `GlobalExceptionHandler` 转包络。禁止第二套响应外壳、禁止裸返 Entity。
3. **20 位 MDM 设备编码**：对外设备标识固定 20 位，用 `@DeviceCode` 校验（非法 → `301`）；禁止自增 id / uuid / 序号作对外主键。
4. **防重放签名**：生产 `signature.enabled=true`，`HmacFilter` 校验 `X-Timestamp`/`X-Nonce`/`X-Signature`；Dev 可挂起，生产禁止旁路。
5. **无状态令牌**：JWT 不落盘、不写 Cookie；`jwt.secret` 走环境变量；access 2h / refresh 7d。
6. **错误码分段**：`1xx` 通用 / `2xx` 鉴权 / `3xx` 设备 / `5xx` 硬控；新增码同步 `ResultCode` + 前端契约 + 调用方分支。

## 二、分层落点

- `controller/`：只做路由、参数绑定、`@Valid`、调 Service。不写业务逻辑、不自定义响应外壳、不注入 Mapper。
- `dto/`：出入参对象，字段带中文注释；**不复用 Entity 做出参**。
- `service/`：业务与事务；不感知 `HttpServletRequest`。
- `entity/` + `mapper/`：结构变更必须同步 `resources/db/*.sql`。

## 三、改对外接口时的四同步（不可跳步）

1. **OpenSpec**：建 `openspec/changes/<name>/`，proposal 写明触及的契约文件与端点。
2. **前端契约**：更新 `frontend-scaffold/docs/api/<domain>.openapi.json`，遵守四条铁律：
   - 按域 / Controller 分组（`tags`）；
   - 每个接口有 `summary` + `description`（含权限、规则、主要错误码）；
   - 每个 property / parameter 有中文 `description`；
   - 每个接口有 example（参数 example、有 body 时请求体 example、成功响应 `code/message/data` example）。
3. **后端实现**：Controller/DTO 与契约对齐，跑 `node scripts/check-api-contract.mjs` 确认无漂移。
4. **前端类型**：通知前端重跑 `npm run gen:api-types`；后端在 `engineering/qa/` 注明「已通知前端重生成类型」。

**禁止**：只改后端不改契约；只改契约不通知前端；用 Markdown 接口清单替代 OpenAPI；在本库复制契约文件。

## 四、提交前自检清单

- [ ] 端点返回统一 `Result<T>`，`code=0` 才含 `data`。
- [ ] 无下行控制写接口；硬控路径由拦截器兜底。
- [ ] 设备标识用 20 位 MDM 编码并挂 `@DeviceCode`。
- [ ] 生产不旁路 HMAC 签名；密钥走环境变量，未硬编码。
- [ ] 新增 / 变更错误码落在正确分段，并同步 `ResultCode`。
- [ ] Controller 不写业务、Service 不感知 HTTP、Entity 未直接做出参。
- [ ] DTO 字段有中文注释，未复用 Entity。
- [ ] 表 / 字段变更已同步 `schema.sql` 与 `resources/db/` 增量 SQL。
- [ ] 前端 `docs/api/<domain>.openapi.json` 已同交付更新，`check-api-contract.mjs` 无差异。
- [ ] 已跑 §2 验证矩阵对应行（compile / test / 冒烟），结果记入 `engineering/qa/`。
