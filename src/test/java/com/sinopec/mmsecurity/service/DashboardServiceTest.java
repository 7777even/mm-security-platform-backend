package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.AlarmTrendPoint;
import com.sinopec.mmsecurity.dto.DashboardOverview;
import com.sinopec.mmsecurity.dto.RiskHeatItem;
import com.sinopec.mmsecurity.dto.SystemMessageItem;
import com.sinopec.mmsecurity.dto.Workstation;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.FacPerimeterAlarm;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.entity.FacSystemMessage;
import com.sinopec.mmsecurity.entity.FacWorkstation;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.FacDeviceMapper;
import com.sinopec.mmsecurity.mapper.FacSystemMessageMapper;
import com.sinopec.mmsecurity.mapper.FacWorkstationMapper;
import com.sinopec.mmsecurity.mapper.FacPerimeterAlarmMapper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import org.junit.jupiter.api.BeforeEach;
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
    private final FacSystemMessageMapper systemMessageMapper = mock(FacSystemMessageMapper.class);
    private final DataScopeResolver dataScopeResolver = mock(DataScopeResolver.class);
    private final FacPerimeterAlarmMapper perimeterAlarmMapper = mock(FacPerimeterAlarmMapper.class);
    private final DashboardService service =
            new DashboardService(deviceMapper, alarmMapper, workstationMapper, systemMessageMapper, dataScopeResolver, perimeterAlarmMapper);

    @BeforeEach
    void resetCaches() {
        service.clearCaches();
    }

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
    void trendDaily_bucketsByDay() {
        // 固定 now = 2026-09-07 11:30 → 窗口 09-01 .. 09-07（7 天），今天 = 09-07
        LocalDateTime now = LocalDateTime.of(2026, 9, 7, 11, 30);
        when(alarmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                alarmAt(2026, 9, 7, 8, 0),   // 计入「09-07」
                alarmAt(2026, 9, 7, 9, 0),   // 计入「09-07」（同日累加）
                alarmAt(2026, 9, 7, 9, 45),  // 计入「09-07」
                alarmAt(2026, 9, 7, 11, 15), // 计入「09-07」
                alarmAt(2026, 9, 5, 14, 0)   // 计入「09-05」，不应混入「09-07」
        ));
        when(perimeterAlarmMapper.selectList(any())).thenReturn(List.of(
                perimeterAt(2026, 9, 6, 10, 0) // 计入「09-06」
        ));

        List<AlarmTrendPoint> points = service.trendDaily(now);
        assertEquals(7, points.size());

        Map<String, Integer> byDay = points.stream()
                .collect(Collectors.toMap(AlarmTrendPoint::getDate, AlarmTrendPoint::getCount));
        // 今天（09-07）4 条主告警
        assertEquals(4, byDay.get("09-07"));
        // 09-05 主告警 1 条
        assertEquals(1, byDay.get("09-05"));
        // 09-06 周界告警 1 条
        assertEquals(1, byDay.get("09-06"));
        // 其余 4 天补 0
        assertEquals(0, byDay.get("09-01"));
        assertEquals(0, byDay.get("09-02"));
        assertEquals(0, byDay.get("09-03"));
        assertEquals(0, byDay.get("09-04"));
    }

    @Test
    void trendDaily_emptyWindow_returnsAllZeros() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 7, 11, 30);
        when(alarmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(perimeterAlarmMapper.selectList(any())).thenReturn(List.of());

        List<AlarmTrendPoint> points = service.trendDaily(now);
        assertEquals(7, points.size());
        assertEquals(0, points.stream().mapToInt(AlarmTrendPoint::getCount).sum());
    }

    private FacAlarm alarmAt(int y, int mo, int d, int h, int mi) {
        FacAlarm a = new FacAlarm();
        a.setOccurredAt(LocalDateTime.of(y, mo, d, h, mi));
        return a;
    }

    private FacPerimeterAlarm perimeterAt(int y, int mo, int d, int h, int mi) {
        FacPerimeterAlarm p = new FacPerimeterAlarm();
        p.setAlarmTime(String.format("%04d-%02d-%02d %02d:%02d:00", y, mo, d, h, mi));
        return p;
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

    @Test
    void systemMessages_mapsRealTable() {
        FacSystemMessage m1 = new FacSystemMessage();
        m1.setId(1L);
        m1.setMsgType("danger");
        m1.setTitle("人员违规进入");
        m1.setContent("A装置区域发现非注册人员，请核实。");
        m1.setOccurredAt("2026-03-17 14:21:30");
        FacSystemMessage m2 = new FacSystemMessage();
        m2.setId(2L);
        m2.setMsgType("warning");
        m2.setTitle("有毒气体超标");
        m2.setContent("有毒气体浓度超标，请撤离。");
        m2.setOccurredAt("2026-03-17 14:21:30");
        when(systemMessageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(m1, m2));

        List<SystemMessageItem> out = service.systemMessages();
        assertEquals(2, out.size());
        assertEquals("danger", out.get(0).getType());
        assertEquals("人员违规进入", out.get(0).getTitle());
        assertEquals("2026-03-17 14:21:30", out.get(0).getTime());
        assertEquals("warning", out.get(1).getType());
    }
}
