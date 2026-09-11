-- =============================================================================
-- V42 地图域「装置区信息牌」数据源（h2方言）—— 大屏「前端数据全后端化」P1
--   目的：把 MaomingPetroCesiumMap 的 MAP_THEME.plantZonePopups(4)/plantZoneTealTags(4)
--         迁到后端，由 GET /api/v1/map/zone-signs 提供。渲染主题（颜色/高度/景深）为
--         by-design 保留前端；地图标注（厂区 label 经纬度）为几何，同样保留前端。
--   口径：与前端原硬编码逐位对齐。
--   坑位：status/value 为 H2 保留字风险列名 → status_text / stat_value。
-- =============================================================================

CREATE TABLE fac_map_zone_sign (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sign_kind VARCHAR(8) NOT NULL,
    title VARCHAR(32) NOT NULL,
    location VARCHAR(32) NULL,
    status_text VARCHAR(32) NOT NULL,
    status_level VARCHAR(8) NULL,
    stat_value VARCHAR(8) NULL,
    sort_no INT NOT NULL DEFAULT 0
);

INSERT INTO fac_map_zone_sign (sign_kind, title, location, status_text, status_level, stat_value, sort_no) VALUES
('ALERT', '反应器', '储罐区B-3', '异常', 'alert', NULL, 1),
('ALERT', '反应器', '储罐区B-5', '异常', 'alert', NULL, 2),
('ALERT', '反应器', '储罐区B-7', '异常', 'alert', NULL, 3),
('ALERT', '反应器', '储罐区B-9', '异常', 'alert', NULL, 4),
('TEAL', '储罐区', NULL, '液位正常', NULL, '85%', 1),
('TEAL', '装置区', NULL, '液位正常', NULL, '82%', 2),
('TEAL', '精制区', NULL, '液位正常', NULL, '78%', 3),
('TEAL', '公用区', NULL, '液位正常', NULL, '91%', 4);
