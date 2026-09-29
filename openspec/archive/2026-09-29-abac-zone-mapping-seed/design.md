# Design: ABAC 防区映射配置注入与零下行闭环验证

## ADR-1：dev 映射对齐 V34 防区种子
示例键（location）取监测/设备实体常用 area 标识，值取与 `sys_zone.zone_name` 对齐的防区名（炼油区/罐区/仓储区/码头区/乙烯区/芳烃区/特勤保障区）。仅为 dev 演示，prod 由运维按真实规则覆盖。

## ADR-2：闭环测试用 mock 解析器
`RealtimeBroadcastServiceTest` 以 mock `DataScopeResolver`（返回会话防区集合或 null=ALL）+ mock `LoginUser` 构造受限/全权限会话，直接断言 `broadcast` 三态结果：
- 会话防区={炼油区}：收炼油区事件、收 zones=null 事件、不收罐区事件。
- 事件 zones=null：fail-open 全推。
- 会话防区={罐区} 且事件 zones={炼油区}：无交集排除。
