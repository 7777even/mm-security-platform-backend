-- =============================================================================
-- V34 data_scope 行级 ABAC 地基 —— PostgreSQL 方言（生产 profile）
--   与 H2 V34 对齐；三方言保持列定义一致（仅类型映射不同）：
--     BIGINT AUTO_INCREMENT -> BIGSERIAL，TINYINT -> SMALLINT。
--   说明：本文件未经 PG 实例实跑验证（本地无 PG 实例），按标准 PG 语法编写，需上环境复核。
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_zone (
    id          BIGSERIAL PRIMARY KEY,
    zone_code   VARCHAR(64)  NOT NULL,
    zone_name   VARCHAR(64)  NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_zone_code ON sys_zone(zone_code);

ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS zone_codes VARCHAR(512);

INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES
  ('LIANYOU', '炼油区', 1, 1),
  ('YIXI', '乙烯区', 2, 1),
  ('GUANQU', '罐区', 3, 1),
  ('CANGCUN', '仓储区', 4, 1),
  ('MATOU', '码头区', 5, 1),
  ('FANGTING', '芳烃区', 6, 1),
  ('TEQIN', '特勤保障区', 7, 1);
