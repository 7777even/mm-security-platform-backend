package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.AccidentRescueIncident;
import com.sinopec.mmsecurity.service.AccidentRescueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccidentRescueControllerTest {

    @Mock
    private AccidentRescueService service;

    @InjectMocks
    private AccidentRescueController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void rescueIncident_returnsAggregate() throws Exception {
        AccidentRescueIncident inc = new AccidentRescueIncident();
        inc.setEventId(4L);
        inc.setTitle("乙烯裂解装置区火灾");
        inc.setStatus("processing");
        when(service.incident(any())).thenReturn(inc);

        mvc().perform(get("/api/v1/accident/rescue-incident"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.eventId").value(4))
                .andExpect(jsonPath("$.data.title").value("乙烯裂解装置区火灾"));
    }

    @Test
    void rescueIncident_whenNoData_throwsNotFound() throws Exception {
        when(service.incident(any())).thenReturn(null);

        mvc().perform(get("/api/v1/accident/rescue-incident"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void rescueIncident_byEventId_passesParam() throws Exception {
        AccidentRescueIncident inc = new AccidentRescueIncident();
        inc.setEventId(4L);
        when(service.incident(4L)).thenReturn(inc);

        mvc().perform(get("/api/v1/accident/rescue-incident").param("eventId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventId").value(4));
    }
}
