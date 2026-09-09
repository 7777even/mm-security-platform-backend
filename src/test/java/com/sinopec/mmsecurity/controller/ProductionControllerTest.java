package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.OverviewGridItem;
import com.sinopec.mmsecurity.dto.PersonnelMarker;
import com.sinopec.mmsecurity.dto.ProductionAlarmItem;
import com.sinopec.mmsecurity.dto.ProductionAreaDetail;
import com.sinopec.mmsecurity.dto.ProductionDeviceItem;
import com.sinopec.mmsecurity.dto.ProductionDevicePage;
import com.sinopec.mmsecurity.dto.ProductionOverview;
import com.sinopec.mmsecurity.dto.RiskSummary;
import com.sinopec.mmsecurity.dto.RiskWarningItem;
import com.sinopec.mmsecurity.dto.StatOverviewItem;
import com.sinopec.mmsecurity.service.ProductionService;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 生产应急大屏接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class ProductionControllerTest {

    @Mock
    private ProductionService service;

    @InjectMocks
    private ProductionController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void overview_returnsFourBlocks() throws Exception {
        ProductionOverview ov = new ProductionOverview();
        OverviewGridItem facility = new OverviewGridItem();
        facility.setId(2L);
        facility.setName("生产装置");
        facility.setCount(596);
        facility.setImage("image_0008.png");
        ov.setFacilities(List.of(facility));
        StatOverviewItem stat = new StatOverviewItem();
        stat.setId(1L);
        stat.setLabel("报警总数");
        stat.setValue("36");
        stat.setUnit("起");
        stat.setIconIndex(0);
        ov.setStats(List.of(stat));
        RiskSummary summary = new RiskSummary();
        summary.setRed(2);
        summary.setOrange(3);
        summary.setYellow(2);
        ov.setRiskSummary(summary);
        when(service.overview()).thenReturn(ov);

        mvc().perform(get("/api/v1/production/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.facilities[0].name").value("生产装置"))
                .andExpect(jsonPath("$.data.facilities[0].count").value(596))
                .andExpect(jsonPath("$.data.stats[0].unit").value("起"))
                .andExpect(jsonPath("$.data.riskSummary.red").value(2));
    }

    @Test
    void alarms_returnsAllWhenNoFacility() throws Exception {
        ProductionAlarmItem item = new ProductionAlarmItem();
        item.setId(1L);
        item.setTitle("人员跌倒");
        item.setTitleColor("warning");
        item.setLocation("化工区乙烯装置东侧");
        item.setTime("2026-03-17 14:21:30");
        item.setStatus("未处置");
        item.setIconIndex(0);
        item.setThumb("person_fall.png");
        when(service.alarms(isNull())).thenReturn(List.of(item));

        mvc().perform(get("/api/v1/production/alarms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].title").value("人员跌倒"))
                .andExpect(jsonPath("$.data[0].time").value("2026-03-17 14:21:30"));
    }

    @Test
    void alarms_withFacilityId_passesParam() throws Exception {
        when(service.alarms(2L)).thenReturn(List.of());

        mvc().perform(get("/api/v1/production/alarms").param("facilityId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).alarms(2L);
    }

    @Test
    void riskWarnings_returnsLevels() throws Exception {
        RiskWarningItem item = new RiskWarningItem();
        item.setId(1L);
        item.setLocation("乙烯装置区（二）");
        item.setType("高温预警");
        item.setTime("2026-03-17 02:00:46");
        item.setPerson("王立军");
        item.setPhone("1380255****");
        item.setLevel("red");
        item.setLevelLabel("红色");
        when(service.riskWarnings()).thenReturn(List.of(item));

        mvc().perform(get("/api/v1/production/risk-warnings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].level").value("red"))
                .andExpect(jsonPath("$.data[0].levelLabel").value("红色"));
    }

    @Test
    void personnel_returnsMarkers() throws Exception {
        PersonnelMarker marker = new PersonnelMarker();
        marker.setId(1L);
        marker.setLeft("54.2%");
        marker.setTop("25.3%");
        marker.setLongitude(110.8836);
        marker.setLatitude(21.6838);
        marker.setLocation("炼化厂区丙侧");
        marker.setCount(365);
        marker.setMarkerDot("#3ec6ff");
        when(service.personnel()).thenReturn(List.of(marker));

        mvc().perform(get("/api/v1/production/personnel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].left").value("54.2%"))
                .andExpect(jsonPath("$.data[0].count").value(365));
    }

    @Test
    void areaDetail_returnsAggregate() throws Exception {
        ProductionAreaDetail detail = new ProductionAreaDetail();
        detail.setFacilityId(2L);
        detail.setFacilityName("生产装置");
        detail.setPersonnelTotal(37);
        when(service.areaDetail(2L)).thenReturn(detail);

        mvc().perform(get("/api/v1/production/areas/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.facilityId").value(2))
                .andExpect(jsonPath("$.data.facilityName").value("生产装置"))
                .andExpect(jsonPath("$.data.personnelTotal").value(37));
    }

    @Test
    void areaDetail_whenMissing_returnsNotFoundCode() throws Exception {
        when(service.areaDetail(any())).thenReturn(null);

        mvc().perform(get("/api/v1/production/areas/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void devices_returnsPageWithFilters() throws Exception {
        ProductionDevicePage page = new ProductionDevicePage();
        page.setPage(1);
        page.setSize(10);
        page.setTotal(35L);
        ProductionDeviceItem item = new ProductionDeviceItem();
        item.setId(1L);
        item.setName("催化裂解监测1#");
        item.setType("气体监测");
        item.setCategory("监测点");
        item.setArea("炼油区");
        item.setStatus("正常");
        page.setItems(List.of(item));
        when(service.devices("监测点", "正常", 1, 10)).thenReturn(page);

        mvc().perform(get("/api/v1/production/devices")
                        .param("category", "监测点")
                        .param("status", "正常")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(35))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.items[0].category").value("监测点"))
                .andExpect(jsonPath("$.data.items[0].status").value("正常"));
    }

    @Test
    void devices_withoutParams_usesServiceDefaults() throws Exception {
        ProductionDevicePage page = new ProductionDevicePage();
        page.setPage(1);
        page.setSize(10);
        page.setTotal(35L);
        when(service.devices(isNull(), isNull(), isNull(), isNull())).thenReturn(page);

        mvc().perform(get("/api/v1/production/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(35));
        verify(service).devices(isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void devices_allStatusIsPassedThrough() throws Exception {
        when(service.devices(isNull(), eq("全部状态"), isNull(), isNull()))
                .thenReturn(new ProductionDevicePage());

        mvc().perform(get("/api/v1/production/devices").param("status", "全部状态"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).devices(isNull(), eq("全部状态"), isNull(), isNull());
    }
}
