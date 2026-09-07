package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.ClosedCase;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.DutyMember;
import com.sinopec.mmsecurity.dto.DutyRoster;
import com.sinopec.mmsecurity.dto.EmergencyPhone;
import com.sinopec.mmsecurity.dto.EmergencyPhoneBook;
import com.sinopec.mmsecurity.dto.EmergencyResource;
import com.sinopec.mmsecurity.dto.EmergencyStrength;
import com.sinopec.mmsecurity.dto.KnowledgeItem;
import com.sinopec.mmsecurity.dto.KnowledgeList;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 应急资源服务。
 *
 * <p>数据来源分两类（见 openspec Change design ADR-1）：
 * <ul>
 *   <li><b>真实聚合</b>：{@link #closedCases()} 来自 fac_alarm(status=3 CLOSED)，随库变化。</li>
 *   <li><b>静态参考配置</b>：strength / duty / phones / knowledge 为企业应急资源固定配置（通讯录、值班表、
 *       知识库、力量统计），本质为配置而非运行时遥测，故以内置参考列表提供，不造表、不写随机。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final AlarmMapper alarmMapper;

    /** 应急力量统计：静态参考配置 */
    public EmergencyStrength strength() {
        EmergencyStrength s = new EmergencyStrength();
        s.setResources(List.of(
                res("应急专家", 47, "UserFilled"),
                res("应急物资", 3510, "Box"),
                res("救援队伍", 12, "Soldier"),
                res("装备车辆", 28, "Van"),
                res("应急场所", 6, "LocationFilled"),
                res("医疗机构", 3, "FirstAidKit"),
                res("应急车辆", 18, "Truck"),
                res("消防设施", 42, "Fire")));
        return s;
    }

    /** 近期已结案：fac_alarm(status=3 CLOSED) 真实聚合 */
    public ClosedCaseList closedCases() {
        List<FacAlarm> closed = alarmMapper.selectList(new LambdaQueryWrapper<FacAlarm>()
                .eq(FacAlarm::getDeleted, 0)
                .eq(FacAlarm::getStatus, 3)
                .orderByDesc(FacAlarm::getOccurredAt));
        List<ClosedCase> cases = new ArrayList<>();
        for (FacAlarm a : closed) {
            ClosedCase c = new ClosedCase();
            c.setCaseId(a.getAlarmId());
            c.setTitle(a.getTitle());
            c.setLocation(a.getLocation());
            c.setClosedAt(a.getOccurredAt());
            c.setHandler("系统归档");
            cases.add(c);
        }
        ClosedCaseList list = new ClosedCaseList();
        list.setCases(cases);
        return list;
    }

    /** 应急值班值守表：静态参考配置 */
    public DutyRoster duty() {
        DutyRoster r = new DutyRoster();
        r.setDepartments(List.of("全部"));
        r.setShift("白班");
        r.setMembers(List.of(
                member("d1", "杨恒明", "13792536966", "值班领导", "全部", "白班"),
                member("d2", "李伟", "13800138000", "值班员", "全部", "白班")));
        return r;
    }

    /** 应急电话通讯录：静态参考配置 */
    public EmergencyPhoneBook phones() {
        EmergencyPhoneBook b = new EmergencyPhoneBook();
        b.setEntries(List.of(
                phone("ph1", "消防报警", "119", "消防"),
                phone("ph2", "医疗急救", "120", "医疗"),
                phone("ph3", "公安报警", "110", "公安"),
                phone("ph4", "厂内应急", "0668-2222111", "厂内应急"),
                phone("ph5", "保卫值班", "0668-2222333", "保卫值班")));
        return b;
    }

    /** 应急生产安全知识：静态参考配置 */
    public KnowledgeList knowledge() {
        KnowledgeList k = new KnowledgeList();
        k.setItems(List.of(
                item("k1", "岗位应急处置卡", 158, "Document"),
                item("k2", "火灾爆炸应急预案", 42, "Files"),
                item("k3", "气体泄漏处置", 67, "Warning")));
        return k;
    }

    private EmergencyResource res(String kind, int count, String icon) {
        EmergencyResource r = new EmergencyResource();
        r.setKind(kind);
        r.setCount(count);
        r.setIcon(icon);
        return r;
    }

    private DutyMember member(String id, String name, String phone, String role, String dept, String shift) {
        DutyMember m = new DutyMember();
        m.setId(id);
        m.setName(name);
        m.setPhone(phone);
        m.setRole(role);
        m.setDepartment(dept);
        m.setShift(shift);
        return m;
    }

    private EmergencyPhone phone(String id, String name, String number, String category) {
        EmergencyPhone p = new EmergencyPhone();
        p.setId(id);
        p.setName(name);
        p.setNumber(number);
        p.setCategory(category);
        return p;
    }

    private KnowledgeItem item(String id, String title, int count, String icon) {
        KnowledgeItem k = new KnowledgeItem();
        k.setId(id);
        k.setTitle(title);
        k.setCount(count);
        k.setIcon(icon);
        return k;
    }
}
