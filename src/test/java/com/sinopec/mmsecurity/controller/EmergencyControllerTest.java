package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.ClosedCase;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.CommandActionDetail;
import com.sinopec.mmsecurity.dto.DutyRoster;
import com.sinopec.mmsecurity.dto.EmergencyCommandGroup;
import com.sinopec.mmsecurity.dto.EmergencyCommandInstruction;
import com.sinopec.mmsecurity.dto.EmergencyPhone;
import com.sinopec.mmsecurity.dto.EmergencyPhoneBook;
import com.sinopec.mmsecurity.dto.EmergencyResource;
import com.sinopec.mmsecurity.dto.EmergencyStrength;
import com.sinopec.mmsecurity.dto.KnowledgeItem;
import com.sinopec.mmsecurity.dto.KnowledgeList;
import com.sinopec.mmsecurity.dto.NodePhaseConfig;
import com.sinopec.mmsecurity.dto.NodePhaseDuty;
import com.sinopec.mmsecurity.dto.NodePhaseMapCamera;
import com.sinopec.mmsecurity.service.EmergencyService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * EmergencyController（standalone MockMvc，不启动 Spring 上下文）：5 个只读端点字段对齐契约。
 */
class EmergencyControllerTest {

    private final EmergencyService service = mock(EmergencyService.class);
    private final EmergencyController controller = new EmergencyController(service);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void strength_returnsResources() throws Exception {
        EmergencyStrength s = new EmergencyStrength();
        s.setResources(List.of(res("应急专家", 47)));
        when(service.strength()).thenReturn(s);

        mockMvc.perform(get("/api/v1/emergency/strength"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.resources[0].kind").value("应急专家"))
                .andExpect(jsonPath("$.data.resources[0].count").value(47));
    }

    @Test
    void closedCases_returnsCases() throws Exception {
        ClosedCaseList list = new ClosedCaseList();
        ClosedCase c = new ClosedCase();
        c.setCaseId("C-2026-081");
        c.setTitle("A 装置反应釜温度异常");
        c.setHandler("系统归档");
        list.setCases(List.of(c));
        when(service.closedCases()).thenReturn(list);

        mockMvc.perform(get("/api/v1/emergency/closed-cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.cases[0].caseId").value("C-2026-081"));
    }

    @Test
    void duty_returnsRoster() throws Exception {
        DutyRoster r = new DutyRoster();
        r.setShift("白班");
        when(service.duty()).thenReturn(r);

        mockMvc.perform(get("/api/v1/emergency/duty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.shift").value("白班"));
    }

    @Test
    void phones_returnsPhoneBook() throws Exception {
        EmergencyPhoneBook b = new EmergencyPhoneBook();
        EmergencyPhone p = new EmergencyPhone();
        p.setId("ph1");
        p.setName("消防报警");
        p.setNumber("119");
        b.setEntries(List.of(p));
        when(service.phones()).thenReturn(b);

        mockMvc.perform(get("/api/v1/emergency/phones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.entries[0].number").value("119"));
    }

    @Test
    void knowledge_returnsItems() throws Exception {
        KnowledgeList k = new KnowledgeList();
        KnowledgeItem i = new KnowledgeItem();
        i.setId("k1");
        i.setTitle("岗位应急处置卡");
        k.setItems(List.of(i));
        when(service.knowledge()).thenReturn(k);

        mockMvc.perform(get("/api/v1/emergency/knowledge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].title").value("岗位应急处置卡"));
    }

    @Test
    void commands_returnsGroups() throws Exception {
        EmergencyCommandGroup g = new EmergencyCommandGroup();
        g.setId("notify");
        g.setLabel("一键通知");
        EmergencyCommandInstruction it = new EmergencyCommandInstruction();
        it.setId("n1");
        it.setName("通知值班人员");
        it.setStatus("待处置");
        g.setItems(new ArrayList<>(List.of(it)));
        when(service.commandGroups("fixed")).thenReturn(List.of(g));

        mockMvc.perform(get("/api/v1/emergency/commands").param("tab", "fixed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("notify"))
                .andExpect(jsonPath("$.data[0].items[0].name").value("通知值班人员"))
                .andExpect(jsonPath("$.data[0].items[0].status").value("待处置"));
    }

    @Test
    void commandDetail_returnsDetail() throws Exception {
        CommandActionDetail d = new CommandActionDetail();
        d.setId("n1");
        d.setName("通知值班人员");
        d.setType("通知");
        d.setStatus("待处置");
        d.setNotifyChannels(List.of("app", "sms", "voice"));
        when(service.commandDetail("n1")).thenReturn(d);

        mockMvc.perform(get("/api/v1/emergency/commands/n1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("n1"))
                .andExpect(jsonPath("$.data.name").value("通知值班人员"))
                .andExpect(jsonPath("$.data.notifyChannels[0]").value("app"));
    }

    private EmergencyResource res(String kind, int count) {
        EmergencyResource r = new EmergencyResource();
        r.setKind(kind);
        r.setCount(count);
        return r;
    }

    @Test
    void nodePhaseConfigs_returnsList() throws Exception {
        NodePhaseConfig c = new NodePhaseConfig();
        c.setNodeId("alarmJudgement");
        c.setNodeName("1. 接警研判");
        NodePhaseMapCamera cam = new NodePhaseMapCamera();
        cam.setAnchorPriorityList(List.of("alarm_phone_location", "event_device"));
        cam.setBufferRadiusMeters(260);
        c.setMapCamera(cam);
        c.setRightPanelHiddenTabs(List.of());
        c.setLeftPanelHiddenPanels(List.of());
        NodePhaseDuty duty = new NodePhaseDuty();
        duty.setAutoRoster(true);
        c.setDuty(duty);
        when(service.nodePhaseConfigs()).thenReturn(List.of(c));

        mockMvc.perform(get("/api/v1/emergency/process/node-configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].nodeId").value("alarmJudgement"))
                .andExpect(
                        jsonPath("$.data[0].mapCamera.anchorPriorityList[0]")
                                .value("alarm_phone_location"))
                .andExpect(jsonPath("$.data[0].duty.autoRoster").value(true));
    }

    @Test
    void saveNodePhaseConfigs_returnsSavedList() throws Exception {
        NodePhaseConfig c = new NodePhaseConfig();
        c.setNodeId("3min");
        c.setNodeName("3. 三分钟退守稳态");
        when(service.saveNodePhaseConfigs(any())).thenReturn(List.of(c));

        mockMvc.perform(put("/api/v1/emergency/process/node-configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"nodeId\":\"3min\",\"nodeName\":\"3. 三分钟退守稳态\","
                                + "\"mapCamera\":{\"anchorPriorityList\":[\"event_device\"],"
                                + "\"bufferRadiusMeters\":320},\"rightPanelHiddenTabs\":[],"
                                + "\"leftPanelHiddenPanels\":[],\"duty\":{\"autoRoster\":true}}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].nodeId").value("3min"));
    }
}
