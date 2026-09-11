-- =============================================================================
-- V36 告警 / 巡检字典选项种子（H2 方言）
--   目的：把大屏「消防报警列表」筛选项与「防火巡查」班次/状态下拉，从
--         前端本地常量迁到后端字典（sys_dict_type/sys_dict_item），
--         由 GET /api/v1/system/dicts/{dictCode} 提供（登录可读、仅启用项、按 sort_order）。
--   口径：item_value/item_label 的取值与前端原常量逐一对齐——
--         类型/来源/对象类型/对象：筛选按显示串比较，故 item_value=item_label；
--         状态：筛选按 code 比较（ACTIVE/ACKED/...），item_label 为中文展示。
--   「全部X」哨兵由前端补，不入种子。
--   三方言同步：postgresql/（多行 VALUES）、dameng/（逐条 INSERT）保持同一字典内容。
-- =============================================================================

INSERT INTO sys_dict_type (dict_code, dict_name, description, status, built_in) VALUES
  ('fire_alarm_type',        '消防报警类型',     '消防报警列表-类型筛选',       1, 1),
  ('fire_alarm_source',      '消防报警来源',     '消防报警列表-来源筛选',       1, 1),
  ('fire_alarm_object_type', '消防报警对象类型', '消防报警列表-对象类型筛选',   1, 1),
  ('fire_alarm_object',      '消防报警对象',     '消防报警列表-对象筛选',       1, 1),
  ('fire_alarm_status',      '消防报警状态',     '消防报警列表-状态筛选',       1, 1),
  ('patrol_shift',           '防火巡查班次',     '防火巡查记录-班次筛选',       1, 1),
  ('patrol_status',          '防火巡查状态',     '防火巡查记录-状态筛选',       1, 1);

INSERT INTO sys_dict_item (dict_code, item_value, item_label, sort_order, status) VALUES
  ('fire_alarm_type', '火灾报警', '火灾报警', 1, 1),
  ('fire_alarm_type', '烟雾报警', '烟雾报警', 2, 1),
  ('fire_alarm_type', 'GDS报警',  'GDS报警',  3, 1),
  ('fire_alarm_type', '设备故障', '设备故障', 4, 1),
  ('fire_alarm_source', '火灾报警', '火灾报警', 1, 1),
  ('fire_alarm_source', 'DCS/GDS',  'DCS/GDS',  2, 1),
  ('fire_alarm_source', '视频识别', '视频识别', 3, 1),
  ('fire_alarm_source', '人工上报', '人工上报', 4, 1),
  ('fire_alarm_object_type', '装置', '装置', 1, 1),
  ('fire_alarm_object_type', '储罐', '储罐', 2, 1),
  ('fire_alarm_object_type', '仓库', '仓库', 3, 1),
  ('fire_alarm_object_type', '管网', '管网', 4, 1),
  ('fire_alarm_object', '蜡油加氢装置', '蜡油加氢装置', 1, 1),
  ('fire_alarm_object', '催化裂化装置', '催化裂化装置', 2, 1),
  ('fire_alarm_object', '重整装置',     '重整装置',     3, 1),
  ('fire_alarm_object', '储罐区B-3',    '储罐区B-3',    4, 1),
  ('fire_alarm_object', '仓储区A库',    '仓储区A库',    5, 1),
  ('fire_alarm_object', '火炬系统',     '火炬系统',     6, 1),
  ('fire_alarm_status', 'ACTIVE',     '待处理', 1, 1),
  ('fire_alarm_status', 'ACKED',      '已确认', 2, 1),
  ('fire_alarm_status', 'DISPATCHED', '已派单', 3, 1),
  ('fire_alarm_status', 'CLOSED',     '已闭环', 4, 1),
  ('patrol_shift',  '上午', '上午', 1, 1),
  ('patrol_shift',  '下午', '下午', 2, 1),
  ('patrol_shift',  '夜间', '夜间', 3, 1),
  ('patrol_status', '已完成', '已完成', 1, 1),
  ('patrol_status', '未完成', '未完成', 2, 1);
