-- V88 工业电视维修工单真实台账（L4 库结构变更，PostgreSQL 方言）。
-- 内容同 h2/V88（建表 + 种子 45 条 + 删除已被取代的 fac_tv_stat_item.MAINTENANCE 字典行）。

CREATE TABLE fac_tv_maintenance_order (
    id                BIGSERIAL PRIMARY KEY,
    order_no          VARCHAR(32)  NOT NULL,
    device_name       VARCHAR(128) NOT NULL,
    device_code       VARCHAR(64),
    fault_desc        VARCHAR(256),
    order_status      VARCHAR(16)  NOT NULL,
    assignee          VARCHAR(64),
    department        VARCHAR(64),
    zone_code         VARCHAR(32),
    created_at        VARCHAR(32)  NOT NULL,
    plan_finish_time  VARCHAR(32),
    actual_finish_time VARCHAR(32),
    handle_desc       VARCHAR(256),
    sort_no           INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_tv_maint_order_status ON fac_tv_maintenance_order (order_status);

-- 未接单（12）
INSERT INTO fac_tv_maintenance_order (order_no, device_name, device_code, fault_desc, order_status, assignee, department, zone_code, created_at, plan_finish_time, actual_finish_time, handle_desc, sort_no) VALUES
  ('WO-2026-0901', '乙烯装置球机-01', 'CAM-YX-01', '画面持续模糊',            'PENDING', NULL, '储运车间', 'YIXI',     '2026-09-28 08:12:33', NULL, NULL, NULL, 1),
  ('WO-2026-0902', '罐区枪机-03',     'CAM-GQ-03', '离线无法回看',            'PENDING', NULL, '储运车间', 'GUANQU',   '2026-09-28 09:40:11', NULL, NULL, NULL, 2),
  ('WO-2026-0903', '厂界球机-02',     'CAM-CJ-02', '角度偏移',                'PENDING', NULL, '安环部',   'BOUNDARY', '2026-09-29 07:25:50', NULL, NULL, NULL, 3),
  ('WO-2026-0904', '消防泵房监控-01', 'CAM-XF-01', '红外补光失效',            'PENDING', NULL, '消防支队', 'YIXI',     '2026-09-29 10:05:42', NULL, NULL, NULL, 4),
  ('WO-2026-0905', '装卸站台枪机-02', 'CAM-ZX-02', '云台卡顿',                'PENDING', NULL, '储运车间', 'GUANQU',   '2026-09-29 14:33:09', NULL, NULL, NULL, 5),
  ('WO-2026-0906', '污水处理监控-01', 'CAM-WS-01', '画面卡顿',                'PENDING', NULL, '公用工程', 'YIXI',     '2026-09-30 08:48:21', NULL, NULL, NULL, 6),
  ('WO-2026-0907', '危化品库监控-03', 'CAM-WH-03', '存储异常',                'PENDING', NULL, '储运车间', 'GUANQU',   '2026-09-30 09:15:37', NULL, NULL, NULL, 7),
  ('WO-2026-0908', '中央控制室球机', 'CAM-ZK-01', '镜头脏污',                'PENDING', NULL, '电仪车间', 'YIXI',     '2026-09-30 10:02:18', NULL, NULL, NULL, 8),
  ('WO-2026-0909', '球形储罐区枪机-05', 'CAM-GQ-05', '焦距失准',             'PENDING', NULL, '储运车间', 'GUANQU',   '2026-09-30 11:20:44', NULL, NULL, NULL, 9),
  ('WO-2026-0910', '厂界门禁监控-04', 'CAM-CJ-04', '信号中断',                'PENDING', NULL, '安环部',   'BOUNDARY', '2026-09-30 13:11:05', NULL, NULL, NULL, 10),
  ('WO-2026-0911', '装置区高空AR-07', 'CAM-AR-07', '画面撕裂',                'PENDING', NULL, '电仪车间', 'YIXI',     '2026-09-30 15:47:52', NULL, NULL, NULL, 11),
  ('WO-2026-0912', '装卸区球机-06',   'CAM-ZX-06', '夜间噪点严重',            'PENDING', NULL, '储运车间', 'GUANQU',   '2026-09-30 16:30:19', NULL, NULL, NULL, 12);

-- 处理中（25）
INSERT INTO fac_tv_maintenance_order (order_no, device_name, device_code, fault_desc, order_status, assignee, department, zone_code, created_at, plan_finish_time, actual_finish_time, handle_desc, sort_no) VALUES
  ('WO-2026-0801', '乙烯装置球机-02', 'CAM-YX-02', '画面偏色',       'PROCESSING', '李伟', '电仪车间', 'YIXI',     '2026-09-20 09:00:00', '2026-09-30 18:00:00', NULL, '已派单，等待备件', 1),
  ('WO-2026-0802', '罐区枪机-01',     'CAM-GQ-01', '雨罩破损',       'PROCESSING', '王强', '储运车间', 'GUANQU',   '2026-09-21 10:30:00', '2026-10-01 18:00:00', NULL, '现场排查中', 2),
  ('WO-2026-0803', '厂界球机-01',     'CAM-CJ-01', '支架松动',       'PROCESSING', '赵敏', '安环部',   'BOUNDARY', '2026-09-21 14:12:00', '2026-10-02 18:00:00', NULL, '已加固，待复测', 3),
  ('WO-2026-0804', '消防泵房监控-02', 'CAM-XF-02', '网线老化',       'PROCESSING', '陈刚', '消防支队', 'YIXI',     '2026-09-22 08:45:00', '2026-10-01 12:00:00', NULL, '光路重新熔接中', 4),
  ('WO-2026-0805', '装卸站台枪机-01', 'CAM-ZX-01', '外壳锈蚀',       'PROCESSING', '刘洋', '储运车间', 'GUANQU',   '2026-09-22 11:20:00', '2026-10-03 18:00:00', NULL, '待除锈喷涂', 5),
  ('WO-2026-0806', '污水处理监控-02', 'CAM-WS-02', '电源适配器故障', 'PROCESSING', '孙磊', '公用工程', 'YIXI',     '2026-09-23 09:10:00', '2026-09-30 18:00:00', NULL, '已更换电源适配器', 6),
  ('WO-2026-0807', '危化品库监控-01', 'CAM-WH-01', '防抖异常',       'PROCESSING', '周婷', '储运车间', 'GUANQU',   '2026-09-23 15:40:00', '2026-10-02 18:00:00', NULL, '已派单，等待备件', 7),
  ('WO-2026-0808', '中央控制室枪机', 'CAM-ZK-02', '镜头起雾',       'PROCESSING', '李伟', '电仪车间', 'YIXI',     '2026-09-24 08:00:00', '2026-10-01 18:00:00', NULL, '现场排查中', 8),
  ('WO-2026-0809', '球形储罐区枪机-02', 'CAM-GQ-02', '云台失控',     'PROCESSING', '王强', '储运车间', 'GUANQU',   '2026-09-24 10:25:00', '2026-10-03 18:00:00', NULL, '已派单，等待备件', 9),
  ('WO-2026-0810', '厂界门禁监控-01', 'CAM-CJ-01b', '读卡器故障',    'PROCESSING', '赵敏', '安环部',   'BOUNDARY', '2026-09-24 13:50:00', '2026-10-02 12:00:00', NULL, '已更换读卡器', 10),
  ('WO-2026-0811', '装置区高空AR-01', 'CAM-AR-01', '定位漂移',       'PROCESSING', '陈刚', '电仪车间', 'YIXI',     '2026-09-25 09:30:00', '2026-10-01 18:00:00', NULL, '现场排查中', 11),
  ('WO-2026-0812', '装卸区球机-01',   'CAM-ZX-01b', '信号弱',        'PROCESSING', '刘洋', '储运车间', 'GUANQU',   '2026-09-25 11:05:00', '2026-10-03 18:00:00', NULL, '已派单，等待备件', 12),
  ('WO-2026-0813', '乙烯装置枪机-05', 'CAM-YX-05', '外壳破损',       'PROCESSING', '孙磊', '电仪车间', 'YIXI',     '2026-09-25 14:20:00', '2026-10-02 18:00:00', NULL, '待更换外壳', 13),
  ('WO-2026-0814', '罐区球机-04',     'CAM-GQ-04', '红外距离不足',   'PROCESSING', '周婷', '储运车间', 'GUANQU',   '2026-09-26 08:40:00', '2026-10-01 18:00:00', NULL, '已派单，等待备件', 14),
  ('WO-2026-0815', '厂界球机-03',     'CAM-CJ-03', '画面闪烁',       'PROCESSING', '李伟', '安环部',   'BOUNDARY', '2026-09-26 10:15:00', '2026-10-02 12:00:00', NULL, '现场排查中', 15),
  ('WO-2026-0816', '消防泵房监控-03', 'CAM-XF-03', '存储空间满',     'PROCESSING', '王强', '消防支队', 'YIXI',     '2026-09-26 13:35:00', '2026-09-30 18:00:00', NULL, '已清理录像，待复测', 16),
  ('WO-2026-0817', '装卸站台枪机-03', 'CAM-ZX-03', '焦距异常',       'PROCESSING', '赵敏', '储运车间', 'GUANQU',   '2026-09-27 09:00:00', '2026-10-03 18:00:00', NULL, '已派单，等待备件', 17),
  ('WO-2026-0818', '污水处理监控-03', 'CAM-WS-03', '网络延迟',       'PROCESSING', '陈刚', '公用工程', 'YIXI',     '2026-09-27 11:25:00', '2026-10-01 18:00:00', NULL, '现场排查中', 18),
  ('WO-2026-0819', '危化品库监控-02', 'CAM-WH-02', '防雷模块告警',   'PROCESSING', '刘洋', '储运车间', 'GUANQU',   '2026-09-27 14:50:00', '2026-10-02 18:00:00', NULL, '已派单，等待备件', 19),
  ('WO-2026-0820', '中央控制室枪机-03', 'CAM-ZK-03', '云台卡顿',     'PROCESSING', '孙磊', '电仪车间', 'YIXI',     '2026-09-28 08:30:00', '2026-10-01 18:00:00', NULL, '现场排查中', 20),
  ('WO-2026-0821', '球形储罐区枪机-06', 'CAM-GQ-06', '外壳锈蚀',     'PROCESSING', '周婷', '储运车间', 'GUANQU',   '2026-09-28 10:10:00', '2026-10-03 18:00:00', NULL, '待除锈喷涂', 21),
  ('WO-2026-0822', '厂界门禁监控-02', 'CAM-CJ-02b', '镜头脏污',     'PROCESSING', '李伟', '安环部',   'BOUNDARY', '2026-09-28 13:40:00', '2026-10-02 12:00:00', NULL, '已派单，等待备件', 22),
  ('WO-2026-0823', '装置区高空AR-03', 'CAM-AR-03', '画面撕裂',       'PROCESSING', '王强', '电仪车间', 'YIXI',     '2026-09-29 09:05:00', '2026-10-01 18:00:00', NULL, '现场排查中', 23),
  ('WO-2026-0824', '装卸区球机-03',   'CAM-ZX-03b', '红外补光失效', 'PROCESSING', '赵敏', '储运车间', 'GUANQU',   '2026-09-29 11:30:00', '2026-10-03 18:00:00', NULL, '已派单，等待备件', 24),
  ('WO-2026-0825', '乙烯装置球机-06', 'CAM-YX-06', '电源适配器故障', 'PROCESSING', '陈刚', '电仪车间', 'YIXI',     '2026-09-29 14:15:00', '2026-09-30 18:00:00', NULL, '已更换电源适配器', 25);

-- 已超时（8）
INSERT INTO fac_tv_maintenance_order (order_no, device_name, device_code, fault_desc, order_status, assignee, department, zone_code, created_at, plan_finish_time, actual_finish_time, handle_desc, sort_no) VALUES
  ('WO-2026-0701', '乙烯装置枪机-01', 'CAM-YX-01b', '主板故障',        'OVERTIME', '刘洋', '电仪车间', 'YIXI',     '2026-09-10 09:00:00', '2026-09-20 18:00:00', NULL, '备件采购延迟', 1),
  ('WO-2026-0702', '罐区枪机-02',     'CAM-GQ-02b', '防水套破损',      'OVERTIME', '孙磊', '储运车间', 'GUANQU',   '2026-09-11 10:20:00', '2026-09-21 18:00:00', NULL, '等待停检窗口', 2),
  ('WO-2026-0703', '厂界球机-04',     'CAM-CJ-04b', '支架变形',        'OVERTIME', '周婷', '安环部',   'BOUNDARY', '2026-09-12 08:50:00', '2026-09-22 12:00:00', NULL, '高空作业未排期', 3),
  ('WO-2026-0704', '消防泵房监控-04', 'CAM-XF-04', '硬盘损坏',        'OVERTIME', '李伟', '消防支队', 'YIXI',     '2026-09-12 14:30:00', '2026-09-22 18:00:00', NULL, '备件未到货', 4),
  ('WO-2026-0705', '装卸站台枪机-04', 'CAM-ZX-04', '云台失控',        'OVERTIME', '王强', '储运车间', 'GUANQU',   '2026-09-13 09:40:00', '2026-09-23 18:00:00', NULL, '等待备件', 5),
  ('WO-2026-0706', '污水处理监控-04', 'CAM-WS-04', '电源模块烧毁',    'OVERTIME', '赵敏', '公用工程', 'YIXI',     '2026-09-14 11:10:00', '2026-09-24 18:00:00', NULL, '采购流程滞后', 6),
  ('WO-2026-0707', '危化品库监控-04', 'CAM-WH-04', '通信中断',        'OVERTIME', '陈刚', '储运车间', 'GUANQU',   '2026-09-15 08:30:00', '2026-09-25 18:00:00', NULL, '等待停检窗口', 7),
  ('WO-2026-0708', '中央控制室枪机-04', 'CAM-ZK-04', '解码卡故障',    'OVERTIME', '刘洋', '电仪车间', 'YIXI',     '2026-09-15 13:00:00', '2026-09-25 18:00:00', NULL, '备件采购延迟', 8);

DELETE FROM fac_tv_stat_item WHERE item_category = 'MAINTENANCE';
