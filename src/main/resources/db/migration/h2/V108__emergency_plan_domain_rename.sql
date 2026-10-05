-- =============================================================================
-- V108 把 fac_emergency_plan / fac_emergency_plan_invoke_log 的 domain 列改名为 domain_code
--      （H2 方言）
--
-- 原因：`domain` 是达梦 DM8 的保留字。达梦执行 `ALTER TABLE ... ADD domain ...` 会报
--       -2007「[domain]附近出现错误: 语法分析出错」，且**双引号也绕不过**（DM8 与 Oracle
--       不同，`ADD "domain"` 同样失败），唯一出路是改列名。
--       仓库规约：`domain` -> `domain_code` + 实体用 `@TableField("domain_code")` 映射
--       （先例见 MgmtLedgerRow / MgmtLedgerMeta）。为让三方言列名一致，H2 在此改名。
--
-- 为什么不直接改 V85：H2 的 dev 文件库（data/mm_security_dev.mv.db）已经应用过 V85，
--       修改已应用的迁移会导致 Flyway checksum 校验失败。故 V85 原样保留，用本迁移改名。
--       新库执行顺序：V85 建 domain -> V108 改名 domain_code，最终列名一致；
--       老库（已过 V85）执行 V108 后同样为 domain_code。
--
-- 达梦 / PostgreSQL 的 V85 已直接写入 domain_code，无需此迁移。
-- =============================================================================

ALTER TABLE fac_emergency_plan ALTER COLUMN domain RENAME TO domain_code;
ALTER TABLE fac_emergency_plan_invoke_log ALTER COLUMN domain RENAME TO domain_code;
