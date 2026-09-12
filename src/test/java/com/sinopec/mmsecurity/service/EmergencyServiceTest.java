package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.ClosedCase;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.CommandActionDetail;
import com.sinopec.mmsecurity.dto.EmergencyCommandGroup;
import com.sinopec.mmsecurity.dto.EmergencyStrength;
import com.sinopec.mmsecurity.dto.NodePhaseConfig;
import com.sinopec.mmsecurity.dto.NodePhaseDuty;
import com.sinopec.mmsecurity.dto.NodePhaseMapCamera;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.FacEmergencyCmd;
import com.sinopec.mmsecurity.entity.FacEmergencyGuidanceRoster;
import com.sinopec.mmsecurity.entity.FacEmergencyNodeGuidance;
import com.sinopec.mmsecurity.entity.FacEmergencyPhase;
import com.sinopec.mmsecurity.entity.FacEmergencyProcessStage;
import com.sinopec.mmsecurity.entity.FacEmergencyResponseMode;
import com.sinopec.mmsecurity.entity.FacNodePhaseConfig;
import com.sinopec.mmsecurity.entity.SysDutyMember;
import com.sinopec.mmsecurity.entity.SysEmergencyPhone;
import com.sinopec.mmsecurity.entity.SysEmergencyStrength;
import com.sinopec.mmsecurity.entity.SysKnowledgeItem;
import com.sinopec.mmsecurity.entity.FacEmergencyAssistStat;
import com.sinopec.mmsecurity.dto.EmergencyAssistStatSummary;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyAssistStatMapper;
import com.sinopec.mmsecurity.mapper.FacDispatchPersonnelMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyCmdMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyGuidanceRosterMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyNodeGuidanceMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyPhaseMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyProcessStageMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyResponseModeMapper;
import com.sinopec.mmsecurity.mapper.FacNodePhaseConfigMapper;
import com.sinopec.mmsecurity.mapper.SysDutyMemberMapper;
import com.sinopec.mmsecurity.mapper.SysEmergencyPhoneMapper;
import com.sinopec.mmsecurity.mapper.SysEmergencyStrengthMapper;
import com.sinopec.mmsecurity.mapper.SysKnowledgeItemMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EmergencyService（纯 Mockito）：
 * strength/phones/knowledge/duty 来自 DB 参考表（sys_emergency_strength 等）；
 * closedCases 来自 fac_alarm(status=3) 真实聚合。
 */
class EmergencyServiceTest {

