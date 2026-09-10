-- =============================================================================
-- V34 data_scope 行级 ABAC 地基（H2 方言）
--   1) sys_zone 防区主数据（zone_name 对齐 BRIGADE_AREA 字典 / fac_brigade_team.area）
--   2) sys_user 增加 zone_codes（可访问防区，逗号串；空=未分派）
--   设计：openspec/changes/2026-09-10-rbac-data-scope-abac/design.md
--   说明：sys_role.data_scope（ALL/DEPT/SELF）列自 V32 已存在，本期仅补
--         「防区主数据 + 用户↔防区绑定」两块地基，供救援队伍域试点行级过滤。
--   H2 保留字规避：不新增裸 type/value/order 列名（用 sort_order、zone_code）。
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_zone (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    zone_code   VARCHAR(64)  NOT NULL,
    zone_name   VARCHAR(64)  NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_zone_code ON sys_zone(zone_code);

ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS zone_codes VARCHAR(512);

-- 防区种子：zone_name 严格对齐 BRIGADE_AREA 字典项与 fac_brigade_team.area 实际取值，
-- 保证 qw.in(FacBrigadeTeam::getArea, zones) 可直接命中救援队伍数据。
INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES
  ('LIANYOU',   '炼油区',    1, 1),
  ('YIXI',      '乙烯区',    2, 1),
  ('GUANQU',    '罐区',      3, 1),
  ('CANGCUN',   '仓储区',    4, 1),
  ('MATOU',     '码头区',    5, 1),
  ('FANGTING',  '芳烃区',    6, 1),
  ('TEQIN',     '特勤保障区', 7, 1);
