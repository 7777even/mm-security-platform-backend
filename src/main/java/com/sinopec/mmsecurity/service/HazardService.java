package com.sinopec.mmsecurity.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.dto.FacilityDetailInfo;
import com.sinopec.mmsecurity.dto.MajorHazardDetail;
import com.sinopec.mmsecurity.dto.MajorHazardItem;
import com.sinopec.mmsecurity.dto.MonitoringAlarm;
import com.sinopec.mmsecurity.dto.MonitoringPoint;
import com.sinopec.mmsecurity.entity.FacFacilityDetail;
import com.sinopec.mmsecurity.entity.FacMajorHazard;
import com.sinopec.mmsecurity.entity.FacMonitoringAlarm;
import com.sinopec.mmsecurity.entity.FacMonitoringPoint;
import com.sinopec.mmsecurity.mapper.FacFacilityDetailMapper;
import com.sinopec.mmsecurity.mapper.FacMajorHazardMapper;
import com.sinopec.mmsecurity.mapper.FacMonitoringAlarmMapper;
import com.sinopec.mmsecurity.mapper.FacMonitoringPointMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 重大危险源 / 监测点位 / 设施档案 业务服务。
 * 数据全部来自真实表（fac_major_hazard / fac_monitoring_point / fac_monitoring_alarm / fac_facility_detail），
 * 不再返回前端本地 fixture。嵌套明细由实体 JSON 列解析为 DTO 的 List&lt;Map&gt;。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HazardService {

    private final FacMajorHazardMapper majorHazardMapper;
    private final FacMonitoringPointMapper monitoringPointMapper;
    private final FacMonitoringAlarmMapper monitoringAlarmMapper;
    private final FacFacilityDetailMapper facilityDetailMapper;
    private final ObjectMapper objectMapper;

    /**
     * 重大危险源 / 监测点位 / 监测报警 / 设施档案 均为低频参考数据，加大屏/管理端高频轮询入口。
     * 这些端点每次刷新都全表 selectList，加短 TTL 缓存可大幅削减重复扫描；只读无写入口，TTL 即一致窗口。
     */
    private final Cache<String, List<MajorHazardItem>> majorHazardCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(1).build();
    private final Cache<String, List<MonitoringPoint>> monitoringPointCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(1).build();
    private final Cache<String, List<MonitoringAlarm>> monitoringAlarmCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(1).build();
    private final Cache<String, FacilityDetailInfo> facilityDetailCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(64).build();

    public List<MajorHazardItem> listMajorHazards() {
        return majorHazardCache.get("MAJOR_HAZARDS", k -> majorHazardMapper.selectList(null).stream().map(this::toItem).toList());
    }

    public MajorHazardDetail getMajorHazardDetail(Long id) {
        FacMajorHazard e = majorHazardMapper.selectById(id);
        return e == null ? null : toDetail(e);
    }

    public List<MonitoringPoint> listMonitoringPoints() {
        return monitoringPointCache.get("MONITORING_POINTS", k -> computeMonitoringPoints());
    }

    private List<MonitoringPoint> computeMonitoringPoints() {
        return monitoringPointMapper.selectList(null).stream()
                .map(p -> {
                    MonitoringPoint d = new MonitoringPoint();
                    d.setId(p.getId());
                    d.setName(p.getName());
                    d.setCategory(p.getCategory());
                    d.setStatus(p.getStatus());
                    d.setLastTime(p.getLastTime());
                    d.setOrg(p.getOrg());
                    d.setLongitude(p.getLongitude());
                    d.setLatitude(p.getLatitude());
                    return d;
                })
                .toList();
    }

    public List<MonitoringAlarm> listMonitoringAlarms() {
        return monitoringAlarmCache.get("MONITORING_ALARMS", k -> computeMonitoringAlarms());
    }

    private List<MonitoringAlarm> computeMonitoringAlarms() {
        return monitoringAlarmMapper.selectList(null).stream()
                .map(a -> {
                    MonitoringAlarm d = new MonitoringAlarm();
                    d.setId(a.getId());
                    d.setTitle(a.getTitle());
                    d.setDetail(a.getDetail());
                    d.setArea(a.getArea());
                    d.setTime(a.getTime());
                    d.setLevel(a.getLevel());
                    return d;
                })
                .toList();
    }

    public FacilityDetailInfo getFacilityDetail(String name) {
        String key = (name == null || name.isBlank()) ? "__ALL__" : name;
        return facilityDetailCache.get(key, k -> computeFacilityDetail(name));
    }

    private FacilityDetailInfo computeFacilityDetail(String name) {
        List<FacFacilityDetail> all = facilityDetailMapper.selectList(null);
        FacFacilityDetail e = all.stream()
                .filter(f -> name == null || name.isBlank() || name.equals(f.getFacilityName()))
                .findFirst()
                .orElse(null);
        if (e == null) return null;
        FacilityDetailInfo d = new FacilityDetailInfo();
        d.setFacilityName(e.getFacilityName());
        d.setHazardSourceCode(e.getHazardSourceCode());
        d.setBasicFields(parseJson(e.getBasicFieldsJson()));
        d.setChemicalFields(parseJson(e.getChemicalFieldsJson()));
        d.setArchives(parseJson(e.getArchivesJson()));
        return d;
    }

    private MajorHazardItem toItem(FacMajorHazard e) {
        MajorHazardItem d = new MajorHazardItem();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setLevel(e.getLevel());
        d.setRValue(e.getRValue());
        d.setMonitorCount(e.getMonitorCount());
        d.setVideoCount(e.getVideoCount());
        d.setEnterprise(e.getEnterprise());
        d.setCategory(e.getCategory());
        d.setCode(e.getCode());
        d.setLongitude(e.getLongitude());
        d.setLatitude(e.getLatitude());
        return d;
    }

    private MajorHazardDetail toDetail(FacMajorHazard e) {
        MajorHazardDetail d = new MajorHazardDetail();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setLevel(e.getLevel());
        d.setRValue(e.getRValue());
        d.setMonitorCount(e.getMonitorCount());
        d.setVideoCount(e.getVideoCount());
        d.setEnterprise(e.getEnterprise());
        d.setCategory(e.getCategory());
        d.setCode(e.getCode());
        d.setLongitude(e.getLongitude());
        d.setLatitude(e.getLatitude());
        d.setCommissionDate(e.getCommissionDate());
        d.setKeyProcess(e.getKeyProcess());
        d.setInChemicalPark(e.getInChemicalPark());
        d.setContacts(parseJson(e.getContactsJson()));
        d.setFiles(parseJson(e.getFilesJson()));
        d.setMonitors(parseJson(e.getMonitorsJson()));
        d.setVideos(parseJson(e.getVideosJson()));
        d.setChemicals(parseJson(e.getChemicalsJson()));
        d.setEvacuationRoutes(parseJson(e.getEvacuationRoutesJson()));
        d.setOperations(parseJson(e.getOperationsJson()));
        return d;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception ex) {
            log.warn("解析重大危险源 JSON 列失败: {}", ex.getMessage());
            return List.of();
        }
    }

    /** 测试隔离用：清空全部缓存，避免跨用例污染。 */
    void clearCaches() {
        majorHazardCache.invalidateAll();
        monitoringPointCache.invalidateAll();
        monitoringAlarmCache.invalidateAll();
        facilityDetailCache.invalidateAll();
    }
}
