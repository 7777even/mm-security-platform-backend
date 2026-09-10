package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.EmergencyPlanOptions;
import com.sinopec.mmsecurity.dto.PlanActionCard;
import com.sinopec.mmsecurity.dto.PlanActionCardCreate;
import com.sinopec.mmsecurity.dto.PlanActionCardUpdate;
import com.sinopec.mmsecurity.dto.PlanInstance;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 应急预案服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class EmergencyPlanServiceTest {

    @Mock
    private FacEmergencyPlanMapper emergencyPlanMapper;
    @Mock
    private FacPlanInstanceMapper planInstanceMapper;
    @Mock
    private FacPlanMajorPhaseMapper planMajorPhaseMapper;
    @Mock
    private FacPlanSubPhaseMapper planSubPhaseMapper;
    @Mock
    private FacPlanRiskEventMapper planRiskEventMapper;
    @Mock
    private FacPlanResourceMapper planResourceMapper;
    @Mock
    private FacPlanActionCardMapper planActionCardMapper;

    @InjectMocks
    private EmergencyPlanService service;

    private static FacEmergencyPlan catalog(Long id, String tab, String name, int sortNo) {
        FacEmergencyPlan entity = new FacEmergencyPlan();
        entity.setId(id);
        entity.setTabKey(tab);
        entity.setPlanName(name);
        entity.setAccidentType("火灾/爆炸");
        entity.setFacility("乙烯罐区");
        entity.setSortNo(sortNo);
        return entity;
    }

    private static FacPlanInstance instance(Long id, String code, int sortNo) {
        FacPlanInstance entity = new FacPlanInstance();
        entity.setId(id);
        entity.setPlanCode(code);
        entity.setTitle("预案-" + code);
        entity.setDescription("预案-" + code + "-说明");
        entity.setSortNo(sortNo);
        return entity;
    }

    @Test
    void options_returnsTabsDictionariesAndCatalog() {
        when(emergencyPlanMapper.selectList(any())).thenReturn(List.of(
                catalog(1L, "disposal", "乙烯储罐火灾处置方案", 1),
                catalog(2L, "fire", "乙烯装置消防救援处置方案", 3)));

        EmergencyPlanOptions options = service.options();

        assertEquals(4, options.getTabs().size());
        assertEquals("disposal", options.getTabs().get(0).getKey());
        assertEquals("应急处置方案", options.getTabs().get(0).getLabel());
        assertEquals("superior", options.getTabs().get(3).getKey());
        assertEquals("全部类型", options.getAccidentTypes().get(0));
        assertEquals("全部装置", options.getFacilities().get(0));
        assertEquals(2, options.getPlans().size());
        assertEquals("1", options.getPlans().get(0).getId());
        assertEquals("disposal", options.getPlans().get(0).getTab());
        assertEquals("乙烯储罐火灾处置方案", options.getPlans().get(0).getName());
        assertEquals("乙烯罐区", options.getPlans().get(0).getFacility());
    }

    @Test
    void matrix_mapsRequestedPlanMatrix() {
        when(planInstanceMapper.selectList(any())).thenReturn(List.of(
                instance(1L, "plan-t103-002", 1), instance(2L, "plan-flood-003", 2)));
        FacPlanMajorPhase major = new FacPlanMajorPhase();
        major.setInstanceId(2L);
        major.setPhaseCode("m4_1");
        major.setPhaseName("1、预备值守与防汛动员");
        major.setPhaseOrder(1);
        major.setUpgradeProcess("启动领导带班值守");
        when(planMajorPhaseMapper.selectList(any())).thenReturn(List.of(major));
        FacPlanSubPhase sub = new FacPlanSubPhase();
        sub.setInstanceId(2L);
        sub.setPhaseCode("sp4_1_1");
        sub.setParentCode("m4_1");
        sub.setPhaseName("启动领导带班与预案动员");
        sub.setPhaseOrder(1);
        sub.setProgress(100);
        when(planSubPhaseMapper.selectList(any())).thenReturn(List.of(sub));
        FacPlanRiskEvent risk = new FacPlanRiskEvent();
        risk.setInstanceId(2L);
        risk.setEventCode("re4_1");
        risk.setSubPhaseCode("sp4_2_2");
        risk.setEventName("强降雨沙土流失导致排洪沟堵塞溢流");
        when(planRiskEventMapper.selectList(any())).thenReturn(List.of(risk));
        FacPlanResource resource = new FacPlanResource();
        resource.setInstanceId(2L);
        resource.setResourceCode("res-flood-1");
        resource.setResourceName("应急中心领导与带班室");
        resource.setExpectedCount("5");
        resource.setActualCount("4");
        resource.setLeaderName("王立新");
        resource.setContactPhone("0668-2288301");
        resource.setDuties("负责该状态下中心所有管理工作的指挥和协调");
        resource.setLongitude(110.8845);
        resource.setLatitude(21.6868);
        resource.setSortNo(1);
        when(planResourceMapper.selectList(any())).thenReturn(List.of(resource));
        FacPlanActionCard card = new FacPlanActionCard();
        card.setInstanceId(2L);
        card.setCardCode("c-flood-401");
        card.setResourceCode("res-flood-3");
        card.setTitle("使用漏电测试仪对配电房周边水体检测");
        card.setContentText("检测强排低洼淹没区有无动力漏电");
        card.setStartSubPhaseCode("sp4_3_2");
        card.setEndSubPhaseCode("sp4_4_2");
        card.setCardStatus("pending");
        card.setIsGlobal(true);
        card.setSortNo(8);
        when(planActionCardMapper.selectList(any())).thenReturn(List.of(card));

        PlanInstance matrix = service.matrix("plan-flood-003");

        assertEquals("plan-flood-003", matrix.getId());
        assertEquals("预案-plan-flood-003", matrix.getTitle());
        assertEquals("m4_1", matrix.getMajorPhases().get(0).getId());
        assertEquals(1, matrix.getMajorPhases().get(0).getOrder());
        assertEquals("启动领导带班值守", matrix.getMajorPhases().get(0).getUpgradeProcess());
        assertEquals("m4_1", matrix.getSubPhases().get(0).getParentId());
        assertEquals(100, matrix.getSubPhases().get(0).getProgress());
        assertEquals("sp4_2_2", matrix.getRiskEvents().get(0).getSubPhaseId());
        assertEquals("res-flood-1", matrix.getResources().get(0).getId());
        assertEquals("5", matrix.getResources().get(0).getExpectedCount());
        assertEquals("4", matrix.getResources().get(0).getActualCount());
        assertEquals(110.8845, matrix.getResources().get(0).getLon());
        assertEquals(21.6868, matrix.getResources().get(0).getLat());
        assertEquals("检测强排低洼淹没区有无动力漏电", matrix.getActionCards().get(0).getContent());
        assertEquals("sp4_4_2", matrix.getActionCards().get(0).getEndSubPhaseId());
        assertTrue(matrix.getActionCards().get(0).getIsGlobal());
        assertNull(matrix.getActionCards().get(0).getRiskEventId());
    }

    @Test
    void matrix_fallsBackToFirstInstanceWhenPlanIdMissingOrUnknown() {
        when(planInstanceMapper.selectList(any())).thenReturn(List.of(
                instance(1L, "plan-t103-002", 1), instance(2L, "plan-flood-003", 2)));
        when(planMajorPhaseMapper.selectList(any())).thenReturn(List.of());
        when(planSubPhaseMapper.selectList(any())).thenReturn(List.of());
        when(planRiskEventMapper.selectList(any())).thenReturn(List.of());
        when(planResourceMapper.selectList(any())).thenReturn(List.of());
        when(planActionCardMapper.selectList(any())).thenReturn(List.of());

        assertEquals("plan-t103-002", service.matrix(null).getId());
        assertEquals("plan-t103-002", service.matrix("").getId());
        assertEquals("plan-t103-002", service.matrix("plan-unknown").getId());
    }

    @Test
    void matrix_returnsEmptyBlocksWhenNoInstanceSeeded() {
        when(planInstanceMapper.selectList(any())).thenReturn(List.of());

        PlanInstance matrix = service.matrix(null);

        assertNull(matrix.getId());
        assertTrue(matrix.getMajorPhases().isEmpty());
        assertTrue(matrix.getSubPhases().isEmpty());
        assertTrue(matrix.getRiskEvents().isEmpty());
        assertTrue(matrix.getResources().isEmpty());
        assertTrue(matrix.getActionCards().isEmpty());
    }

    private static FacPlanActionCard card(Long id, String code, String status) {
        FacPlanActionCard c = new FacPlanActionCard();
        c.setId(id);
        c.setInstanceId(2L);
        c.setCardCode(code);
        c.setResourceCode("res-flood-3");
        c.setTitle("原标题");
        c.setStartSubPhaseCode("sp4_3_2");
        c.setEndSubPhaseCode("sp4_4_2");
        c.setCardStatus(status);
        return c;
    }

    @Test
    void createActionCard_insertsWithGeneratedCodeAndDefaults() {
        when(planInstanceMapper.selectList(any()))
                .thenReturn(List.of(instance(2L, "plan-flood-003", 2)));
        when(planActionCardMapper.selectList(any())).thenReturn(List.of());
        when(planActionCardMapper.insert(any())).thenReturn(1);

        PlanActionCardCreate in = new PlanActionCardCreate();
        in.setResourceId("res-flood-3");
        in.setTitle("启动排水泵");
        in.setStartSubPhaseId("sp4_3_2");
        in.setEndSubPhaseId("sp4_4_2");

        PlanActionCard created = service.createActionCard("plan-flood-003", in);

        assertEquals("启动排水泵", created.getTitle());
        assertEquals("res-flood-3", created.getResourceId());
        assertEquals("pending", created.getStatus());
        assertEquals(false, created.getIsGlobal());
        assertNotNull(created.getId());
        assertTrue(created.getId().startsWith("ac-"));
        verify(planActionCardMapper).insert(any(FacPlanActionCard.class));
    }

    @Test
    void createActionCard_returnsNullWhenInstanceMissing() {
        when(planInstanceMapper.selectList(any())).thenReturn(List.of());

        assertNull(service.createActionCard("plan-x", new PlanActionCardCreate()));
    }

    @Test
    void updateActionCard_onlyOverwritesProvidedFields() {
        when(planInstanceMapper.selectList(any()))
                .thenReturn(List.of(instance(2L, "plan-flood-003", 2)));
        when(planActionCardMapper.selectList(any())).thenReturn(List.of(card(9L, "c-flood-401", "pending")));
        when(planActionCardMapper.updateById(any())).thenReturn(1);

        PlanActionCardUpdate in = new PlanActionCardUpdate();
        in.setStatus("completed");

        PlanActionCard updated = service.updateActionCard("plan-flood-003", "c-flood-401", in);

        assertEquals("completed", updated.getStatus());
        assertEquals("原标题", updated.getTitle());
        verify(planActionCardMapper).updateById(any(FacPlanActionCard.class));
    }

    @Test
    void updateActionCard_returnsNullWhenCardMissing() {
        when(planInstanceMapper.selectList(any()))
                .thenReturn(List.of(instance(2L, "plan-flood-003", 2)));
        when(planActionCardMapper.selectList(any())).thenReturn(List.of());

        assertNull(service.updateActionCard("plan-flood-003", "nope", new PlanActionCardUpdate()));
    }

    @Test
    void deleteActionCard_okWhenFound() {
        when(planInstanceMapper.selectList(any()))
                .thenReturn(List.of(instance(2L, "plan-flood-003", 2)));
        when(planActionCardMapper.selectList(any())).thenReturn(List.of(card(9L, "c-flood-401", "pending")));
        when(planActionCardMapper.deleteById(9L)).thenReturn(1);

        assertTrue(service.deleteActionCard("plan-flood-003", "c-flood-401").getOk());
    }

    @Test
    void deleteActionCard_falseWhenInstanceMissing() {
        when(planInstanceMapper.selectList(any())).thenReturn(List.of());

        assertFalse(service.deleteActionCard("plan-x", "c-flood-401").getOk());
    }
}
