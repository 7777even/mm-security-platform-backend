-- =============================================================================
-- V40 视频域「常驻视频监控」分组/通道数据源（postgresql方言）—— 大屏「前端数据全后端化」P0
--   目的：把 ImportantVideoPanel 的 高空AR(3 组×4) + 重点关注区域(3 组×4) 迁到后端，
--         由 GET /api/v1/video/important-groups 提供。图像静态资源（mock-cameras/*.png）
--         保留前端按 image_key 映射（图资非业务数据）。
--   口径：与前端原硬编码逐位对齐（本文件由脚本生成，勿手改数值）。
-- =============================================================================

CREATE TABLE fac_video_important_group (
    id BIGSERIAL PRIMARY KEY,
    group_code VARCHAR(32) NOT NULL,
    group_label VARCHAR(32) NOT NULL,
    group_type VARCHAR(16) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_video_important_feed (
    id BIGSERIAL PRIMARY KEY,
    feed_id VARCHAR(32) NOT NULL,
    group_code VARCHAR(32) NOT NULL,
    feed_label VARCHAR(64) NOT NULL,
    image_key VARCHAR(16) NOT NULL,
    position VARCHAR(16) NULL,
    online BOOLEAN NOT NULL DEFAULT 1,
    sort_no INT NOT NULL DEFAULT 0
);

INSERT INTO fac_video_important_group (group_code, group_label, group_type, sort_no) VALUES
('park', '园区全景组', 'highAr', 1),
('refinery', '炼油区高点组', 'highAr', 2),
('chemical', '化工区高点组', 'highAr', 3),
('tank', '储罐区组', 'focus', 4),
('device', '装置区组', 'focus', 5),
('boundary', '厂界出入口组', 'focus', 6);

INSERT INTO fac_video_important_feed (feed_id, group_code, feed_label, image_key, position, online, sort_no) VALUES
('ar-park-1', 'park', '园区北向全景', 'highAr', '50% 30%', 1, 1),
('ar-park-2', 'park', '炼油区全景', 'highAr', '32% 50%', 1, 2),
('ar-park-3', 'park', '化工区全景', 'highAr', '68% 48%', 1, 3),
('ar-park-4', 'park', '港区全景', 'highAr', '50% 72%', 1, 4),
('ar-refinery-1', 'refinery', '一号高点西向', 'highAr', '50% 30%', 1, 1),
('ar-refinery-2', 'refinery', '一号高点东向', 'highAr', '32% 50%', 1, 2),
('ar-refinery-3', 'refinery', '二号高点南向', 'highAr', '68% 48%', 1, 3),
('ar-refinery-4', 'refinery', '二号高点北向', 'highAr', '50% 72%', 1, 4),
('ar-chemical-1', 'chemical', '乙烯装置全景', 'highAr', '50% 30%', 1, 1),
('ar-chemical-2', 'chemical', '芳烃装置全景', 'highAr', '32% 50%', 1, 2),
('ar-chemical-3', 'chemical', '管廊全景', 'highAr', '68% 48%', 1, 3),
('ar-chemical-4', 'chemical', '装卸区全景', 'highAr', '50% 72%', 1, 4),
('tank-1', 'tank', '储罐区B-3东侧', 'tanks', NULL, 1, 1),
('tank-2', 'tank', '液化烃罐区南侧', 'tanks', '60% 54%', 1, 2),
('tank-3', 'tank', '罐区管廊入口', 'pipes', NULL, 1, 3),
('tank-4', 'tank', '罐区装卸平台', 'reactor', NULL, 1, 4),
('device-1', 'device', '催化裂化装置', 'reactor', NULL, 1, 1),
('device-2', 'device', '乙烯反应装置', 'pipes', NULL, 1, 2),
('device-3', 'device', '加氢装置入口', 'reactor', '60% 55%', 1, 3),
('device-4', 'device', '公共管廊区', 'pipes', '35% 50%', 1, 4),
('boundary-1', 'boundary', '厂区西门', 'highAr', '22% 66%', 1, 1),
('boundary-2', 'boundary', '厂区北门', 'highAr', '50% 38%', 1, 2),
('boundary-3', 'boundary', '东侧厂界', 'highAr', '78% 56%', 1, 3),
('boundary-4', 'boundary', '南侧物流门', 'highAr', '55% 82%', 0, 4);
