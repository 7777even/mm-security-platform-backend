package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinopec.mmsecurity.dto.TyphoonAuxItem;
import com.sinopec.mmsecurity.dto.TyphoonDispatchResource;
import com.sinopec.mmsecurity.dto.TyphoonDutyPerson;
import com.sinopec.mmsecurity.dto.TyphoonEmergencyIncident;
import com.sinopec.mmsecurity.dto.TyphoonEventInfoField;
import com.sinopec.mmsecurity.dto.TyphoonLiveVideo;
import com.sinopec.mmsecurity.dto.TyphoonMapRiskPoint;
import com.sinopec.mmsecurity.dto.TyphoonMonitorObject;
import com.sinopec.mmsecurity.dto.TyphoonRiskWarning;
import com.sinopec.mmsecurity.entity.FacTyphoonAuxItem;
import com.sinopec.mmsecurity.entity.FacTyphoonDispatchResource;
import com.sinopec.mmsecurity.entity.FacTyphoonEventInfo;
import com.sinopec.mmsecurity.entity.FacTyphoonIncident;
import com.sinopec.mmsecurity.entity.FacTyphoonLiveVideo;
import com.sinopec.mmsecurity.entity.FacTyphoonMapRiskPoint;
import com.sinopec.mmsecurity.entity.FacTyphoonMonitorObject;
import com.sinopec.mmsecurity.entity.FacTyphoonRiskWarning;
import com.sinopec.mmsecurity.entity.FacTyphoonSeries;
import com.sinopec.mmsecurity.entity.SysDutyMember;
import com.sinopec.mmsecurity.entity.SysKnowledgeItem;
import com.sinopec.mmsecurity.mapper.FacTyphoonAuxItemMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonDispatchResourceMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonEventInfoMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonIncidentMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonLiveVideoMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonMapRiskPointMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonMonitorObjectMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonRiskWarningMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonSeriesMapper;
import com.sinopec.mmsecurity.mapper.SysDutyMemberMapper;
import com.sinopec.mmsecurity.mapper.SysKnowledgeItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 台风应急大屏（fm-typhoon）服务。
 *
 * <p>数据来源为 V11 落地的 fac_typhoon_* 真实表，取代前端硬编码的 defaultTyphoonIncident 与
 * typhoonDispatchResources。值班人员与知识库条目沿用 V8 已建的 sys_duty_member / sys_knowledge_item，
 * 不重复建模。
 *
 * <p>时间序列（降雨 / 风速 / 水位）统一存放于 fac_typhoon_series，以 series_key 区分、
 * sort_no 定序，读取时按 key 还原为三条独立序列，保证与前端图表结构一致。
 */
@Service
@RequiredArgsConstructor
public class TyphoonEmergencyService {

    private static final String SERIES_PRECIPITATION = "precipitation";
    private static final String SERIES_WIND = "wind";
    private static final String SERIES_WATER_LEVEL = "waterLevel";

    private final FacTyphoonIncidentMapper incidentMapper;
    private final FacTyphoonMonitorObjectMapper monitorObjectMapper;
    private final FacTyphoonRiskWarningMapper riskWarningMapper;
    private final FacTyphoonLiveVideoMapper liveVideoMapper;
    private final FacTyphoonMapRiskPointMapper mapRiskPointMapper;
    private final FacTyphoonSeriesMapper seriesMapper;
    private final FacTyphoonEventInfoMapper eventInfoMapper;
    private final FacTyphoonAuxItemMapper auxItemMapper;
    private final FacTyphoonDispatchResourceMapper dispatchResourceMapper;
    private final SysDutyMemberMapper dutyMemberMapper;
    private final SysKnowledgeItemMapper knowledgeItemMapper;

