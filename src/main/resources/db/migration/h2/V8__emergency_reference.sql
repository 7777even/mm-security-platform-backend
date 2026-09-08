-- V8 应急参考配置真实数据源：将 strength / phones / knowledge / duty 从 Java 硬编码常量迁移至 DB 参考表。
-- 语义不变（仍为应急资源固定参考配置），但改为可维护的数据源，便于运营更新而无需改代码。
CREATE TABLE sys_emergency_strength (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kind VARCHAR(32) NOT NULL,
    count INT NOT NULL DEFAULT 0,
    icon VARCHAR(64)
);
CREATE TABLE sys_emergency_phone (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    number VARCHAR(32) NOT NULL,
    category VARCHAR(32)
);
CREATE TABLE sys_knowledge_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    count INT NOT NULL DEFAULT 0,
    icon VARCHAR(64)
);
CREATE TABLE sys_duty_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(32),
    role VARCHAR(32),
    department VARCHAR(64),
    shift VARCHAR(16)
);

INSERT INTO sys_emergency_strength (kind, count, icon) VALUES
 ('应急专家', 47, 'UserFilled'),
 ('应急物资', 3510, 'Box'),
 ('救援队伍', 12, 'Soldier'),
 ('装备车辆', 28, 'Van'),
 ('应急场所', 6, 'LocationFilled'),
 ('医疗机构', 3, 'FirstAidKit'),
 ('应急车辆', 18, 'Truck'),
 ('消防设施', 42, 'Fire');

INSERT INTO sys_emergency_phone (name, number, category) VALUES
 ('消防报警', '119', '消防'),
 ('医疗急救', '120', '医疗'),
 ('公安报警', '110', '公安'),
 ('厂内应急', '0668-2222111', '厂内应急'),
 ('保卫值班', '0668-2222333', '保卫值班');

INSERT INTO sys_knowledge_item (title, count, icon) VALUES
 ('岗位应急处置卡', 158, 'Document'),
 ('火灾爆炸应急预案', 42, 'Files'),
 ('气体泄漏处置', 67, 'Warning');

INSERT INTO sys_duty_member (name, phone, role, department, shift) VALUES
 ('杨恒明', '13792536966', '值班领导', '全部', '白班'),
 ('李伟', '13800138000', '值班员', '全部', '白班');
