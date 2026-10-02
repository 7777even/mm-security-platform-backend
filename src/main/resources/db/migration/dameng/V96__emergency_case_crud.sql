-- =============================================================================
-- V96 事故案例库表 + 写权限码种子（达梦 DM8 方言）
--   1) 新建可编辑台账表 fac_emergency_case（区别于 fac_alarm 自动归档的只读结案聚合）
--   2) 登记按钮级权限码 emergency:case:write 到 fm-emergency 下
--   3) 授权 ADMIN / 指挥 / 调度岗
--   4) 预置 2 条示例案例（explicit id，避免自增序列滞后）
-- 说明：三方言表结构一致（BIGINT/VARCHAR/TIMESTAMP 均通用），仅注释头方言名不同。
-- =============================================================================

CREATE TABLE fac_emergency_case (
    id           BIGINT       PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    accident_type VARCHAR(100),
    location      VARCHAR(200),
    occurred_at   TIMESTAMP,
    summary       VARCHAR(2000),
    lessons       VARCHAR(2000),
    create_time   TIMESTAMP
);

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '案例库维护', 'fm-emergency-case-write', NULL, NULL, 137, 1, 0, 'BUTTON', 'emergency:case:write', 0
FROM sys_menu p WHERE p.code = 'fm-emergency';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER')
  AND m.code = 'fm-emergency-case-write';

INSERT INTO fac_emergency_case (id, title, accident_type, location, occurred_at, summary, lessons, create_time)
VALUES (1, 'T-301 罐区泄漏处置复盘', '泄漏', '储运部 T-301 罐区', '2026-08-21 09:03:00',
        '初起泄漏点位于进料线阀门法兰，巡检及时发现并启动围堵，未扩大。',
        '法兰螺栓定期紧固 + 巡检路线覆盖进料线是关键。', '2026-08-22 10:00:00');

INSERT INTO fac_emergency_case (id, title, accident_type, location, occurred_at, summary, lessons, create_time)
VALUES (2, '泵房电气火灾应急案例', '火灾', '动力车间 3# 泵房', '2026-09-05 14:20:00',
        '电机过载引燃周边线缆，断电后干粉灭火成功。',
        '过载保护定值复核 + 电缆桥架防火封堵需加强。', '2026-09-06 09:30:00');
