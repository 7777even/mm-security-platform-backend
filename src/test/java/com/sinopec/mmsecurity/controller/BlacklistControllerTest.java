package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.BlacklistPersonItem;
import com.sinopec.mmsecurity.dto.BlacklistSummary;
import com.sinopec.mmsecurity.dto.BlacklistVehicleItem;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.service.BlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 安防黑名单接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class BlacklistControllerTest {

    @Mock
    private BlacklistService service;

    @InjectMocks
    private BlacklistController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void blacklist_returnsVehiclesAndPersons() throws Exception {
        BlacklistSummary summary = new BlacklistSummary();
        BlacklistVehicleItem vehicle = new BlacklistVehicleItem();
        vehicle.setId(1L);
        vehicle.setPlate("粤K·A4543");
        vehicle.setReason("违规闯入生产区");
        vehicle.setTime("2026-08-05 14:20:11");
        vehicle.setStatus("生效中");
        summary.setVehicles(List.of(vehicle));
        BlacklistPersonItem person = new BlacklistPersonItem();
        person.setId(4L);
        person.setName("张**");
        person.setIdCard("4409**********1234");
        person.setReason("未佩戴安全帽进入高危区");
        person.setTime("2026-08-06 10:02:45");
        person.setStatus("生效中");
        summary.setPersons(List.of(person));
        when(service.blacklist()).thenReturn(summary);

        mvc().perform(get("/api/v1/security/blacklist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.vehicles[0].plate").value("粤K·A4543"))
                .andExpect(jsonPath("$.data.vehicles[0].status").value("生效中"))
                .andExpect(jsonPath("$.data.persons[0].name").value("张**"))
                .andExpect(jsonPath("$.data.persons[0].idCard").value("4409**********1234"));
    }

    @Test
    void blacklist_returnsEmptyListsWhenNoData() throws Exception {
        BlacklistSummary summary = new BlacklistSummary();
        summary.setVehicles(List.of());
        summary.setPersons(List.of());
        when(service.blacklist()).thenReturn(summary);

        mvc().perform(get("/api/v1/security/blacklist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.vehicles.length()").value(0))
                .andExpect(jsonPath("$.data.persons.length()").value(0));
    }

    @Test
    void removeVehicle_returnsDeleteResult() throws Exception {
        DeleteResult result = new DeleteResult();
        result.setOk(true);
        when(service.removeVehicle(1L)).thenReturn(result);

        mvc().perform(delete("/api/v1/security/blacklist/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.ok").value(true));
    }

    @Test
    void removePerson_returnsDeleteResult() throws Exception {
        DeleteResult result = new DeleteResult();
        result.setOk(false);
        when(service.removePerson(99L)).thenReturn(result);

        mvc().perform(delete("/api/v1/security/blacklist/persons/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.ok").value(false));
    }
}
