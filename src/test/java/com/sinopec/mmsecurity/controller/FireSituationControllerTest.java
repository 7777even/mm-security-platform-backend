package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.FireSituationMarkerItem;
import com.sinopec.mmsecurity.dto.FireSituationMarkerSummary;
import com.sinopec.mmsecurity.service.FireSituationService;
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

/** 火情态势地图点位接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class FireSituationControllerTest {

    @Mock
    private FireSituationService service;

    @InjectMocks
    private FireSituationController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void markers_returnsMarkerItems() throws Exception {
        FireSituationMarkerSummary summary = new FireSituationMarkerSummary();
        FireSituationMarkerItem event = new FireSituationMarkerItem();
        event.setId("event-1");
        event.setKind("event");
        event.setTitle("当前应急事件");
        event.setSubtitle("A装置区火灾处置中");
        event.setLongitude(110.875);
        event.setLatitude(21.6855);
        event.setImportant(true);
        event.setIconUrl("/icons/fire-situation/flame.svg");
        event.setLevel("处置中");
        event.setTargetId(1L);
        FireSituationMarkerItem alarm = new FireSituationMarkerItem();
        alarm.setId("alarm-2");
        alarm.setKind("alarm");
        alarm.setTitle("GDS报警");
        alarm.setSubtitle("输油管廊 · 未销警");
        alarm.setLongitude(110.8902);
        alarm.setLatitude(21.6752);
        alarm.setImportant(false);
        alarm.setIconUrl("/icons/fire-situation/gas.svg");
        alarm.setLevel("未销警");
        alarm.setTargetId(2L);
        summary.setItems(List.of(event, alarm));
        when(service.markers()).thenReturn(summary);

        mvc().perform(get("/api/v1/fire-situation/markers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].id").value("event-1"))
                .andExpect(jsonPath("$.data.items[0].important").value(true))
                .andExpect(jsonPath("$.data.items[0].iconUrl").value("/icons/fire-situation/flame.svg"))
                .andExpect(jsonPath("$.data.items[1].kind").value("alarm"))
                .andExpect(jsonPath("$.data.items[1].targetId").value(2));
    }

    @Test
    void markers_returnsEmptyItemsWhenNoData() throws Exception {
        FireSituationMarkerSummary summary = new FireSituationMarkerSummary();
        summary.setItems(List.of());
        when(service.markers()).thenReturn(summary);

        mvc().perform(get("/api/v1/fire-situation/markers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }
}
