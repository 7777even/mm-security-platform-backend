package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.MsdsDetail;
import com.sinopec.mmsecurity.dto.MsdsItem;
import com.sinopec.mmsecurity.dto.MsdsList;
import com.sinopec.mmsecurity.service.MsdsService;
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

/** 化学品 MSDS 接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class MsdsControllerTest {

    @Mock
    private MsdsService msdsService;

    @InjectMocks
    private MsdsController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void list_returnsItemsAndTotal() throws Exception {
        MsdsItem item = new MsdsItem();
        item.setId(1L);
        item.setName("乙烯（Ethylene）");
        item.setCas("74-85-1");
        item.setClassification("易燃气体 类别1");
        MsdsList list = new MsdsList();
        list.setItems(List.of(item));
        list.setTotal(1);
        when(msdsService.list()).thenReturn(list);

        mvc().perform(get("/api/v1/msds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("乙烯（Ethylene）"))
                .andExpect(jsonPath("$.data.items[0].cas").value("74-85-1"));
    }

    @Test
    void detail_byCas_returnsDetail() throws Exception {
        MsdsDetail d = new MsdsDetail();
        d.setId(1L);
        d.setName("乙烯（Ethylene）");
        d.setCas("74-85-1");
        d.setClassification("易燃气体 类别1");
        d.setState("气体（液化）");
        d.setBoilingPoint("-103.7℃");
        d.setFlashPoint("—");
        d.setExplosionLimit("2.7%-36%");
        d.setStorage("阴凉通风，远离火源热源");
        d.setSafety("禁火区域作业、接地防静电");
        d.setEmergency("切断泄漏源，喷雾稀释，下风向疏散");
        when(msdsService.detail("74-85-1")).thenReturn(d);

        mvc().perform(get("/api/v1/msds/74-85-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.cas").value("74-85-1"))
                .andExpect(jsonPath("$.data.boilingPoint").value("-103.7℃"))
                .andExpect(jsonPath("$.data.explosionLimit").value("2.7%-36%"))
                .andExpect(jsonPath("$.data.safety").value("禁火区域作业、接地防静电"));
    }
}
