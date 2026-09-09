package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.EmergencyPlanOptions;
import com.sinopec.mmsecurity.dto.EmergencyPlanTab;
import com.sinopec.mmsecurity.dto.PlanActionCard;
import com.sinopec.mmsecurity.dto.PlanCombatResource;
import com.sinopec.mmsecurity.dto.PlanInstance;
import com.sinopec.mmsecurity.dto.PlanMajorPhase;
import com.sinopec.mmsecurity.dto.PlanRiskEvent;
import com.sinopec.mmsecurity.dto.PlanSubPhase;
import com.sinopec.mmsecurity.dto.SelectableEmergencyPlan;
import com.sinopec.mmsecurity.entity.FacEmergencyPlan;
import com.sinopec.mmsecurity.entity.FacPlanActionCard;
import com.sinopec.mmsecurity.entity.FacPlanInstance;
import com.sinopec.mmsecurity.entity.FacPlanMajorPhase;
import com.sinopec.mmsecurity.entity.FacPlanResource;
import com.sinopec.mmsecurity.entity.FacPlanRiskEvent;
import com.sinopec.mmsecurity.entity.FacPlanSubPhase;
import com.sinopec.mmsecurity.mapper.FacEmergencyPlanMapper;
import com.sinopec.mmsecurity.mapper.FacPlanActionCardMapper;
import com.sinopec.mmsecurity.mapper.FacPlanInstanceMapper;
import com.sinopec.mmsecurity.mapper.FacPlanMajorPhaseMapper;
import com.sinopec.mmsecurity.mapper.FacPlanResourceMapper;
import com.sinopec.mmsecurity.mapper.FacPlanRiskEventMapper;
import com.sinopec.mmsecurity.mapper.FacPlanSubPhaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应急预案大屏服务。
 *
 * <p>数据来源为 V18 落地的 fac_emergency_plan / fac_plan_* 真实表，取代前端硬编码的
 * emergencyPlanSwitchMock 与 planMatrixMock。页签与事故类型/装置筛选字典为前端筛选维度
 * （字典中"中毒窒息"当前无对应预案，故与 mock 一致以常量维护），预案目录取自 fac_emergency_plan。
 */
@Service
@RequiredArgsConstructor
public class EmergencyPlanService {

    private static final String[][] TAB_DEFS = {
            {"disposal", "应急处置方案"},
            {"fire", "消防救援预案"},
            {"company", "公司级应急预案"},
            {"superior", "上级单位应急预案"}
    };

    private static final List<String> ACCIDENT_TYPES = List.of("全部类型", "火灾/爆炸", "泄漏", "中毒窒息");

    private static final List<String> FACILITIES = List.of("全部装置", "乙烯罐区", "乙烯裂解装置", "重油加氢装置");

    private final FacEmergencyPlanMapper emergencyPlanMapper;
    private final FacPlanInstanceMapper planInstanceMapper;
    private final FacPlanMajorPhaseMapper planMajorPhaseMapper;
    private final FacPlanSubPhaseMapper planSubPhaseMapper;
    private final FacPlanRiskEventMapper planRiskEventMapper;
    private final FacPlanResourceMapper planResourceMapper;
    private final FacPlanActionCardMapper planActionCardMapper;

    /** 预案切换面板：页签 + 事故类型/装置筛选字典 + 预案目录。 */
    public EmergencyPlanOptions options() {
        List<FacEmergencyPlan> plans = emergencyPlanMapper.selectList(
                new LambdaQueryWrapper<FacEmergencyPlan>().orderByAsc(FacEmergencyPlan::getSortNo));

        EmergencyPlanOptions options = new EmergencyPlanOptions();
        options.setTabs(Arrays.stream(TAB_DEFS).map(def -> {
            EmergencyPlanTab tab = new EmergencyPlanTab();
            tab.setKey(def[0]);
            tab.setLabel(def[1]);
            return tab;
        }).collect(Collectors.toList()));
        options.setAccidentTypes(ACCIDENT_TYPES);
        options.setFacilities(FACILITIES);
        options.setPlans(plans.stream().map(this::toSelectable).collect(Collectors.toList()));
        return options;
    }

