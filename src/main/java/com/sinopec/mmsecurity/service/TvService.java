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
import com.sinopec.mmsecurity.entity.FacMajorHazard;
import com.sinopec.mmsecurity.entity.FacTvInspectionRecord;
import com.sinopec.mmsecurity.entity.FacTvMapPoint;
import com.sinopec.mmsecurity.entity.FacTvMonitor;
import com.sinopec.mmsecurity.entity.FacTvOperationStat;
import com.sinopec.mmsecurity.entity.FacTvSnapshot;
import com.sinopec.mmsecurity.entity.FacTvStatItem;
import com.sinopec.mmsecurity.mapper.FacMajorHazardMapper;
import com.sinopec.mmsecurity.mapper.FacTvInspectionRecordMapper;
import com.sinopec.mmsecurity.mapper.FacTvMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacTvOperationStatMapper;
import com.sinopec.mmsecurity.mapper.FacTvSnapshotMapper;
import com.sinopec.mmsecurity.mapper.FacTvStatItemMapper;
import com.sinopec.mmsecurity.mapper.FacTvMapPointMapper;
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
        d.setLocation(m.getLocation());
        d.setHeight(m.getHeight());
        d.setAngle(m.getAngle());
        return d;
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
        if (name == null || name.isBlank()) {
            FacTvMonitor m = tvMonitorMapper.selectOne(new LambdaQueryWrapper<FacTvMonitor>()
                    .eq(FacTvMonitor::getMonitorCode, req.getMonitorCode()));
            if (m != null) name = m.getMonitorName();
        }

        FacTvSnapshot e = new FacTvSnapshot();
        e.setMonitorCode(req.getMonitorCode());
        e.setMonitorName(name);
        e.setCaptureTime(req.getCaptureTime() == null || req.getCaptureTime().isBlank()
                ? now() : req.getCaptureTime());
        e.setEventType(req.getEventType());
        e.setReviewStatus("PENDING");
        e.setSource(req.getSource() == null || req.getSource().isBlank() ? "DEVICE" : req.getSource());
        e.setSnapshotBytes(bytes);
        e.setCreatedAt(now());
        e.setSortNo(0);
        snapshotMapper.insert(e);
        return e;
    }

    /** 录像截图分页列表（最新在前）。 */
    public TvSnapshotPage listSnapshots(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 12;
        final int p = page, s = size;
        return snapshotListCache.get("p" + p + "s" + s, k -> computeSnapshotPage(p, s));
    }

    private TvSnapshotPage computeSnapshotPage(int page, int size) {
        Page<FacTvSnapshot> pg = snapshotMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<FacTvSnapshot>().orderByDesc(FacTvSnapshot::getId));
        TvSnapshotPage out = new TvSnapshotPage();
        out.setTotal(pg.getTotal());
        out.setPage(page);
        out.setSize(size);
        out.setPages((int) Math.ceil(pg.getTotal() / (double) size));
        out.setList(pg.getRecords().stream().map(this::toItem).collect(Collectors.toList()));
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

    private TvSnapshotItem toItem(FacTvSnapshot e) {
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
        return i;
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
