package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.DrillDetail;
import com.sinopec.mmsecurity.dto.DrillItem;
import com.sinopec.mmsecurity.dto.DrillList;
import com.sinopec.mmsecurity.dto.DrillTaskItem;
import com.sinopec.mmsecurity.service.DrillService;
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

/** 应急演练接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class DrillControllerTest {

    @Mock
    private DrillService drillService;

    @InjectMocks
    private DrillController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void list_returnsItemsTotalAndTaskCount() throws Exception {
        DrillItem item = new DrillItem();
        item.setId(1L);
        item.setDrillCode("YL-008");
        item.setName("储运部液化烃储罐泄漏实战演练");
        item.setDrillType("实战演练");
        item.setForm("现场演练");
        item.setTimeRange("2026-08-18 09:30-11:30");
        item.setPlace("储运部 T-301 罐区");
        item.setStatus("进行中");
        item.setDepartments("应急救援中心、储运部、消防大队、安环部");
        item.setTaskCount(2);
        DrillList list = new DrillList();
        list.setItems(List.of(item));
        list.setTotal(1);
        when(drillService.list()).thenReturn(list);

        mvc().perform(get("/api/v1/drills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].drillCode").value("YL-008"))
                .andExpect(jsonPath("$.data.items[0].status").value("进行中"))
                .andExpect(jsonPath("$.data.items[0].taskCount").value(2));
    }

    @Test
    void detail_returnsDetailWithTasks() throws Exception {
        DrillTaskItem t = new DrillTaskItem();
        t.setName("任务 1 · 现场泄漏点确认与上报");
        t.setStatus("待确认");
        DrillDetail detail = new DrillDetail();
        detail.setId(1L);
        detail.setDrillCode("YL-008");
        detail.setName("储运部液化烃储罐泄漏实战演练");
        detail.setDrillType("实战演练");
        detail.setForm("现场演练");
        detail.setTimeRange("2026-08-18 09:30-11:30");
        detail.setPlace("储运部 T-301 罐区");
        detail.setStatus("进行中");
        detail.setDepartments("应急救援中心、储运部、消防大队、安环部");
        detail.setTasks(List.of(t));
        when(drillService.detail(1L)).thenReturn(detail);

        mvc().perform(get("/api/v1/drills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.drillCode").value("YL-008"))
                .andExpect(jsonPath("$.data.place").value("储运部 T-301 罐区"))
                .andExpect(jsonPath("$.data.tasks[0].name").value("任务 1 · 现场泄漏点确认与上报"))
                .andExpect(jsonPath("$.data.tasks[0].status").value("待确认"));
    }
}
