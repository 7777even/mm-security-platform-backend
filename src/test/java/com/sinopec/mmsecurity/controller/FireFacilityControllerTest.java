package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.FireFacilityAlarmItem;
import com.sinopec.mmsecurity.dto.FireFacilityAlarmResult;
import com.sinopec.mmsecurity.dto.FireFacilityFaultItem;
import com.sinopec.mmsecurity.dto.FireFacilityFaultResult;
import com.sinopec.mmsecurity.dto.FireFacilityFaultTimeline;
import com.sinopec.mmsecurity.dto.FireFacilityLedgerItem;
import com.sinopec.mmsecurity.dto.FireFacilityLedgerResult;
import com.sinopec.mmsecurity.dto.FireFacilityMaintenanceRecord;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorParam;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorResult;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorSummary;
import com.sinopec.mmsecurity.dto.FireFacilityWorkOrderItem;
import com.sinopec.mmsecurity.dto.FireFacilityWorkOrderResult;
import com.sinopec.mmsecurity.service.FireFacilityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 消防设施监测大屏接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class FireFacilityControllerTest {

    @Mock
    private FireFacilityService service;

    @InjectMocks
    private FireFacilityController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void monitors_returnsTypeOptionsAndCards() throws Exception {
        FireFacilityMonitorResult result = new FireFacilityMonitorResult();
        result.setTypeOptions(List.of("全部类型", "消防水源"));
        FireFacilityMonitorParam param = new FireFacilityMonitorParam();
        param.setLabel("水位");
        param.setValue("32%");
        param.setTone("warning");
        FireFacilityMonitorSummary summary = new FireFacilityMonitorSummary();
        summary.setKey("water");
        summary.setFacilityType("消防水源");
        summary.setTotal(46);
        summary.setOnline(44);
        summary.setOffline(1);
        summary.setFault(1);
        summary.setStatus("告警");
        summary.setParams(List.of(param));
        summary.setLastReportTime("2026-08-20 10:22:40");
        result.setItems(List.of(summary));
        when(service.monitors(any())).thenReturn(result);

        mvc().perform(get("/api/v1/fire-facility/monitors").param("facilityType", "消防水源"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.typeOptions[1]").value("消防水源"))
                .andExpect(jsonPath("$.data.items[0].key").value("water"))
                .andExpect(jsonPath("$.data.items[0].total").value(46))
                .andExpect(jsonPath("$.data.items[0].params[0].value").value("32%"))
                .andExpect(jsonPath("$.data.items[0].lastReportTime").value("2026-08-20 10:22:40"));
    }

    @Test
    void ledger_returnsTypeOptionsAndMaintenanceRecords() throws Exception {
        FireFacilityLedgerResult result = new FireFacilityLedgerResult();
        result.setTypeOptions(List.of("全部类型"));
        FireFacilityMaintenanceRecord record = new FireFacilityMaintenanceRecord();
        record.setDate("2026-08-05");
        record.setContent("季度维保：控制器巡检、探测器抽测，全部正常。");
        FireFacilityLedgerItem item = new FireFacilityLedgerItem();
        item.setFacilityCode("XF-001");
        item.setFacilityName("火灾自动报警系统-1#联合装置");
        item.setFacilityType("火灾自动报警系统");
        item.setLocation("炼油一部 1#联合装置");
        item.setDevice("1#联合装置");
        item.setMaintainerName("茂名石化消防维保公司");
        item.setMaintainerPhone("0668-2110001");
        item.setEnabled(true);
        item.setMaintenanceRecords(List.of(record));
        result.setItems(List.of(item));
        when(service.ledger(any())).thenReturn(result);

        mvc().perform(get("/api/v1/fire-facility/ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].facilityCode").value("XF-001"))
                .andExpect(jsonPath("$.data.items[0].location").value("炼油一部 1#联合装置"))
                .andExpect(jsonPath("$.data.items[0].enabled").value(true))
                .andExpect(jsonPath("$.data.items[0].maintenanceRecords[0].date").value("2026-08-05"))
                .andExpect(jsonPath("$.data.items[0].maintenanceRecords[0].content")
                        .value("季度维保：控制器巡检、探测器抽测，全部正常。"));
    }

    @Test
    void faults_returnsItemsWithTimeline() throws Exception {
        FireFacilityFaultResult result = new FireFacilityFaultResult();
        FireFacilityFaultTimeline timeline = new FireFacilityFaultTimeline();
        timeline.setTime("2026-08-19 08:42:31");
        timeline.setOperator("值班员-高策");
        timeline.setAction("生成工单并派发");
        timeline.setDetail("派发至 李维修（电气车间）");
        FireFacilityFaultItem item = new FireFacilityFaultItem();
        item.setId(4L);
        item.setFaultCode("FLT-20260819-004");
        item.setFacilityCode("XF-008");
        item.setFacilityName("防烟排烟系统-常减压装置");
        item.setFacilityType("防烟排烟系统");
        item.setFaultType("硬件故障");
        item.setFaultLevel("紧急");
        item.setDiscoverTime("2026-08-19 08:30:05");
        item.setDiscoverMethod("系统告警");
        item.setPhenomenon("1#排烟风机故障停机");
        item.setCause("风机电机过载");
        item.setStatus("已派单");
        item.setWorkOrderNo("WO-20260819-001");
        item.setRepairPerson("李维修");
        item.setEstimatedFinish("2026-08-20 18:00:00");
        item.setTimeline(List.of(timeline));
        result.setItems(List.of(item));
        when(service.faults(eq("紧急"), eq("已派单"))).thenReturn(result);

        mvc().perform(get("/api/v1/fire-facility/faults")
                        .param("faultLevel", "紧急").param("faultStatus", "已派单"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].faultCode").value("FLT-20260819-004"))
                .andExpect(jsonPath("$.data.items[0].faultLevel").value("紧急"))
                .andExpect(jsonPath("$.data.items[0].cause").value("风机电机过载"))
                .andExpect(jsonPath("$.data.items[0].workOrderNo").value("WO-20260819-001"))
                .andExpect(jsonPath("$.data.items[0].timeline[0].operator").value("值班员-高策"))
                .andExpect(jsonPath("$.data.items[0].timeline[0].action").value("生成工单并派发"));
    }

    @Test
    void alarms_returnsDerivedItems() throws Exception {
        FireFacilityAlarmResult result = new FireFacilityAlarmResult();
        FireFacilityAlarmItem item = new FireFacilityAlarmItem();
        item.setId("AL-20260820001");
        item.setSource("火灾自动报警系统");
        item.setFacilityType("火灾自动报警系统");
        item.setLevel("紧急");
        item.setCategory("故障");
        item.setContent("3#装置区感烟探测器报警");
        item.setTime("2026-08-20 10:23:15");
        item.setStatus("待确认");
        item.setFaultCode("FLT-20260820-001");
        result.setItems(List.of(item));
        when(service.alarms(any(), any())).thenReturn(result);

        mvc().perform(get("/api/v1/fire-facility/alarms").param("level", "紧急"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].id").value("AL-20260820001"))
                .andExpect(jsonPath("$.data.items[0].category").value("故障"))
                .andExpect(jsonPath("$.data.items[0].level").value("紧急"))
                .andExpect(jsonPath("$.data.items[0].faultCode").value("FLT-20260820-001"));
    }

    @Test
    void workOrders_returnsDispatchTimeAndMappedStatus() throws Exception {
        FireFacilityWorkOrderResult result = new FireFacilityWorkOrderResult();
        FireFacilityWorkOrderItem item = new FireFacilityWorkOrderItem();
        item.setId(1L);
        item.setWorkOrderNo("WO-20260819-001");
        item.setFaultCode("FLT-20260819-004");
        item.setFacilityCode("XF-008");
        item.setFacilityName("防烟排烟系统-常减压装置");
        item.setFacilityType("防烟排烟系统");
        item.setFaultLevel("紧急");
        item.setDescription("1#排烟风机故障停机");
        item.setStatus("已派发");
        item.setDispatchTime("2026-08-19 08:42:31");
        item.setRepairPerson("李维修");
        item.setEstimatedFinish("2026-08-20 18:00:00");
        item.setTimeline(List.of());
        result.setItems(List.of(item));
        when(service.workOrders(any())).thenReturn(result);

        mvc().perform(get("/api/v1/fire-facility/work-orders").param("status", "已派发"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].workOrderNo").value("WO-20260819-001"))
                .andExpect(jsonPath("$.data.items[0].status").value("已派发"))
                .andExpect(jsonPath("$.data.items[0].dispatchTime").value("2026-08-19 08:42:31"))
                .andExpect(jsonPath("$.data.items[0].repairPerson").value("李维修"));
    }
}
