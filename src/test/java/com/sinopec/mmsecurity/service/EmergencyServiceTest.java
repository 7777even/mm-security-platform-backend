package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.ClosedCase;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.CommandActionDetail;
import com.sinopec.mmsecurity.dto.EmergencyCommandGroup;
import com.sinopec.mmsecurity.dto.EmergencyStrength;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.FacEmergencyCmd;
import com.sinopec.mmsecurity.entity.SysDutyMember;
import com.sinopec.mmsecurity.entity.SysEmergencyPhone;
import com.sinopec.mmsecurity.entity.SysEmergencyStrength;
import com.sinopec.mmsecurity.entity.SysKnowledgeItem;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyCmdMapper;
import com.sinopec.mmsecurity.mapper.SysDutyMemberMapper;
import com.sinopec.mmsecurity.mapper.SysEmergencyPhoneMapper;
import com.sinopec.mmsecurity.mapper.SysEmergencyStrengthMapper;
import com.sinopec.mmsecurity.mapper.SysKnowledgeItemMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * EmergencyService（纯 Mockito）：
 * strength/phones/knowledge/duty 来自 DB 参考表（sys_emergency_strength 等）；
 * closedCases 来自 fac_alarm(status=3) 真实聚合。
 */
class EmergencyServiceTest {

    private final AlarmMapper alarmMapper = mock(AlarmMapper.class);
    private final SysEmergencyStrengthMapper strengthMapper = mock(SysEmergencyStrengthMapper.class);
    private final SysEmergencyPhoneMapper phoneMapper = mock(SysEmergencyPhoneMapper.class);
    private final SysKnowledgeItemMapper knowledgeMapper = mock(SysKnowledgeItemMapper.class);
    private final SysDutyMemberMapper dutyMapper = mock(SysDutyMemberMapper.class);
    private final FacEmergencyCmdMapper cmdMapper = mock(FacEmergencyCmdMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EmergencyService service = new EmergencyService(
            alarmMapper, strengthMapper, phoneMapper, knowledgeMapper, dutyMapper,
            cmdMapper, objectMapper);

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
}
