package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.EmergencyEventGroup;
import com.sinopec.mmsecurity.dto.EmergencyEventItem;
import com.sinopec.mmsecurity.dto.EvacuationPerson;
import com.sinopec.mmsecurity.entity.FacEmergencyEvent;
import com.sinopec.mmsecurity.entity.FacEvacuationPerson;
import com.sinopec.mmsecurity.mapper.FacEmergencyEventMapper;
import com.sinopec.mmsecurity.mapper.FacEvacuationPersonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** 应急事件服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class EmergencyEventServiceTest {

    @Mock
    private FacEmergencyEventMapper emergencyEventMapper;
    @Mock
    private FacEvacuationPersonMapper evacuationPersonMapper;

    @InjectMocks
    private EmergencyEventService service;

    private static FacEmergencyEvent event(String scene, String groupCode, String groupLabel,
                                          String kind, String title, int sortNo) {
        FacEmergencyEvent row = new FacEmergencyEvent();
        row.setScene(scene);
        row.setGroupCode(groupCode);
        row.setGroupLabel(groupLabel);
        row.setKind(kind);
        row.setTitle(title);
        row.setLocation("化工区乙烯裂解装置东北侧");
        row.setDescription("现场报告裂解炉炉管疑似泄漏并伴有明火");
        row.setEventTime("2026-03-17 14:21:54");
        row.setReported(true);
        row.setStatus("processing");
        row.setStatusLabel("泄漏着火");
        row.setLeftPercent("47.1%");
        row.setTopPercent("22.6%");
        row.setLongitude(110.880710);
        row.setLatitude(21.684459);
        row.setAreaCode("chemical");
        row.setEventCategory("default");
        row.setHazardSourceLevel("二级");
        row.setSortNo(sortNo);
        return row;
    }

    private static FacEvacuationPerson person(String name, String org, String job,
                                              double progress, int sortNo) {
        FacEvacuationPerson row = new FacEvacuationPerson();
        row.setPersonName(name);
        row.setOrgName(org);
        row.setJobTitle(job);
        row.setRouteProgress(progress);
        row.setSortNo(sortNo);
        return row;
    }

    @Test
    void eventGroups_groupsBySceneAndGroupCode() {
        when(emergencyEventMapper.selectList(any())).thenReturn(List.of(
                event("FIRE", "phone", "消防电话报警", "EVENT", "乙烯裂解炉炉管泄漏着火", 1),
                event("FIRE", "phone", "消防电话报警", "EVENT", "液体化工码头装卸臂泄漏着火", 2),
                event("FIRE", "drill-plan", "计划演练", "DRILL", "储罐区消防演练", 11),
                event("PRELIMINARY", "device", "装置异常报警", "EVENT", "乙烯装置火灾", 1)));

        List<EmergencyEventGroup> groups = service.eventGroups(null);

        assertEquals(3, groups.size());
        assertEquals("phone", groups.get(0).getId());
        assertEquals("消防电话报警", groups.get(0).getLabel());
        assertEquals(2, groups.get(0).getEvents().size());
        assertEquals("drill-plan", groups.get(1).getId());
        assertEquals("drill", groups.get(1).getEvents().get(0).getKind());
        assertEquals("device", groups.get(2).getId());
    }

    @Test
    void eventGroups_mapsItemFieldsAndSkipsWeatherMetaWhenAbsent() {
        when(emergencyEventMapper.selectList(any())).thenReturn(List.of(
                event("FIRE", "phone", "消防电话报警", "EVENT", "乙烯裂解炉炉管泄漏着火", 1)));

        List<EmergencyEventGroup> groups = service.eventGroups("fire");

        EmergencyEventItem item = groups.get(0).getEvents().get(0);
        assertEquals("乙烯裂解炉炉管泄漏着火", item.getTitle());
        assertEquals("47.1%", item.getLeft());
        assertEquals("22.6%", item.getTop());
        assertEquals(110.880710, item.getLongitude());
        assertEquals(21.684459, item.getLatitude());
        assertEquals("chemical", item.getAreaCode());
        assertEquals("二级", item.getHazardSourceLevel());
        assertEquals("event", item.getKind());
        assertEquals("default", item.getEventCategory());
        assertNull(item.getEndedAt());
        assertNull(item.getWeatherMeta());
    }

    @Test
    void eventGroups_buildsWeatherMetaWhenWeatherTypePresent() {
        FacEmergencyEvent weather = event("FIRE", "extreme-weather", "极端天气", "EVENT",
                "台风沙迦防台防汛工作", 10);
        weather.setEventCategory("extremeWeather");
        weather.setWeatherType("台风");
        weather.setWarningLevel("Ⅱ级");
        weather.setAffectedArea("全厂范围");
        weather.setMonitoringPeriod("2026-06-25 08:00 至 2026-06-26 08:00");
        weather.setWeatherSource("气象台");
        weather.setMeasures("启动防台防汛Ⅱ级响应");
        when(emergencyEventMapper.selectList(any())).thenReturn(List.of(weather));

        EmergencyEventItem item = service.eventGroups("FIRE").get(0).getEvents().get(0);

        assertEquals("extremeWeather", item.getEventCategory());
        assertEquals("台风", item.getWeatherMeta().getWeatherType());
        assertEquals("Ⅱ级", item.getWeatherMeta().getWarningLevel());
        assertEquals("全厂范围", item.getWeatherMeta().getAffectedArea());
        assertEquals("气象台", item.getWeatherMeta().getSource());
        assertEquals("启动防台防汛Ⅱ级响应", item.getWeatherMeta().getMeasures());
    }

    @Test
    void evacuationPeople_respectsCountAndMapsRoster() {
        when(evacuationPersonMapper.selectList(any())).thenReturn(List.of(
                person("张建", "生产管理部", "班长", 0.047619, 1),
                person("李明", "装置运行一班", "主操", 0.095238, 2),
                person("王强", "装置运行二班", "外操", 0.142857, 3)));

        List<EvacuationPerson> people = service.evacuationPeople(2);

        assertEquals(2, people.size());
        assertEquals("张建", people.get(0).getName());
        assertEquals("生产管理部", people.get(0).getOrg());
        assertEquals("班长", people.get(0).getJob());
        assertEquals(0.047619, people.get(0).getRouteProgress());
        assertEquals("李明", people.get(1).getName());
    }

    @Test
    void evacuationPeople_clampsCountToAvailableRows() {
        when(evacuationPersonMapper.selectList(any())).thenReturn(List.of(
                person("张建", "生产管理部", "班长", 0.047619, 1)));

        assertEquals(1, service.evacuationPeople(20).size());
        assertEquals(0, service.evacuationPeople(0).size());
        assertEquals(1, service.evacuationPeople(null).size());
    }
}
