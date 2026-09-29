package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.TvEventBreakdownItem;
import com.sinopec.mmsecurity.dto.TvInspectionItem;
import com.sinopec.mmsecurity.dto.TvInspectionSummary;
import com.sinopec.mmsecurity.dto.TvMaintenanceOrder;
import com.sinopec.mmsecurity.dto.TvMapPoint;
import com.sinopec.mmsecurity.dto.TvMonitorDetail;
import com.sinopec.mmsecurity.dto.TvOverview;
import com.sinopec.mmsecurity.dto.TvOverviewItem;
import com.sinopec.mmsecurity.dto.TvOperationStats;
import com.sinopec.mmsecurity.dto.TvSnapshotAckResult;
import com.sinopec.mmsecurity.dto.TvSnapshotIngestRequest;
import com.sinopec.mmsecurity.dto.TvSnapshotIngestResult;
import com.sinopec.mmsecurity.dto.TvSnapshotItem;
import com.sinopec.mmsecurity.dto.TvSnapshotPage;
import com.sinopec.mmsecurity.dto.TvMonitorSummary;
import com.sinopec.mmsecurity.dto.TvMonitorUpsertRequest;
import com.sinopec.mmsecurity.entity.FacMajorHazard;
import com.sinopec.mmsecurity.entity.FacTvInspectionRecord;
import com.sinopec.mmsecurity.entity.FacTvMapPoint;
import com.sinopec.mmsecurity.entity.FacTvMonitor;
import com.sinopec.mmsecurity.entity.FacTvOperationStat;
import com.sinopec.mmsecurity.entity.FacTvSnapshot;
import com.sinopec.mmsecurity.entity.FacTvStatItem;
import com.sinopec.mmsecurity.entity.SysZone;
import com.sinopec.mmsecurity.mapper.FacMajorHazardMapper;
import com.sinopec.mmsecurity.mapper.FacTvInspectionRecordMapper;
import com.sinopec.mmsecurity.mapper.FacTvMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacTvOperationStatMapper;
import com.sinopec.mmsecurity.mapper.FacTvSnapshotMapper;
import com.sinopec.mmsecurity.mapper.FacTvStatItemMapper;
import com.sinopec.mmsecurity.mapper.FacTvMapPointMapper;
import com.sinopec.mmsecurity.mapper.SysZoneMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工业电视大屏（fm-tv）服务。
 *
 * <p>数据来源为 V15 落地的 fac_tv_* 真实表，取代前端硬编码的 tvMock 业务数据。
 * 地图撒点/巡检圆/扫描点位等前端静态几何不在本服务范围（与 V13 生产域同一先例）。
 *
 * <p>V78 起新增「录像截图采集入库闭环」：设备/采集端上报截图（{@link #submitSnapshot}）→ 落库
 * fac_tv_snapshot → 经 {@link RealtimeSync} 广播 tv.snapshot.changed → 前端实时刷新上屏。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TvService {

    private static final String CAT_OVERVIEW = "OVERVIEW";
    private static final String CAT_MAINTENANCE = "MAINTENANCE";
    private static final String CAT_EVENT = "EVENT";
    private static final String KIND_VEHICLE = "VEHICLE";

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FacTvStatItemMapper statItemMapper;
    private final FacTvOperationStatMapper operationStatMapper;
    private final FacTvInspectionRecordMapper inspectionRecordMapper;
    private final FacTvMapPointMapper tvMapPointMapper;
    private final FacTvMonitorMapper tvMonitorMapper;
    private final FacTvSnapshotMapper snapshotMapper;
    /** 重大危险源（与 GET /hazards 同源）：用于校正总览卡片的「重大危险源」数量。 */
    private final FacMajorHazardMapper majorHazardMapper;
    /** 防区主数据（sys_zone）：解析 zone_code → zone_name，供监控点位/截图展示防区名称。 */
    private final SysZoneMapper zoneMapper;

    /**
     * 防区编码 → 防区名称 映射（实时从 sys_zone 读取，7 行量级，调用方按需取用）。
     * 不缓存：防区主数据可被运维维护，直读保证与库一致。
     */
    private Map<String, String> zoneNameMap() {
        return zoneMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysZone::getZoneCode, SysZone::getZoneName, (a, b) -> a));
    }

    /**
     * 工业电视首屏聚合短 TTL 缓存：大屏高频轮询入口，聚合读 fac_tv_stat_item + fac_tv_operation_stat。
     * 只读无写入口，TTL 即最终一致窗口。
     */
    private final Cache<String, TvOverview> overviewCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(1).build();

    /** 录像截图列表短 TTL 缓存：写后显式失效（见 clearSnapshotCaches），保证实时刷新。 */
    private final Cache<String, TvSnapshotPage> snapshotListCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(10)).maximumSize(8).build();

    /** 首屏聚合：概览卡片 + 运行统计 + 维保工单 + 事件分析（带短 TTL 缓存）。 */
    public TvOverview overview() {
        return overviewCache.get("OVERVIEW", k -> computeOverview());
    }

    private TvOverview computeOverview() {
        List<FacTvStatItem> items = statItemMapper.selectList(
                new LambdaQueryWrapper<FacTvStatItem>().orderByAsc(FacTvStatItem::getSortNo));

        TvOverview overview = new TvOverview();
        long hazardCount = majorHazardMapper.selectCount(null);
        overview.setOverviewItems(items.stream()
                .filter(i -> CAT_OVERVIEW.equals(i.getItemCategory()))
                .map(i -> {
                    TvOverviewItem item = new TvOverviewItem();
                    item.setId(i.getId());
                    item.setLabel(i.getLabel());
                    // 「重大危险源」改由 fac_major_hazard 实时计数（与 GET /hazards 同源），不再取手填值；
                    // 其余项（生产设施/厂界/封闭入口/其他入口/其它）暂无对应明细表，沿用字典值。
                    item.setValue("重大危险源".equals(i.getLabel()) ? (int) hazardCount : i.getItemCount());
                    item.setIconIndex(i.getIconIndex());
                    return item;
                }).collect(Collectors.toList()));
        overview.setMaintenanceOrders(items.stream()
                .filter(i -> CAT_MAINTENANCE.equals(i.getItemCategory()))
                .map(i -> {
                    TvMaintenanceOrder order = new TvMaintenanceOrder();
                    order.setLabel(i.getLabel());
                    order.setValue(i.getItemCount());
                    order.setTone(i.getTone());
                    return order;
                }).collect(Collectors.toList()));
        overview.setEventBreakdown(items.stream()
                .filter(i -> CAT_EVENT.equals(i.getItemCategory()))
                .map(i -> {
                    TvEventBreakdownItem item = new TvEventBreakdownItem();
                    item.setLabel(i.getLabel());
                    item.setValue(i.getItemCount());
                    item.setColor(i.getColor());
                    return item;
                }).collect(Collectors.toList()));

        // 运行统计：改由监控点明细 fac_tv_monitor 实时聚合（取代手填的 fac_tv_operation_stat 单行表：
        // 原 total 1233 与监测点实际数量完全脱节）。eventTotal 无对应明细表，沿用统计表原值。
        List<FacTvMonitor> monitors = tvMonitorMapper.selectList(null);
        int monitorTotal = monitors.size();
        int offlineCount = (int) monitors.stream()
                .filter(m -> !Boolean.TRUE.equals(m.getOnline())).count();
        int faultCount = (int) monitors.stream()
                .filter(m -> m.getIntegrity() != null && !"良好".equals(m.getIntegrity())).count();
        FacTvOperationStat stat = operationStatMapper.selectList(null).stream().findFirst().orElse(null);
        TvOperationStats stats = new TvOperationStats();
        stats.setTotal(monitorTotal);
        stats.setOffline(offlineCount);
        stats.setFault(faultCount);
        stats.setOnlineRate(monitorTotal <= 0 ? 0 : Math.round((monitorTotal - offlineCount) * 100f / monitorTotal));
        stats.setIntegrityRate(monitorTotal <= 0 ? 0 : Math.round((monitorTotal - faultCount) * 100f / monitorTotal));
        stats.setEventTotal(stat == null ? 0 : stat.getEventTotal());
        overview.setOperationStats(stats);
        return overview;
    }

    /** 测试隔离用：清空全部缓存，避免跨用例污染。 */
    void clearCaches() {
        overviewCache.invalidateAll();
        snapshotListCache.invalidateAll();
    }

    /** 入厂巡检聚合：车辆列表 + 人员列表。 */
    public TvInspectionSummary inspections() {
        List<FacTvInspectionRecord> records = inspectionRecordMapper.selectList(
                new LambdaQueryWrapper<FacTvInspectionRecord>().orderByAsc(FacTvInspectionRecord::getSortNo));
        TvInspectionSummary summary = new TvInspectionSummary();
        summary.setVehicles(records.stream()
                .filter(r -> KIND_VEHICLE.equals(r.getRecordKind()))
                .map(this::toInspectionItem).collect(Collectors.toList()));
        summary.setPersons(records.stream()
                .filter(r -> !KIND_VEHICLE.equals(r.getRecordKind()))
                .map(this::toInspectionItem).collect(Collectors.toList()));
        return summary;
    }

    /** 工业电视地图撒点：来自 V24 fac_tv_map_point，取代前端硬编码 tvVideoMapPoints。 */
    public List<TvMapPoint> tvMapPoints() {
        List<FacTvMapPoint> rows = tvMapPointMapper.selectList(
                new LambdaQueryWrapper<FacTvMapPoint>().orderByAsc(FacTvMapPoint::getSortNo));
        List<TvMapPoint> out = new ArrayList<>();
        for (FacTvMapPoint r : rows) {
            TvMapPoint p = new TvMapPoint();
            p.setId(r.getPointCode());
            p.setLabel(r.getPointLabel());
            p.setGroup(r.getPointGroup());
            p.setLongitude(r.getLongitude());
            p.setLatitude(r.getLatitude());
            p.setHeight(r.getHeight());
            p.setOnline(Boolean.TRUE.equals(r.getOnline()));
            out.add(p);
        }
        return out;
    }

    /** 视频监控点位档案：来自 V24 fac_tv_monitor，取代前端硬编码 tvVideoMonitorDetails。 */
    public TvMonitorDetail tvMonitorByCode(String code) {
        FacTvMonitor m = tvMonitorMapper.selectOne(
                new LambdaQueryWrapper<FacTvMonitor>().eq(FacTvMonitor::getMonitorCode, code));
        if (m == null) return null;
        TvMonitorDetail d = new TvMonitorDetail();
        d.setId(m.getMonitorCode());
        d.setName(m.getMonitorName());
        d.setOnline(Boolean.TRUE.equals(m.getOnline()));
        d.setIntegrity(m.getIntegrity());
        d.setMonitorType(m.getMonitorType());
        d.setDepartment(m.getDepartment());
        Map<String, String> zoneMap = zoneNameMap();
        d.setZoneCode(m.getZoneCode());
        d.setZoneName(m.getZoneCode() == null ? null : zoneMap.get(m.getZoneCode()));
        d.setLocation(m.getLocation());
        d.setHeight(m.getHeight());
        d.setAngle(m.getAngle());
        return d;
    }

    /**
     * 视频监控点位摘要列表（设备下拉/筛选）：返回全部点位（含防区）。
     * 供「设备/防区筛选」二级页设备维度下拉使用。
     */
    public List<TvMonitorSummary> listMonitors() {
        Map<String, String> zoneMap = zoneNameMap();
        return tvMonitorMapper.selectList(
                new LambdaQueryWrapper<FacTvMonitor>().orderByAsc(FacTvMonitor::getMonitorCode))
                .stream().map(m -> {
                    TvMonitorSummary s = new TvMonitorSummary();
                    s.setCode(m.getMonitorCode());
                    s.setName(m.getMonitorName());
                    s.setOnline(Boolean.TRUE.equals(m.getOnline()));
                    s.setDepartment(m.getDepartment());
                    s.setZoneCode(m.getZoneCode());
                    s.setZoneName(m.getZoneCode() == null ? null : zoneMap.get(m.getZoneCode()));
                    return s;
                }).collect(Collectors.toList());
    }

    // ===================== 监控点位管理 CRUD（设备/防区管理，V87） =====================

    /**
     * 新增监控点位（设备/防区管理）。monitorCode 重复时抛 PARAM_INVALID。
     * 成功后广播 tv.monitor.changed，使大屏/管理页点位列表实时刷新。
     */
    @RealtimeSync(domain = "tv.monitor")
    public TvMonitorSummary createMonitor(TvMonitorUpsertRequest req) {
        if (req.getMonitorCode() == null || req.getMonitorCode().isBlank()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "monitorCode 不能为空");
        }
        if (tvMonitorMapper.selectOne(new LambdaQueryWrapper<FacTvMonitor>()
                .eq(FacTvMonitor::getMonitorCode, req.getMonitorCode())) != null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "监控点位编码已存在：" + req.getMonitorCode());
        }
        FacTvMonitor e = toMonitorEntity(req);
        tvMonitorMapper.insert(e);
        return toMonitorSummary(e, zoneNameMap());
    }

    /**
     * 更新监控点位（含防区归属 zoneCode）。仅覆盖非空字段（read-modify-write）。
     * 成功后广播 tv.monitor.changed。
     */
    @RealtimeSync(domain = "tv.monitor")
    public TvMonitorSummary updateMonitor(String code, TvMonitorUpsertRequest req) {
        FacTvMonitor e = tvMonitorMapper.selectOne(new LambdaQueryWrapper<FacTvMonitor>()
                .eq(FacTvMonitor::getMonitorCode, code));
        if (e == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "监控点位不存在：" + code);
        }
        if (req.getMonitorName() != null) e.setMonitorName(req.getMonitorName());
        if (req.getOnline() != null) e.setOnline(req.getOnline());
        if (req.getIntegrity() != null) e.setIntegrity(req.getIntegrity());
        if (req.getMonitorType() != null) e.setMonitorType(req.getMonitorType());
        if (req.getDepartment() != null) e.setDepartment(req.getDepartment());
        if (req.getZoneCode() != null) e.setZoneCode(req.getZoneCode());
        if (req.getLocation() != null) e.setLocation(req.getLocation());
        if (req.getHeight() != null) e.setHeight(req.getHeight());
        if (req.getAngle() != null) e.setAngle(req.getAngle());
        tvMonitorMapper.updateById(e);
        return toMonitorSummary(e, zoneNameMap());
    }

    /** 删除监控点位（设备/防区管理）。成功后广播 tv.monitor.changed。 */
    @RealtimeSync(domain = "tv.monitor")
    public void deleteMonitor(String code) {
        FacTvMonitor e = tvMonitorMapper.selectOne(new LambdaQueryWrapper<FacTvMonitor>()
                .eq(FacTvMonitor::getMonitorCode, code));
        if (e == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "监控点位不存在：" + code);
        }
        tvMonitorMapper.deleteById(e.getId());
    }

    private FacTvMonitor toMonitorEntity(TvMonitorUpsertRequest req) {
        FacTvMonitor e = new FacTvMonitor();
        e.setMonitorCode(req.getMonitorCode());
        e.setMonitorName(req.getMonitorName());
        e.setOnline(req.getOnline());
        e.setIntegrity(req.getIntegrity());
        e.setMonitorType(req.getMonitorType());
        e.setDepartment(req.getDepartment());
        e.setZoneCode(req.getZoneCode());
        e.setLocation(req.getLocation());
        e.setHeight(req.getHeight());
        e.setAngle(req.getAngle());
        return e;
    }

    private TvMonitorSummary toMonitorSummary(FacTvMonitor e, Map<String, String> zoneMap) {
        TvMonitorSummary s = new TvMonitorSummary();
        s.setCode(e.getMonitorCode());
        s.setName(e.getMonitorName());
        s.setOnline(Boolean.TRUE.equals(e.getOnline()));
        s.setDepartment(e.getDepartment());
        s.setZoneCode(e.getZoneCode());
        s.setZoneName(e.getZoneCode() == null ? null : zoneMap.get(e.getZoneCode()));
        return s;
    }

    // ===================== 录像截图采集入库闭环（V78） =====================

    /**
     * 设备/采集端上报单条录像截图：解码 base64 → 落库 fac_tv_snapshot → 广播 tv.snapshot.changed。
     * monitor_name 缺省时按 monitor_code 回查 fac_tv_monitor；created_at 由服务端生成。
     */
    @RealtimeSync(domain = "tv.snapshot")
    public TvSnapshotIngestResult submitSnapshot(TvSnapshotIngestRequest req) {
        FacTvSnapshot e = ingestOne(req);
        snapshotListCache.invalidateAll();
        return toIngestResult(e);
    }

    /**
     * 批量采集入库（供 {@link com.sinopec.mmsecurity.collector.TvCollector} 主动拉取场景）：
     * 循环 ingestOne（单条非法仅告警跳过，不中断整批）→ 写后一次性失效缓存 → 单次广播
     * tv.snapshot.changed，避免 N 条上报触发 N 次大屏刷新。
     *
     * @return 成功入库条数
     */
    @RealtimeSync(domain = "tv.snapshot")
    public int submitSnapshots(List<TvSnapshotIngestRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return 0;
        int n = 0;
        for (TvSnapshotIngestRequest req : reqs) {
            try {
                ingestOne(req);
                n++;
            } catch (BusinessException be) {
                log.warn("[tv] 采集项入库失败（monitorCode={}）：{}", req.getMonitorCode(), be.getMessage());
            }
        }
        snapshotListCache.invalidateAll();
        return n;
    }

    /** 单条入库核心逻辑（校验 + 解码 + 落库，不含缓存失效与广播）。 */
    private FacTvSnapshot ingestOne(TvSnapshotIngestRequest req) {
        if (req.getMonitorCode() == null || req.getMonitorCode().isBlank()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "monitorCode 不能为空");
        }
        if (req.getImageBase64() == null || req.getImageBase64().isBlank()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "imageBase64 不能为空");
        }
        byte[] bytes = decodeBase64(req.getImageBase64());

        String name = req.getMonitorName();
        String zoneCode = null;
        FacTvMonitor m = tvMonitorMapper.selectOne(new LambdaQueryWrapper<FacTvMonitor>()
                .eq(FacTvMonitor::getMonitorCode, req.getMonitorCode()));
        if (m != null) {
            if (name == null || name.isBlank()) name = m.getMonitorName();
            zoneCode = m.getZoneCode();
        }

        FacTvSnapshot e = new FacTvSnapshot();
        e.setMonitorCode(req.getMonitorCode());
        e.setMonitorName(name);
        e.setZoneCode(zoneCode);
        e.setCaptureTime(req.getCaptureTime() == null || req.getCaptureTime().isBlank()
                ? now() : req.getCaptureTime());
        e.setEventType(req.getEventType());
        e.setReviewStatus("PENDING");
        e.setSource(req.getSource() == null || req.getSource().isBlank() ? "DEVICE" : req.getSource());
        e.setSnapshotBytes(bytes);
        e.setCreatedAt(now());
        e.setSortNo(0);
        e.setAlarmId(req.getAlarmId());
        e.setAlarmType(req.getAlarmType());
        snapshotMapper.insert(e);
        return e;
    }

    /**
     * 录像截图分页列表（最新在前）。可按关联告警 alarmId / alarmType 反向过滤，
     * 也可按监控点位 monitorCode / 防区 zoneCode / 采集时间区间 startTime~endTime 过滤，
     * 供「生产告警详情内嵌关联抓拍」精准取数（跨域联动）及「设备/防区筛选」二级页使用。
     */
    public TvSnapshotPage listSnapshots(int page, int size, Long alarmId, String alarmType,
                                        String monitorCode, String zoneCode, String startTime, String endTime) {
        if (page < 1) page = 1;
        if (size < 1) size = 12;
        final int p = page, s = size;
        final String key = "p" + p + "s" + s
                + "a" + (alarmId == null ? "0" : alarmId)
                + "t" + (alarmType == null ? "" : alarmType)
                + "mc" + (monitorCode == null ? "" : monitorCode)
                + "zc" + (zoneCode == null ? "" : zoneCode)
                + "st" + (startTime == null ? "" : startTime)
                + "et" + (endTime == null ? "" : endTime);
        return snapshotListCache.get(key, k -> computeSnapshotPage(p, s, alarmId, alarmType, monitorCode, zoneCode, startTime, endTime));
    }

    private TvSnapshotPage computeSnapshotPage(int page, int size, Long alarmId, String alarmType,
                                               String monitorCode, String zoneCode, String startTime, String endTime) {
        LambdaQueryWrapper<FacTvSnapshot> q = new LambdaQueryWrapper<FacTvSnapshot>()
                .orderByDesc(FacTvSnapshot::getId);
        if (alarmId != null) q.eq(FacTvSnapshot::getAlarmId, alarmId);
        if (alarmType != null && !alarmType.isBlank()) q.eq(FacTvSnapshot::getAlarmType, alarmType);
        if (monitorCode != null && !monitorCode.isBlank()) q.eq(FacTvSnapshot::getMonitorCode, monitorCode);
        if (zoneCode != null && !zoneCode.isBlank()) q.eq(FacTvSnapshot::getZoneCode, zoneCode);
        if (startTime != null && !startTime.isBlank()) q.ge(FacTvSnapshot::getCaptureTime, startTime);
        if (endTime != null && !endTime.isBlank()) q.le(FacTvSnapshot::getCaptureTime, endTime);
        Page<FacTvSnapshot> pg = snapshotMapper.selectPage(new Page<>(page, size), q);
        TvSnapshotPage out = new TvSnapshotPage();
        out.setTotal(pg.getTotal());
        out.setPage(page);
        out.setSize(size);
        out.setPages((int) Math.ceil(pg.getTotal() / (double) size));
        Map<String, String> zoneMap = zoneNameMap();
        out.setList(pg.getRecords().stream().map(e -> toItem(e, zoneMap)).collect(Collectors.toList()));
        return out;
    }

    /**
     * 设备级历史回放：指定监控点位（monitorCode）的录像截图分页列表（最新在前）。
     * 供「设备/防区筛选」二级页按设备维度回放历史抓拍。
     */
    public TvSnapshotPage monitorSnapshots(String code, int page, int size, String startTime, String endTime) {
        if (page < 1) page = 1;
        if (size < 1) size = 12;
        final int p = page, s = size;
        final String key = "dev" + (code == null ? "" : code) + "p" + p + "s" + s
                + "st" + (startTime == null ? "" : startTime) + "et" + (endTime == null ? "" : endTime);
        return snapshotListCache.get(key, k -> computeMonitorPage(code, p, s, startTime, endTime));
    }

    private TvSnapshotPage computeMonitorPage(String code, int page, int size, String startTime, String endTime) {
        LambdaQueryWrapper<FacTvSnapshot> q = new LambdaQueryWrapper<FacTvSnapshot>()
                .eq(FacTvSnapshot::getMonitorCode, code)
                .orderByDesc(FacTvSnapshot::getId);
        if (startTime != null && !startTime.isBlank()) q.ge(FacTvSnapshot::getCaptureTime, startTime);
        if (endTime != null && !endTime.isBlank()) q.le(FacTvSnapshot::getCaptureTime, endTime);
        Page<FacTvSnapshot> pg = snapshotMapper.selectPage(new Page<>(page, size), q);
        TvSnapshotPage out = new TvSnapshotPage();
        out.setTotal(pg.getTotal());
        out.setPage(page);
        out.setSize(size);
        out.setPages((int) Math.ceil(pg.getTotal() / (double) size));
        Map<String, String> zoneMap = zoneNameMap();
        out.setList(pg.getRecords().stream().map(e -> toItem(e, zoneMap)).collect(Collectors.toList()));
        return out;
    }

    /** 录像截图字节（JPEG）；无则 null（端点层转 404）。 */
    public byte[] getSnapshotBytes(Long id) {
        FacTvSnapshot e = snapshotMapper.selectById(id);
        return e == null ? null : e.getSnapshotBytes();
    }

    /**
     * 确认录像截图（PENDING→ACKED），需权限码 video:snapshot:ack（Controller 层校验）。
     * 成功后广播 tv.snapshot.changed。未命中 id 时抛 NOT_FOUND。
     */
    @RealtimeSync(domain = "tv.snapshot")
    public TvSnapshotAckResult ackSnapshot(Long id) {
        FacTvSnapshot e = snapshotMapper.selectById(id);
        if (e == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "录像截图不存在");
        }
        e.setReviewStatus("ACKED");
        snapshotMapper.updateById(e);
        snapshotListCache.invalidateAll();
        TvSnapshotAckResult r = new TvSnapshotAckResult();
        r.setId(e.getId());
        r.setReviewStatus(e.getReviewStatus());
        return r;
    }

    private TvSnapshotItem toItem(FacTvSnapshot e, Map<String, String> zoneMap) {
        TvSnapshotItem i = new TvSnapshotItem();
        i.setId(e.getId());
        i.setMonitorCode(e.getMonitorCode());
        i.setMonitorName(e.getMonitorName());
        i.setCaptureTime(e.getCaptureTime());
        i.setEventType(e.getEventType());
        i.setReviewStatus(e.getReviewStatus());
        i.setSource(e.getSource());
        i.setCreatedAt(e.getCreatedAt());
        i.setHasImage(e.getSnapshotBytes() != null && e.getSnapshotBytes().length > 0);
        i.setAlarmId(e.getAlarmId());
        i.setAlarmType(e.getAlarmType());
        i.setZoneCode(e.getZoneCode());
        i.setZoneName(e.getZoneCode() == null ? null : zoneMap.get(e.getZoneCode()));
        return i;
    }

    /**
     * 生产告警自动关联兜底：当告警无显式关联抓拍时，把「告警发生时间 ±15 分钟内、且位置关键词
     * （告警 location 与快照 monitorName/zoneName 任一包含）匹配」的未关联快照回填 alarm_id/alarm_type，
     * 落库建立数据级关联（绝不编造新抓拍）。仅更新 alarm_id 为空的快照，幂等；
     * 真实环境若采集端上报时已带 alarmId 则走显式关联，本方法不会覆盖。返回新关联条数。
     */
    public int autoRelateSnapshotsForAlarm(Long alarmId, String alarmType, String location, String occurredAt) {
        if (alarmId == null || occurredAt == null || occurredAt.isBlank()) return 0;
        LocalDateTime occ = parseTs(occurredAt);
        if (occ == null) return 0;
        String fromStr = occ.minusMinutes(15).format(TS_FMT);
        String toStr = occ.plusMinutes(15).format(TS_FMT);
        List<FacTvSnapshot> candidates = snapshotMapper.selectList(new LambdaQueryWrapper<FacTvSnapshot>()
                .isNull(FacTvSnapshot::getAlarmId)
                .between(FacTvSnapshot::getCaptureTime, fromStr, toStr)
                .orderByDesc(FacTvSnapshot::getId));
        if (candidates.isEmpty()) return 0;
        Map<String, String> zoneMap = zoneNameMap();
        String locKey = location == null ? "" : location.toLowerCase();
        if (locKey.length() < 2) return 0;
        int n = 0;
        for (FacTvSnapshot s : candidates) {
            String snapKey = ((s.getMonitorName() == null ? "" : s.getMonitorName())
                    + " " + (s.getZoneCode() == null ? "" : zoneMap.getOrDefault(s.getZoneCode(), "")))
                    .toLowerCase();
            if (snapKey.contains(locKey) || (locKey.contains(snapKey) && !snapKey.isBlank())) {
                s.setAlarmId(alarmId);
                s.setAlarmType(alarmType);
                snapshotMapper.updateById(s);
                n++;
            }
        }
        if (n > 0) snapshotListCache.invalidateAll();
        return n;
    }

    private LocalDateTime parseTs(String s) {
        try {
            return LocalDateTime.parse(s, TS_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private TvSnapshotIngestResult toIngestResult(FacTvSnapshot e) {
        TvSnapshotIngestResult r = new TvSnapshotIngestResult();
        r.setId(e.getId());
        r.setMonitorCode(e.getMonitorCode());
        r.setCaptureTime(e.getCaptureTime());
        r.setReviewStatus(e.getReviewStatus());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }

    private String now() {
        return LocalDateTime.now().format(TS_FMT);
    }

    private byte[] decodeBase64(String s) {
        String b64 = s;
        int comma = s.indexOf(',');
        if (comma >= 0 && s.startsWith("data:")) {
            b64 = s.substring(comma + 1);
        }
        try {
            return Base64.getDecoder().decode(b64);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "imageBase64 解码失败");
        }
    }

    private TvInspectionItem toInspectionItem(FacTvInspectionRecord record) {
        TvInspectionItem item = new TvInspectionItem();
        item.setId(record.getId());
        item.setKind(record.getRecordKind());
        item.setAreaCode(record.getAreaCode());
        item.setBadge(record.getBadge());
        item.setDepartment(record.getDepartment());
        item.setGate(record.getGateName());
        item.setTime(record.getRecordTime());
        if (KIND_VEHICLE.equals(record.getRecordKind())) {
            item.setPlate(record.getSubjectName());
        } else {
            item.setName(record.getSubjectName());
        }
        return item;
    }
}
