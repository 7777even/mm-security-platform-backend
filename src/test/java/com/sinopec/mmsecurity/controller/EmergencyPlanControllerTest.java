package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.EmergencyPlanCatalogItem;
import com.sinopec.mmsecurity.dto.EmergencyPlanCatalogSummary;
import com.sinopec.mmsecurity.dto.EmergencyPlanDetailField;
import com.sinopec.mmsecurity.dto.EmergencyPlanDetailSection;
import com.sinopec.mmsecurity.dto.EmergencyPlanDetailSummary;
import com.sinopec.mmsecurity.dto.EmergencyPlanOptions;
import com.sinopec.mmsecurity.dto.EmergencyPlanTab;
import com.sinopec.mmsecurity.dto.PlanActionCard;
import com.sinopec.mmsecurity.dto.PlanCombatResource;
import com.sinopec.mmsecurity.dto.PlanInstance;
import com.sinopec.mmsecurity.dto.PlanMajorPhase;
import com.sinopec.mmsecurity.dto.SelectableEmergencyPlan;
import com.sinopec.mmsecurity.service.EmergencyPlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 应急预案大屏接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class EmergencyPlanControllerTest {

    @Mock
    private EmergencyPlanService service;

    @InjectMocks
    private EmergencyPlanController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void options_returnsTabsDictionariesAndPlans() throws Exception {
        EmergencyPlanOptions options = new EmergencyPlanOptions();
        EmergencyPlanTab tab = new EmergencyPlanTab();
        tab.setKey("disposal");
        tab.setLabel("应急处置方案");
        options.setTabs(List.of(tab));
        options.setAccidentTypes(List.of("全部类型", "火灾/爆炸"));
        options.setFacilities(List.of("全部装置", "乙烯罐区"));
        SelectableEmergencyPlan plan = new SelectableEmergencyPlan();
        plan.setId("1");
        plan.setTab("disposal");
        plan.setName("乙烯储罐火灾处置方案");
        plan.setAccidentType("火灾/爆炸");
        plan.setFacility("乙烯罐区");
        options.setPlans(List.of(plan));
        when(service.options()).thenReturn(options);

        mvc().perform(get("/api/v1/emergency-plans/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.tabs[0].key").value("disposal"))
                .andExpect(jsonPath("$.data.accidentTypes[0]").value("全部类型"))
                .andExpect(jsonPath("$.data.facilities[1]").value("乙烯罐区"))
                .andExpect(jsonPath("$.data.plans[0].name").value("乙烯储罐火灾处置方案"))
                .andExpect(jsonPath("$.data.plans[0].facility").value("乙烯罐区"));
    }

    @Test
    void matrix_returnsPlanByPlanId() throws Exception {
        PlanInstance instance = new PlanInstance();
        instance.setId("plan-flood-003");
        instance.setTitle("应急救援中心防洪防内涝应急专项预案");
        instance.setDescription("应对厂区降雨量达到150毫米至200毫米状态下的实战响应矩阵。");
        PlanMajorPhase major = new PlanMajorPhase();
        major.setId("m4_1");
        major.setName("1、预备值守与防汛动员");
        major.setOrder(1);
        major.setUpgradeProcess("启动领导带班值守");
        instance.setMajorPhases(List.of(major));
        PlanCombatResource resource = new PlanCombatResource();
        resource.setId("res-flood-1");
        resource.setName("应急中心领导与带班室");
        resource.setExpectedCount("5");
        resource.setActualCount("5");
        resource.setDuties("负责该状态下中心所有管理工作的指挥和协调");
        resource.setLon(110.8845);
        resource.setLat(21.6868);
        instance.setResources(List.of(resource));
        PlanActionCard card = new PlanActionCard();
        card.setId("c-flood-401");
        card.setResourceId("res-flood-3");
        card.setTitle("使用漏电测试仪对配电房周边水体检测");
        card.setStartSubPhaseId("sp4_3_2");
        card.setEndSubPhaseId("sp4_4_2");
        card.setStatus("pending");
        card.setIsGlobal(true);
        instance.setActionCards(List.of(card));
        when(service.matrix("plan-flood-003")).thenReturn(instance);

        mvc().perform(get("/api/v1/emergency-plans/matrix").param("planId", "plan-flood-003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("plan-flood-003"))
                .andExpect(jsonPath("$.data.majorPhases[0].order").value(1))
                .andExpect(jsonPath("$.data.resources[0].expectedCount").value("5"))
                .andExpect(jsonPath("$.data.resources[0].lat").value(21.6868))
                .andExpect(jsonPath("$.data.actionCards[0].status").value("pending"))
                .andExpect(jsonPath("$.data.actionCards[0].isGlobal").value(true));
    }

    @Test
    void createActionCard_returnsCreatedCard() throws Exception {
        PlanActionCard card = new PlanActionCard();
        card.setId("ac-new-1");
        card.setResourceId("res-flood-3");
        card.setTitle("启动排水泵");
        card.setStartSubPhaseId("sp4_3_2");
        card.setEndSubPhaseId("sp4_4_2");
        card.setStatus("pending");
        when(service.createActionCard(eq("plan-flood-003"), any())).thenReturn(card);

        mvc().perform(post("/api/v1/emergency-plans/plan-flood-003/action-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceId\":\"res-flood-3\",\"title\":\"启动排水泵\","
                                + "\"startSubPhaseId\":\"sp4_3_2\",\"endSubPhaseId\":\"sp4_4_2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("ac-new-1"))
                .andExpect(jsonPath("$.data.status").value("pending"));
    }

    @Test
    void updateActionCard_returnsUpdatedCard() throws Exception {
        PlanActionCard card = new PlanActionCard();
        card.setId("c-flood-401");
        card.setStatus("completed");
        when(service.updateActionCard(eq("plan-flood-003"), eq("c-flood-401"), any())).thenReturn(card);

        mvc().perform(put("/api/v1/emergency-plans/plan-flood-003/action-cards/c-flood-401")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"completed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("completed"));
    }

    @Test
    void deleteActionCard_returnsOkResult() throws Exception {
        DeleteResult result = new DeleteResult();
        result.setOk(true);
        when(service.deleteActionCard("plan-flood-003", "c-flood-401")).thenReturn(result);

        mvc().perform(delete("/api/v1/emergency-plans/plan-flood-003/action-cards/c-flood-401"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.ok").value(true));
    }

    @Test
    void catalog_returnsPlanHierarchyRows() throws Exception {
        EmergencyPlanCatalogSummary summary = new EmergencyPlanCatalogSummary();
        EmergencyPlanCatalogItem company = new EmergencyPlanCatalogItem();
        company.setId("company");
        company.setLabel("公司级预案");
        company.setPlanName("茂名石化应急预案");
        company.setCanSwitch(true);
        company.setIsCurrent(true);
        summary.setItems(List.of(company));
        when(service.planCatalog()).thenReturn(summary);

        mvc().perform(get("/api/v1/emergency-plans/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].id").value("company"))
                .andExpect(jsonPath("$.data.items[0].label").value("公司级预案"))
                .andExpect(jsonPath("$.data.items[0].planName").value("茂名石化应急预案"))
                .andExpect(jsonPath("$.data.items[0].canSwitch").value(true))
                .andExpect(jsonPath("$.data.items[0].isCurrent").value(true));
    }

    @Test
    void catalogDetail_returnsSectionedFields() throws Exception {
        EmergencyPlanDetailSummary summary = new EmergencyPlanDetailSummary();
        EmergencyPlanDetailSection section = new EmergencyPlanDetailSection();
        section.setTitle("基础信息");
        EmergencyPlanDetailField field = new EmergencyPlanDetailField();
        field.setLabel("所属组织");
        field.setValue("茂名石化应急指挥中心");
        section.setFields(List.of(field));
        summary.setSections(List.of(section));
        when(service.planCatalogDetail()).thenReturn(summary);

        mvc().perform(get("/api/v1/emergency-plans/catalog-detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.sections[0].title").value("基础信息"))
                .andExpect(jsonPath("$.data.sections[0].fields[0].label").value("所属组织"))
                .andExpect(jsonPath("$.data.sections[0].fields[0].value").value("茂名石化应急指挥中心"));
    }
}
