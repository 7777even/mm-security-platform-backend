package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.ClosedCase;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.EmergencyStrength;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.SysDutyMember;
import com.sinopec.mmsecurity.entity.SysEmergencyPhone;
import com.sinopec.mmsecurity.entity.SysEmergencyStrength;
import com.sinopec.mmsecurity.entity.SysKnowledgeItem;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.SysDutyMemberMapper;
import com.sinopec.mmsecurity.mapper.SysEmergencyPhoneMapper;
import com.sinopec.mmsecurity.mapper.SysEmergencyStrengthMapper;
import com.sinopec.mmsecurity.mapper.SysKnowledgeItemMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
    private final EmergencyService service = new EmergencyService(
            alarmMapper, strengthMapper, phoneMapper, knowledgeMapper, dutyMapper);

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
}