    private final AlarmMapper alarmMapper = mock(AlarmMapper.class);
    private final FacEmergencyAssistStatMapper assistStatMapper = mock(FacEmergencyAssistStatMapper.class);
    private final SysEmergencyStrengthMapper strengthMapper = mock(SysEmergencyStrengthMapper.class);
    private final SysEmergencyPhoneMapper phoneMapper = mock(SysEmergencyPhoneMapper.class);
    private final SysKnowledgeItemMapper knowledgeMapper = mock(SysKnowledgeItemMapper.class);
    private final SysDutyMemberMapper dutyMapper = mock(SysDutyMemberMapper.class);
    private final FacDispatchPersonnelMapper dispatchPersonnelMapper =
            mock(FacDispatchPersonnelMapper.class);
    private final FacEmergencyCmdMapper cmdMapper = mock(FacEmergencyCmdMapper.class);
    private final FacNodePhaseConfigMapper nodePhaseConfigMapper =
            mock(FacNodePhaseConfigMapper.class);
    private final FacEmergencyPhaseMapper emergencyPhaseMapper = mock(FacEmergencyPhaseMapper.class);
    private final FacEmergencyResponseModeMapper responseModeMapper =
            mock(FacEmergencyResponseModeMapper.class);
    private final FacEmergencyProcessStageMapper processStageMapper =
            mock(FacEmergencyProcessStageMapper.class);
    private final FacEmergencyNodeGuidanceMapper nodeGuidanceMapper =
            mock(FacEmergencyNodeGuidanceMapper.class);
    private final FacEmergencyGuidanceRosterMapper guidanceRosterMapper =
            mock(FacEmergencyGuidanceRosterMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EmergencyService service = new EmergencyService(
            alarmMapper, assistStatMapper, strengthMapper, phoneMapper, knowledgeMapper, dutyMapper,
            dispatchPersonnelMapper, cmdMapper, nodePhaseConfigMapper, emergencyPhaseMapper,
            responseModeMapper, processStageMapper, nodeGuidanceMapper, guidanceRosterMapper,
            objectMapper);

    private static SysEmergencyStrength strength(String kind, int count, String icon) {
        SysEmergencyStrength s = new SysEmergencyStrength();
        s.setKind(kind);
        s.setCount(count);
        s.setIcon(icon);
        return s;
    }

    private static SysEmergencyPhone phone(String name, String number, String category) {
        SysEmergencyPhone p = new SysEmergencyPhone();
        p.setName(name);
        p.setNumber(number);
        p.setCategory(category);
        return p;
    }

    private static SysKnowledgeItem knowledge(String title, int count, String icon) {
        SysKnowledgeItem k = new SysKnowledgeItem();
        k.setTitle(title);
        k.setCount(count);
        k.setIcon(icon);
        return k;
    }

    private static SysDutyMember duty(String name, String phone, String role, String dept, String shift) {
        SysDutyMember m = new SysDutyMember();
        m.setName(name);
        m.setPhone(phone);
        m.setRole(role);
        m.setDepartment(dept);
        m.setShift(shift);
        return m;
    }

    @BeforeEach
    void resetCaches() {
        service.clearCaches();
    }

    @Test
    void strength_readsFromReferenceTable() {
        when(strengthMapper.selectList(null)).thenReturn(List.of(
                strength("应急专家", 47, "UserFilled"),
                strength("应急物资", 3510, "Box"),
                strength("救援队伍", 12, "Soldier"),
                strength("装备车辆", 28, "Van"),
                strength("应急场所", 6, "LocationFilled"),
                strength("医疗机构", 3, "FirstAidKit"),
                strength("应急车辆", 18, "Truck"),
                strength("消防设施", 42, "Fire")));
        EmergencyStrength s = service.strength();
        assertEquals(8, s.getResources().size());
        assertEquals("应急专家", s.getResources().get(0).getKind());
        assertEquals(47, s.getResources().get(0).getCount());
    }

    @Test
    void closedCases_aggregatesFromAlarm() {
        FacAlarm closed = new FacAlarm();
        closed.setAlarmId("AE-2026-005");
        closed.setTitle("A装置反应釜温度异常");
        closed.setLocation("装置区 03 单元");
        closed.setOccurredAt(LocalDateTime.of(2026, 9, 4, 7, 45));
        closed.setStatus(3);
        when(alarmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(closed));

        ClosedCaseList list = service.closedCases();
        assertEquals(1, list.getCases().size());
        assertEquals("AE-2026-005", list.getCases().get(0).getCaseId());
        assertEquals("A装置反应釜温度异常", list.getCases().get(0).getTitle());
        assertEquals("系统归档", list.getCases().get(0).getHandler());
    }

    @Test
    void phones_readsFromReferenceTable() {
        when(phoneMapper.selectList(null)).thenReturn(List.of(
                phone("消防报警", "119", "消防"),
                phone("医疗急救", "120", "医疗"),
                phone("公安报警", "110", "公安"),
                phone("厂内应急", "0668-2222111", "厂内应急"),
                phone("保卫值班", "0668-2222333", "保卫值班")));
        assertEquals(5, service.phones().getEntries().size());
        assertEquals("119", service.phones().getEntries().get(0).getNumber());
    }

    @Test
    void knowledge_readsFromReferenceTable() {
        when(knowledgeMapper.selectList(null)).thenReturn(List.of(
                knowledge("岗位应急处置卡", 158, "Document"),
                knowledge("火灾爆炸应急预案", 42, "Files"),
                knowledge("气体泄漏处置", 67, "Warning")));
        assertEquals(3, service.knowledge().getItems().size());
    }

    @Test
    void duty_readsFromReferenceTable() {
        when(dutyMapper.selectList(null)).thenReturn(List.of(
                duty("杨恒明", "13792536966", "值班领导", "全部", "白班"),
                duty("李伟", "13800138000", "值班员", "全部", "白班")));
        assertEquals("白班", service.duty().getShift());
        assertEquals(2, service.duty().getMembers().size());
        assertEquals("全部", service.duty().getDepartments().get(0));
    }

    @Test
    void commandGroups_groupsByGrpId() {
        FacEmergencyCmd r1 = new FacEmergencyCmd();
        r1.setId("n1");
        r1.setGrpId("notify");
        r1.setGrpLabel("一键通知");
        r1.setGrpTab("fixed");
        r1.setInstructionType("通知");
        r1.setName("通知值班人员");
        r1.setLocation("中海壳牌石油化工有限公司");
        r1.setStatus("待处置");
        r1.setActionLabel(null);
        r1.setDone(false);
        when(cmdMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(r1));

        List<EmergencyCommandGroup> groups = service.commandGroups("fixed");
        assertEquals(1, groups.size());
        assertEquals("notify", groups.get(0).getId());
        assertEquals(1, groups.get(0).getItems().size());
        assertEquals("n1", groups.get(0).getItems().get(0).getId());
        assertEquals("待处置", groups.get(0).getItems().get(0).getStatus());
    }

    @Test
    void commandDetail_deserializesDetailJson() {
        FacEmergencyCmd row = new FacEmergencyCmd();
        row.setId("n1");
        row.setInstructionType("通知");
        row.setName("通知值班人员");
        row.setStatus("待处置");
        row.setLocation("中海壳牌石油化工有限公司");
        row.setDetailJson("{\"notifyChannels\":[\"app\",\"sms\",\"voice\"],\"dispatchMode\":\"自动派发\","
                + "\"description\":\"x\",\"attachment\":\"—\",\"addressBookRecipients\":[],"
                + "\"dutyRecipients\":[],\"dynamics\":[]}");
        when(cmdMapper.selectById("n1")).thenReturn(row);

        CommandActionDetail result = service.commandDetail("n1");
        assertEquals("n1", result.getId());
        assertEquals("通知", result.getType());
        assertEquals("通知值班人员", result.getName());
        assertEquals("待处置", result.getStatus());
        assertEquals("中海壳牌石油化工有限公司", result.getLocation());
        assertEquals(List.of("app", "sms", "voice"), result.getNotifyChannels());
    }

    @Test
    void commandDetail_unknownIdReturnsNull() {
        when(cmdMapper.selectById("unknown")).thenReturn(null);
        assertEquals(null, service.commandDetail("unknown"));
    }

    @Test
    void nodePhaseConfigs_mapsCsvToListAndDutyFlag() {
        FacNodePhaseConfig row = new FacNodePhaseConfig();
        row.setNodeId("3min");
        row.setNodeName("3. 三分钟退守稳态");
        row.setCameraAnchors("event_device, factory_center");
        row.setCustomCenter("110.9265,21.662");
        row.setBufferRadiusMeters(320);
        row.setRightHiddenTabs("dynamics");
        row.setLeftHiddenPanels("info");
        row.setDutyAutoRoster(true);
        row.setSortNo(3);
        when(nodePhaseConfigMapper.selectList(any())).thenReturn(List.of(row));

        List<NodePhaseConfig> list = service.nodePhaseConfigs();

        assertEquals(1, list.size());
        assertEquals("3min", list.get(0).getNodeId());
        assertEquals(List.of("event_device", "factory_center"),
                list.get(0).getMapCamera().getAnchorPriorityList());
        assertEquals(List.of(110.9265, 21.662), list.get(0).getMapCamera().getCustomCenter());
        assertEquals(320, list.get(0).getMapCamera().getBufferRadiusMeters());
        assertEquals(List.of("dynamics"), list.get(0).getRightPanelHiddenTabs());
        assertEquals(List.of("info"), list.get(0).getLeftPanelHiddenPanels());
        assertTrue(list.get(0).getDuty().getAutoRoster());
    }

    @Test
    void saveNodePhaseConfigs_updatesExistingRow() {
        FacNodePhaseConfig existing = new FacNodePhaseConfig();
        existing.setId(3L);
        existing.setNodeId("3min");
        existing.setNodeName("旧名称");
        existing.setCameraAnchors("event_device");
        existing.setBufferRadiusMeters(300);
        existing.setRightHiddenTabs("");
        existing.setLeftHiddenPanels("");
        existing.setDutyAutoRoster(false);
        existing.setSortNo(3);
        when(nodePhaseConfigMapper.selectList(any())).thenReturn(List.of(existing));

        NodePhaseConfig in = new NodePhaseConfig();
        in.setNodeId("3min");
        in.setNodeName("3. 三分钟退守稳态");
        NodePhaseMapCamera camera = new NodePhaseMapCamera();
        camera.setAnchorPriorityList(List.of("event_device", "factory_center"));
        camera.setBufferRadiusMeters(360);
        in.setMapCamera(camera);
        in.setRightPanelHiddenTabs(List.of("dynamics"));
        in.setLeftPanelHiddenPanels(List.of());
        NodePhaseDuty duty = new NodePhaseDuty();
        duty.setAutoRoster(true);
        in.setDuty(duty);

        List<NodePhaseConfig> saved = service.saveNodePhaseConfigs(List.of(in));

        verify(nodePhaseConfigMapper).updateById(any(FacNodePhaseConfig.class));
        assertEquals(1, saved.size());
        assertEquals("3. 三分钟退守稳态", saved.get(0).getNodeName());
        assertEquals(List.of("event_device", "factory_center"),
                saved.get(0).getMapCamera().getAnchorPriorityList());
        assertEquals(360, saved.get(0).getMapCamera().getBufferRadiusMeters());
        assertEquals(List.of("dynamics"), saved.get(0).getRightPanelHiddenTabs());
        assertEquals(List.of(), saved.get(0).getLeftPanelHiddenPanels());
        assertTrue(saved.get(0).getDuty().getAutoRoster());
    }

    @Test
    void saveNodePhaseConfigs_createsMissingRowWithDefaults() {
        when(nodePhaseConfigMapper.selectList(any())).thenReturn(List.of());

        NodePhaseConfig in = new NodePhaseConfig();
        in.setNodeId("newNode");

        List<NodePhaseConfig> saved = service.saveNodePhaseConfigs(List.of(in));

        ArgumentCaptor<FacNodePhaseConfig> captor = ArgumentCaptor.forClass(FacNodePhaseConfig.class);
        verify(nodePhaseConfigMapper).insert(captor.capture());
        FacNodePhaseConfig inserted = captor.getValue();
        assertEquals("newNode", inserted.getNodeId());
        assertEquals("newNode", inserted.getNodeName());
        assertEquals("", inserted.getCameraAnchors());
        assertEquals(260, inserted.getBufferRadiusMeters());
        assertEquals(Boolean.FALSE, inserted.getDutyAutoRoster());
        assertEquals(1, inserted.getSortNo());
        assertTrue(saved.isEmpty());
    }

    @Test
    void processPanorama_mapsPhasesModesAndDeserializesStages() {
        FacEmergencyPhase phase = new FacEmergencyPhase();
        phase.setPhaseCode("phase-team");
        phase.setPhaseName("班组处置");
        phase.setStartStage(1);
        phase.setEndStage(4);
        phase.setTone("blue");
        when(emergencyPhaseMapper.selectList(any())).thenReturn(List.of(phase));

        FacEmergencyResponseMode mode = new FacEmergencyResponseMode();
        mode.setModeCode("team");
        mode.setModeLabel("一、班组处置");
        mode.setStageId(1);
        when(responseModeMapper.selectList(any())).thenReturn(List.of(mode));

        FacEmergencyProcessStage row = new FacEmergencyProcessStage();
        row.setStageId(1);
        row.setPhaseCode("phase-team");
        row.setDetailJson("{\"id\":1,\"name\":\"接警研判\",\"shortName\":\"1. 接警研判\","
                + "\"commandLevel\":\"班组处置\",\"previousContext\":[\"a\",\"b\"],"
                + "\"currentActions\":[{\"id\":\"act-1-1\",\"label\":\"报告\",\"done\":true,"
                + "\"type\":\"primary\"}],"
                + "\"criteriaChecklist\":[{\"id\":\"cri-1-1\",\"label\":\"确认\",\"checked\":true}],"
                + "\"escalationRule\":{\"triggerCondition\":\"t\",\"fromRole\":\"f\",\"toRole\":\"to\","
                + "\"details\":{\"location\":\"L\",\"substance\":\"S\",\"casualty\":\"C\","
                + "\"currentStatus\":\"P\"}}}");
        when(processStageMapper.selectList(any())).thenReturn(List.of(row));

        var panorama = service.processPanorama();

        assertEquals(1, panorama.getPhases().size());
        assertEquals("phase-team", panorama.getPhases().get(0).getId());
        assertEquals(1, panorama.getPhases().get(0).getStart());
        assertEquals(4, panorama.getPhases().get(0).getEnd());
        assertEquals("team", panorama.getResponseModes().get(0).getValue());
        assertEquals(1, panorama.getStages().size());
        var stage = panorama.getStages().get(0);
        assertEquals(1, stage.getId());
        assertEquals("接警研判", stage.getName());
        assertEquals(List.of("a", "b"), stage.getPreviousContext());
        assertEquals("primary", stage.getCurrentActions().get(0).getType());
        assertEquals(Boolean.TRUE, stage.getCurrentActions().get(0).getDone());
        assertEquals(Boolean.TRUE, stage.getCriteriaChecklist().get(0).getChecked());
        assertEquals("L", stage.getEscalationRule().getDetails().getLocation());
    }

    @Test
    void processPanorama_toleratesBrokenDetailJson() {
        FacEmergencyProcessStage row = new FacEmergencyProcessStage();
        row.setStageId(1);
        row.setDetailJson("{ not json");
        when(emergencyPhaseMapper.selectList(any())).thenReturn(List.of());
        when(responseModeMapper.selectList(any())).thenReturn(List.of());
        when(processStageMapper.selectList(any())).thenReturn(List.of(row));

        var panorama = service.processPanorama();

        assertEquals(1, panorama.getStages().size());
        assertEquals(null, panorama.getStages().get(0).getId());
    }

    @Test
    void processGuidances_mapsRosterAndDeserializesGuidances() {
        FacEmergencyGuidanceRoster roster = new FacEmergencyGuidanceRoster();
        roster.setShiftGroup("乙班（白班）");
        roster.setSupervisor("李明辉（加氢制氢部值班长）");
        roster.setSupervisorPhone("138-0288-3456");
        roster.setBoardOperator("张建国（DCS 内操人员）");
        roster.setBoardOperatorPhone("139-0668-2233");
        roster.setFieldOperator("王安全（现场外操巡检员）");
        roster.setFieldOperatorPhone("137-0668-8378");
        when(guidanceRosterMapper.selectList(any())).thenReturn(List.of(roster));

        FacEmergencyNodeGuidance row = new FacEmergencyNodeGuidance();
        row.setNodeId("1");
        row.setDetailJson("{\"nodeId\":\"1\",\"nodeName\":\"节点 1：接警研判\","
                + "\"reportingChain\":[{\"step\":1,\"fromRole\":\"外操\",\"toRole\":\"内操\","
                + "\"method\":\"对讲机\",\"notice\":\"汇报\"}],"
                + "\"roleTasks\":[{\"roleName\":\"内操\",\"roleTitle\":\"DCS 内操\","
                + "\"personName\":\"张建国\",\"avatarIcon\":\"i\",\"phone\":\"139\","
                + "\"tasks\":[\"t1\",\"t2\"]}],\"generalNotice\":\"注意安全\"}");
        when(nodeGuidanceMapper.selectList(any())).thenReturn(List.of(row));

        var guidance = service.processGuidances();

        assertEquals("乙班（白班）", guidance.getDutyRoster().getShiftGroup());
        assertEquals(1, guidance.getGuidances().size());
        var node = guidance.getGuidances().get(0);
        assertEquals("1", node.getNodeId());
        assertEquals("对讲机", node.getReportingChain().get(0).getMethod());
        assertEquals(List.of("t1", "t2"), node.getRoleTasks().get(0).getTasks());
        assertEquals("注意安全", node.getGeneralNotice());
    }

    private static FacEmergencyAssistStat assistStat(String label, int value, String unit, String tone, int sortNo) {
        FacEmergencyAssistStat s = new FacEmergencyAssistStat();
        s.setLabel(label);
        s.setValue(value);
        s.setUnit(unit);
        s.setTone(tone);
        s.setSortNo(sortNo);
        return s;
    }

    @Test
    void assistStats_mapsAllFieldsAndSortsBySortNo() {
        // Mock 不执行 ORDER BY，故按 sortNo 升序预置（与 DB 返回顺序一致）
        when(assistStatMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                assistStat("应急预案", 15, "套", "blue", 1),
                assistStat("现场处置卡", 32, "张", "cyan", 2),
                assistStat("应急联络人", 18, "人", "green", 3),
                assistStat("可用消防水源", 306, "处", "orange", 4)));

        EmergencyAssistStatSummary summary = service.assistStats();

        assertEquals(4, summary.getItems().size());
        assertEquals("应急预案", summary.getItems().get(0).getLabel());
        assertEquals(15, summary.getItems().get(0).getValue());
        assertEquals("套", summary.getItems().get(0).getUnit());
        assertEquals("blue", summary.getItems().get(0).getTone());
        assertEquals("现场处置卡", summary.getItems().get(1).getLabel());
        assertEquals("应急联络人", summary.getItems().get(2).getLabel());
        assertEquals("可用消防水源", summary.getItems().get(3).getLabel());
        assertEquals(306, summary.getItems().get(3).getValue());
    }

    @Test
    void assistStats_handlesEmptyTable() {
        when(assistStatMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        assertTrue(service.assistStats().getItems().isEmpty());
    }
}
