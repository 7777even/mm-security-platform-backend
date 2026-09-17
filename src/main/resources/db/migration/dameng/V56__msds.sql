-- V56 化学品 MSDS 域（移动端化学品知识/详情）真后端化（达梦方言）
-- 新增只读表 fac_msds，替代 apps/mobile/data/mock.ts 的 msds 静态数据。
-- 契约：docs/api/msds.openapi.json（GET /msds、GET /msds/{cas}）。

CREATE TABLE fac_msds (
    id NUMBER(19) IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR2(128 CHAR) NOT NULL,
    cas VARCHAR2(32 CHAR) NOT NULL,
    classification VARCHAR2(64 CHAR),
    state VARCHAR2(32 CHAR),
    boiling_point VARCHAR2(32 CHAR),
    flash_point VARCHAR2(32 CHAR),
    explosion_limit VARCHAR2(64 CHAR),
    storage VARCHAR2(256 CHAR),
    safety VARCHAR2(256 CHAR),
    emergency VARCHAR2(256 CHAR)
);
SET IDENTITY_INSERT fac_msds ON;


INSERT INTO fac_msds (id, name, cas, classification, state, boiling_point, flash_point, explosion_limit, storage, safety, emergency) VALUES (1, '乙烯（Ethylene）', '74-85-1', '易燃气体 类别1', '气体（液化）', '-103.7℃', '—', '2.7%-36%', '阴凉通风，远离火源热源', '禁火区域作业、接地防静电', '切断泄漏源，喷雾稀释，下风向疏散');
INSERT INTO fac_msds (id, name, cas, classification, state, boiling_point, flash_point, explosion_limit, storage, safety, emergency) VALUES (2, '液化石油气（LPG）', '68476-85-7', '易燃气体 类别1', '气体（液化）', '-42.1℃', '—', '1.5%-9.5%', '压力容器储存，远离明火', '定期检漏，作业区通风', '切断气源，禁止开关电器');
INSERT INTO fac_msds (id, name, cas, classification, state, boiling_point, flash_point, explosion_limit, storage, safety, emergency) VALUES (3, '苯（Benzene）', '71-43-2', '致癌类别1A/易燃液体', '液体', '80.1℃', '-11℃', '1.2%-8%', '密封储存，阴凉通风', '防静电接地，佩戴防护', '撤离污染区，泡沫灭火');

SET IDENTITY_INSERT fac_msds OFF;
