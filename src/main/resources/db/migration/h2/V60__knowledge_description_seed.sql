-- 补全应急生产安全知识分类说明：V35 新增的 6 个分类在 V59 加列时未回填 description，此处补种子。
-- （V59 已应用于既有库，不可修改其校验和，故以 V60 追加回填。）
UPDATE sys_knowledge_item SET description = '危险化学品泄漏处置要点，涵盖介质确认、围堵收集、洗消与废弃物处置。' WHERE title = '危险化学品泄漏处置';
UPDATE sys_knowledge_item SET description = '中毒窒息人员急救要点，涵盖脱离环境、心肺复苏、供氧与转运衔接。' WHERE title = '人员中毒窒息急救';
UPDATE sys_knowledge_item SET description = '应急疏散与集合清点要点，涵盖疏散路线、引导职责、集合点与人数核对。' WHERE title = '应急疏散与集合清点';
UPDATE sys_knowledge_item SET description = '消防器材使用规范要点，涵盖灭火器 / 消火栓 / 水带的选用、操作与维护。' WHERE title = '消防器材使用规范';
UPDATE sys_knowledge_item SET description = '环境应急监测要点，涵盖大气 / 水体 / 土壤采样、快速检测与结果上报。' WHERE title = '环境保护应急监测';
UPDATE sys_knowledge_item SET description = '事故上报与信息发布要点，涵盖上报时限、口径统一与对外发言流程。' WHERE title = '事故上报与信息发布';
