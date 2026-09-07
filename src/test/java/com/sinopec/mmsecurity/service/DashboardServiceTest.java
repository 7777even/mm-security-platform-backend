package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.DashboardOverview;
import com.sinopec.mmsecurity.dto.Workstation;
import com.sinopec.mmsecurity.entity.FacWorkstation;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.FacDeviceMapper;
import com.sinopec.mmsecurity.mapper.FacWorkstationMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    private FacWorkstation ws(String id, String name, String zone, boolean online) {
        FacWorkstation w = new FacWorkstation();
        w.setWorkstationId(id);
        w.setName(name);
        w.setZone(zone);
        w.setOnline(online);
        return w;
    }
}
