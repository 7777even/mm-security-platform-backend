package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.dto.ClosedCase;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.CommandActionDetail;
import com.sinopec.mmsecurity.dto.DutyMember;
import com.sinopec.mmsecurity.dto.DutyRoster;
import com.sinopec.mmsecurity.dto.EmergencyCommandGroup;
import com.sinopec.mmsecurity.dto.EmergencyCommandInstruction;
import com.sinopec.mmsecurity.dto.EmergencyPhone;
import com.sinopec.mmsecurity.dto.EmergencyPhoneBook;
import com.sinopec.mmsecurity.dto.EmergencyResource;
import com.sinopec.mmsecurity.dto.EmergencyStrength;
import com.sinopec.mmsecurity.dto.KnowledgeItem;
import com.sinopec.mmsecurity.dto.KnowledgeList;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 应急资源服务。
 *
 * <p>数据来源分两类（见 openspec Change design ADR-1）：
 * <ul>
 *   <li><b>真实聚合</b>：{@link #closedCases()} 来自 fac_alarm(status=3 CLOSED)，随库变化。</li>
 *   <li><b>DB 参考配置</b>：strength / duty / phones / knowledge 为企业应急资源固定配置（通讯录、值班表、
 *       知识库、力量统计），由 V8 迁移至 DB 参考表（sys_emergency_strength / sys_emergency_phone /
 *       sys_knowledge_item / sys_duty_member），运营可在不改动代码的前提下维护。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final AlarmMapper alarmMapper;
    private final SysEmergencyStrengthMapper strengthMapper;
    private final SysEmergencyPhoneMapper phoneMapper;
    private final SysKnowledgeItemMapper knowledgeMapper;
    private final SysDutyMemberMapper dutyMapper;
    private final FacEmergencyCmdMapper cmdMapper;
    private final ObjectMapper objectMapper;

    /** 应急力量统计：来自 sys_emergency_strength 参考表 */
    public EmergencyStrength strength() {
        EmergencyStrength s = new EmergencyStrength();
        List<SysEmergencyStrength> rows = strengthMapper.selectList(null);
        List<EmergencyResource> resources = new ArrayList<>();
        for (SysEmergencyStrength r : rows) {
            EmergencyResource res = new EmergencyResource();
            res.setKind(r.getKind());
            res.setCount(r.getCount());
            res.setIcon(r.getIcon());
            resources.add(res);
        }
        s.setResources(resources);
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

    /** 应急值班值守表：来自 sys_duty_member 参考表（department / shift 随数据驱动） */
    public DutyRoster duty() {
        List<SysDutyMember> rows = dutyMapper.selectList(null);
        List<DutyMember> members = new ArrayList<>();
        for (SysDutyMember r : rows) {
            DutyMember m = new DutyMember();
            m.setId(r.getId() == null ? null : String.valueOf(r.getId()));
            m.setName(r.getName());
            m.setPhone(r.getPhone());
            m.setRole(r.getRole());
            m.setDepartment(r.getDepartment());
            m.setShift(r.getShift());
            members.add(m);
        }
        List<String> departments = members.stream()
                .map(DutyMember::getDepartment)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        String shift = members.isEmpty() ? "白班"
                : (members.get(0).getShift() == null ? "白班" : members.get(0).getShift());
        DutyRoster r = new DutyRoster();
        r.setDepartments(departments.isEmpty() ? List.of("全部") : departments);
        r.setShift(shift);
        r.setMembers(members);
        return r;
    }

    /** 应急电话通讯录：来自 sys_emergency_phone 参考表 */
    public EmergencyPhoneBook phones() {
        EmergencyPhoneBook b = new EmergencyPhoneBook();
        List<SysEmergencyPhone> rows = phoneMapper.selectList(null);
        List<EmergencyPhone> entries = new ArrayList<>();
        for (SysEmergencyPhone r : rows) {
            EmergencyPhone p = new EmergencyPhone();
            p.setId(r.getId() == null ? null : String.valueOf(r.getId()));
            p.setName(r.getName());
            p.setNumber(r.getNumber());
            p.setCategory(r.getCategory());
            entries.add(p);
        }
        b.setEntries(entries);
        return b;
    }

    /** 应急生产安全知识：来自 sys_knowledge_item 参考表 */
    public KnowledgeList knowledge() {
        KnowledgeList k = new KnowledgeList();
        List<SysKnowledgeItem> rows = knowledgeMapper.selectList(null);
        List<KnowledgeItem> items = new ArrayList<>();
        for (SysKnowledgeItem r : rows) {
            KnowledgeItem it = new KnowledgeItem();
            it.setId(r.getId() == null ? null : String.valueOf(r.getId()));
            it.setTitle(r.getTitle());
            it.setCount(r.getCount());
            it.setIcon(r.getIcon());
            items.add(it);
        }
        k.setItems(items);
        return k;
    }

    /** 应急指挥指令分组（固定/临时），按 tab 过滤。来自 fac_emergency_cmd 参考表。 */
    public List<EmergencyCommandGroup> commandGroups(String tab) {
        List<FacEmergencyCmd> rows = cmdMapper.selectList(
                new LambdaQueryWrapper<FacEmergencyCmd>().eq(FacEmergencyCmd::getGrpTab, tab)
                        .orderByAsc(FacEmergencyCmd::getGrpId));
        Map<String, EmergencyCommandGroup> groups = new LinkedHashMap<>();
        for (FacEmergencyCmd r : rows) {
            EmergencyCommandGroup g = groups.computeIfAbsent(r.getGrpId(), k -> {
                EmergencyCommandGroup ng = new EmergencyCommandGroup();
                ng.setId(k);
                ng.setLabel(r.getGrpLabel());
                ng.setItems(new ArrayList<>());
                return ng;
            });
            EmergencyCommandInstruction it = new EmergencyCommandInstruction();
            it.setId(r.getId());
            it.setType(r.getInstructionType());
            it.setName(r.getName());
            it.setLocation(r.getLocation());
            it.setStatus(r.getStatus());
            it.setActionLabel(r.getActionLabel());
            it.setDone(r.getDone());
            g.getItems().add(it);
        }
        return new ArrayList<>(groups.values());
    }

    /** 应急指挥指令行动详情：detail_json 反序列化为 CommandActionDetail 后补全标量字段。 */
    public CommandActionDetail commandDetail(String commandId) {
        FacEmergencyCmd row = cmdMapper.selectById(commandId);
        if (row == null) return null;
        CommandActionDetail d;
        try {
            d = objectMapper.readValue(row.getDetailJson(), CommandActionDetail.class);
        } catch (Exception e) {
            d = new CommandActionDetail();
        }
        if (d == null) {
            d = new CommandActionDetail();
        }
        d.setId(row.getId());
        d.setName(row.getName());
        d.setType(row.getInstructionType());
        d.setStatus(row.getStatus());
        d.setLocation(row.getLocation());
        return d;
    }
}
