-- V14 视频控制/视频墙大屏真实数据源（fm-video-control / fm-video-wall）。
-- 数据来源：src/screen/lib/data/videoControlMock.ts、videoLinkageMock.ts 存量硬编码
-- （分类与分组树、摄像头分页网格、视频联动配置与规则），迁移后数值与文案语义保持不变。
-- 命名回避数据库保留字（value/left/top/type/status/level/count/command）：状态列名 status_name；
-- 分组树 id 沿用前端字符串编码（node_code / parent_code），联动配置沿用 config_code（lk-001...）。
-- 网格布局（1x1/2x2/3x3）与画面轮巡为前端交互状态，不在此建表。

CREATE TABLE fac_video_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    node_code VARCHAR(32) NOT NULL,
    label VARCHAR(64) NOT NULL,
    parent_code VARCHAR(32),
    group_kind VARCHAR(16) NOT NULL,
    icon_index INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_video_camera (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    camera_type VARCHAR(32) NOT NULL,
    location VARCHAR(128),
    status_name VARCHAR(16) NOT NULL DEFAULT 'live',
    hd BOOLEAN NOT NULL DEFAULT TRUE,
    thumb_index INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_video_linkage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_code VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    category VARCHAR(32) NOT NULL,
    linkage_count INT NOT NULL DEFAULT 0,
    business_objects VARCHAR(256),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_video_linkage_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_code VARCHAR(32) NOT NULL,
    preset_point VARCHAR(64) NOT NULL,
    object_category VARCHAR(32) NOT NULL,
    object_name VARCHAR(64) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

-- 分类（group_kind=CATEGORY，8 个，沿用 videoControlMock.videoControlCategories）
INSERT INTO fac_video_group (node_code, label, parent_code, group_kind, icon_index, sort_no) VALUES
  ('refining', '炼油区', NULL, 'CATEGORY', 0, 1),
  ('tank', '原油罐区', NULL, 'CATEGORY', 1, 2),
  ('hazard', '重大危险源', NULL, 'CATEGORY', 2, 3),
  ('production', '生产区', NULL, 'CATEGORY', 3, 4),
  ('gate', '出入口', NULL, 'CATEGORY', 4, 5),
  ('warehouse', '仓库', NULL, 'CATEGORY', 5, 6),
  ('pump', '泵房', NULL, 'CATEGORY', 6, 7),
  ('other', '其它区域', NULL, 'CATEGORY', 7, 8);

-- 分组树（group_kind=TREE，沿用 videoControlMock.videoControlTree）
INSERT INTO fac_video_group (node_code, label, parent_code, group_kind, icon_index, sort_no) VALUES
  ('drill', '应急演练', NULL, 'TREE', 0, 1),
  ('drill-1', '演练1', 'drill', 'TREE', 0, 1),
  ('drill-2', '演练2', 'drill', 'TREE', 0, 2),
  ('event', '应急事件', NULL, 'TREE', 0, 2),
  ('event-1', '事件1', 'event', 'TREE', 0, 1),
  ('event-2', '事件2', 'event', 'TREE', 0, 2),
  ('patrol', '日常巡检', NULL, 'TREE', 0, 3),
  ('patrol-1', '巡检区域-1', 'patrol', 'TREE', 0, 1),
  ('patrol-2', '巡检区域-2', 'patrol', 'TREE', 0, 2),
  ('key', '重点监控', NULL, 'TREE', 0, 4),
  ('key-1', '炼油罐区-5#球机', 'key', 'TREE', 0, 1);

-- 摄像头台账（27 路，沿用 videoControlMock.buildPageCells 生成规则：名称/类型循环、
-- 第 1 页第 2 格 loading、第 5 格 ai、thumbIndex = 序号 % 6）
INSERT INTO fac_video_camera (name, camera_type, location, status_name, hd, thumb_index, sort_no) VALUES
  ('炼油区-1', '固定点机', '中海壳牌石油化工有限公司', 'live', TRUE, 0, 1),
  ('炼油区-2', '球机', '中海壳牌石油化工有限公司', 'loading', TRUE, 1, 2),
  ('炼油区-3', '枪机', '中海壳牌石油化工有限公司', 'live', TRUE, 2, 3),
  ('催化区-1', '云台', '中海壳牌石油化工有限公司', 'live', TRUE, 3, 4),
  ('催化区-2', '固定点机', '中海壳牌石油化工有限公司', 'ai', TRUE, 4, 5),
  ('罐区-1', '球机', '中海壳牌石油化工有限公司', 'live', TRUE, 5, 6),
  ('罐区-2', '枪机', '中海壳牌石油化工有限公司', 'live', TRUE, 0, 7),
  ('罐区-3', '云台', '中海壳牌石油化工有限公司', 'live', TRUE, 1, 8),
  ('码头-1', '固定点机', '中海壳牌石油化工有限公司', 'live', TRUE, 2, 9),
  ('码头-2', '球机', '中海壳牌石油化工有限公司', 'live', TRUE, 3, 10),
  ('泵房-1', '枪机', '中海壳牌石油化工有限公司', 'live', TRUE, 4, 11),
  ('泵房-2', '云台', '中海壳牌石油化工有限公司', 'live', TRUE, 5, 12),
  ('仓库-1', '固定点机', '中海壳牌石油化工有限公司', 'live', TRUE, 0, 13),
  ('仓库-2', '球机', '中海壳牌石油化工有限公司', 'live', TRUE, 1, 14),
  ('出入口-1', '枪机', '中海壳牌石油化工有限公司', 'live', TRUE, 2, 15),
  ('出入口-2', '云台', '中海壳牌石油化工有限公司', 'live', TRUE, 3, 16),
  ('生产区-1', '固定点机', '中海壳牌石油化工有限公司', 'live', TRUE, 4, 17),
  ('生产区-2', '球机', '中海壳牌石油化工有限公司', 'live', TRUE, 5, 18),
  ('厂界-1', '枪机', '中海壳牌石油化工有限公司', 'live', TRUE, 0, 19),
  ('厂界-2', '云台', '中海壳牌石油化工有限公司', 'live', TRUE, 1, 20),
  ('装卸区-1', '固定点机', '中海壳牌石油化工有限公司', 'live', TRUE, 2, 21),
  ('装卸区-2', '球机', '中海壳牌石油化工有限公司', 'live', TRUE, 3, 22),
  ('办公区-1', '枪机', '中海壳牌石油化工有限公司', 'live', TRUE, 4, 23),
  ('办公区-2', '云台', '中海壳牌石油化工有限公司', 'live', TRUE, 5, 24),
  ('配电房-1', '固定点机', '中海壳牌石油化工有限公司', 'live', TRUE, 0, 25),
  ('配电房-2', '球机', '中海壳牌石油化工有限公司', 'live', TRUE, 1, 26),
  ('消防站-1', '枪机', '中海壳牌石油化工有限公司', 'live', TRUE, 2, 27);

-- 视频联动配置（沿用 videoLinkageMock.videoLinkageConfigs）
INSERT INTO fac_video_linkage (config_code, name, code, category, linkage_count, business_objects, sort_no) VALUES
  ('lk-001', 'XX强3-2棚伯', 'HKJK-5124863', '枪机', 4, '石脑油罐区、催化裂化装置、催化氢解装置、储油罐区', 1),
  ('lk-002', 'XX强3-5棚伯', 'HKJK-5124864', '枪机', 1, '石脑油罐区', 2),
  ('lk-003', '1#厂区高空AR', 'HKJK-5124865', '高空AR', 2, 'A生产区、B生产区', 3),
  ('lk-004', '北2路33#枪机', 'HKJK-5124866', '枪机', 3, '北2路、储油罐区、炼化厂区门口', 4),
  ('lk-005', '储油罐区-2#球机', 'HKJK-5124867', '球机', 2, '储油罐区、消防水系统', 5);

-- 联动规则（沿用 videoLinkageMock.buildLinkageRules；未预置的配置由服务端回退默认规则）
INSERT INTO fac_video_linkage_rule (config_code, preset_point, object_category, object_name, sort_no) VALUES
  ('lk-001', '石脑油罐区-东南角', '重大危险源', '石脑油罐区', 1),
  ('lk-001', '催化裂化装置-北侧', '生产装置', '催化裂化装置', 2),
  ('lk-001', '催化氢解装置-西侧', '生产装置', '催化氢解装置', 3),
  ('lk-001', '储油罐区-西侧出入口', '储罐', '储油罐区', 4),
  ('lk-002', '石脑油罐区-东南角', '重大危险源', '石脑油罐区', 1),
  ('lk-003', '炼化厂区门口', '库区', 'A生产区', 1),
  ('lk-003', '北2路中段', '库区', 'B生产区', 2);
