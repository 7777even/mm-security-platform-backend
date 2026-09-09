package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.EmergencyEventGroup;
import com.sinopec.mmsecurity.dto.EmergencyEventItem;
import com.sinopec.mmsecurity.dto.EmergencyEventWeatherMeta;
import com.sinopec.mmsecurity.dto.EvacuationPerson;
import com.sinopec.mmsecurity.entity.FacEmergencyEvent;
import com.sinopec.mmsecurity.entity.FacEvacuationPerson;
import com.sinopec.mmsecurity.mapper.FacEmergencyEventMapper;
import com.sinopec.mmsecurity.mapper.FacEvacuationPersonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 应急事件大屏服务。
 *
 * <p>数据来源为 V17 落地的 fac_emergency_event / fac_evacuation_person 真实表，
 * 取代前端硬编码的 fireEmergencyMock / preliminaryMock / evacuationPeopleMock 业务数据。
 */
@Service
@RequiredArgsConstructor
public class EmergencyEventService {

    private static final int DEFAULT_PERSON_COUNT = 20;

    private final FacEmergencyEventMapper emergencyEventMapper;
    private final FacEvacuationPersonMapper evacuationPersonMapper;

    /**
     * 按场景查询应急事件分组。
     *
     * <p>scene 为空时返回全部场景（FIRE 在前、PRELIMINARY 在后）的分组；
     * 分组按 scene + group_code 聚合，因此不传 scene 时不同场景间可能出现同名分组 id
     * （如 tank / video），此时应以分组内的事件内容区分。
     */
    public List<EmergencyEventGroup> eventGroups(String scene) {
        LambdaQueryWrapper<FacEmergencyEvent> query = new LambdaQueryWrapper<>();
        if (scene != null && !scene.isBlank()) {
            query.eq(FacEmergencyEvent::getScene, scene.trim().toUpperCase(Locale.ROOT));
        }
        query.orderByAsc(FacEmergencyEvent::getScene, FacEmergencyEvent::getSortNo);

        Map<String, EmergencyEventGroup> groups = new LinkedHashMap<>();
        for (FacEmergencyEvent row : emergencyEventMapper.selectList(query)) {
            String key = row.getScene() + "|" + row.getGroupCode();
            EmergencyEventGroup group = groups.computeIfAbsent(key, ignored -> {
                EmergencyEventGroup created = new EmergencyEventGroup();
                created.setId(row.getGroupCode());
                created.setLabel(row.getGroupLabel());
                created.setEvents(new ArrayList<>());
                return created;
            });
            group.getEvents().add(toItem(row));
        }
        return new ArrayList<>(groups.values());
    }

    /**
     * 查询疏散人员名册。
     *
     * <p>count 为返回条数上限，按 sort_no 升序取前 N 条；routeProgress 取库内种子值
     * （(i+1)/(count+1) 等分值），前端据此沿疏散路线摆放人员。
     */
    public List<EvacuationPerson> evacuationPeople(Integer count) {
        List<FacEvacuationPerson> rows = evacuationPersonMapper.selectList(
                new LambdaQueryWrapper<FacEvacuationPerson>().orderByAsc(FacEvacuationPerson::getSortNo));
        int limit = Math.min(Math.max(count == null ? DEFAULT_PERSON_COUNT : count, 0), rows.size());
        return rows.subList(0, limit).stream().map(this::toPerson).collect(Collectors.toList());
    }

    private EmergencyEventItem toItem(FacEmergencyEvent row) {
        EmergencyEventItem item = new EmergencyEventItem();
        item.setId(row.getId());
        item.setAreaCode(row.getAreaCode());
        item.setTitle(row.getTitle());
        item.setLocation(row.getLocation());
        item.setDescription(row.getDescription());
        item.setTime(row.getEventTime());
        item.setReported(row.getReported());
        item.setStatus(row.getStatus());
        item.setStatusLabel(row.getStatusLabel());
        item.setLeft(row.getLeftPercent());
        item.setTop(row.getTopPercent());
        item.setLongitude(row.getLongitude());
        item.setLatitude(row.getLatitude());
        item.setKind(row.getKind());
        item.setEventCategory(row.getEventCategory());
        item.setHazardSourceLevel(row.getHazardSourceLevel());
        item.setEndedAt(row.getEndedAt());
        item.setWeatherMeta(toWeatherMeta(row));
        return item;
    }

    private EmergencyEventWeatherMeta toWeatherMeta(FacEmergencyEvent row) {
        if (row.getWeatherType() == null) {
            return null;
        }
        EmergencyEventWeatherMeta meta = new EmergencyEventWeatherMeta();
        meta.setWeatherType(row.getWeatherType());
        meta.setWarningLevel(row.getWarningLevel());
        meta.setAffectedArea(row.getAffectedArea());
        meta.setMonitoringPeriod(row.getMonitoringPeriod());
        meta.setSource(row.getWeatherSource());
        meta.setMeasures(row.getMeasures());
        return meta;
    }

    private EvacuationPerson toPerson(FacEvacuationPerson row) {
        EvacuationPerson person = new EvacuationPerson();
        person.setId(row.getId());
        person.setName(row.getPersonName());
        person.setOrg(row.getOrgName());
        person.setJob(row.getJobTitle());
        person.setRouteProgress(row.getRouteProgress());
        return person;
    }
}