    /**
     * 台风应急事件聚合。eventId 为空或未命中时回退到默认防台防汛事件（is_default=TRUE），
     * 保证大屏不空屏；连默认事件都缺失时返回 null，由 Controller 转业务异常。
     */
    public TyphoonEmergencyIncident incident(Long eventId) {
        FacTyphoonIncident row = eventId == null ? null : firstByEventId(eventId);
        if (row == null) {
            row = firstDefault();
        }
        if (row == null) {
            return null;
        }
        Long id = row.getId();

        TyphoonEmergencyIncident dto = new TyphoonEmergencyIncident();
        dto.setEventId(row.getEventId());
        dto.setTitle(row.getTitle());
        dto.setLocation(row.getLocation());
        dto.setLongitude(row.getLongitude());
        dto.setLatitude(row.getLatitude());
        dto.setStartedAt(row.getStartedAt());
        dto.setEndedAt(row.getEndedAt());
        dto.setStatus(row.getStatusName());
        dto.setMeteorologySummary(row.getMeteorologySummary());
        dto.setWaterLevelWarn(row.getWaterLevelWarn());
        dto.setWaterLevelDanger(row.getWaterLevelDanger());
        dto.setTyphoonApiCode(row.getTyphoonApiCode());

        dto.setMonitoringObjects(monitorObjectMapper.selectList(byIncident(id)).stream()
                .map(this::toMonitorObject).collect(Collectors.toList()));
        dto.setRiskWarnings(riskWarningMapper.selectList(byIncident(id)).stream()
                .map(this::toRiskWarning).collect(Collectors.toList()));
        dto.setLiveVideos(liveVideoMapper.selectList(byIncident(id)).stream()
                .map(this::toLiveVideo).collect(Collectors.toList()));
        dto.setMapRiskPoints(mapRiskPointMapper.selectList(byIncident(id)).stream()
                .map(this::toMapRiskPoint).collect(Collectors.toList()));
        dto.setEventInfoFields(eventInfoMapper.selectList(byIncident(id)).stream()
                .map(this::toEventInfoField).collect(Collectors.toList()));
        dto.setAuxiliaryItems(auxItemMapper.selectList(byIncident(id)).stream()
                .map(this::toAuxItem).collect(Collectors.toList()));

        fillSeries(dto, id);
        dto.setDutyPersons(dutyPersons());
        return dto;
    }

    /** 防汛排涝可调度力量清单 */
    public List<TyphoonDispatchResource> dispatchResources() {
        return dispatchResourceMapper.selectList(
                        new LambdaQueryWrapper<FacTyphoonDispatchResource>()
                                .orderByAsc(FacTyphoonDispatchResource::getSortNo)).stream()
                .map(this::toDispatchResource)
                .collect(Collectors.toList());
    }

    private void fillSeries(TyphoonEmergencyIncident dto, Long incidentId) {
        List<FacTyphoonSeries> all = seriesMapper.selectList(
                new LambdaQueryWrapper<FacTyphoonSeries>()
                        .eq(FacTyphoonSeries::getIncidentId, incidentId)
                        .orderByAsc(FacTyphoonSeries::getSortNo));
        dto.setPrecipitationSeries(values(all, SERIES_PRECIPITATION));
        dto.setWindSpeedSeries(values(all, SERIES_WIND));
        dto.setWaterLevelSeries(values(all, SERIES_WATER_LEVEL));
        // 降雨与风速共用同一套小时标签；水位用独立标签
        dto.setWeatherChartLabels(labels(all, SERIES_PRECIPITATION));
        dto.setWaterLevelLabels(labels(all, SERIES_WATER_LEVEL));
    }

    private static List<Double> values(List<FacTyphoonSeries> all, String key) {
        return all.stream()
                .filter(s -> key.equals(s.getSeriesKey()))
                .map(FacTyphoonSeries::getPointValue)
                .collect(Collectors.toList());
    }

    private static List<String> labels(List<FacTyphoonSeries> all, String key) {
        return all.stream()
                .filter(s -> key.equals(s.getSeriesKey()))
                .map(s -> s.getPointLabel() == null ? "" : s.getPointLabel())
                .collect(Collectors.toList());
    }

    /** 值班人员：复用 V8 应急值班表 sys_duty_member */
    private List<TyphoonDutyPerson> dutyPersons() {
        List<TyphoonDutyPerson> out = new ArrayList<>();
        List<SysDutyMember> members = dutyMemberMapper.selectList(null);
        for (int i = 0; i < members.size(); i++) {
            SysDutyMember m = members.get(i);
            TyphoonDutyPerson p = new TyphoonDutyPerson();
            p.setId(m.getId() == null ? (long) (i + 1) : m.getId());
            p.setName(m.getName());
            p.setRole(m.getRole());
            p.setPhone(m.getPhone());
            p.setAvatarIndex(i % 4);
            out.add(p);
        }
        return out;
    }

