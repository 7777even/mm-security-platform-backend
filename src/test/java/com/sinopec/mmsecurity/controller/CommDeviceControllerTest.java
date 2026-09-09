package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.CommunicationDevice;
import com.sinopec.mmsecurity.dto.CommunicationDeviceDetail;
import com.sinopec.mmsecurity.dto.CommunicationDeviceGroups;
import com.sinopec.mmsecurity.dto.CommunicationGroup;
import com.sinopec.mmsecurity.service.CommDeviceService;
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

/** 通讯设备接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class CommDeviceControllerTest {

    @Mock
    private CommDeviceService service;

    @InjectMocks
    private CommDeviceController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void devices_returnsThreeTabsRegardlessOfTabParam() throws Exception {
        CommunicationGroup group = new CommunicationGroup();
        group.setKey("area-a");
        group.setLabel("A装置区 (6)");
        group.setDevices(List.of(device()));
        CommunicationDeviceGroups groups = new CommunicationDeviceGroups();
        groups.setBroadcast(List.of(group));
        groups.setPhone(List.of());
        groups.setIntercom(List.of());
        when(service.groups()).thenReturn(groups);

        mvc().perform(get("/api/v1/communication/devices").param("tab", "broadcast"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.broadcast[0].key").value("area-a"))
                .andExpect(jsonPath("$.data.broadcast[0].label").value("A装置区 (6)"))
                .andExpect(jsonPath("$.data.broadcast[0].devices[0].id").value("bc-a1"))
                .andExpect(jsonPath("$.data.broadcast[0].devices[0].detail.ip").value("10.20.31.101"))
                .andExpect(jsonPath("$.data.phone.length()").value(0))
                .andExpect(jsonPath("$.data.intercom.length()").value(0));
    }

    @Test
    void device_returnsSingleDeviceByCode() throws Exception {
        when(service.byCode("bc-a1")).thenReturn(device());

        mvc().perform(get("/api/v1/communication/devices/{id}", "bc-a1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.type").value("broadcast"))
                .andExpect(jsonPath("$.data.name").value("A装置区1#广播"))
                .andExpect(jsonPath("$.data.status").value("在线"))
                .andExpect(jsonPath("$.data.longitude").value(110.881))
                .andExpect(jsonPath("$.data.latitude").value(21.671))
                .andExpect(jsonPath("$.data.detail.category").value("室外防爆广播"))
                .andExpect(jsonPath("$.data.detail.lastCheck").value("2026-08-10 08:30:00"));
    }

    private static CommunicationDevice device() {
        CommunicationDeviceDetail detail = new CommunicationDeviceDetail();
        detail.setCategory("室外防爆广播");
        detail.setInstallTime("2024-03-12");
        detail.setOwner("安环部");
        detail.setIp("10.20.31.101");
        detail.setLastCheck("2026-08-10 08:30:00");
        CommunicationDevice device = new CommunicationDevice();
        device.setId("bc-a1");
        device.setType("broadcast");
        device.setName("A装置区1#广播");
        device.setArea("A装置区");
        device.setLocation("A装置区东北角");
        device.setStatus("在线");
        device.setLongitude(110.881);
        device.setLatitude(21.671);
        device.setDetail(detail);
        return device;
    }
}