    /** 预案矩阵：planId 为空或未命中时回退到默认（sort_no 最小）预案实例。 */
    public PlanInstance matrix(String planId) {
        FacPlanInstance instance = resolveInstance(planId);
        PlanInstance dto = new PlanInstance();
        if (instance == null) {
            dto.setMajorPhases(List.of());
            dto.setSubPhases(List.of());
            dto.setRiskEvents(List.of());
            dto.setResources(List.of());
            dto.setActionCards(List.of());
            return dto;
        }
        Long instanceId = instance.getId();
        dto.setId(instance.getPlanCode());
        dto.setTitle(instance.getTitle());
        dto.setDescription(instance.getDescription());
        dto.setMajorPhases(planMajorPhaseMapper.selectList(
                        new LambdaQueryWrapper<FacPlanMajorPhase>()
                                .eq(FacPlanMajorPhase::getInstanceId, instanceId)
                                .orderByAsc(FacPlanMajorPhase::getPhaseOrder))
                .stream().map(this::toMajorPhase).collect(Collectors.toList()));
        dto.setSubPhases(planSubPhaseMapper.selectList(
                        new LambdaQueryWrapper<FacPlanSubPhase>()
                                .eq(FacPlanSubPhase::getInstanceId, instanceId)
                                .orderByAsc(FacPlanSubPhase::getPhaseOrder))
                .stream().map(this::toSubPhase).collect(Collectors.toList()));
        dto.setRiskEvents(planRiskEventMapper.selectList(
                        new LambdaQueryWrapper<FacPlanRiskEvent>()
                                .eq(FacPlanRiskEvent::getInstanceId, instanceId))
                .stream().map(this::toRiskEvent).collect(Collectors.toList()));
        dto.setResources(planResourceMapper.selectList(
                        new LambdaQueryWrapper<FacPlanResource>()
                                .eq(FacPlanResource::getInstanceId, instanceId)
                                .orderByAsc(FacPlanResource::getSortNo))
                .stream().map(this::toCombatResource).collect(Collectors.toList()));
        dto.setActionCards(planActionCardMapper.selectList(
                        new LambdaQueryWrapper<FacPlanActionCard>()
                                .eq(FacPlanActionCard::getInstanceId, instanceId)
                                .orderByAsc(FacPlanActionCard::getSortNo))
                .stream().map(this::toActionCard).collect(Collectors.toList()));
        return dto;
    }

    private FacPlanInstance resolveInstance(String planId) {
        List<FacPlanInstance> instances = planInstanceMapper.selectList(
                new LambdaQueryWrapper<FacPlanInstance>().orderByAsc(FacPlanInstance::getSortNo));
        if (instances.isEmpty()) {
            return null;
        }
        if (planId != null && !planId.isBlank()) {
            return instances.stream()
                    .filter(i -> planId.equals(i.getPlanCode()))
                    .findFirst()
                    .orElse(instances.get(0));
        }
        return instances.get(0);
    }

    private SelectableEmergencyPlan toSelectable(FacEmergencyPlan entity) {
        SelectableEmergencyPlan dto = new SelectableEmergencyPlan();
        dto.setId(String.valueOf(entity.getId()));
        dto.setTab(entity.getTabKey());
        dto.setName(entity.getPlanName());
        dto.setAccidentType(entity.getAccidentType());
        dto.setFacility(entity.getFacility());
        return dto;
    }

    private PlanMajorPhase toMajorPhase(FacPlanMajorPhase entity) {
        PlanMajorPhase dto = new PlanMajorPhase();
        dto.setId(entity.getPhaseCode());
        dto.setName(entity.getPhaseName());
        dto.setOrder(entity.getPhaseOrder());
        dto.setUpgradeProcess(entity.getUpgradeProcess());
        return dto;
    }

    private PlanSubPhase toSubPhase(FacPlanSubPhase entity) {
        PlanSubPhase dto = new PlanSubPhase();
        dto.setId(entity.getPhaseCode());
        dto.setParentId(entity.getParentCode());
        dto.setName(entity.getPhaseName());
        dto.setOrder(entity.getPhaseOrder());
        dto.setProgress(entity.getProgress());
        return dto;
    }

    private PlanRiskEvent toRiskEvent(FacPlanRiskEvent entity) {
        PlanRiskEvent dto = new PlanRiskEvent();
        dto.setId(entity.getEventCode());
        dto.setSubPhaseId(entity.getSubPhaseCode());
        dto.setName(entity.getEventName());
        return dto;
    }

    private PlanCombatResource toCombatResource(FacPlanResource entity) {
        PlanCombatResource dto = new PlanCombatResource();
        dto.setId(entity.getResourceCode());
        dto.setName(entity.getResourceName());
        dto.setExpectedCount(entity.getExpectedCount());
        dto.setActualCount(entity.getActualCount());
        dto.setLeaderName(entity.getLeaderName());
        dto.setContactPhone(entity.getContactPhone());
        dto.setDuties(entity.getDuties());
        dto.setLon(entity.getLongitude());
        dto.setLat(entity.getLatitude());
        return dto;
    }

    private PlanActionCard toActionCard(FacPlanActionCard entity) {
        PlanActionCard dto = new PlanActionCard();
        dto.setId(entity.getCardCode());
        dto.setResourceId(entity.getResourceCode());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContentText());
        dto.setDescription(entity.getDescriptionText());
        dto.setStartSubPhaseId(entity.getStartSubPhaseCode());
        dto.setEndSubPhaseId(entity.getEndSubPhaseCode());
        dto.setRiskEventId(entity.getRiskEventCode());
        dto.setStatus(entity.getCardStatus());
        dto.setIsGlobal(entity.getIsGlobal());
        return dto;
    }
}
