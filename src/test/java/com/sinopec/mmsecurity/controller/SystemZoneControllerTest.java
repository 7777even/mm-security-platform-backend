package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.ZoneItem;
import com.sinopec.mmsecurity.entity.SysZone;
import com.sinopec.mmsecurity.mapper.SysZoneMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SystemZoneControllerTest {

    @Mock
    private SysZoneMapper zoneMapper;

    @InjectMocks
    private SystemZoneController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void list_returnsEnabledZones() throws Exception {
        SysZone z = new SysZone();
        z.setId(1L);
        z.setZoneCode("A");
        z.setZoneName("防区A");
        z.setSortOrder(1);
        z.setStatus(1);
        when(zoneMapper.selectList(any())).thenReturn(List.of(z));

        mvc().perform(get("/api/v1/system/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].zoneCode").value("A"))
                .andExpect(jsonPath("$.data[0].zoneName").value("防区A"));
    }

    @Test
    void list_empty_returnsOk() throws Exception {
        when(zoneMapper.selectList(any())).thenReturn(List.of());
        mvc().perform(get("/api/v1/system/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
