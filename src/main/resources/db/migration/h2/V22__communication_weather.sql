-- V22 通讯设备 + 天气观测/预报真实数据源。
-- 数据来源（前端存量硬编码，迁移后数值与文案语义保持不变）：
--   1) src/screen/lib/data/communicationDeviceMock.ts —— 广播/电话/对讲三类设备的分组与档案明细；
--   2) src/screen/lib/data/weatherMock.ts —— 天气实况(currentWeather)、逐小时(hourlyWeather, 8 点)、
--      七日预报(dailyWeather, 7 天)。
-- 命名回避 H2 保留字（value/command）：数值列统一加 _value 后缀（rain_value/wind_value 等），
-- condition → condition_text；type/status/level/time/day 在 H2 中可裸用，但设备类型与状态仍加业务前缀
-- 以免与后续多域混用混淆。带量纲的展示文案（'2.4m/s'、'76%'、'1004hPa'、'18km'、'0.0mm'）原样以
-- VARCHAR 入库，服务层不做单位换算。

-- ---------------------------------------------------------------- 通讯设备
CREATE TABLE fac_comm_device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_code VARCHAR(32) NOT NULL,
    device_type VARCHAR(16) NOT NULL,
    group_key VARCHAR(32) NOT NULL,
    group_label VARCHAR(64) NOT NULL,
    device_name VARCHAR(64) NOT NULL,
    area_name VARCHAR(64) NOT NULL,
    location_name VARCHAR(64) NOT NULL,
    device_status VARCHAR(16) NOT NULL,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    category_name VARCHAR(64) NOT NULL,
    install_time VARCHAR(32) NOT NULL,
    owner_name VARCHAR(64) NOT NULL,
    ip_address VARCHAR(64) NOT NULL,
    last_check_time VARCHAR(32) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

-- 广播（broadcast，6 台：A装置区 3 / B装置区 2 / 公共区 1）
INSERT INTO fac_comm_device (device_code, device_type, group_key, group_label, device_name, area_name,
    location_name, device_status, longitude, latitude, category_name, install_time, owner_name,
    ip_address, last_check_time, sort_no) VALUES
  ('bc-a1', 'broadcast', 'area-a', 'A装置区 (6)', 'A装置区1#广播', 'A装置区', 'A装置区东北角', '在线',
   110.881, 21.671, '室外防爆广播', '2024-03-12', '安环部', '10.20.31.101', '2026-08-10 08:30:00', 1),
  ('bc-a2', 'broadcast', 'area-a', 'A装置区 (6)', 'A装置区2#广播', 'A装置区', 'A装置区西南角', '在线',
   110.879, 21.669, '室外防爆广播', '2024-03-12', '安环部', '10.20.31.102', '2026-08-10 08:30:00', 2),
  ('bc-a3', 'broadcast', 'area-a', 'A装置区 (6)', 'A装置区3#广播', 'A装置区', 'A装置区北侧', '故障',
   110.882, 21.672, '室外防爆广播', '2024-05-20', '安环部', '10.20.31.103', '2026-08-09 20:15:00', 3),
  ('bc-b1', 'broadcast', 'area-b', 'B装置区 (2)', 'B装置区1#广播', 'B装置区', 'B装置区东侧', '在线',
   110.886, 21.668, '室外防爆广播', '2024-06-01', '安环部', '10.20.32.101', '2026-08-10 08:30:00', 4),
  ('bc-b2', 'broadcast', 'area-b', 'B装置区 (2)', 'B装置区2#广播', 'B装置区', 'B装置区西侧', '离线',
   110.884, 21.667, '室外防爆广播', '2024-06-01', '安环部', '10.20.32.102', '2026-08-08 18:00:00', 5),
  ('bc-p1', 'broadcast', 'public', '公共区 (1)', '厂区大门广播', '公共区', '厂区大门西侧', '在线',
   110.878, 21.674, '室内广播', '2024-02-10', '综合部', '10.20.30.101', '2026-08-10 08:30:00', 6);

-- 电话（phone，3 台：A装置区 2 / B装置区 1）
INSERT INTO fac_comm_device (device_code, device_type, group_key, group_label, device_name, area_name,
    location_name, device_status, longitude, latitude, category_name, install_time, owner_name,
    ip_address, last_check_time, sort_no) VALUES
  ('ph-a1', 'phone', 'area-a', 'A装置区 (2)', 'A装置区1#电话', 'A装置区', 'A装置区控制室', '在线',
   110.881, 21.671, '防爆电话', '2023-11-18', '生产部', '10.20.41.101', '2026-08-10 08:30:00', 7),
  ('ph-a2', 'phone', 'area-a', 'A装置区 (2)', 'A装置区2#电话', 'A装置区', 'A装置区东北角', '在线',
   110.882, 21.672, '防爆电话', '2023-11-18', '生产部', '10.20.41.102', '2026-08-10 08:30:00', 8),
  ('ph-b1', 'phone', 'area-b', 'B装置区 (1)', 'B装置区1#电话', 'B装置区', 'B装置区控制室', '在线',
   110.885, 21.668, '防爆电话', '2024-01-08', '生产部', '10.20.42.101', '2026-08-10 08:30:00', 9);