    /** 知识库条目：复用 V8 sys_knowledge_item（供大屏辅助面板使用） */
    public List<TyphoonAuxItem> knowledgeAuxItems() {
        List<SysKnowledgeItem> items = knowledgeItemMapper.selectList(null);
        List<TyphoonAuxItem> out = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            SysKnowledgeItem k = items.get(i);
            TyphoonAuxItem a = new TyphoonAuxItem();
            a.setId(k.getId() == null ? (long) (i + 1) : k.getId());
            a.setLine1(k.getTitle());
            a.setLine2("");
            a.setCount(k.getCount());
            a.setCountTone(i % 2 == 0 ? "cyan" : "lime");
            a.setIconIndex(i % 4);
            out.add(a);
        }
        return out;
    }

    private FacTyphoonIncident firstByEventId(Long eventId) {
        return incidentMapper.selectOne(new LambdaQueryWrapper<FacTyphoonIncident>()
                .eq(FacTyphoonIncident::getEventId, eventId)
                .last("LIMIT 1"));
    }

    private FacTyphoonIncident firstDefault() {
        return incidentMapper.selectOne(new LambdaQueryWrapper<FacTyphoonIncident>()
                .eq(FacTyphoonIncident::getIsDefault, Boolean.TRUE)
                .last("LIMIT 1"));
    }

    private static <T> QueryWrapper<T> byIncident(Long incidentId) {
        return new QueryWrapper<T>()
                .eq("incident_id", incidentId)
                .orderByAsc("sort_no");
    }

    private TyphoonMonitorObject toMonitorObject(FacTyphoonMonitorObject e) {
        TyphoonMonitorObject d = new TyphoonMonitorObject();
        d.setId(e.getObjCode());
        d.setName(e.getObjName());
        d.setValue(e.getObjValue());
        d.setUnit(e.getUnit());
        d.setStatus(e.getStatusName());
        d.setStatusText(e.getStatusText());
        return d;
    }

    private TyphoonRiskWarning toRiskWarning(FacTyphoonRiskWarning e) {
        TyphoonRiskWarning d = new TyphoonRiskWarning();
        d.setId(e.getWarnCode());
        d.setTime(e.getWarnTime());
        d.setType(e.getWarnType());
        d.setContent(e.getContent());
        return d;
    }

    private TyphoonLiveVideo toLiveVideo(FacTyphoonLiveVideo e) {
        TyphoonLiveVideo d = new TyphoonLiveVideo();
        d.setId(e.getVideoCode());
        d.setLabel(e.getVideoLabel());
        d.setSceneIndex(e.getSceneIndex());
        d.setAngle(e.getAngle());
        d.setStatus(e.getStatusName());
        d.setDeviceCode(e.getDeviceCode());
        return d;
    }

    private TyphoonMapRiskPoint toMapRiskPoint(FacTyphoonMapRiskPoint e) {
        TyphoonMapRiskPoint d = new TyphoonMapRiskPoint();
        d.setId(e.getPointCode());
        d.setName(e.getPointName());
        d.setLongitude(e.getLongitude());
        d.setLatitude(e.getLatitude());
        d.setStatus(e.getStatusName());
        d.setStatusText(e.getStatusText());
        d.setResponsibleUnit(e.getResponsibleUnit());
        d.setPredeployed(Boolean.TRUE.equals(e.getPredeployed()));
        d.setDeployment(e.getDeployment());
        d.setLabelOffsetX(e.getLabelOffsetX());
        d.setLabelOffsetY(e.getLabelOffsetY());
        d.setClusterCount(e.getClusterCount());
        d.setKind(e.getKind());
        d.setVideoIds(split(e.getVideoIds()));
        return d;
    }

    private TyphoonEventInfoField toEventInfoField(FacTyphoonEventInfo e) {
        TyphoonEventInfoField d = new TyphoonEventInfoField();
        d.setLabel(e.getFieldLabel());
        d.setValue(e.getFieldValue());
        return d;
    }

    private TyphoonAuxItem toAuxItem(FacTyphoonAuxItem e) {
        TyphoonAuxItem d = new TyphoonAuxItem();
        d.setId(e.getId());
        d.setLine1(e.getLine1());
        d.setLine2(e.getLine2() == null ? "" : e.getLine2());
        d.setCount(e.getItemCount());
        d.setCountTone(e.getCountTone());
        d.setIconIndex(e.getIconIndex());
        return d;
    }

    private TyphoonDispatchResource toDispatchResource(FacTyphoonDispatchResource e) {
        TyphoonDispatchResource d = new TyphoonDispatchResource();
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

    /** video_ids 以逗号分隔存储，输出为列表；空值返回空列表。 */
    private static List<String> split(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
