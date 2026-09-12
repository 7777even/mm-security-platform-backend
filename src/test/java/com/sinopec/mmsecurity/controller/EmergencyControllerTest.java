package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.ClosedCase;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.CommandActionDetail;
import com.sinopec.mmsecurity.dto.DutyRoster;
import com.sinopec.mmsecurity.dto.DutySignInView;
import com.sinopec.mmsecurity.dto.DutySignInWriteRequest;
import com.sinopec.mmsecurity.dto.EmergencyAssistStat;
import com.sinopec.mmsecurity.dto.EmergencyAssistStatSummary;
import com.sinopec.mmsecurity.dto.EmergencyCommandGroup;
import com.sinopec.mmsecurity.dto.EmergencyCommandRecordView;
import com.sinopec.mmsecurity.dto.EmergencyCommandRecordWriteRequest;
import com.sinopec.mmsecurity.dto.EmergencyCommandInstruction;
import com.sinopec.mmsecurity.dto.EmergencyPhase;
import com.sinopec.mmsecurity.dto.EmergencyPhone;
import com.sinopec.mmsecurity.dto.EmergencyPhoneBook;
import com.sinopec.mmsecurity.dto.EmergencyProcessGuidance;
import com.sinopec.mmsecurity.dto.EmergencyProcessPanorama;
import com.sinopec.mmsecurity.dto.EmergencyResource;
import com.sinopec.mmsecurity.dto.EmergencyStrength;
import com.sinopec.mmsecurity.dto.GuidanceDutyRoster;
import com.sinopec.mmsecurity.dto.KnowledgeItem;
import com.sinopec.mmsecurity.dto.KnowledgeList;
import com.sinopec.mmsecurity.dto.NodeGuidance;
import com.sinopec.mmsecurity.dto.NodePhaseConfig;
import com.sinopec.mmsecurity.dto.NodePhaseDuty;
import com.sinopec.mmsecurity.dto.NodePhaseMapCamera;
import com.sinopec.mmsecurity.dto.ProcessStage;
import com.sinopec.mmsecurity.dto.ResponseModeOption;
import com.sinopec.mmsecurity.service.BusinessWriteService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * EmergencyController（standalone MockMvc，不启动 Spring 上下文）：5 个只读端点字段对齐契约。
 */
class EmergencyControllerTest {

    private final EmergencyService service = mock(EmergencyService.class);
    private final BusinessWriteService businessWriteService = mock(BusinessWriteService.class);
    private final EmergencyController controller = new EmergencyController(service, businessWriteService);
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
    void assistStats_returnsItems() throws Exception {
        EmergencyAssistStatSummary summary = new EmergencyAssistStatSummary();
        EmergencyAssistStat stat = new EmergencyAssistStat();
        stat.setLabel("应急预案");
        stat.setValue(15);
        stat.setUnit("套");
        stat.setTone("blue");
        summary.setItems(List.of(stat));
        when(service.assistStats()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/emergency/assist-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].label").value("应急预案"))
                .andExpect(jsonPath("$.data.items[0].value").value(15))
                .andExpect(jsonPath("$.data.items[0].unit").value("套"))
                .andExpect(jsonPath("$.data.items[0].tone").value("blue"));
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

    @Test
    void processPanorama_returnsPhasesModesStages() throws Exception {
        EmergencyProcessPanorama panorama = new EmergencyProcessPanorama();
        EmergencyPhase phase = new EmergencyPhase();
        phase.setId("phase-team");
        phase.setName("班组处置");
        phase.setStart(1);
        phase.setEnd(4);
        phase.setTone("blue");
        panorama.setPhases(List.of(phase));
        ResponseModeOption mode = new ResponseModeOption();
        mode.setValue("team");
        mode.setLabel("一、班组处置");
        mode.setStageId(1);
        panorama.setResponseModes(List.of(mode));
        ProcessStage stage = new ProcessStage();
        stage.setId(1);
        stage.setName("接警研判");
        panorama.setStages(List.of(stage));
        when(service.processPanorama()).thenReturn(panorama);

        mockMvc.perform(get("/api/v1/emergency/process/panorama"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.phases[0].id").value("phase-team"))
                .andExpect(jsonPath("$.data.phases[0].start").value(1))
                .andExpect(jsonPath("$.data.responseModes[0].value").value("team"))
                .andExpect(jsonPath("$.data.stages[0].name").value("接警研判"));
    }

    @Test
    void processGuidances_returnsRosterAndGuidances() throws Exception {
        EmergencyProcessGuidance guidance = new EmergencyProcessGuidance();
        GuidanceDutyRoster roster = new GuidanceDutyRoster();
        roster.setShiftGroup("乙班（白班）");
        guidance.setDutyRoster(roster);
        NodeGuidance node = new NodeGuidance();
        node.setNodeId("1");
        node.setNodeName("节点 1：接警研判");
        guidance.setGuidances(List.of(node));
        when(service.processGuidances()).thenReturn(guidance);

        mockMvc.perform(get("/api/v1/emergency/process/guidances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.dutyRoster.shiftGroup").value("乙班（白班）"))
                .andExpect(jsonPath("$.data.guidances[0].nodeId").value("1"));
    }

    /* ==================== A2 业务写侧：应急指令 / 值班签到 ==================== */

    @Test
    void commandRecords_getReturnsB3Envelope() throws Exception {
        EmergencyCommandRecordView v = new EmergencyCommandRecordView();
        v.setId(1L);
        v.setCommandCode("w1");
        v.setCurrStatus("执行中");
        when(businessWriteService.listCommandRecords()).thenReturn(List.of(v));

        mockMvc.perform(get("/api/v1/emergency/command-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].commandCode").value("w1"))
                .andExpect(jsonPath("$.data[0].currStatus").value("执行中"));
    }

    @Test
    void commandRecords_postDelegatesToWriteService() throws Exception {
        EmergencyCommandRecordView v = new EmergencyCommandRecordView();
        v.setCommandCode("w1");
        v.setPrevStatus("待执行");
        v.setCurrStatus("执行中");
        when(businessWriteService.createCommandRecord(any(EmergencyCommandRecordWriteRequest.class))).thenReturn(v);

        mockMvc.perform(post("/api/v1/emergency/command-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commandCode\":\"w1\",\"currStatus\":\"执行中\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.prevStatus").value("待执行"))
                .andExpect(jsonPath("$.data.currStatus").value("执行中"));
    }

    @Test
    void dutySignIns_getReturnsB3Envelope() throws Exception {
        DutySignInView v = new DutySignInView();
        v.setId(1L);
        v.setPersonName("tester");
        v.setSignAction("SIGN_IN");
        when(businessWriteService.listDutySignIns()).thenReturn(List.of(v));

        mockMvc.perform(get("/api/v1/emergency/duty-sign-ins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].signAction").value("SIGN_IN"));
    }

    @Test
    void dutySignIns_postDelegatesToWriteService() throws Exception {
        DutySignInView v = new DutySignInView();
        v.setPersonName("tester");
        v.setSignAction("SIGN_IN");
        v.setSignTime("2026-09-13 08:00:00");
        when(businessWriteService.createDutySignIn(any(DutySignInWriteRequest.class))).thenReturn(v);

        mockMvc.perform(post("/api/v1/emergency/duty-sign-ins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dutyDate\":\"2026-09-13\",\"personName\":\"tester\",\"signAction\":\"SIGN_IN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.signTime").value("2026-09-13 08:00:00"));
    }
}
