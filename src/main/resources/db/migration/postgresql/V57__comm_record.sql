-- V57 通讯通知记录域（后台管理端「通讯通知管理」五个记录页）真后端化（PostgreSQL 方言）
-- 新增只读表 fac_comm_record，替代 public/pc-admin 原型 comm-* 页的静态演示数据。
-- 契约：docs/api/communication.openapi.json（GET /communication/records?type=）。
-- 列名用 result_text 而非 result：回避各方言保留字冲突（Java 字段仍为 result，@TableField 映射）。

CREATE TABLE IF NOT EXISTS fac_comm_record (
    id BIGSERIAL PRIMARY KEY,
    record_no VARCHAR(64) NOT NULL,
    record_type VARCHAR(16) NOT NULL,
    occurred_at VARCHAR(32),
    category VARCHAR(64),
    sender VARCHAR(64),
    receiver VARCHAR(128),
    summary VARCHAR(512),
    result_text VARCHAR(32),
    duration VARCHAR(16),
    channel VARCHAR(64),
    direction VARCHAR(16),
    content_type VARCHAR(16)
);

INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (1, 'SMS-091', 'sms', '2026-08-21 09:03', '告警通知', '系统', '138****2211', 'T-301 感温报警，请立即核实', '成功', '', '短信网关', '下发', '文本');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (2, 'SMS-092', 'sms', '2026-08-21 09:10', '值班提醒', '系统', '139****3344', '今日 20:00 交接班提醒', '成功', '', '短信网关', '下发', '文本');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (3, 'SMS-093', 'sms', '2026-08-21 09:22', '应急通知', '系统', '137****5566', '储运部启动三级应急响应', '失败', '', '短信网关', '下发', '文本');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (4, 'CALL-44', 'call', '2026-08-21 09:05', '告警外呼', '系统', '李主任', '', '接通', '00:42', '调度电话', '外呼', '语音');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (5, 'CALL-45', 'call', '2026-08-21 09:18', '应急联络', '王班长', '调度中心', '', '未接通', '00:00', '调度电话', '呼入', '语音');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (6, 'CALL-46', 'call', '2026-08-21 09:31', '值班查岗', '调度中心', '张工', '', '接通', '01:15', '调度电话', '外呼', '语音');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (7, 'BC-R-12', 'broadcast', '2026-08-21 09:06', '应急广播', '系统', '储运部', '请相关人员立即撤离至安全集合点', '', '', 'BC-D-01', '定时', '文本');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (8, 'BC-R-13', 'broadcast', '2026-08-21 09:25', '日常播报', '广播主机', '全厂', '上下午班交接提示，请各岗位确认', '', '', 'BC-D-02', '手动', '文本');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (9, 'BC-R-14', 'broadcast', '2026-08-21 09:40', '应急广播', '系统', '化工区', '演练开始通知，请各岗位就位', '', '', 'BC-D-05', '定时', '语音');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (10, 'PUSH-77', 'push', '2026-08-21 09:02', '告警', '系统', '值班组', '一级火警待确认', '已送达', '', '报警管理', '', '文本');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (11, 'PUSH-78', 'push', '2026-08-21 09:12', '工单', '系统', '维保组', '新维修工单待接收', '已送达', '', '故障管理', '', '文本');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (12, 'PUSH-79', 'push', '2026-08-21 09:35', '通知', '系统', '全员', '厂区临时限行通知', '已送达', '', '通知公告', '', '文本');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (13, 'IC-09', 'intercom', '2026-08-21 08:50', '语音对讲', '王班长', '巡查组', '', '结束', '01:20', 'CH-3', '组呼', '语音');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (14, 'IC-10', 'intercom', '2026-08-21 09:02', '语音对讲', '调度中心', '储运部', '', '结束', '00:45', 'CH-1', '单呼', '语音');
INSERT INTO fac_comm_record (id, record_no, record_type, occurred_at, category, sender, receiver, summary, result_text, duration, channel, direction, content_type) VALUES (15, 'IC-11', 'intercom', '2026-08-21 09:27', '语音对讲', '李主任', '应急处置组', '', '结束', '02:10', 'CH-5', '组呼', '语音');
