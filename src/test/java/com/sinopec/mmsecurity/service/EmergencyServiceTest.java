package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.ClosedCase;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.CommandActionDetail;
import com.sinopec.mmsecurity.dto.EmergencyCommandGroup;
import com.sinopec.mmsecurity.dto.EmergencyResource;
import com.sinopec.mmsecurity.dto.EmergencyStrength;
import com.sinopec.mmsecurity.dto.NodePhaseConfig;
import com.sinopec.mmsecurity.dto.NodePhaseDuty;
import com.sinopec.mmsecurity.dto.NodePhaseMapCamera;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.FacEmergencyCmd;
import com.sinopec.mmsecurity.entity.FacEmergencyCommandRecord;
import com.sinopec.mmsecurity.entity.FacEmergencyGuidanceRoster;
import com.sinopec.mmsecurity.entity.FacEmergencyNodeGuidance;
import com.sinopec.mmsecurity.entity.FacEmergencyPhase;
import com.sinopec.mmsecurity.entity.FacEmergencyProcessStage;
import com.sinopec.mmsecurity.entity.FacEmergencyResponseMode;
import com.sinopec.mmsecurity.entity.FacNodePhaseConfig;
import com.sinopec.mmsecurity.entity.SysDutyMember;
import com.sinopec.mmsecurity.entity.SysEmergencyPhone;
import com.sinopec.mmsecurity.entity.FacBrigadeTeam;
import com.sinopec.mmsecurity.entity.FacRescueEquipment;
import com.sinopec.mmsecurity.entity.FacRescuePersonnel;
import com.sinopec.mmsecurity.entity.FacRescueVehicle;
import com.sinopec.mmsecurity.entity.SysEmergencyStrength;
import com.sinopec.mmsecurity.entity.SysEmergencyStrengthItem;
import com.sinopec.mmsecurity.entity.FacFireFacilityLedger;
import com.sinopec.mmsecurity.entity.SysKnowledgeItem;
import com.sinopec.mmsecurity.entity.FacEmergencyAssistStat;
import com.sinopec.mmsecurity.dto.EmergencyAssistStatSummary;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.FacBrigadeTeamMapper;
import com.sinopec.mmsecurity.mapper.FacRescueEquipmentMapper;
import com.sinopec.mmsecurity.mapper.FacRescuePersonnelMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyAssistStatMapper;
import com.sinopec.mmsecurity.mapper.FacDispatchPersonnelMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyCmdMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyCommandRecordMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyGuidanceRosterMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyNodeGuidanceMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyPhaseMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyProcessStageMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyResponseModeMapper;
import com.sinopec.mmsecurity.mapper.FacNodePhaseConfigMapper;
import com.sinopec.mmsecurity.mapper.SysDutyMemberMapper;
import com.sinopec.mmsecurity.mapper.SysEmergencyPhoneMapper;
import com.sinopec.mmsecurity.mapper.SysEmergencyStrengthMapper;
import com.sinopec.mmsecurity.mapper.SysEmergencyStrengthItemMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityLedgerMapper;
import com.sinopec.mmsecurity.mapper.SysKnowledgeItemMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    private final SysEmergencyStrengthItemMapper strengthItemMapper = mock(SysEmergencyStrengthItemMapper.class);
    private final FacFireFacilityLedgerMapper fireFacilityLedgerMapper = mock(FacFireFacilityLedgerMapper.class);
    private final FacRescuePersonnelMapper rescuePersonnelMapper = mock(FacRescuePersonnelMapper.class);
    private final FacRescueEquipmentMapper rescueEquipmentMapper = mock(FacRescueEquipmentMapper.class);
    private final FacRescueVehicleMapper rescueVehicleMapper = mock(FacRescueVehicleMapper.class);
    private final FacBrigadeTeamMapper brigadeTeamMapper = mock(FacBrigadeTeamMapper.class);
    private final SysEmergencyPhoneMapper phoneMapper = mock(SysEmergencyPhoneMapper.class);
    private final SysKnowledgeItemMapper knowledgeMapper = mock(SysKnowledgeItemMapper.class);
    private final SysDutyMemberMapper dutyMapper = mock(SysDutyMemberMapper.class);
    private final FacDispatchPersonnelMapper dispatchPersonnelMapper =
            mock(FacDispatchPersonnelMapper.class);
    private final FacEmergencyCmdMapper cmdMapper = mock(FacEmergencyCmdMapper.class);
    private final FacEmergencyCommandRecordMapper commandRecordMapper =
            mock(FacEmergencyCommandRecordMapper.class);
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
            alarmMapper, assistStatMapper, strengthMapper, strengthItemMapper, fireFacilityLedgerMapper,
            rescuePersonnelMapper, rescueEquipmentMapper, rescueVehicleMapper, brigadeTeamMapper,
            phoneMapper, knowledgeMapper, dutyMapper,
            dispatchPersonnelMapper, cmdMapper, commandRecordMapper, nodePhaseConfigMapper,
            emergencyPhaseMapper,
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
    void strength_aggregatesFromLedgerAndKeepsManualForStatisticalKinds() {
        when(strengthMapper.selectList(null)).thenReturn(List.of(
                strength("应急专家", 47, "UserFilled"),
                strength("应急物资", 3510, "Box"),
                strength("救援队伍", 12, "Soldier"),
                strength("救援装备", 28, "Van"),
                strength("应急场所", 6, "LocationFilled"),
                strength("应急车辆", 18, "Truck")));
        FacRescuePersonnel p1 = new FacRescuePersonnel();
        p1.setPersonName("张伟");
        p1.setPersonRole("救援专家");
        p1.setSquadron("一中队");
        when(rescuePersonnelMapper.selectList(any())).thenReturn(List.of(
                p1, new FacRescuePersonnel(), new FacRescuePersonnel()));
        FacRescueEquipment eq = new FacRescueEquipment();
        eq.setEquipName("正压式空气呼吸器");
        eq.setEquipModel("RHZK6.8");
        when(rescueEquipmentMapper.selectList(any())).thenReturn(List.of(eq));
        FacRescueVehicle v1 = new FacRescueVehicle();
        v1.setPlate("粤KX1234");
        v1.setVehicleType("泡沫消防车");
        when(rescueVehicleMapper.selectList(any())).thenReturn(List.of(
                v1, new FacRescueVehicle()));
        FacBrigadeTeam team = new FacBrigadeTeam();
        team.setTeamName("一中队");
        team.setArea("炼油区");
        when(brigadeTeamMapper.selectList(any())).thenReturn(List.of(team));
        SysEmergencyStrengthItem place = new SysEmergencyStrengthItem();
        place.setKind("应急场所");
        place.setName("中心控制室前应急集结点");
        place.setMeta("中心控制室广场 · 可容纳 200 人");
        when(strengthItemMapper.selectList(any())).thenReturn(List.of(place));

        EmergencyStrength s = service.strength();

        assertEquals(6, s.getResources().size());
        assertEquals("应急专家", s.getResources().get(0).getKind());
        assertEquals(3, s.getResources().get(0).getCount(), "应急专家取人员台账计数，覆盖手填 47");
        assertEquals(3510, s.getResources().get(1).getCount(), "应急物资为统计口径，保留手填 3510");
        assertEquals(1, s.getResources().get(2).getCount(), "救援队伍取队伍台账计数，覆盖手填 12");
        assertEquals(1, s.getResources().get(3).getCount(), "救援装备取装备台账计数，覆盖手填 28");
        assertEquals(1, s.getResources().get(4).getCount(), "应急场所计数改从参考表 sys_emergency_strength_item 取，与明细行数一致");
        assertEquals(2, s.getResources().get(5).getCount(), "应急车辆取车辆台账计数，覆盖手填 18");

        // 明细预览：ledger 源类别填充真实项（name + 拼接 meta），无明细源类别为 null。
        EmergencyResource expert = s.getResources().get(0);
        assertEquals(3, expert.getItems().size());
        assertEquals("张伟", expert.getItems().get(0).getName());
        assertEquals("救援专家 · 一中队", expert.getItems().get(0).getMeta(), "meta 由岗位/中队拼接");
        assertNull(s.getResources().get(1).getItems(), "应急物资为统计口径，items 为 null");
        assertEquals("炼油区", s.getResources().get(2).getItems().get(0).getMeta());
        assertEquals("正压式空气呼吸器", s.getResources().get(3).getItems().get(0).getName(), "救援装备取装备台账明细");
        assertEquals("粤KX1234", s.getResources().get(5).getItems().get(0).getName());
        // 应急场所：从参考表 sys_emergency_strength_item 取明细（不再是 null）
        EmergencyResource placeRes = s.getResources().get(4);
        assertEquals(1, placeRes.getItems().size(), "应急场所从参考表取明细");
        assertEquals("中心控制室前应急集结点", placeRes.getItems().get(0).getName());
    }

    @Test
    void strength_fireFacilityAndMedicalItemsPopulated() {
        when(strengthMapper.selectList(null)).thenReturn(List.of(
                strength("消防设施", 42, "Fire"),
                strength("医疗机构", 3, "FirstAidKit")));
        FacFireFacilityLedger f1 = new FacFireFacilityLedger();
        f1.setFacilityName("1#泡沫站");
        f1.setLocationName("中心控制室");
        f1.setFacilityType("固定泡沫灭火设施");
        FacFireFacilityLedger f2 = new FacFireFacilityLedger();
        f2.setFacilityName("2#消防水炮");
        f2.setLocationName("乙烯区");
        f2.setFacilityType("消防水炮");
        when(fireFacilityLedgerMapper.selectList(any())).thenReturn(List.of(f1, f2));
        SysEmergencyStrengthItem m1 = new SysEmergencyStrengthItem();
        m1.setKind("医疗机构");
        m1.setName("厂区医务室");
        m1.setMeta("综合办公楼 1 层 · 全科门诊");
        when(strengthItemMapper.selectList(any())).thenReturn(List.of(m1));

        EmergencyStrength s = service.strength();

        EmergencyResource fire = s.getResources().get(0);
        assertEquals("消防设施", fire.getKind());
        assertEquals(2, fire.getCount(), "消防设施计数由 fac_fire_facility_ledger 实时计数覆盖（取代手填 42）");
        assertEquals(2, fire.getItems().size(), "消防设施从真实台账取明细");
        assertEquals("1#泡沫站", fire.getItems().get(0).getName());
        assertEquals("中心控制室 · 固定泡沫灭火设施", fire.getItems().get(0).getMeta());

        EmergencyResource medical = s.getResources().get(1);
        assertEquals("医疗机构", medical.getKind());
        assertEquals(1, medical.getItems().size(), "医疗机构从参考表 sys_emergency_strength_item 取明细");
        assertEquals("厂区医务室", medical.getItems().get(0).getName());
    }

    @Test
    void strength_returnsZeroWhenLedgerEmpty() {
        when(strengthMapper.selectList(null)).thenReturn(List.of(strength("应急专家", 47, "UserFilled")));

        EmergencyStrength s = service.strength();

        assertEquals(1, s.getResources().size());
        assertEquals(0, s.getResources().get(0).getCount(), "台账为空即 0，不回退手填值以免造假");
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
        SysKnowledgeItem k1 = knowledge("岗位应急处置卡", 158, "Document");
        k1.setDescription("岗位员工应掌握的应急处置卡片要点。");
        when(knowledgeMapper.selectList(null)).thenReturn(List.of(
                k1,
                knowledge("火灾爆炸应急预案", 42, "Files"),
                knowledge("气体泄漏处置", 67, "Warning")));
        var items = service.knowledge().getItems();
        assertEquals(3, items.size());
        assertEquals("岗位员工应掌握的应急处置卡片要点。", items.get(0).getDescription(),
                "透传 sys_knowledge_item.description 作为真实分类说明");
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
    void commandGroups_issuedRecordAppendedAsNewCard() {
        // 模板表为空，仅有一条管理端自由编码下发的留痕记录
        when(cmdMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(commandRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(record("CMD-20260918-001", "罐区泡沫联锁", "应急调度", "已下发")));

        List<EmergencyCommandGroup> groups = service.commandGroups("fixed");
        assertEquals(1, groups.size());
        assertEquals("issued", groups.get(0).getId());
        assertEquals("下发指令", groups.get(0).getLabel());
        assertEquals(1, groups.get(0).getItems().size());
        var it = groups.get(0).getItems().get(0);
        assertEquals("CMD-20260918-001", it.getId());
        assertEquals("罐区泡沫联锁", it.getName());
        // 已下发 → 契约枚举「待处置」（已发出、待处置推进）
        assertEquals("待处置", it.getStatus());
        assertFalse(it.getDone());
    }

    @Test
    void commandGroups_recordOverlayUpdatesTemplateStatus() {
        FacEmergencyCmd r1 = new FacEmergencyCmd();
        r1.setId("d1");
        r1.setGrpId("dispatch");
        r1.setGrpLabel("一键调度");
        r1.setGrpTab("fixed");
        r1.setInstructionType("任务");
        r1.setName("调度消防一队");
        r1.setLocation("中海壳牌石油化工有限公司");
        r1.setStatus("待处置");
        r1.setActionLabel("一键派发");
        r1.setDone(false);
        when(cmdMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(r1));
        when(commandRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(record("d1", "调度消防一队", "任务", "已完成")));

        List<EmergencyCommandGroup> groups = service.commandGroups("fixed");
        assertEquals(1, groups.size());
        // 命中模板：覆写状态而非新增「下发指令」组
        assertEquals("已处置", groups.get(0).getItems().get(0).getStatus());
        assertTrue(groups.get(0).getItems().get(0).getDone());
    }

    @Test
    void commandGroups_latestRecordWinsForSameCode() {
        when(cmdMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(commandRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(
                        record("CMD-002", "通知消防队伍", "通知", "待下发"),
                        record("CMD-002", "通知消防队伍", "通知", "已完成")));

        List<EmergencyCommandGroup> groups = service.commandGroups("temp");
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).getItems().size());
        var it = groups.get(0).getItems().get(0);
        // 同码多条：最新一条（id 更大）生效
        assertEquals("已处置", it.getStatus());
        assertTrue(it.getDone());
    }

    private static long recordIdSeq = 0;

    private static FacEmergencyCommandRecord record(String code, String name, String kind,
            String currStatus) {
        FacEmergencyCommandRecord r = new FacEmergencyCommandRecord();
        r.setId(++recordIdSeq);
        r.setCommandCode(code);
        r.setCommandName(name);
        r.setCommandKind(kind);
        r.setCurrStatus(currStatus);
        r.setTarget("中海壳牌石油化工有限公司");
        r.setDeleted(0);
        return r;
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
        s.setStatValue(value);
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

    @Test
    void referenceTables_cacheCollapsesRepeatedCalls() {
        when(strengthMapper.selectList(null)).thenReturn(List.of(strength("应急专家", 47, "UserFilled")));
        when(dutyMapper.selectList(null)).thenReturn(List.of(duty("杨恒明", "137", "值班领导", "全部", "白班")));
        when(phoneMapper.selectList(null)).thenReturn(List.of(phone("消防", "119", "消防")));
        when(knowledgeMapper.selectList(null)).thenReturn(List.of(knowledge("卡", 1, "Doc")));

        // 每个参考表方法连续调用两次，缓存应将 DB 查询合并为 1 次
        service.strength();
        service.strength();
        service.duty();
        service.duty();
        service.phones();
        service.phones();
        service.knowledge();
        service.knowledge();

        verify(strengthMapper).selectList(null);
        verify(dutyMapper).selectList(null);
        verify(phoneMapper).selectList(null);
        verify(knowledgeMapper).selectList(null);
    }
}
