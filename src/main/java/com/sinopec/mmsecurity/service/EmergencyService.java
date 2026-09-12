package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sinopec.mmsecurity.dto.ClosedCase;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.CommandActionDetail;
import com.sinopec.mmsecurity.dto.DispatchPersonnel;
import com.sinopec.mmsecurity.dto.DutyMember;
import com.sinopec.mmsecurity.dto.DutyRoster;
import com.sinopec.mmsecurity.dto.EmergencyCommandGroup;
import com.sinopec.mmsecurity.dto.EmergencyCommandInstruction;
import com.sinopec.mmsecurity.dto.EmergencyPhase;
import com.sinopec.mmsecurity.dto.EmergencyPhone;
import com.sinopec.mmsecurity.dto.EmergencyPhoneBook;
import com.sinopec.mmsecurity.dto.EmergencyProcessGuidance;
import com.sinopec.mmsecurity.dto.EmergencyProcessPanorama;
import com.sinopec.mmsecurity.dto.EmergencyResource;
import com.sinopec.mmsecurity.dto.EmergencyAssistStat;
import com.sinopec.mmsecurity.dto.EmergencyAssistStatSummary;
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
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.FacDispatchPersonnel;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final FacEmergencyAssistStatMapper assistStatMapper;
    private final SysEmergencyStrengthMapper strengthMapper;
    private final SysEmergencyPhoneMapper phoneMapper;
    private final SysKnowledgeItemMapper knowledgeMapper;
    private final SysDutyMemberMapper dutyMapper;
    private final FacDispatchPersonnelMapper dispatchPersonnelMapper;
    private final FacEmergencyCmdMapper cmdMapper;
    private final FacNodePhaseConfigMapper nodePhaseConfigMapper;
    private final FacEmergencyPhaseMapper emergencyPhaseMapper;
    private final FacEmergencyResponseModeMapper responseModeMapper;
    private final FacEmergencyProcessStageMapper processStageMapper;
    private final FacEmergencyNodeGuidanceMapper nodeGuidanceMapper;
    private final FacEmergencyGuidanceRosterMapper guidanceRosterMapper;
    private final ObjectMapper objectMapper;

    /**
     * 应急流程参考配置读穿缓存（TTL 5min 兜底）。
     * {@link #processPanorama()}/{@link #processGuidances()} 每次调用要物化 3~5 张静态参考表
     * （fac_emergency_phase / response_mode / process_stage / guidance_roster / node_guidance），
     * 这些表属「运营可维护但极少变更」的参考配置——缓存后命中即返回，避免重复全表物化。
     * 一致性窗口为 TTL；管理员变更应急流程配置后最多 5min 生效（与字典/菜单缓存同策略）。
     */
    private final Cache<String, Object> refConfigCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(16)
            .build();

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

    /**
     * 应急辅助信息统计（4 项 KPI：应急预案/现场处置卡/应急联络人/可用消防水源）。
     * 来自 V39 fac_emergency_assist_stat 参考表，取代前端 EmergencyAssistPanel 硬编码。
     */
    public EmergencyAssistStatSummary assistStats() {
        List<FacEmergencyAssistStat> rows = assistStatMapper.selectList(
                new LambdaQueryWrapper<FacEmergencyAssistStat>().orderByAsc(FacEmergencyAssistStat::getSortNo));
        EmergencyAssistStatSummary summary = new EmergencyAssistStatSummary();
        summary.setItems(rows.stream().map(row -> {
            EmergencyAssistStat item = new EmergencyAssistStat();
            item.setLabel(row.getLabel());
            item.setValue(row.getValue());
            item.setUnit(row.getUnit());
            item.setTone(row.getTone());
            return item;
        }).collect(Collectors.toList()));
        return summary;
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

    /**
     * 应急派单人员名册：来自 fac_dispatch_personnel 参考表（V35）。
     * 供告警详情「派单人员」下拉使用。不复用 sys_duty_member（出参经脱敏，无法辨识）
     * 与救援人员（375 人，过多不适合下拉）。
     */
    public List<DispatchPersonnel> dispatchPersonnel() {
        List<FacDispatchPersonnel> rows = dispatchPersonnelMapper.selectList(
                new LambdaQueryWrapper<FacDispatchPersonnel>()
                        .eq(FacDispatchPersonnel::getStatus, 1)
                        .orderByAsc(FacDispatchPersonnel::getSortNo));
        List<DispatchPersonnel> list = new ArrayList<>();
        for (FacDispatchPersonnel r : rows) {
            DispatchPersonnel p = new DispatchPersonnel();
            p.setId(r.getId());
            p.setName(r.getPersonName());
            p.setRole(r.getDutyRole());
            p.setDepartment(r.getDepartment());
            p.setPhone(r.getPhone());
            list.add(p);
        }
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

    // ------------------------------------------------------------------
    // 应急流程节点联动配置（V30 fac_node_phase_config）
    // ------------------------------------------------------------------

    /** 流程节点联动配置列表（按 sort_no 升序）。 */
    public List<NodePhaseConfig> nodePhaseConfigs() {
        return nodePhaseConfigMapper.selectList(new LambdaQueryWrapper<FacNodePhaseConfig>()
                        .orderByAsc(FacNodePhaseConfig::getSortNo)).stream()
                .map(this::toNodePhaseConfig).collect(Collectors.toList());
    }

    /**
     * 保存流程节点联动配置（按 node_id 整体 upsert：存在则更新，不存在则新建），返回落库后的全量列表。
     *
     * <p>入参为前端整表单次提交的全部节点配置；nodeId 缺失的条目跳过，新建行按输入顺序写 sort_no。</p>
     */
    public List<NodePhaseConfig> saveNodePhaseConfigs(List<NodePhaseConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return nodePhaseConfigs();
        }
        int index = 0;
        for (NodePhaseConfig config : configs) {
            index++;
            if (config == null || config.getNodeId() == null || config.getNodeId().isBlank()) {
                continue;
            }
            FacNodePhaseConfig entity = findNodePhaseConfig(config.getNodeId());
            boolean creating = entity == null;
            if (creating) {
                entity = new FacNodePhaseConfig();
                entity.setNodeId(config.getNodeId());
                entity.setNodeName(config.getNodeId());
                entity.setCameraAnchors("");
                entity.setBufferRadiusMeters(260);
                entity.setRightHiddenTabs("");
                entity.setLeftHiddenPanels("");
                entity.setDutyAutoRoster(Boolean.FALSE);
                entity.setSortNo(index);
            }
            applyNodePhaseConfig(entity, config);
            if (creating) {
                nodePhaseConfigMapper.insert(entity);
            } else {
                nodePhaseConfigMapper.updateById(entity);
            }
        }
        return nodePhaseConfigs();
    }

    private FacNodePhaseConfig findNodePhaseConfig(String nodeId) {
        return nodePhaseConfigMapper.selectList(new LambdaQueryWrapper<FacNodePhaseConfig>()
                        .eq(FacNodePhaseConfig::getNodeId, nodeId))
                .stream().findFirst().orElse(null);
    }

    private void applyNodePhaseConfig(FacNodePhaseConfig entity, NodePhaseConfig config) {
        if (config.getNodeName() != null && !config.getNodeName().isBlank()) {
            entity.setNodeName(config.getNodeName());
        }
        NodePhaseMapCamera camera = config.getMapCamera();
        if (camera != null) {
            if (camera.getAnchorPriorityList() != null) {
                entity.setCameraAnchors(String.join(",", camera.getAnchorPriorityList()));
            }
            entity.setCustomCenter(toCenterText(camera.getCustomCenter()));
            if (camera.getBufferRadiusMeters() != null) {
                entity.setBufferRadiusMeters(camera.getBufferRadiusMeters());
            }
        }
        if (config.getRightPanelHiddenTabs() != null) {
            entity.setRightHiddenTabs(String.join(",", config.getRightPanelHiddenTabs()));
        }
        if (config.getLeftPanelHiddenPanels() != null) {
            entity.setLeftHiddenPanels(String.join(",", config.getLeftPanelHiddenPanels()));
        }
        NodePhaseDuty duty = config.getDuty();
        if (duty != null && duty.getAutoRoster() != null) {
            entity.setDutyAutoRoster(duty.getAutoRoster());
        }
    }

    private NodePhaseConfig toNodePhaseConfig(FacNodePhaseConfig entity) {
        NodePhaseConfig config = new NodePhaseConfig();
        config.setNodeId(entity.getNodeId());
        config.setNodeName(entity.getNodeName());
        NodePhaseMapCamera camera = new NodePhaseMapCamera();
        camera.setAnchorPriorityList(splitCsv(entity.getCameraAnchors()));
        camera.setCustomCenter(parseCenter(entity.getCustomCenter()));
        camera.setBufferRadiusMeters(entity.getBufferRadiusMeters());
        config.setMapCamera(camera);
        config.setRightPanelHiddenTabs(splitCsv(entity.getRightHiddenTabs()));
        config.setLeftPanelHiddenPanels(splitCsv(entity.getLeftHiddenPanels()));
        NodePhaseDuty duty = new NodePhaseDuty();
        duty.setAutoRoster(Boolean.TRUE.equals(entity.getDutyAutoRoster()));
        config.setDuty(duty);
        return config;
    }

    /** 逗号串 → 去空白后的列表；空/null 返回空列表（前端按数组消费）。 */
    private static List<String> splitCsv(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (String part : text.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }
        return values;
    }

    /** [lon,lat] → "lon,lat"；null/空返回 null。 */
    private static String toCenterText(List<Double> center) {
        if (center == null || center.isEmpty()) {
            return null;
        }
        return center.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /** "lon,lat" → [lon,lat]；空或非法返回 null。 */
    private static List<Double> parseCenter(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        List<Double> center = new ArrayList<>();
        for (String part : text.split(",")) {
            try {
                center.add(Double.valueOf(part.trim()));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return center.isEmpty() ? null : center;
    }

    // ------------------------------------------------------------------
    // 应急流程全景（V31 fac_emergency_phase / response_mode / process_stage / node_guidance）
    // ------------------------------------------------------------------

    /** 流程全景聚合：5 阶段 + 4 响应模式 + 15 流程节点（节点嵌套结构由 detail_json 反序列化）。 */
    public EmergencyProcessPanorama processPanorama() {
        return (EmergencyProcessPanorama) refConfigCache.get("panorama", k -> computePanorama());
    }

    private EmergencyProcessPanorama computePanorama() {
        EmergencyProcessPanorama panorama = new EmergencyProcessPanorama();
        panorama.setPhases(emergencyPhaseMapper.selectList(
                        new LambdaQueryWrapper<FacEmergencyPhase>()
                                .orderByAsc(FacEmergencyPhase::getSortNo)).stream()
                .map(this::toEmergencyPhase).collect(Collectors.toList()));
        panorama.setResponseModes(responseModeMapper.selectList(
                        new LambdaQueryWrapper<FacEmergencyResponseMode>()
                                .orderByAsc(FacEmergencyResponseMode::getSortNo)).stream()
                .map(this::toResponseModeOption).collect(Collectors.toList()));
        panorama.setStages(processStageMapper.selectList(
                        new LambdaQueryWrapper<FacEmergencyProcessStage>()
                                .orderByAsc(FacEmergencyProcessStage::getSortNo)).stream()
                .map(row -> readJson(row.getDetailJson(), ProcessStage.class))
                .collect(Collectors.toList()));
        return panorama;
    }

    /** 节点处置指引聚合：实时值班表 + 9 条节点指引（嵌套结构由 detail_json 反序列化）。 */
    public EmergencyProcessGuidance processGuidances() {
        return (EmergencyProcessGuidance) refConfigCache.get("guidance", k -> computeGuidances());
    }

    private EmergencyProcessGuidance computeGuidances() {
        EmergencyProcessGuidance guidance = new EmergencyProcessGuidance();
        FacEmergencyGuidanceRoster roster = guidanceRosterMapper.selectList(
                        new LambdaQueryWrapper<FacEmergencyGuidanceRoster>()
                                .orderByAsc(FacEmergencyGuidanceRoster::getSortNo)).stream()
                .findFirst().orElse(null);
        guidance.setDutyRoster(roster == null ? null : toGuidanceDutyRoster(roster));
        guidance.setGuidances(nodeGuidanceMapper.selectList(
                        new LambdaQueryWrapper<FacEmergencyNodeGuidance>()
                                .orderByAsc(FacEmergencyNodeGuidance::getSortNo)).stream()
                .map(row -> readJson(row.getDetailJson(), NodeGuidance.class))
                .collect(Collectors.toList()));
        return guidance;
    }

    /** 失效参考配置缓存（供测试在用例间隔离，避免命中他例的桩数据）。 */
    void clearCaches() {
        refConfigCache.invalidateAll();
    }

    /**
     * detail_json 反序列化。
     *
     * <p>数据由脚本从 TS 常量直出、列非空，理论上必可解析；此处仍做防御：解析失败或为空时
     * 返回空实例（而非 null），避免前端拿到的列表里出现 null 元素导致渲染崩溃。</p>
     */
    private <T> T readJson(String json, Class<T> type) {
        T value = null;
        if (json != null && !json.isBlank()) {
            try {
                value = objectMapper.readValue(json, type);
            } catch (Exception ignored) {
                value = null;
            }
        }
        if (value != null) {
            return value;
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("无法实例化 " + type.getSimpleName(), e);
        }
    }

    private EmergencyPhase toEmergencyPhase(FacEmergencyPhase row) {
        EmergencyPhase dto = new EmergencyPhase();
        dto.setId(row.getPhaseCode());
        dto.setName(row.getPhaseName());
        dto.setStart(row.getStartStage());
        dto.setEnd(row.getEndStage());
        dto.setTone(row.getTone());
        return dto;
    }

    private ResponseModeOption toResponseModeOption(FacEmergencyResponseMode row) {
        ResponseModeOption dto = new ResponseModeOption();
        dto.setValue(row.getModeCode());
        dto.setLabel(row.getModeLabel());
        dto.setStageId(row.getStageId());
        return dto;
    }

    private GuidanceDutyRoster toGuidanceDutyRoster(FacEmergencyGuidanceRoster row) {
        GuidanceDutyRoster dto = new GuidanceDutyRoster();
        dto.setShiftGroup(row.getShiftGroup());
        dto.setSupervisor(row.getSupervisor());
        dto.setSupervisorPhone(row.getSupervisorPhone());
        dto.setBoardOperator(row.getBoardOperator());
        dto.setBoardOperatorPhone(row.getBoardOperatorPhone());
        dto.setFieldOperator(row.getFieldOperator());
        dto.setFieldOperatorPhone(row.getFieldOperatorPhone());
        return dto;
    }
}
