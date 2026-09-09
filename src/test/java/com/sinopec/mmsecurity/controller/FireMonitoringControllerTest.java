package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.FireEquipmentStatus;
import com.sinopec.mmsecurity.dto.FirePatrolRecord;
import com.sinopec.mmsecurity.dto.RescueForceStat;
import com.sinopec.mmsecurity.dto.SpecialOperationStat;
import com.sinopec.mmsecurity.service.FireMonitoringService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 消防监控接口 standalone 校验：路由、B3 包络与数据形态（纯 Mockito，不起 Spring 上下文）。 */
class FireMonitoringControllerTest {

    private final FireMonitoringService service = Mockito.mock(FireMonitoringService.class);

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new FireMonitoringController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void rescueForces_returnsB3EnvelopeWithStats() throws Exception {
        RescueForceStat s = new RescueForceStat();
        s.setLabel("消防队伍");
        s.setValue(10);
        s.setUnit("支");
        s.setIconType("squad");
        Mockito.when(service.rescueForces()).thenReturn(List.of(s));

        mvc.perform(get("/api/v1/fire/rescue-forces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].label").value("消防队伍"))
                .andExpect(jsonPath("$.data[0].value").value(10))
                .andExpect(jsonPath("$.data[0].unit").value("支"))
                .andExpect(jsonPath("$.data[0].iconType").value("squad"));
    }

    @Test
    void specialOperations_returnsCountAndLabel() throws Exception {
        SpecialOperationStat s = new SpecialOperationStat();
        s.setId(4L);
        s.setLabel("动土作业");
        s.setCount(0);
        Mockito.when(service.specialOperations()).thenReturn(List.of(s));

        mvc.perform(get("/api/v1/fire/special-operations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].label").value("动土作业"))
                .andExpect(jsonPath("$.data[0].count").value(0));
    }

    @Test
    void equipmentStatus_returnsAggregate() throws Exception {
        FireEquipmentStatus s = new FireEquipmentStatus();
        s.setTotal(1233);
        s.setOffline(23);
        s.setFault(23);
        s.setIntegrityRate(98);
        s.setOnlineRate(98);
        Mockito.when(service.equipmentStatus()).thenReturn(s);

        mvc.perform(get("/api/v1/fire/equipment-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1233))
                .andExpect(jsonPath("$.data.integrityRate").value(98))
                .andExpect(jsonPath("$.data.onlineRate").value(98));
    }

    @Test
    void patrols_returnsRecordsWithCheckItems() throws Exception {
        FirePatrolRecord rec = new FirePatrolRecord();
        rec.setId(7L);
        rec.setPatrolDate("2026-08-18");
        rec.setShift("上午");
        rec.setDutyPerson("李五");
        rec.setPatrolCount("第1次");
        rec.setLocations(List.of("1#联合装置", "中央控制室"));
        rec.setCompleted(true);

        FirePatrolRecord empty = new FirePatrolRecord();
        empty.setId(8L);
        empty.setCompleted(false);
        empty.setLocations(Collections.emptyList());
        empty.setCheckItems(Collections.emptyList());

        Mockito.when(service.patrols()).thenReturn(List.of(rec, empty));

        mvc.perform(get("/api/v1/fire/patrols"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].patrolDate").value("2026-08-18"))
                .andExpect(jsonPath("$.data[0].shift").value("上午"))
                .andExpect(jsonPath("$.data[0].dutyPerson").value("李五"))
                .andExpect(jsonPath("$.data[0].locations[0]").value("1#联合装置"))
                .andExpect(jsonPath("$.data[0].completed").value(true))
                // 未设置 checkItems 时应序列化为空数组而非 null，避免前端 .some() 崩
                .andExpect(jsonPath("$.data[0].checkItems").isArray());
    }

    @Test
    void rescueForces_emptyWhenServiceReturnsEmpty() throws Exception {
        Mockito.when(service.rescueForces()).thenReturn(Collections.emptyList());

        mvc.perform(get("/api/v1/fire/rescue-forces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
