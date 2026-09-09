package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.SpecialOperationDetail;
import com.sinopec.mmsecurity.dto.SpecialOperationGasPoint;
import com.sinopec.mmsecurity.dto.SpecialOperationItem;
import com.sinopec.mmsecurity.dto.SpecialOperationPage;
import com.sinopec.mmsecurity.dto.SpecialOperationPersonItem;
import com.sinopec.mmsecurity.dto.SpecialOperationVideoItem;
import com.sinopec.mmsecurity.service.SpecialOperationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 特殊作业大屏接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class SpecialOperationControllerTest {

    @Mock
    private SpecialOperationService service;

    @InjectMocks
    private SpecialOperationController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void list_returnsPagedTickets() throws Exception {
        SpecialOperationPage page = new SpecialOperationPage();
        page.setTotal(12L);
        page.setPage(1);
        page.setSize(10);
        page.setPages(2);
        SpecialOperationItem item = new SpecialOperationItem();
        item.setId(1L);
        item.setArea("重油加氢装置");
        item.setType("动火作业");
        item.setLevel("二级");
        item.setStatus("已签发");
        item.setPermitNo("20260601150001321.pdf");
        page.setList(List.of(item));
        when(service.list(1, 10, null, null, null, null)).thenReturn(page);

        mvc().perform(get("/api/v1/special-operations").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(12))
                .andExpect(jsonPath("$.data.list[0].type").value("动火作业"));
        verify(service).list(1, 10, null, null, null, null);
    }

    @Test
    void list_passesFiltersVerbatim() throws Exception {
        SpecialOperationPage page = new SpecialOperationPage();
        page.setTotal(0L);
        page.setPage(1);
        page.setSize(10);
        page.setPages(0);
        page.setList(List.of());
        when(service.list(1, 10, "全部类型", "罐区", "一级", "进行中")).thenReturn(page);

        mvc().perform(get("/api/v1/special-operations")
                        .param("type", "全部类型").param("area", "罐区")
                        .param("level", "一级").param("status", "进行中"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).list(1, 10, "全部类型", "罐区", "一级", "进行中");
    }

    @Test
    void detail_returnsChildren() throws Exception {
        SpecialOperationDetail detail = new SpecialOperationDetail();
        detail.setId(1L);
        detail.setVideos(List.of(new SpecialOperationVideoItem()));
        SpecialOperationGasPoint gas = new SpecialOperationGasPoint();
        gas.setName("可燃气体");
        gas.setValue("0.2%LEL");
        gas.setStatus("正常");
        detail.setGasPoints(List.of(gas));
        SpecialOperationPersonItem person = new SpecialOperationPersonItem();
        person.setName("阮国述");
        person.setRole("施工人员");
        detail.setPersonnel(List.of(person));
        when(service.detail(1L)).thenReturn(detail);

        mvc().perform(get("/api/v1/special-operations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.gasPoints[0].value").value("0.2%LEL"))
                .andExpect(jsonPath("$.data.personnel[0].role").value("施工人员"));
    }

    @Test
    void detail_returnsNotFoundWhenMissing() throws Exception {
        when(service.detail(999L)).thenReturn(null);

        mvc().perform(get("/api/v1/special-operations/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("作业票不存在：999"));
    }
}
