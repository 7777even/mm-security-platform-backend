package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.EmergencyEventCreateRequest;
import com.sinopec.mmsecurity.dto.EmergencyEventGroup;
import com.sinopec.mmsecurity.dto.EmergencyEventItem;
import com.sinopec.mmsecurity.dto.EmergencyEventWeatherMeta;
import com.sinopec.mmsecurity.dto.EvacuationPerson;
import com.sinopec.mmsecurity.service.EmergencyEventService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 应急事件大屏接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class EmergencyEventControllerTest {

    @Mock
    private EmergencyEventService service;

    @InjectMocks
    private EmergencyEventController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void eventGroups_returnsGroupedEvents() throws Exception {
        EmergencyEventItem item = new EmergencyEventItem();
        item.setId(1L);
        item.setAreaCode("chemical");
        item.setTitle("乙烯裂解炉炉管泄漏着火");
        item.setLocation("化工区乙烯裂解装置东北侧");
        item.setTime("2026-03-17 14:21:54");
        item.setReported(true);
        item.setStatus("processing");
        item.setStatusLabel("泄漏着火");
        item.setLeft("47.1%");
        item.setTop("22.6%");
        item.setLongitude(110.880710);
        item.setLatitude(21.684459);
        item.setKind("EVENT");
        item.setEventCategory("default");
        item.setHazardSourceLevel("二级");
        EmergencyEventGroup group = new EmergencyEventGroup();
        group.setId("phone");
        group.setLabel("消防电话报警");
        group.setEvents(List.of(item));
        when(service.eventGroups(any())).thenReturn(List.of(group));

        mvc().perform(get("/api/v1/emergency-events").param("scene", "FIRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("phone"))
                .andExpect(jsonPath("$.data[0].label").value("消防电话报警"))
                .andExpect(jsonPath("$.data[0].events[0].title").value("乙烯裂解炉炉管泄漏着火"))
                .andExpect(jsonPath("$.data[0].events[0].statusLabel").value("泄漏着火"))
                .andExpect(jsonPath("$.data[0].events[0].left").value("47.1%"))
                .andExpect(jsonPath("$.data[0].events[0].hazardSourceLevel").value("二级"))
                .andExpect(jsonPath("$.data[0].events[0].longitude").value(110.880710));
    }

    @Test
    void eventGroups_withoutSceneReturnsAllScenes() throws Exception {
        EmergencyEventItem weather = new EmergencyEventItem();
        weather.setId(100L);
        weather.setTitle("台风沙迦防台防汛工作");
        weather.setKind("EVENT");
        weather.setEventCategory("extremeWeather");
        EmergencyEventWeatherMeta meta = new EmergencyEventWeatherMeta();
        meta.setWeatherType("台风");
        meta.setWarningLevel("Ⅱ级");
        meta.setAffectedArea("全厂范围");
        meta.setMonitoringPeriod("2026-06-25 08:00 至 2026-06-26 08:00");
        meta.setSource("气象台");
        meta.setMeasures("启动防台防汛Ⅱ级响应，重点监测内涝与排涝设施");
        weather.setWeatherMeta(meta);
        EmergencyEventGroup group = new EmergencyEventGroup();
        group.setId("extreme-weather");
        group.setLabel("极端天气");
        group.setEvents(List.of(weather));
        when(service.eventGroups(eq(null))).thenReturn(List.of(group));

        mvc().perform(get("/api/v1/emergency-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].events[0].eventCategory").value("extremeWeather"))
                .andExpect(jsonPath("$.data[0].events[0].weatherMeta.weatherType").value("台风"))
                .andExpect(jsonPath("$.data[0].events[0].weatherMeta.source").value("气象台"));
    }

    @Test
    void evacuationPeople_returnsRosterWithRouteProgress() throws Exception {
        EvacuationPerson person = new EvacuationPerson();
        person.setId(1L);
        person.setName("张建");
        person.setOrg("生产管理部");
        person.setJob("班长");
        person.setRouteProgress(0.047619);
        when(service.evacuationPeople(eq(20))).thenReturn(List.of(person));

        mvc().perform(get("/api/v1/emergency-events/evacuation-people").param("count", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("张建"))
                .andExpect(jsonPath("$.data[0].org").value("生产管理部"))
                .andExpect(jsonPath("$.data[0].job").value("班长"))
                .andExpect(jsonPath("$.data[0].routeProgress").value(0.047619));
    }

    /**
     * 新增事件成功路径：合法入参 → 200 且返回后端生成的真实 id。鉴权（@RequireAuth）由独立拦截器
     * RequireAuthInterceptor 负责，standalone MockMvc 不加载拦截器，故此处不校验 401（由
     * RequireAuthInterceptorTest 覆盖；真实链路 401 在实服冒烟中验证）。
     */
    @Test
    void create_persistsAndReturnsEventWithId() throws Exception {
        EmergencyEventItem saved = new EmergencyEventItem();
        saved.setId(42L);
        when(service.create(any(EmergencyEventCreateRequest.class))).thenReturn(saved);

        mvc().perform(post("/api/v1/emergency-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"scene":"FIRE","kind":"event","eventCategory":"default",\
                            "title":"催化裂化装置新增泄漏","location":"炼油一部 1#催化装置",\
                            "description":"现场人员上报，联系电话 138xxxx，暂无伤亡。",\
                            "eventTime":"2026-09-20 14:00:00","leftPercent":"48.3%","topPercent":"36.1%",\
                            "longitude":110.123456,"latitude":21.654321}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(42));
        verify(service).create(any(EmergencyEventCreateRequest.class));
    }

    /** 新增事件参数校验失败：缺必填字段 → B3 包络 code!=0（HTTP 仍 200，由 GlobalExceptionHandler 收敛），且 service.create 不被调用。 */
    @Test
    void create_validationFailure_missingRequiredField_returnsErrorEnvelope() throws Exception {
        mvc().perform(post("/api/v1/emergency-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));
        verify(service, never()).create(any());
    }
}
