package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.FireFacilityAlarmResult;
import com.sinopec.mmsecurity.dto.FireFacilityFaultCreateRequest;
import com.sinopec.mmsecurity.dto.FireFacilityFaultItem;
import com.sinopec.mmsecurity.dto.FireFacilityFaultResult;
import com.sinopec.mmsecurity.dto.FireFacilityFaultTimelineCreate;
import com.sinopec.mmsecurity.dto.FireFacilityFaultUpdateRequest;
import com.sinopec.mmsecurity.dto.FireFacilityLedgerItem;
import com.sinopec.mmsecurity.dto.FireFacilityLedgerResult;
import com.sinopec.mmsecurity.dto.FireFacilityLedgerWriteRequest;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    @Mock
    private EmergencyService emergencyService;

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

    private static FacFireFacilityParam param(Long monitorId, String keyCode, String label, String value, String tone) {
        FacFireFacilityParam e = new FacFireFacilityParam();
        e.setMonitorId(monitorId);
        e.setKeyCode(keyCode);
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
                param(2L, "water", "水泵运行", "运行", "normal"),
                param(2L, "water", "水位", "32%", "warning")));
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
        when(paramMapper.selectList(any())).thenReturn(List.of(param(1L, "fas", "运行状态", "报警", "danger")));
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

    /* ==================== 写回 updateFault ==================== */

    @Test
    void updateFault_statusOnly_persistsAndReturnsItem() {
        when(faultMapper.selectById(1L)).thenReturn(fault(1L, "FLT-1", "硬件故障", "紧急", "待确认", null));
        when(timelineMapper.selectList(any())).thenReturn(List.of());
        FireFacilityFaultUpdateRequest req = new FireFacilityFaultUpdateRequest();
        req.setFaultStatus("已确认");

        FireFacilityFaultItem item = service.updateFault("1", req);

        ArgumentCaptor<FacFireFacilityFault> captor = ArgumentCaptor.forClass(FacFireFacilityFault.class);
        verify(faultMapper).updateById(captor.capture());
        assertEquals("已确认", captor.getValue().getFaultStatus());
        assertEquals("已确认", item.getStatus());
        assertEquals("FLT-1", item.getFaultCode());
    }

    @Test
    void updateFault_fieldsAndTimeline_appendAndPersist() {
        when(faultMapper.selectById(2L)).thenReturn(fault(2L, "FLT-2", "硬件故障", "紧急", "已确认", null));
        when(timelineMapper.selectList(any())).thenReturn(List.of(timeline(2L, "发现故障", "2026-08-19 08:30:05")));
        FireFacilityFaultUpdateRequest req = new FireFacilityFaultUpdateRequest();
        req.setFaultStatus("已派单");
        req.setWorkOrderNo("WO-20260922-001");
        req.setRepairPerson("李维修");
        req.setEstimatedFinish("2026-09-24 18:00:00");
        FireFacilityFaultTimelineCreate tc = new FireFacilityFaultTimelineCreate();
        tc.setTime("2026-09-22 10:00:00");
        tc.setOperator("值班员");
        tc.setAction("生成工单并派发");
        tc.setDetail("派发至 李维修");
        req.setTimelines(List.of(tc));

        service.updateFault("2", req);

        ArgumentCaptor<FacFireFacilityFault> captor = ArgumentCaptor.forClass(FacFireFacilityFault.class);
        verify(faultMapper).updateById(captor.capture());
        assertEquals("已派单", captor.getValue().getFaultStatus());
        assertEquals("WO-20260922-001", captor.getValue().getWorkOrderNo());
        assertEquals("李维修", captor.getValue().getRepairPerson());
        assertEquals("2026-09-24 18:00:00", captor.getValue().getEstimatedFinish());

        ArgumentCaptor<FacFireFacilityFaultTimeline> tcap =
                ArgumentCaptor.forClass(FacFireFacilityFaultTimeline.class);
        verify(timelineMapper).insert(tcap.capture());
        assertEquals(2L, tcap.getValue().getFaultId());
        assertEquals(2, tcap.getValue().getSortNo());
        assertEquals("生成工单并派发", tcap.getValue().getActionName());
        assertEquals("值班员", tcap.getValue().getOperatorName());
        assertEquals("2026-09-22 10:00:00", tcap.getValue().getEventTime());
    }

    @Test
    void updateFault_nullFields_keepExistingValues() {
        FacFireFacilityFault e = fault(3L, "FLT-3", "硬件故障", "紧急", "已确认", "WO-EXIST");
        e.setRepairPerson("老维修");
        when(faultMapper.selectById(3L)).thenReturn(e);
        when(timelineMapper.selectList(any())).thenReturn(List.of());
        FireFacilityFaultUpdateRequest req = new FireFacilityFaultUpdateRequest();
        req.setFaultStatus("已派单");

        service.updateFault("3", req);

        ArgumentCaptor<FacFireFacilityFault> captor = ArgumentCaptor.forClass(FacFireFacilityFault.class);
        verify(faultMapper).updateById(captor.capture());
        assertEquals("已派单", captor.getValue().getFaultStatus());
        assertEquals("WO-EXIST", captor.getValue().getWorkOrderNo());
        assertEquals("老维修", captor.getValue().getRepairPerson());
    }

    @Test
    void updateFault_invalidStatus_throwsParamInvalid() {
        when(faultMapper.selectById(4L)).thenReturn(fault(4L, "FLT-4", "硬件故障", "紧急", "待确认", null));
        FireFacilityFaultUpdateRequest req = new FireFacilityFaultUpdateRequest();
        req.setFaultStatus("不存在的状态");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateFault("4", req));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
        verify(faultMapper, never()).updateById(any());
    }

    @Test
    void updateFault_nonNumericId_throwsParamInvalid() {
        FireFacilityFaultUpdateRequest req = new FireFacilityFaultUpdateRequest();
        req.setFaultStatus("已确认");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateFault("abc", req));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
        verify(faultMapper, never()).updateById(any());
    }

    @Test
    void updateFault_notFound_throwsNotFound() {
        when(faultMapper.selectById(999L)).thenReturn(null);
        FireFacilityFaultUpdateRequest req = new FireFacilityFaultUpdateRequest();
        req.setFaultStatus("已确认");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateFault("999", req));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(faultMapper, never()).updateById(any());
    }

    /* ==================== 报警处置 updateAlarm ==================== */

    @Test
    void updateAlarm_resolvesByAlarmIdAndPersists() {
        // AL-7 → 余串 7 → REPLACE(REPLACE(fault_code,'FLT-',''),'-','')=7 命中 faultCode=FLT-7
        when(faultMapper.selectOne(any())).thenReturn(fault(7L, "FLT-7", "硬件故障", "紧急", "待确认", null));
        when(timelineMapper.selectList(any())).thenReturn(List.of());
        FireFacilityFaultUpdateRequest req = new FireFacilityFaultUpdateRequest();
        req.setFaultStatus("已确认");
        FireFacilityFaultTimelineCreate tc = new FireFacilityFaultTimelineCreate();
        tc.setTime("2026-10-04 10:00:00");
        tc.setOperator("值班员");
        tc.setAction("确认故障");
        tc.setDetail("确认为故障，待派单");
        req.setTimelines(List.of(tc));

        FireFacilityFaultItem item = service.updateAlarm("AL-7", req);

        ArgumentCaptor<FacFireFacilityFault> captor = ArgumentCaptor.forClass(FacFireFacilityFault.class);
        verify(faultMapper).updateById(captor.capture());
        assertEquals("已确认", captor.getValue().getFaultStatus());
        verify(timelineMapper).insert(any());
        assertEquals("FLT-7", item.getFaultCode());
        assertEquals("已确认", item.getStatus());
    }

    @Test
    void updateAlarm_notFound_throwsNotFound() {
        when(faultMapper.selectOne(any())).thenReturn(null);
        FireFacilityFaultUpdateRequest req = new FireFacilityFaultUpdateRequest();
        req.setFaultStatus("已确认");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateAlarm("AL-999", req));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(faultMapper, never()).updateById(any());
    }

    @Test
    void updateAlarm_invalidId_throwsParamInvalid() {
        FireFacilityFaultUpdateRequest req = new FireFacilityFaultUpdateRequest();
        req.setFaultStatus("已确认");

        // 去掉 AL- 后余串不含字母/数字/下划线 → 无可用反查键，直接参数非法
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateAlarm("AL-###", req));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
        verify(faultMapper, never()).updateById(any());
    }

    @Test
    void updateAlarm_alphanumericFaultCode_resolves() {
        // 真实种子编号形如 F-20260317-001：派生出的报警 id 含字母，反查不能只取数字（否则与库侧永不匹配）
        when(faultMapper.selectOne(any()))
                .thenReturn(fault(8L, "F-20260317-001", "硬件故障", "紧急", "待确认", null));
        when(timelineMapper.selectList(any())).thenReturn(List.of());
        FireFacilityFaultUpdateRequest req = new FireFacilityFaultUpdateRequest();
        req.setFaultStatus("已确认");

        FireFacilityFaultItem item = service.updateAlarm("AL-F20260317001", req);

        assertEquals("F-20260317-001", item.getFaultCode());
        assertEquals("已确认", item.getStatus());
        verify(faultMapper).updateById(any());
    }

    /* ==================== 新增 createFault / 删除 deleteFault ==================== */

    private static FireFacilityFaultCreateRequest createReq(String faultCode, String level) {
        FireFacilityFaultCreateRequest req = new FireFacilityFaultCreateRequest();
        req.setFaultCode(faultCode);
        req.setFacilityCode("XF-002");
        req.setFacilityName("消火栓系统-2#罐区");
        req.setFacilityType("消火栓系统");
        req.setFaultType("硬件故障");
        req.setFaultLevel(level);
        req.setDiscoverTime("2026-10-01 09:15:00");
        // 以下两列在 fac_fire_facility_fault 为 NOT NULL 且无默认值，service 已显式校验，构造请求须带上
        req.setDiscoverMethod("巡检发现");
        req.setPhenomenon("末端试水无压，压力表持续下降");
        return req;
    }

    @Test
    void createFault_valid_persistsDefaultsPendingAndAppendsSortNo() {
        when(faultMapper.selectCount(any())).thenReturn(0L);
        when(faultMapper.selectList(any())).thenReturn(List.of(fault(1L, "FLT-1", "硬件故障", "紧急", "待确认", null)));

        FireFacilityFaultItem item = service.createFault(createReq("FLT-2026-0001", "紧急"));

        ArgumentCaptor<FacFireFacilityFault> captor = ArgumentCaptor.forClass(FacFireFacilityFault.class);
        verify(faultMapper).insert(captor.capture());
        assertEquals("FLT-2026-0001", captor.getValue().getFaultCode());
        assertEquals("紧急", captor.getValue().getFaultLevel());
        // 未传状态时默认「待确认」，且 sort_no 接续现有最大值（既有 1 → 新 2）
        assertEquals("待确认", captor.getValue().getFaultStatus());
        assertEquals(2, captor.getValue().getSortNo());
        assertEquals("待确认", item.getStatus());
        assertTrue(item.getTimeline() == null || item.getTimeline().isEmpty());
    }

    @Test
    void createFault_missingFaultCode_throwsParamInvalid() {
        FireFacilityFaultCreateRequest req = createReq("FLT-2026-0002", "紧急");
        req.setFaultCode("  ");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createFault(req));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
        verify(faultMapper, never()).insert(any());
    }

    @Test
    void createFault_duplicateCode_throwsConflict() {
        when(faultMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(
                BusinessException.class, () -> service.createFault(createReq("FLT-2026-0001", "紧急")));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
        verify(faultMapper, never()).insert(any());
    }

    @Test
    void createFault_invalidLevel_throwsParamInvalid() {
        BusinessException ex = assertThrows(
                BusinessException.class, () -> service.createFault(createReq("FLT-2026-0003", "超紧急")));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
        verify(faultMapper, never()).insert(any());
    }

    @Test
    void createFault_nullTextColumns_normalizedToEmptyInsteadOfConflict() {
        // discover_method / phenomenon / facility_name / facility_type 均为 NOT NULL 且无默认值：
        // 漏传时过去会落到数据库约束异常、被笼统映射为 CONFLICT（「数据冲突：请检查唯一键或必填字段」），
        // 调用方无法定位缺哪个字段（实测漏 discoverMethod 即此症状）。现应归一为空串后正常落库。
        when(faultMapper.selectCount(any())).thenReturn(0L);
        when(faultMapper.selectList(any())).thenReturn(List.of());
        FireFacilityFaultCreateRequest req = createReq("FLT-2026-0004", "紧急");
        req.setDiscoverMethod(null);
        req.setPhenomenon(null);
        req.setFacilityName(null);
        req.setFacilityType(null);

        service.createFault(req);

        ArgumentCaptor<FacFireFacilityFault> captor = ArgumentCaptor.forClass(FacFireFacilityFault.class);
        verify(faultMapper).insert(captor.capture());
        assertEquals("", captor.getValue().getDiscoverMethod());
        assertEquals("", captor.getValue().getPhenomenon());
        assertEquals("", captor.getValue().getFacilityName());
        assertEquals("", captor.getValue().getFacilityType());
    }

    @Test
    void deleteFault_existing_cascadesTimelineThenDeletes() {
        when(faultMapper.selectById(7L)).thenReturn(fault(7L, "FLT-7", "硬件故障", "一般", "已闭环", "WO-1"));

        service.deleteFault("7");

        verify(timelineMapper).delete(any());
        verify(faultMapper).deleteById(7L);
    }

    @Test
    void deleteFault_notFound_throwsNotFound() {
        when(faultMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteFault("999"));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(faultMapper, never()).deleteById(any());
    }

    /* ==================== 台账 createLedger / updateLedger / deleteLedger ==================== */

    private static FacFireFacilityLedger ledger(Long id, String code, String name, int sortNo) {
        FacFireFacilityLedger e = new FacFireFacilityLedger();
        e.setId(id);
        e.setFacilityCode(code);
        e.setFacilityName(name);
        e.setFacilityType("消防水泵");
        e.setLocationName("炼油一部泵房");
        e.setDeviceName("XBD8/30-150L");
        e.setMaintainerName("张伟");
        e.setMaintainerPhone("13800000001");
        e.setEnabledFlag(true);
        e.setSortNo(sortNo);
        return e;
    }

    private static FireFacilityLedgerWriteRequest ledgerWriteReq(String code, String name) {
        FireFacilityLedgerWriteRequest req = new FireFacilityLedgerWriteRequest();
        req.setFacilityCode(code);
        req.setFacilityName(name);
        req.setFacilityType("消防水泵");
        req.setLocation("炼油三部泵房");
        req.setDevice("XBD8/30-200L");
        req.setMaintainerName("王芳");
        req.setMaintainerPhone("13800000099");
        req.setEnabled(true);
        return req;
    }

    @Test
    void createLedger_valid_persistsAndAppendsSortNo() {
        when(ledgerMapper.selectCount(any())).thenReturn(0L);
        when(ledgerMapper.selectList(null)).thenReturn(List.of(ledger(1L, "FP-001", "1#消防水泵", 1)));

        FireFacilityLedgerItem item = service.createLedger(ledgerWriteReq("FP-099", "99#消防水泵"));

        ArgumentCaptor<FacFireFacilityLedger> captor = ArgumentCaptor.forClass(FacFireFacilityLedger.class);
        verify(ledgerMapper).insert(captor.capture());
        assertEquals("FP-099", captor.getValue().getFacilityCode());
        assertEquals("炼油三部泵房", captor.getValue().getLocationName());
        assertEquals("XBD8/30-200L", captor.getValue().getDeviceName());
        assertEquals(Boolean.TRUE, captor.getValue().getEnabledFlag());
        // sort_no 接续现有最大值（既有 1 → 新 2）
        assertEquals(2, captor.getValue().getSortNo());
        // id 由 DB 自增赋值（纯 Mockito 下 insert 不回填，仅校验映射字段完整）
        assertEquals("FP-099", item.getFacilityCode());
        assertEquals("炼油三部泵房", item.getLocation());
        assertEquals(Boolean.TRUE, item.getEnabled());
    }

    @Test
    void createLedger_missingFacilityCode_throwsParamInvalid() {
        FireFacilityLedgerWriteRequest req = ledgerWriteReq("  ", "99#消防水泵");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createLedger(req));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
        verify(ledgerMapper, never()).insert(any());
    }

    @Test
    void createLedger_duplicateCode_throwsConflict() {
        when(ledgerMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(
                BusinessException.class, () -> service.createLedger(ledgerWriteReq("FP-001", "1#消防水泵")));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
        verify(ledgerMapper, never()).insert(any());
    }

    @Test
    void updateLedger_nullFields_keepExistingValues() {
        FacFireFacilityLedger e = ledger(3L, "FP-003", "3#消防水泵", 3);
        e.setMaintainerName("老维保");
        when(ledgerMapper.selectById(3L)).thenReturn(e);
        FireFacilityLedgerWriteRequest req = new FireFacilityLedgerWriteRequest();
        req.setMaintainerPhone("13800000003");

        FireFacilityLedgerItem item = service.updateLedger(3L, req);

        ArgumentCaptor<FacFireFacilityLedger> captor = ArgumentCaptor.forClass(FacFireFacilityLedger.class);
        verify(ledgerMapper).updateById(captor.capture());
        assertEquals("13800000003", captor.getValue().getMaintainerPhone());
        assertEquals("老维保", captor.getValue().getMaintainerName());
        assertEquals("FP-003", captor.getValue().getFacilityCode());
        assertEquals("13800000003", item.getMaintainerPhone());
    }

    @Test
    void updateLedger_notFound_throwsNotFound() {
        when(ledgerMapper.selectById(999L)).thenReturn(null);
        FireFacilityLedgerWriteRequest req = new FireFacilityLedgerWriteRequest();
        req.setFacilityName("x");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateLedger(999L, req));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(ledgerMapper, never()).updateById(any());
    }

    @Test
    void deleteLedger_existing_cascadesMaintenanceThenDeletes() {
        when(ledgerMapper.selectById(5L)).thenReturn(ledger(5L, "FP-005", "5#消防水泵", 5));

        service.deleteLedger(5L);

        verify(maintenanceMapper).delete(any());
        verify(ledgerMapper).deleteById(5L);
    }

    @Test
    void deleteLedger_notFound_throwsNotFound() {
        when(ledgerMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteLedger(999L));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(ledgerMapper, never()).deleteById(any());
    }
}
