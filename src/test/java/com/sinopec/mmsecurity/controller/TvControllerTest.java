package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.TvEventBreakdownItem;
import com.sinopec.mmsecurity.dto.TvInspectionItem;
import com.sinopec.mmsecurity.dto.TvInspectionSummary;
import com.sinopec.mmsecurity.dto.TvMonitorSummary;
import com.sinopec.mmsecurity.dto.TvOperationStats;
import com.sinopec.mmsecurity.dto.TvOverview;
import com.sinopec.mmsecurity.dto.TvMapPoint;
import com.sinopec.mmsecurity.dto.TvMonitorDetail;
import com.sinopec.mmsecurity.dto.TvOverviewItem;
import com.sinopec.mmsecurity.dto.TvSnapshotAckResult;
import com.sinopec.mmsecurity.dto.TvSnapshotIngestResult;
import com.sinopec.mmsecurity.dto.TvSnapshotPage;
import com.sinopec.mmsecurity.service.TvService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 工业电视大屏接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class TvControllerTest {

    @Mock
    private TvService service;

    @InjectMocks
    private TvController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void overview_returnsAggregatedBlocks() throws Exception {
        TvOverview overview = new TvOverview();
        TvOverviewItem item = new TvOverviewItem();
        item.setId(1L);
        item.setLabel("重大危险源");
        item.setValue(665);
        item.setIconIndex(0);
        overview.setOverviewItems(List.of(item));
        TvOperationStats stats = new TvOperationStats();
        stats.setTotal(1233);
        stats.setOffline(23);
        stats.setFault(23);
        stats.setIntegrityRate(98);
        stats.setOnlineRate(98);
        stats.setEventTotal(110);
        overview.setOperationStats(stats);
        TvEventBreakdownItem event = new TvEventBreakdownItem();
        event.setLabel("区域入侵");
        event.setValue(152);
        event.setColor("#f0b429");
        overview.setEventBreakdown(List.of(event));
        when(service.overview()).thenReturn(overview);

        mvc().perform(get("/api/v1/tv/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.overviewItems[0].value").value(665))
                .andExpect(jsonPath("$.data.operationStats.total").value(1233))
                .andExpect(jsonPath("$.data.eventBreakdown[0].color").value("#f0b429"));
    }

    @Test
    void inspections_splitsVehiclesAndPersons() throws Exception {
        TvInspectionSummary summary = new TvInspectionSummary();
        TvInspectionItem vehicle = new TvInspectionItem();
        vehicle.setId(1L);
        vehicle.setKind("VEHICLE");
        vehicle.setAreaCode("refinery");
        vehicle.setPlate("粤KAA543");
        vehicle.setBadge("入厂");
        vehicle.setGate("3#门-入");
        vehicle.setTime("2026-03-17 10:22:23");
        summary.setVehicles(List.of(vehicle));
        TvInspectionItem person = new TvInspectionItem();
        person.setId(9L);
        person.setKind("PERSON");
        person.setAreaCode("refinery");
        person.setName("陈志强");
        person.setBadge("员工");
        person.setDepartment("炼油运行一部");
        person.setGate("3#门-入");
        person.setTime("10:21:18");
        summary.setPersons(List.of(person));
        when(service.inspections()).thenReturn(summary);

        mvc().perform(get("/api/v1/tv/inspections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.vehicles[0].plate").value("粤KAA543"))
                .andExpect(jsonPath("$.data.persons[0].name").value("陈志强"));
    }

    @Test
    void mapPoints_returnsList() throws Exception {
        when(service.tvMapPoints()).thenReturn(List.of(new TvMapPoint()));
        mvc().perform(get("/api/v1/tv/map-points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void monitor_byCode_returnsDetail() throws Exception {
        TvMonitorDetail detail = new TvMonitorDetail();
        detail.setId("C1");
        when(service.tvMonitorByCode("C1")).thenReturn(detail);
        mvc().perform(get("/api/v1/tv/monitors/C1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("C1"));
    }

    @Test
    void monitor_notFound_returns404() throws Exception {
        when(service.tvMonitorByCode("X")).thenReturn(null);
        mvc().perform(get("/api/v1/tv/monitors/X"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ---- 录像截图采集入库闭环（V78） ----

    @Test
    void submitSnapshot_returnsIngestResult() throws Exception {
        TvSnapshotIngestResult r = new TvSnapshotIngestResult();
        r.setId(1L);
        r.setMonitorCode("ar-01");
        r.setReviewStatus("PENDING");
        r.setCreatedAt("2026-09-28 17:30:01");
        when(service.submitSnapshot(any())).thenReturn(r);
        mvc().perform(post("/api/v1/tv/snapshots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monitorCode\":\"ar-01\",\"imageBase64\":\"data:image/jpeg;base64,AAAA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.reviewStatus").value("PENDING"));
    }

    @Test
    void snapshots_returnsPage() throws Exception {
        TvSnapshotPage page = new TvSnapshotPage();
        page.setTotal(1);
        page.setPage(1);
        page.setSize(12);
        page.setPages(1);
        page.setList(List.of());
        when(service.listSnapshots(1, 12, null, null, null, null, null, null)).thenReturn(page);
        mvc().perform(get("/api/v1/tv/snapshots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void ackSnapshot_returnsResult() throws Exception {
        TvSnapshotAckResult r = new TvSnapshotAckResult();
        r.setId(2L);
        r.setReviewStatus("ACKED");
        when(service.ackSnapshot(2L)).thenReturn(r);
        mvc().perform(post("/api/v1/tv/snapshots/2/ack"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.reviewStatus").value("ACKED"));
    }

    @Test
    void snapshot_withBytes_returnsJpeg() throws Exception {
        when(service.getSnapshotBytes(3L)).thenReturn(new byte[]{1, 2, 3});
        mvc().perform(get("/api/v1/tv/snapshots/3/snapshot"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void snapshot_noBytes_returns404() throws Exception {
        when(service.getSnapshotBytes(3L)).thenReturn(null);
        mvc().perform(get("/api/v1/tv/snapshots/3/snapshot"))
                .andExpect(status().isNotFound());
    }

    // ---- 监控点位管理 CRUD 端点（设备/防区管理，V87） ----

    @Test
    void monitors_returnsList() throws Exception {
        when(service.listMonitors()).thenReturn(List.of(new TvMonitorSummary()));
        mvc().perform(get("/api/v1/tv/monitors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void createMonitor_returnsSummary() throws Exception {
        TvMonitorSummary s = new TvMonitorSummary();
        s.setCode("ar-09");
        s.setName("新点位");
        when(service.createMonitor(any())).thenReturn(s);
        mvc().perform(post("/api/v1/tv/monitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monitorCode\":\"ar-09\",\"monitorName\":\"新点位\",\"zoneCode\":\"Z9\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.code").value("ar-09"));
    }

    @Test
    void updateMonitor_returnsSummary() throws Exception {
        TvMonitorSummary s = new TvMonitorSummary();
        s.setCode("ar-07");
        when(service.updateMonitor(any(), any())).thenReturn(s);
        mvc().perform(put("/api/v1/tv/monitors/ar-07")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monitorName\":\"新名\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.code").value("ar-07"));
    }

    @Test
    void deleteMonitor_returnsOk() throws Exception {
        doNothing().when(service).deleteMonitor(any());
        mvc().perform(delete("/api/v1/tv/monitors/ar-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void monitorSnapshots_returnsPage() throws Exception {
        TvSnapshotPage page = new TvSnapshotPage();
        page.setTotal(0);
        page.setPage(1);
        page.setSize(12);
        page.setPages(0);
        page.setList(List.of());
        when(service.monitorSnapshots(any(), anyInt(), anyInt(), any(), any())).thenReturn(page);
        mvc().perform(get("/api/v1/tv/monitors/ar-01/snapshots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }
}
