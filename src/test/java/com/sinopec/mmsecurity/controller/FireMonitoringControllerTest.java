package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.FireEquipmentStatus;
import com.sinopec.mmsecurity.dto.FirePatrolRecord;
import com.sinopec.mmsecurity.dto.FirePatrolWriteRequest;
import com.sinopec.mmsecurity.dto.PatrolExecutionView;
import com.sinopec.mmsecurity.dto.RescueForceStat;
import com.sinopec.mmsecurity.dto.SpecialOperationStat;
import com.sinopec.mmsecurity.service.BusinessWriteService;
import com.sinopec.mmsecurity.service.FireMonitoringService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 消防监控接口 standalone 校验：路由、B3 包络与数据形态（纯 Mockito，不起 Spring 上下文）。 */
class FireMonitoringControllerTest {

    private final FireMonitoringService service = Mockito.mock(FireMonitoringService.class);
    private final BusinessWriteService businessWriteService = Mockito.mock(BusinessWriteService.class);

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new FireMonitoringController(service, businessWriteService))
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

    /* ==================== A2 业务写侧：巡更执行上报 ==================== */

    @Test
    void patrolExecutions_getReturnsB3Envelope() throws Exception {
        PatrolExecutionView v = new PatrolExecutionView();
        v.setId(1L);
        v.setPatrolDate("2026-09-13");
        v.setExecResult("NORMAL");
        Mockito.when(businessWriteService.listPatrolExecutions()).thenReturn(List.of(v));

        mvc.perform(get("/api/v1/fire/patrol-executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].patrolDate").value("2026-09-13"))
                .andExpect(jsonPath("$.data[0].execResult").value("NORMAL"));
    }

    @Test
    void patrolExecutions_postDelegatesToWriteService() throws Exception {
        PatrolExecutionView v = new PatrolExecutionView();
        v.setExecResult("NORMAL");
        Mockito.when(businessWriteService.createPatrolExecution(Mockito.any())).thenReturn(v);

        mvc.perform(post("/api/v1/fire/patrol-executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patrolDate\":\"2026-09-13\",\"dutyPerson\":\"tester\",\"execResult\":\"NORMAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.execResult").value("NORMAL"));
    }

    /* ==================== A3 防火巡查记录台账（fire.patrol-record） ==================== */

    @Test
    void patrols_postCreatesRecord() throws Exception {
        FirePatrolRecord rec = new FirePatrolRecord();
        rec.setId(20L);
        rec.setPatrolDate("2026-10-03");
        rec.setShift("上午");
        rec.setDutyPerson("王六");
        Mockito.when(service.createFirePatrol(Mockito.any())).thenReturn(rec);

        mvc.perform(post("/api/v1/fire/patrols")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patrolDate\":\"2026-10-03\",\"shift\":\"上午\",\"dutyPerson\":\"王六\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(20))
                .andExpect(jsonPath("$.data.patrolDate").value("2026-10-03"))
                .andExpect(jsonPath("$.data.shift").value("上午"))
                .andExpect(jsonPath("$.data.dutyPerson").value("王六"));
    }

    @Test
    void patrols_putUpdatesRecord() throws Exception {
        FirePatrolRecord rec = new FirePatrolRecord();
        rec.setId(20L);
        rec.setPatrolDate("2026-10-03");
        rec.setShift("下午");
        Mockito.when(service.updateFirePatrol(Mockito.eq(20L), Mockito.any())).thenReturn(rec);

        mvc.perform(put("/api/v1/fire/patrols/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patrolDate\":\"2026-10-03\",\"shift\":\"下午\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(20))
                .andExpect(jsonPath("$.data.shift").value("下午"));
    }

    @Test
    void patrols_deleteReturnsOk() throws Exception {
        Mockito.doNothing().when(service).deleteFirePatrol(20L);

        // DELETE 成功返回 B3 包络 code=0；data 为 null 时按 NON_NULL 规则省略（与 security 删除约定一致）
        mvc.perform(delete("/api/v1/fire/patrols/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void patrols_deleteNotFoundReturnsB3Error() throws Exception {
        Mockito.doThrow(new BusinessException(ResultCode.NOT_FOUND, "防火巡查记录不存在：999"))
                .when(service).deleteFirePatrol(999L);

        // B3 包络：HTTP 200 + code=404（非 4xx），与零下行控制 / B3 包络规范一致
        mvc.perform(delete("/api/v1/fire/patrols/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("不存在")));
    }
}
