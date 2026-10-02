# Design: 业务写侧四域补齐修改与删除

## 决策 1：复用既有写权限码，不新增按钮级权限种子

四域的 `*:write` 权限码已覆盖"写"这一动作粒度（创建/修改/删除同属写），
再拆 `*:delete` 需要额外的 V92 迁移且授权矩阵要同步扩张，收益低于成本。
故 PUT/DELETE 沿用 `emergency:command:write` / `emergency:duty:write` /
`typhoon:dispatch:write` / `fire-alarm:patrol:write`。

## 决策 2：物理删除而非逻辑删除

四张表（`fac_emergency_command_record` / `fac_duty_sign_in` /
`fac_typhoon_dispatch_order` / `fac_patrol_execution`）都带 `deleted` 列，
但**既有列表查询未过滤 `deleted`**，若改为逻辑删除会出现"删除后列表仍在"的假象；
补过滤又有历史行 `deleted IS NULL` 的漏数据风险。物理删除语义明确、与消防故障模块一致。

## 决策 3：修改走局部更新而非全量替换

移动端与大屏侧存在"只传少量字段推进状态"的既有调用，全量替换会把未传字段清空。
故统一 `null`/空表示"不更新"，并保留服务端的枚举强校验。

## 决策 4：统一放在 BusinessWriteService

四域原本就由该类承载（create + list），继续放此处可复用 `assertWritable`、
审计助手与枚举常量，避免逻辑分散到三个 Service。

## 校验矩阵

| 场景 | 结果 |
| --- | --- |
| id 为空 | B3 `PARAM_INVALID` |
| 记录不存在 | B3 `NOT_FOUND` |
| 请求体为空 | B3 `PARAM_INVALID` |
| 枚举值非法（dispatchAction / execResult / signAction） | B3 `PARAM_INVALID` |
