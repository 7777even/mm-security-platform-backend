package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.FireFacilityAlarmResult;
import com.sinopec.mmsecurity.dto.FireFacilityFaultItem;
import com.sinopec.mmsecurity.dto.FireFacilityFaultResult;
import com.sinopec.mmsecurity.dto.FireFacilityLedgerResult;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorResult;
import com.sinopec.mmsecurity.dto.FireFacilityWorkOrderResult;
import com.sinopec.mmsecurity.entity.FacFireFacilityFault;
import com.sinopec.mmsecurity.entity.FacFireFacilityFaultTimeline;
import com.sinopec.mmsecurity.entity.FacFireFacilityLedger;
import com.sinopec.mmsecurity.entity.FacFireFacilityMaintenance;
import com.sinopec.mmsecurity.entity.FacFireFacilityMonitor;
import com.sinopec.mmsecurity.entity.FacFireFacilityOption;
import com.sinopec.mmsecurity.entity.FacFireFacilityParam;
import com.sinopec.mmsecurity.mapper.FacFireFacilityFaultMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityFaultTimelineMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityLedgerMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityMaintenanceMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityOptionMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityParamMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** 消防设施监测服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class FireFacilityServiceTest {

    @Mock
    private FacFireFacilityMonitorMapper monitorMapper;
    @Mock
    private FacFireFacilityParamMapper paramMapper;
    @Mock
    private FacFireFacilityLedgerMapper ledgerMapper;
    @Mock
    private FacFireFacilityMaintenanceMapper maintenanceMapper;
    @Mock
    private FacFireFacilityFaultMapper faultMapper;
    @Mock
    private FacFireFacilityFaultTimelineMapper timelineMapper;
    @Mock
    private FacFireFacilityOptionMapper optionMapper;

    @InjectMocks
    private FireFacilityService service;

    private static FacFireFacilityMonitor monitor(Long id, String key, String type, int total) {
        FacFireFacilityMonitor e = new FacFireFacilityMonitor();
        e.setId(id);
        e.setKeyCode(key);
        e.setFacilityType(type);
        e.setTotalCount(total);
        e.setOnlineCount(total - 1);
        e.setOfflineCount(1);
        e.setFaultCount(0);
        e.setMonitorStatus("正常");
        e.setLastReportTime("2026-08-20 10:23:15");
        e.setSortNo(1);
        return e;
    }

    private static FacFireFacilityParam param(Long monitorId, String label, String value, String tone) {
        FacFireFacilityParam e = new FacFireFacilityParam();
        e.setMonitorId(monitorId);
        e.setLabel(label);
        e.setValueText(value);
        e.setTone(tone);
        e.setSortNo(1);
        return e;
    }

    private static FacFireFacilityOption option(String label, int sortNo) {
        FacFireFacilityOption e = new FacFireFacilityOption();
        e.setKind("FACILITY_TYPE");
        e.setOptionLabel(label);
        e.setSortNo(sortNo);
        return e;
    }

    private static FacFireFacilityFault fault(Long id, String faultCode, String faultType,
                                              String faultLevel, String faultStatus, String workOrderNo) {
        FacFireFacilityFault e = new FacFireFacilityFault();
        e.setId(id);
        e.setFaultCode(faultCode);
        e.setFacilityCode("XF-001");
        e.setFacilityName("火灾自动报警系统-1#联合装置");
        e.setFacilityType("火灾自动报警系统");
        e.setFaultType(faultType);
        e.setFaultLevel(faultLevel);
        e.setDiscoverTime("2026-08-19 08:30:05");
        e.setDiscoverMethod("系统告警");
        e.setPhenomenon("1#排烟风机故障停机");
        e.setCauseText("风机电机过载");
        e.setFaultStatus(faultStatus);
        e.setWorkOrderNo(workOrderNo);
        e.setSortNo(1);
        return e;
    }

    private static FacFireFacilityFaultTimeline timeline(Long faultId, String action, String time) {
        FacFireFacilityFaultTimeline e = new FacFireFacilityFaultTimeline();
        e.setFaultId(faultId);
        e.setEventTime(time);
        e.setOperatorName("值班员-高策");
        e.setActionName(action);
        e.setDetailText("派发至 李维修（电气车间）");
        e.setSortNo(1);
        return e;
    }

    @Test
    void monitors_mergesParamsAndReturnsTypeOptions() {
        when(monitorMapper.selectList(any())).thenReturn(List.of(monitor(2L, "water", "消防水源", 46)));
        when(paramMapper.selectList(any())).thenReturn(List.of(
                param(2L, "水泵运行", "运行", "normal"),
                param(2L, "水位", "32%", "warning")));
        when(optionMapper.selectList(any())).thenReturn(List.of(option("全部类型", 1), option("消防水源", 3)));

        FireFacilityMonitorResult result = service.monitors("消防水源");

        assertEquals(1, result.getItems().size());
        assertEquals("water", result.getItems().get(0).getKey());
        assertEquals(46, result.getItems().get(0).getTotal());
        assertEquals("正常", result.getItems().get(0).getStatus());
        assertEquals(2, result.getItems().get(0).getParams().size());
        assertEquals("32%", result.getItems().get(0).getParams().get(1).getValue());
        assertEquals("warning", result.getItems().get(0).getParams().get(1).getTone());
        assertEquals(List.of("全部类型", "消防水源"), result.getTypeOptions());
    }

    @Test
    void monitors_returnsAllWhenTypeBlankOrAll() {
        when(monitorMapper.selectList(any())).thenReturn(List.of(
                monitor(1L, "fas", "火灾自动报警系统", 128), monitor(2L, "water", "消防水源", 46)));
        when(paramMapper.selectList(any())).thenReturn(List.of(param(1L, "运行状态", "报警", "danger")));
        when(optionMapper.selectList(any())).thenReturn(List.of(option("全部类型", 1)));

        assertEquals(2, service.monitors(null).getItems().size());
        assertEquals(2, service.monitors("   ").getItems().size());
        assertEquals(2, service.monitors("全部类型").getItems().size());
        assertEquals("报警", service.monitors(null).getItems().get(0).getParams().get(0).getValue());
        assertEquals(0, service.monitors("全部类型").getItems().get(1).getParams().size());
    }

    @Test
    void ledger_mergesMaintenanceRecords() {
        FacFireFacilityLedger first = new FacFireFacilityLedger();
        first.setId(1L);
        first.setFacilityCode("XF-001");
        first.setFacilityName("火灾自动报警系统-1#联合装置");
        first.setFacilityType("火灾自动报警系统");
        first.setLocationName("炼油一部 1#联合装置");
        first.setDeviceName("1#联合装置");
        first.setMaintainerName("茂名石化消防维保公司");
        first.setMaintainerPhone("0668-2110001");
        first.setEnabledFlag(true);
        first.setSortNo(1);
        FacFireFacilityLedger second = new FacFireFacilityLedger();
        second.setId(2L);
        second.setFacilityCode("XF-002");
        second.setFacilityName("消防水罐-1#");
        second.setFacilityType("消防水源");
        second.setLocationName("消防泵房");
        second.setDeviceName("消防泵房");
        second.setMaintainerName("茂名石化消防维保公司");
        second.setMaintainerPhone("0668-2110002");
        second.setEnabledFlag(false);
        second.setSortNo(2);
        when(ledgerMapper.selectList(any())).thenReturn(List.of(first, second));

        FacFireFacilityMaintenance m1 = new FacFireFacilityMaintenance();
        m1.setLedgerId(1L);
        m1.setRecordDate("2026-08-05");
        m1.setContentText("季度维保：控制器巡检、探测器抽测，全部正常。");
        m1.setSortNo(1);
        FacFireFacilityMaintenance m2 = new FacFireFacilityMaintenance();
        m2.setLedgerId(1L);
        m2.setRecordDate("2026-05-12");
        m2.setContentText("半年检：联动测试、报警点位核对。");
        m2.setSortNo(2);
        when(maintenanceMapper.selectList(any())).thenReturn(List.of(m1, m2));
        when(optionMapper.selectList(any())).thenReturn(List.of(option("全部类型", 1)));

        FireFacilityLedgerResult result = service.ledger("火灾自动报警系统");

        assertEquals(2, result.getItems().size());
        assertEquals("炼油一部 1#联合装置", result.getItems().get(0).getLocation());
        assertEquals("1#联合装置", result.getItems().get(0).getDevice());
        assertTrue(result.getItems().get(0).getEnabled());
        assertEquals(2, result.getItems().get(0).getMaintenanceRecords().size());
        assertEquals("2026-05-12", result.getItems().get(0).getMaintenanceRecords().get(1).getDate());
        assertNull(result.getItems().get(0).getMaintenanceRecords().get(0).getReportFile());
        assertFalse(result.getItems().get(1).getEnabled());
        assertEquals(0, result.getItems().get(1).getMaintenanceRecords().size());
    }

    @Test
    void faults_mergesTimelineAndMapsAllFields() {
        when(faultMapper.selectList(any())).thenReturn(List.of(
                fault(1L, "FLT-20260819-004", "硬件故障", "紧急", "已派单", "WO-20260819-001")));
        when(timelineMapper.selectList(any())).thenReturn(List.of(
                timeline(1L, "发现故障", "2026-08-19 08:30:05"),
                timeline(1L, "生成工单并派发", "2026-08-19 08:42:31")));

        FireFacilityFaultResult result = service.faults("紧急", "已派单");

        assertEquals(1, result.getItems().size());
        FireFacilityFaultItem item = result.getItems().get(0);
        assertEquals("FLT-20260819-004", item.getFaultCode());
        assertEquals("XF-001", item.getFacilityCode());
        assertEquals("火灾自动报警系统", item.getFacilityType());
        assertEquals("硬件故障", item.getFaultType());
        assertEquals("紧急", item.getFaultLevel());
        assertEquals("系统告警", item.getDiscoverMethod());
        assertEquals("1#排烟风机故障停机", item.getPhenomenon());
        assertEquals("风机电机过载", item.getCause());
        assertEquals("已派单", item.getStatus());
        assertEquals("WO-20260819-001", item.getWorkOrderNo());
        assertEquals(2, item.getTimeline().size());
        assertEquals("2026-08-19 08:30:05", item.getTimeline().get(0).getTime());
        assertEquals("值班员-高策", item.getTimeline().get(0).getOperator());
        assertEquals("生成工单并派发", item.getTimeline().get(1).getAction());
        assertEquals("派发至 李维修（电气车间）", item.getTimeline().get(1).getDetail());
    }

    @Test
    void alarms_derivesFromFaultsWithCategoryRule() {
        when(faultMapper.selectList(any())).thenReturn(List.of(
                fault(1L, "FLT-20260820-001", "硬件故障", "紧急", "待确认", null),
                fault(2L, "FLT-20260818-006", "老化", "重要", "维修中", "WO-20260818-003"),
                fault(3L, "FLT-20260818-005", "通信故障", "一般", "已派单", "WO-20260818-002")));
        when(timelineMapper.selectList(any())).thenReturn(List.of());

        FireFacilityAlarmResult result = service.alarms(null, null);

        assertEquals(3, result.getItems().size());
        assertEquals("AL-20260820001", result.getItems().get(0).getId());
        assertEquals("故障", result.getItems().get(0).getCategory());
        assertEquals("火灾自动报警系统", result.getItems().get(0).getSource());
        assertEquals("紧急", result.getItems().get(0).getLevel());
        assertEquals("1#排烟风机故障停机", result.getItems().get(0).getContent());
        assertEquals("2026-08-19 08:30:05", result.getItems().get(0).getTime());
        assertEquals("FLT-20260818-006", result.getItems().get(1).getFaultCode());
        assertEquals("动作", result.getItems().get(1).getCategory());
        assertEquals("故障", result.getItems().get(2).getCategory());
    }

    @Test
    void workOrders_skipsBlankOrderNoAndMapsStatus() {
        when(faultMapper.selectList(any())).thenReturn(List.of(
                fault(1L, "FLT-A", "硬件故障", "紧急", "已派单", "WO-1"),
                fault(2L, "FLT-B", "硬件故障", "紧急", "待确认", ""),
                fault(3L, "FLT-C", "硬件故障", "紧急", "维修中", "WO-3"),
                fault(4L, "FLT-D", "软件故障", "重要", "已闭环", "WO-4"),
                fault(5L, "FLT-E", "软件故障", "重要", "待验收", "WO-5"),
                fault(6L, "FLT-F", "软件故障", "一般", null, "WO-6")));
        when(timelineMapper.selectList(any())).thenReturn(List.of(
                timeline(1L, "发现故障", "2026-08-19 08:30:05"),
                timeline(1L, "生成工单并派发", "2026-08-19 08:42:31")));

        FireFacilityWorkOrderResult result = service.workOrders(null);

        assertEquals(5, result.getItems().size());
        assertEquals(1L, result.getItems().get(0).getId());
        assertEquals("WO-1", result.getItems().get(0).getWorkOrderNo());
        assertEquals("已派发", result.getItems().get(0).getStatus());
        assertEquals("2026-08-19 08:42:31", result.getItems().get(0).getDispatchTime());
        assertEquals("1#排烟风机故障停机", result.getItems().get(0).getDescription());
        assertEquals(2, result.getItems().get(0).getTimeline().size());
        assertEquals("执行中", result.getItems().get(1).getStatus());
        assertEquals("2026-08-19 08:30:05", result.getItems().get(1).getDispatchTime());
        assertEquals("已完成", result.getItems().get(2).getStatus());
        assertEquals("待验收", result.getItems().get(3).getStatus());
        assertEquals("已派发", result.getItems().get(4).getStatus());
        assertEquals("", result.getItems().get(4).getRepairPerson());
        assertEquals("", result.getItems().get(4).getEstimatedFinish());
        assertNull(result.getItems().get(4).getActualFinish());
    }
}
