package com.sinopec.mmsecurity.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.FacilityDetailInfo;
import com.sinopec.mmsecurity.dto.MajorHazardDetail;
import com.sinopec.mmsecurity.dto.MajorHazardItem;
import com.sinopec.mmsecurity.dto.MajorHazardWriteRequest;
import com.sinopec.mmsecurity.dto.MonitoringAlarm;
import com.sinopec.mmsecurity.dto.MonitoringPoint;
import com.sinopec.mmsecurity.dto.MonitoringPointWriteRequest;
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
import org.springframework.transaction.annotation.Transactional;

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
     * 这些端点每次刷新都全表 selectList，加短 TTL 缓存可大幅削减重复扫描。
     * ⚠️ 自本批起重���危险源与监测点位已有写端点：**写方法必须 invalidateAll 对应缓存**，
     *    否则订阅端收到 `.changed` 后重拉仍命中旧缓存，实时刷新会表现为「改了没反应」。
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

    // ---- 写端点 ----
    // 本域读端点走 Caffeine 缓存（TTL 60s），写后必须 invalidateAll，否则订阅端重拉仍拿到旧列表、
    // 实时刷新看起来「改了没反应」。原「只读无写入口，TTL 即一致窗口」的前提自本批起不再成立。

    /** 新建重大危险源；id 由 LedgerIdSupport 分配。广播 hazard 实时通道。 */
    @RealtimeSync(domain = "hazard")
    @Transactional
    public MajorHazardItem createHazard(MajorHazardWriteRequest in) {
        FacMajorHazard e = new FacMajorHazard();
        e.setId(LedgerIdSupport.nextId(majorHazardMapper, FacMajorHazard::getId, FacMajorHazard::getId));
        applyHazardFields(e, in);
        e.setVersion(0L);
        majorHazardMapper.insert(e);
        majorHazardCache.invalidateAll();
        return toItem(e);
    }

    /** 更新重大危险源（按 id）；未命中返回 null。广播 hazard 实时通道。 */
    @RealtimeSync(domain = "hazard")
    @Transactional
    public MajorHazardItem updateHazard(Long id, MajorHazardWriteRequest in) {
        FacMajorHazard e = majorHazardMapper.selectById(id);
        if (e == null) {
            return null;
        }
        applyHazardFields(e, in);
        majorHazardMapper.updateById(e);
        majorHazardCache.invalidateAll();
        return toItem(e);
    }

    /** 删除重大危险源（按 id）；未命中 ok=false。广播 hazard 实时通道。 */
    @RealtimeSync(domain = "hazard")
    @Transactional
    public DeleteResult deleteHazard(Long id) {
        DeleteResult result = new DeleteResult();
        if (majorHazardMapper.selectById(id) == null) {
            result.setOk(false);
            return result;
        }
        result.setOk(majorHazardMapper.deleteById(id) > 0);
        majorHazardCache.invalidateAll();
        return result;
    }

    /** 新建监测点位；id 为字符串主键由请求给定，重复抛 CONFLICT。广播 hazard.point 实时通道。 */
    @RealtimeSync(domain = "hazard.point")
    @Transactional
    public MonitoringPoint createPoint(MonitoringPointWriteRequest in) {
        if (monitoringPointMapper.selectById(in.getId()) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "监测点位编码已存在：" + in.getId());
        }
        FacMonitoringPoint p = new FacMonitoringPoint();
        p.setId(in.getId());
        applyPointFields(p, in);
        p.setVersion(0L);
        monitoringPointMapper.insert(p);
        monitoringPointCache.invalidateAll();
        return toMonitoringPoint(p);
    }

    /** 更新监测点位（按 id）；未命中返回 null。广播 hazard.point 实时通道。 */
    @RealtimeSync(domain = "hazard.point")
    @Transactional
    public MonitoringPoint updatePoint(String id, MonitoringPointWriteRequest in) {
        FacMonitoringPoint p = monitoringPointMapper.selectById(id);
        if (p == null) {
            return null;
        }
        applyPointFields(p, in);
        monitoringPointMapper.updateById(p);
        monitoringPointCache.invalidateAll();
        return toMonitoringPoint(p);
    }

    /** 删除监测点位（按 id）；未命中 ok=false。广播 hazard.point 实时通道。 */
    @RealtimeSync(domain = "hazard.point")
    @Transactional
    public DeleteResult deletePoint(String id) {
        DeleteResult result = new DeleteResult();
        if (monitoringPointMapper.selectById(id) == null) {
            result.setOk(false);
            return result;
        }
        result.setOk(monitoringPointMapper.deleteById(id) > 0);
        monitoringPointCache.invalidateAll();
        return result;
    }

    private void applyHazardFields(FacMajorHazard e, MajorHazardWriteRequest in) {
        e.setName(in.getName());
        e.setLevel(in.getLevel());
        e.setRValue(in.getRValue());
        e.setMonitorCount(in.getMonitorCount());
        e.setVideoCount(in.getVideoCount());
        e.setEnterprise(in.getEnterprise());
        e.setCategory(in.getCategory());
        e.setCode(in.getCode());
        e.setLongitude(in.getLongitude());
        e.setLatitude(in.getLatitude());
    }

    private void applyPointFields(FacMonitoringPoint p, MonitoringPointWriteRequest in) {
        // ⚠️ 此处刻意不写 p.setId(in.getId())：更新是 PUT /monitoring/points/{id} 子路径，
        //    主键由路径决定。若用请求体 id 覆盖，body 与 path 不一致时会 updateById 到另一行
        //    （命中则改错数据，未命中则静默 0 行且返回成功）。新建由调用方单独 setId。
        p.setName(in.getName());
        p.setCategory(in.getCategory());
        p.setStatus(in.getStatus());
        p.setLastTime(in.getLastTime());
        p.setOrg(in.getOrg());
        p.setLongitude(in.getLongitude());
        p.setLatitude(in.getLatitude());
    }

    private MonitoringPoint toMonitoringPoint(FacMonitoringPoint p) {
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
    }

    /** 测试隔离用：清空全部缓存，避免跨用例污染。 */
    void clearCaches() {
        majorHazardCache.invalidateAll();
        monitoringPointCache.invalidateAll();
        monitoringAlarmCache.invalidateAll();
        facilityDetailCache.invalidateAll();
    }
}
