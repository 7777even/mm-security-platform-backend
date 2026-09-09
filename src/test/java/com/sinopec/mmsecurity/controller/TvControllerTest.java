package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.TvEventBreakdownItem;
import com.sinopec.mmsecurity.dto.TvInspectionItem;
import com.sinopec.mmsecurity.dto.TvInspectionSummary;
import com.sinopec.mmsecurity.dto.TvOperationStats;
import com.sinopec.mmsecurity.dto.TvOverview;
import com.sinopec.mmsecurity.dto.TvOverviewItem;
import com.sinopec.mmsecurity.service.TvService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