-- 对讲（intercom，2 台：A装置区 2）
INSERT INTO fac_comm_device (device_code, device_type, group_key, group_label, device_name, area_name,
    location_name, device_status, longitude, latitude, category_name, install_time, owner_name,
    ip_address, last_check_time, sort_no) VALUES
  ('ic-a1', 'intercom', 'area-a', 'A装置区 (2)', 'A装置区1#对讲', 'A装置区', 'A装置区巡检点1', '在线',
   110.880, 21.670, 'IP对讲终端', '2024-07-15', '安环部', '10.20.51.101', '2026-08-10 08:30:00', 10),
  ('ic-a2', 'intercom', 'area-a', 'A装置区 (2)', 'A装置区2#对讲', 'A装置区', 'A装置区巡检点2', '故障',
   110.883, 21.670, 'IP对讲终端', '2024-07-15', '安环部', '10.20.51.102', '2026-08-09 22:00:00', 11);

-- ---------------------------------------------------------------- 天气实况（单行）
CREATE TABLE fac_weather_current (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    temperature INT NOT NULL DEFAULT 0,
    condition_text VARCHAR(32) NOT NULL,
    air_quality INT NOT NULL DEFAULT 0,
    air_quality_level VARCHAR(16) NOT NULL,
    wind_direction VARCHAR(16) NOT NULL,
    wind_speed VARCHAR(16) NOT NULL,
    wind_level VARCHAR(16) NOT NULL,
    humidity_text VARCHAR(16) NOT NULL,
    pressure_text VARCHAR(16) NOT NULL,
    visibility_text VARCHAR(16) NOT NULL,
    rainfall_text VARCHAR(16) NOT NULL,
    updated_at VARCHAR(32) NOT NULL
);

INSERT INTO fac_weather_current (temperature, condition_text, air_quality, air_quality_level,
    wind_direction, wind_speed, wind_level, humidity_text, pressure_text, visibility_text,
    rainfall_text, updated_at) VALUES
  (29, '多云', 35, '优', '东南风', '2.4m/s', '2级', '76%', '1004hPa', '18km', '0.0mm', '08-25 10:30');

-- ---------------------------------------------------------------- 逐小时序列（8 点）
CREATE TABLE fac_weather_hourly (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    time_label VARCHAR(16) NOT NULL,
    rain_value DOUBLE NOT NULL DEFAULT 0,
    wind_value DOUBLE NOT NULL DEFAULT 0,
    temperature INT NOT NULL DEFAULT 0,
    pressure_value DOUBLE NOT NULL DEFAULT 0,
    humidity_value INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

INSERT INTO fac_weather_hourly (time_label, rain_value, wind_value, temperature, pressure_value,
    humidity_value, sort_no) VALUES
  ('当前',   0.0, 2.4, 29, 1004, 76, 1),
  ('12:00', 0.2, 2.8, 31, 1003, 72, 2),
  ('15:00', 1.8, 3.6, 32, 1001, 78, 3),
  ('18:00', 4.6, 4.2, 29, 1002, 85, 4),
  ('21:00', 2.1, 3.4, 27, 1004, 88, 5),
  ('00:00', 0.6, 2.6, 26, 1005, 89, 6),
  ('03:00', 0.1, 2.2, 26, 1005, 90, 7),
  ('06:00', 0.0, 2.5, 27, 1006, 84, 8);

-- ---------------------------------------------------------------- 七日预报（7 天）
CREATE TABLE fac_weather_daily (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_label VARCHAR(16) NOT NULL,
    date_label VARCHAR(16) NOT NULL,
    condition_text VARCHAR(32) NOT NULL,
    icon_text VARCHAR(16) NOT NULL,
    high_temp INT NOT NULL DEFAULT 0,
    low_temp INT NOT NULL DEFAULT 0,
    wind_text VARCHAR(32) NOT NULL,
    humidity_value INT NOT NULL DEFAULT 0,
    rain_value DOUBLE NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

INSERT INTO fac_weather_daily (day_label, date_label, condition_text, icon_text, high_temp, low_temp,
    wind_text, humidity_value, rain_value, sort_no) VALUES
  ('今天', '08-25', '多云',     '⛅', 32, 26, '东南风 2级', 76, 0.0,  1),
  ('明天', '08-26', '雷阵雨',   '⛈', 31, 25, '东南风 3级', 84, 9.6,  2),
  ('周四', '08-27', '中雨',     '🌧', 29, 25, '南风 3级',   89, 15.0, 3),
  ('周五', '08-28', '阵雨',     '🌦', 30, 25, '东南风 2级', 82, 6.5,  4),
  ('周六', '08-29', '多云',     '⛅', 32, 26, '东风 2级',   75, 1.2,  5),
  ('周日', '08-30', '晴间多云', '🌤', 33, 26, '东风 2级',   70, 0.0,  6),
  ('周一', '08-31', '多云',     '☁', 32, 26, '东南风 2级', 73, 0.0,  7);
