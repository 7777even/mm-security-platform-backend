package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.EmergencyEventCreateRequest;
import com.sinopec.mmsecurity.dto.EmergencyEventGroup;
import com.sinopec.mmsecurity.dto.EmergencyEventItem;
import com.sinopec.mmsecurity.dto.EmergencyEventWeatherMeta;
import com.sinopec.mmsecurity.dto.EvacuationPerson;
import com.sinopec.mmsecurity.entity.FacAccidentDetailField;
import com.sinopec.mmsecurity.entity.FacAccidentIncident;
import com.sinopec.mmsecurity.entity.FacEmergencyEvent;
import com.sinopec.mmsecurity.entity.FacEvacuationPerson;
import com.sinopec.mmsecurity.mapper.FacAccidentDetailFieldMapper;
import com.sinopec.mmsecurity.mapper.FacAccidentIncidentMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyEventMapper;
import com.sinopec.mmsecurity.mapper.FacEvacuationPersonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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

    /** 允许前端显式指定的分组编码白名单：与 V17 种子分组对齐（phone/tank/facility/video/extreme-weather）
     * 及手动新增/预警/演练兜底分组（manual-*）。白名单外的取值一律回落兜底，避免写入游离分组。 */
    private static final Set<String> ALLOWED_GROUP_CODES = Set.of(
            "manual-event", "manual-warning", "manual-drill", "manual-weather",
            "extreme-weather", "phone", "tank", "facility", "video");

    /** 白名单分组编码 → 规范标签（与种子 group_label 一致）；编码合法但前端未给标签时据此推导。 */
    private static final Map<String, String> GROUP_CODE_LABELS = Map.of(
            "manual-event", "突发应急事件",
            "manual-warning", "预警事件",
            "manual-drill", "演练事件",
            "manual-weather", "极端天气",
            "extreme-weather", "极端天气",
            "phone", "消防电话报警",
            "tank", "储罐消防报警",
            "facility", "消防设施异常",
            "video", "视频烟火联动");

    private final FacEmergencyEventMapper emergencyEventMapper;
    private final FacEvacuationPersonMapper evacuationPersonMapper;
    private final FacAccidentIncidentMapper accidentIncidentMapper;
    private final FacAccidentDetailFieldMapper accidentDetailFieldMapper;

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

    /**
     * 新增应急事件，并同事务写入事故救援事件表，使「去处置」可按 event_id 定位到该事件。
     *
     * <p>分组由前端按「事件类型」给出（groupCode/groupLabel，白名单校验）；缺省或非法时回落
     * manual-* 兜底分组（manual-event / manual-weather / manual-drill），与前端口径对齐。
     * kind 落库统一转大写（EVENT/DRILL）。返回映射后的事件项（含后端生成的真实 id）。</p>
     *
     * @param req 新增入参（前端 EmergencyEventCreateRequest）
     * @return 已落库的事件项
     */
    @Transactional
    public EmergencyEventItem create(EmergencyEventCreateRequest req) {
        boolean isDrill = "drill".equalsIgnoreCase(req.getKind());
        boolean isWeather = !isDrill && "extremeWeather".equalsIgnoreCase(req.getEventCategory());

        String groupCode = req.getGroupCode();
        String groupLabel = req.getGroupLabel();
        boolean groupCodeValid =
                groupCode != null && !groupCode.isBlank() && ALLOWED_GROUP_CODES.contains(groupCode);
        if (!groupCodeValid) {
            // 编码非法或缺失：整体回落到兜底分组，编码与标签一同取自 kind/eventCategory，
            // 杜绝写入游离分组或因只回落编码而残留外部传入标签导致的「编码-标签」错位。
            groupCode = isDrill ? "manual-drill" : isWeather ? "manual-weather" : "manual-event";
            groupLabel = isDrill ? "演练事件" : isWeather ? "极端天气" : "突发应急事件";
        } else if (groupLabel == null || groupLabel.isBlank()) {
            // 编码合法但未给标签：按白名单编码推导固定规范标签。
            groupLabel = GROUP_CODE_LABELS.getOrDefault(groupCode, "突发应急事件");
        }

        FacEmergencyEvent event = new FacEmergencyEvent();
        event.setScene(req.getScene() == null ? "FIRE" : req.getScene().trim().toUpperCase(Locale.ROOT));
        event.setGroupCode(groupCode);
        event.setGroupLabel(groupLabel);
        event.setKind((req.getKind() == null ? "event" : req.getKind()).toUpperCase(Locale.ROOT));
        event.setEventCategory(req.getEventCategory() == null ? "default" : req.getEventCategory());
        event.setTitle(req.getTitle());
        event.setLocation(req.getLocation());
        event.setDescription(req.getDescription());
        event.setEventTime(req.getEventTime());
        event.setReported(false);
        event.setStatus("pending");
        event.setStatusLabel("未处置");
        event.setLeftPercent(req.getLeftPercent());
        event.setTopPercent(req.getTopPercent());
        event.setLongitude(req.getLongitude());
        event.setLatitude(req.getLatitude());
        event.setAreaCode(req.getAreaCode() == null ? "refinery" : req.getAreaCode());
        event.setHazardSourceLevel(req.getHazardSourceLevel());
        if (isWeather) {
            event.setWeatherType(req.getWeatherType());
            event.setWarningLevel(req.getWarningLevel());
            event.setAffectedArea(req.getAffectedArea());
            event.setMonitoringPeriod(req.getMonitoringPeriod());
            event.setWeatherSource(req.getWeatherSource());
            event.setMeasures(req.getMeasures());
        }
        event.setSortNo(0);
        emergencyEventMapper.insert(event);

        FacAccidentIncident incident = new FacAccidentIncident();
        incident.setEventId(event.getId());
        incident.setTitle(req.getTitle());
        incident.setLocation(req.getLocation());
        incident.setLongitude(req.getLongitude());
        incident.setLatitude(req.getLatitude());
        incident.setHazardSourceLevel(req.getHazardSourceLevel());
        incident.setMapStatus("pending");
        incident.setStartedAt(req.getEventTime());
        incident.setEndedAt(null);
        incident.setStatusName("未处置");
        incident.setReported(false);
        incident.setFacilityName(req.getAreaCode() == null ? "refinery" : req.getAreaCode());
        incident.setIsDefault(false);
        accidentIncidentMapper.insert(incident);

        // 同步写入事故救援详情字段，保证新事件进入 /accident/rescue-incident 聚合时“事件基础信息”不空白。
        List<FacAccidentDetailField> detailFields = buildAccidentDetailFields(incident, req, groupLabel);
        for (FacAccidentDetailField field : detailFields) {
            accidentDetailFieldMapper.insert(field);
        }

        return toItem(event);
    }

    /**
     * 按新增请求构建事故救援详情字段（label/value）。
     * 字段顺序与 V12 seed 对齐：事故时间、事件分类、事件级别、事发地点、事件描述、事件名称；
     * 天气类事件追加天气相关字段。空值字段写空字符串占位，避免面板渲染时缺行错位。
     */
    private List<FacAccidentDetailField> buildAccidentDetailFields(
            FacAccidentIncident incident,
            EmergencyEventCreateRequest req,
            String groupLabel) {
        List<FacAccidentDetailField> fields = new ArrayList<>();
        Long incidentId = incident.getId();
        // mock 单测中 mapper.insert 不会回填 id；真实 MyBatis-Plus 运行时会回填，正常写入详情字段。
        if (incidentId == null) {
            return fields;
        }
        int sortNo = 1;

        fields.add(detailField(incidentId, "事故时间", orBlank(req.getEventTime()), sortNo++));
        fields.add(detailField(incidentId, "事件分类", orBlank(groupLabel), sortNo++));
        fields.add(detailField(incidentId, "事件级别", orBlank(req.getHazardSourceLevel()), sortNo++));
        fields.add(detailField(incidentId, "事发地点", orBlank(req.getLocation()), sortNo++));
        fields.add(detailField(incidentId, "事件描述", orBlank(req.getDescription()), sortNo++));
        fields.add(detailField(incidentId, "事件名称", orBlank(req.getTitle()), sortNo++));
        fields.add(detailField(incidentId, "涉事区域", orBlank(req.getAreaCode()), sortNo++));

        if ("extreme-weather".equals(req.getEventCategory()) || req.getWeatherType() != null) {
            fields.add(detailField(incidentId, "天气类型", orBlank(req.getWeatherType()), sortNo++));
            fields.add(detailField(incidentId, "预警等级", orBlank(req.getWarningLevel()), sortNo++));
            fields.add(detailField(incidentId, "影响范围", orBlank(req.getAffectedArea()), sortNo++));
            fields.add(detailField(incidentId, "监测时段", orBlank(req.getMonitoringPeriod()), sortNo++));
            fields.add(detailField(incidentId, "已采取措施", orBlank(req.getMeasures()), sortNo++));
        }
        return fields;
    }

    private static FacAccidentDetailField detailField(Long incidentId, String label, String value, int sortNo) {
        FacAccidentDetailField field = new FacAccidentDetailField();
        field.setIncidentId(incidentId);
        field.setFieldLabel(label);
        field.setFieldValue(value);
        field.setSortNo(sortNo);
        return field;
    }

    private static String orBlank(String value) {
        return value == null ? "" : value;
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
        // 契约 EmergencyEventKind 为 'event' | 'drill' 小写，后端库内习惯大写，返回前归一化
        item.setKind(row.getKind() != null ? row.getKind().toLowerCase() : "event");
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
