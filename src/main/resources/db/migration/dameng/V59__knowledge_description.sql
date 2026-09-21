-- 应急生产安全知识分类说明：为 sys_knowledge_item 增加 description 列并回填种子文案。
-- 大屏「应急生产安全知识」面板点击知识卡时展示真实可编辑说明（替代前端写死文案）。
-- 达梦(Oracle 兼容)：ADD 后不写 COLUMN 关键字、类型用 VARCHAR2、不写 NULL。
ALTER TABLE sys_knowledge_item ADD description VARCHAR2(512 CHAR);

UPDATE sys_knowledge_item SET description = '岗位员工应掌握的应急处置卡片要点，覆盖启停、退守、隔离、上报等标准动作。' WHERE title = '岗位应急处置卡';
UPDATE sys_knowledge_item SET description = '火灾爆炸专项应急预案要点，涵盖火警确认、工艺切断、人员疏散与初期扑救。' WHERE title = '火灾爆炸应急预案';
UPDATE sys_knowledge_item SET description = '可燃/有毒气体泄漏处置要点，涵盖检测定位、警戒隔离、禁火断电与人员防护。' WHERE title = '气体泄漏处置';
