package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinopec.mmsecurity.dto.AccidentRescueIncident;
import com.sinopec.mmsecurity.dto.EmergencyDispatchResource;
import com.sinopec.mmsecurity.dto.IncidentDetailField;
import com.sinopec.mmsecurity.dto.RescueAuxiliaryStat;
import com.sinopec.mmsecurity.dto.RescueDynamicEntry;
import com.sinopec.mmsecurity.dto.RescueDutyPerson;
import com.sinopec.mmsecurity.entity.FacAccidentAuxStat;
import com.sinopec.mmsecurity.entity.FacAccidentDetailField;
import com.sinopec.mmsecurity.entity.FacAccidentDispatchResource;
import com.sinopec.mmsecurity.entity.FacAccidentDynamic;
import com.sinopec.mmsecurity.entity.FacAccidentDutyPerson;
import com.sinopec.mmsecurity.entity.FacAccidentIncident;
import com.sinopec.mmsecurity.mapper.FacAccidentAuxStatMapper;
import com.sinopec.mmsecurity.mapper.FacAccidentDetailFieldMapper;
import com.sinopec.mmsecurity.mapper.FacAccidentDispatchResourceMapper;
import com.sinopec.mmsecurity.mapper.FacAccidentDynamicMapper;
import com.sinopec.mmsecurity.mapper.FacAccidentDutyPersonMapper;
import com.sinopec.mmsecurity.mapper.FacAccidentIncidentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 事故救援应急大屏（fm-rescue）服务。
 *
 * <p>数据来源为 V12 落地的 fac_accident_* 真实表，取代前端硬编码的 accidentRescueMock 与
 * fireEmergencyEventsStore。地图标记与路线为前端按事件坐标推算的几何，不在此处理。
 */
@Service
@RequiredArgsConstructor
public class AccidentRescueService {

    private final FacAccidentIncidentMapper incidentMapper;
    private final FacAccidentDetailFieldMapper detailFieldMapper;
    private final FacAccidentDispatchResourceMapper dispatchResourceMapper;
    private final FacAccidentDutyPersonMapper dutyPersonMapper;
    private final FacAccidentAuxStatMapper auxStatMapper;
    private final FacAccidentDynamicMapper dynamicMapper;

    /**
     * 事故救援事件聚合。eventId 为空或未命中时回退到默认事件（is_default=TRUE），
     * 保证大屏不空屏；连默认事件都缺失时返回 null，由 Controller 转业务异常。
     */
    public AccidentRescueIncident incident(Long eventId) {
        FacAccidentIncident row = eventId == null ? null : firstByEventId(eventId);
        if (row == null) {
            row = firstDefault();
        }
        if (row == null) {
            return null;
        }
        Long id = row.getId();

        AccidentRescueIncident dto = new AccidentRescueIncident();
        dto.setEventId(row.getEventId());
        dto.setTitle(row.getTitle());
        dto.setLocation(row.getLocation());
        dto.setLongitude(row.getLongitude());
        dto.setLatitude(row.getLatitude());
        dto.setHazardSourceLevel(row.getHazardSourceLevel());
        dto.setMapStatus(row.getMapStatus());
        dto.setStartedAt(row.getStartedAt());
        dto.setEndedAt(row.getEndedAt());
        dto.setStatus(row.getStatusName());
        dto.setReported(row.getReported());
        dto.setFacilityName(row.getFacilityName());

        // detail_field 与事件一一对应（含 incident_id）；其余四张表为事故救援域的全局参考主数据
        // （调度资源/值班/辅助统计/动态快讯），不按事件拆分，直接全量按 sort_no 返回，与前端契约一致。
        dto.setDetailFields(detailFieldMapper.selectList(byIncident(id)).stream()
                .map(this::toDetailField).collect(Collectors.toList()));
        dto.setDispatchResources(dispatchResourceMapper.selectList(allSorted()).stream()
                .map(this::toDispatchResource).collect(Collectors.toList()));
        dto.setDutyPersons(dutyPersonMapper.selectList(allSorted()).stream()
                .map(this::toDutyPerson).collect(Collectors.toList()));
        dto.setAuxiliaryStats(auxStatMapper.selectList(allSorted()).stream()
                .map(this::toAuxStat).collect(Collectors.toList()));
        dto.setDynamics(dynamicMapper.selectList(allSorted()).stream()
                .map(this::toDynamic).collect(Collectors.toList()));
        return dto;
    }

    private FacAccidentIncident firstByEventId(Long eventId) {
        return incidentMapper.selectOne(new LambdaQueryWrapper<FacAccidentIncident>()
                .eq(FacAccidentIncident::getEventId, eventId)
                .last("LIMIT 1"));
    }

    private FacAccidentIncident firstDefault() {
        return incidentMapper.selectOne(new LambdaQueryWrapper<FacAccidentIncident>()
                .eq(FacAccidentIncident::getIsDefault, Boolean.TRUE)
                .last("LIMIT 1"));
    }

    private static <T> QueryWrapper<T> byIncident(Long incidentId) {
        return new QueryWrapper<T>().eq("incident_id", incidentId).orderByAsc("sort_no");
    }

    private static <T> QueryWrapper<T> allSorted() {
        return new QueryWrapper<T>().orderByAsc("sort_no");
    }

    private IncidentDetailField toDetailField(FacAccidentDetailField e) {
        IncidentDetailField d = new IncidentDetailField();
        d.setLabel(e.getFieldLabel());
        d.setValue(e.getFieldValue());
        return d;
    }

    private EmergencyDispatchResource toDispatchResource(FacAccidentDispatchResource e) {
        EmergencyDispatchResource d = new EmergencyDispatchResource();
        d.setId(e.getResourceCode());
        d.setType(e.getResourceType());
        d.setName(e.getResourceName());
        d.setCode(e.getCode());
        d.setOrganization(e.getOrganization());
        d.setArea(e.getArea());
        d.setStatus(e.getStatusName());
        d.setDistanceKm(e.getDistanceKm());
        d.setEtaMinutes(e.getEtaMinutes());
        d.setCapacity(e.getCapacity());
        d.setContact(e.getContact());
        d.setPhone(e.getPhone());
        d.setLongitude(e.getLongitude());
        d.setLatitude(e.getLatitude());
        return d;
    }

    private RescueDutyPerson toDutyPerson(FacAccidentDutyPerson e) {
        RescueDutyPerson d = new RescueDutyPerson();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setRole(e.getRole());
        d.setPhone(e.getPhone());
        d.setAvatarIndex(e.getAvatarIndex());
        return d;
    }

    private RescueAuxiliaryStat toAuxStat(FacAccidentAuxStat e) {
        RescueAuxiliaryStat d = new RescueAuxiliaryStat();
        d.setLabel(e.getLabel());
        d.setValue(e.getValueName());
        d.setIconIndex(e.getIconIndex());
        return d;
    }

    private RescueDynamicEntry toDynamic(FacAccidentDynamic e) {
        RescueDynamicEntry d = new RescueDynamicEntry();
        d.setId(e.getId());
        d.setCategory(e.getCategory());
        d.setTitle(e.getTitle());
        d.setTag(e.getTag());
        d.setTime(e.getTime());
        d.setCommand(e.getCommandText());
        d.setResponder(e.getResponder());
        d.setReply(e.getReply());
        d.setStageLabel(e.getStageLabel());
        return d;
    }
}
