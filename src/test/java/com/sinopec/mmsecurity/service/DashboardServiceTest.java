package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.AlarmTrendPoint;
import com.sinopec.mmsecurity.dto.DashboardOverview;
import com.sinopec.mmsecurity.dto.RiskHeatItem;
import com.sinopec.mmsecurity.dto.Workstation;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.entity.FacWorkstation;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.FacDeviceMapper;
import com.sinopec.mmsecurity.mapper.FacWorkstationMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * DashboardService（纯 Mockito，不启动 Spring 上下文）：
 * overview() 的真实聚合计数、riskIndex 计算、onlineWorkstation 派生；
 * workstations() 的实体→DTO 映射。无硬编码/随机值。
 */
class DashboardServiceTest {

    private final FacDeviceMapper deviceMapper = mock(FacDeviceMapper.class);
    private final AlarmMapper alarmMapper = mock(AlarmMapper.class);
    private final FacWorkstationMapper workstationMapper = mock(FacWorkstationMapper.class);
    private final DashboardService service = new DashboardService(deviceMapper, alarmMapper, workstationMapper);

    @Test
    void overview_aggregatesRealCounts() {
        // overview() 调用顺序：deviceTotal → deviceOnline → activeAlarm
        when(deviceMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(8L, 5L);
        when(alarmMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(4L);
        when(workstationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                ws("WS-01", "中控室工位-01", "罐区A", true),
                ws("WS-02", "罐区值班室工位", "罐区A", true),
                ws("WS-03", "应急指挥中心工位", "全厂范围", false)
        ));

        DashboardOverview ov = service.overview();
        assertEquals(8, ov.getDeviceTotal());
        assertEquals(5, ov.getDeviceOnline());
        assertEquals(4, ov.getActiveAlarm());
        assertEquals(2, ov.getOnlineWorkstation());
        // riskIndex = 4*0.7 + (8-5)*0.3 = 3.7
        assertEquals(3.7, ov.getRiskIndex(), 0.0001);
    }

    @Test
    void overview_emptyDb_returnsZeros() {
        when(deviceMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L, 0L);
        when(alarmMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(workstationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        DashboardOverview ov = service.overview();
        assertEquals(0, ov.getDeviceTotal());
        assertEquals(0, ov.getDeviceOnline());
        assertEquals(0, ov.getActiveAlarm());
        assertEquals(0, ov.getOnlineWorkstation());
        assertEquals(0.0, ov.getRiskIndex(), 0.0001);
    }

    @Test
    void workstations_mapsEntityToDto() {
        when(workstationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                ws("WS-01", "中控室工位-01", "罐区A", true)
        ));

        List<Workstation> list = service.workstations();
        assertEquals(1, list.size());
        assertEquals("WS-01", list.get(0).getId());
        assertEquals("中控室工位-01", list.get(0).getName());
        assertEquals("罐区A", list.get(0).getZone());
        assertEquals(true, list.get(0).isOnline());
    }

    @Test
    void trend24h_bucketsByHourWithZeroPadding() {
        // 固定 now = 2026-09-07 11:30 → 窗口 [09-06 12:00, 09-07 12:00)
        LocalDateTime now = LocalDateTime.of(2026, 9, 7, 11, 30);
        when(alarmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                alarmAt(2026, 9, 7, 8, 0),   // 桶 20 → "08:00"
                alarmAt(2026, 9, 7, 9, 0),   // 桶 21 → "09:00"
                alarmAt(2026, 9, 7, 9, 45),  // 桶 21 → "09:00"（同桶累加）
                alarmAt(2026, 9, 7, 11, 15)  // 桶 23 → "11:00"
        ));

        List<AlarmTrendPoint> points = service.trend24h(now);
        assertEquals(24, points.size());

        Map<String, Integer> byHour = points.stream()
                .collect(Collectors.toMap(AlarmTrendPoint::getHour, AlarmTrendPoint::getCount));
        assertEquals(1, byHour.get("08:00"));
        assertEquals(2, byHour.get("09:00"));
        assertEquals(1, byHour.get("11:00"));
        // 其余 21 个桶补 0
        assertEquals(0, byHour.get("00:00"));
        assertEquals(0, byHour.get("07:00"));
        assertEquals(0, byHour.get("23:00"));
    }

    @Test
    void trend24h_emptyWindow_returnsAllZeros() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 7, 11, 30);
        when(alarmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<AlarmTrendPoint> points = service.trend24h(now);
        assertEquals(24, points.size());
        assertEquals(0, points.stream().mapToInt(AlarmTrendPoint::getCount).sum());
    }

    private FacAlarm alarmAt(int y, int mo, int d, int h, int mi) {
        FacAlarm a = new FacAlarm();
        a.setOccurredAt(LocalDateTime.of(y, mo, d, h, mi));
        return a;
    }

    private FacWorkstation ws(String id, String name, String zone, boolean online) {
        FacWorkstation w = new FacWorkstation();
        w.setWorkstationId(id);
        w.setName(name);
        w.setZone(zone);
        w.setOnline(online);
        return w;
    }

    @Test
    void riskHeatmap_aggregatesZoneScoresFromRealData() {
        // 罐区A：2 设备（1 离线）；装置C：2 设备（1 离线 1 告警）
        when(deviceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                dev("DC-01", "罐区A", 0),
                dev("DC-02", "罐区A", 1),
                dev("DC-03", "装置C", 2),
                dev("DC-04", "装置C", 0)
        ));
        // 活动报警（status=0）各 1 条关联到 罐区A(DC-01) 与 装置C(DC-04)
        when(alarmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                alarmOnDevice("DC-01"),
                alarmOnDevice("DC-04")
        ));

        List<RiskHeatItem> items = service.riskHeatmap();
        Map<String, Double> byZone = items.stream()
                .collect(Collectors.toMap(RiskHeatItem::getZone, RiskHeatItem::getScore));
        // 装置C = 离线1*0.5 + 告警1*1.5 + 活动报警1*1.0 = 3.0
        assertEquals(3.0, byZone.get("装置C"), 0.0001);
        // 罐区A = 离线1*0.5 + 活动报警1*1.0 = 1.5
        assertEquals(1.5, byZone.get("罐区A"), 0.0001);
    }

    private FacDevice dev(String code, String zone, int status) {
        FacDevice d = new FacDevice();
        d.setDeviceCode(code);
        d.setZone(zone);
        d.setStatus(status);
        return d;
    }

    private FacAlarm alarmOnDevice(String code) {
        FacAlarm a = new FacAlarm();
        a.setDeviceCode(code);
        a.setStatus(0);
        return a;
    }
}
