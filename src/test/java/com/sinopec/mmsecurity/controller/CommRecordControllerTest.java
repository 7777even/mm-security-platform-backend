package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.CommunicationRecord;
import com.sinopec.mmsecurity.dto.CommunicationRecordList;
import com.sinopec.mmsecurity.service.CommRecordService;
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

/** 通讯通知记录接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class CommRecordControllerTest {

    @Mock
    private CommRecordService commRecordService;

    @InjectMocks
    private CommRecordController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static CommunicationRecord sample() {
        CommunicationRecord item = new CommunicationRecord();
        item.setRecordNo("SMS-091");
        item.setRecordType("sms");
        item.setOccurredAt("2026-08-21 09:03");
        item.setCategory("告警通知");
        item.setSender("系统");
        item.setReceiver("138****2211");
        item.setSummary("T-301 感温报警，请立即核实");
        item.setResult("成功");
        item.setDuration("");
        item.setChannel("短信网关");
        item.setDirection("下发");
        item.setContentType("文本");
        return item;
    }

    @Test
    void list_returnsItemsAndTotal() throws Exception {
        CommunicationRecordList list = new CommunicationRecordList();
        list.setItems(List.of(sample()));
        list.setTotal(1);
        when(commRecordService.list(null)).thenReturn(list);

        mvc().perform(get("/api/v1/communication/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].recordNo").value("SMS-091"))
                .andExpect(jsonPath("$.data.items[0].receiver").value("138****2211"))
                .andExpect(jsonPath("$.data.items[0].result").value("成功"));
    }

    @Test
    void list_withType_returnsFilteredItems() throws Exception {
        CommunicationRecordList list = new CommunicationRecordList();
        list.setItems(List.of(sample()));
        list.setTotal(1);
        when(commRecordService.list("sms")).thenReturn(list);

        mvc().perform(get("/api/v1/communication/records").param("type", "sms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].recordType").value("sms"))
                .andExpect(jsonPath("$.data.items[0].category").value("告警通知"));
    }
}
