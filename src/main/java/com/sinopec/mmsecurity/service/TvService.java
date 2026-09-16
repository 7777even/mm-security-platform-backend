package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.TvEventBreakdownItem;
import com.sinopec.mmsecurity.dto.TvInspectionItem;
import com.sinopec.mmsecurity.dto.TvInspectionSummary;
import com.sinopec.mmsecurity.dto.TvMaintenanceOrder;
import com.sinopec.mmsecurity.dto.TvMapPoint;
import com.sinopec.mmsecurity.dto.TvMonitorDetail;
import com.sinopec.mmsecurity.dto.TvOperationStats;
import com.sinopec.mmsecurity.dto.TvOverview;
import com.sinopec.mmsecurity.dto.TvOverviewItem;
import com.sinopec.mmsecurity.entity.FacTvInspectionRecord;
import com.sinopec.mmsecurity.entity.FacTvOperationStat;
import com.sinopec.mmsecurity.entity.FacTvStatItem;
import com.sinopec.mmsecurity.entity.FacTvMapPoint;
import com.sinopec.mmsecurity.entity.FacTvMonitor;
import com.sinopec.mmsecurity.mapper.FacMajorHazardMapper;
import com.sinopec.mmsecurity.entity.FacTvMonitor;
import com.sinopec.mmsecurity.mapper.FacTvInspectionRecordMapper;
import com.sinopec.mmsecurity.mapper.FacTvOperationStatMapper;
import com.sinopec.mmsecurity.mapper.FacTvStatItemMapper;
import com.sinopec.mmsecurity.mapper.FacTvMapPointMapper;
import com.sinopec.mmsecurity.mapper.FacTvMonitorMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * 工业电视大屏（fm-tv）服务。
 *
 * <p>数据来源为 V15 落地的 fac_tv_* 真实表，取代前端硬编码的 tvMock 业务数据。
 * 地图撒点/巡检圆/扫描点位等前端静态几何不在本服务范围（与 V13 生产域同一先例）。
 */
@Service
@RequiredArgsConstructor
public class TvService {

    private static final String CAT_OVERVIEW = "OVERVIEW";
    private static final String CAT_MAINTENANCE = "MAINTENANCE";
    private static final String CAT_EVENT = "EVENT";
    private static final String KIND_VEHICLE = "VEHICLE";

    private final FacTvStatItemMapper statItemMapper;
    private final FacTvOperationStatMapper operationStatMapper;
    private final FacTvInspectionRecordMapper inspectionRecordMapper;
    private final FacTvMapPointMapper tvMapPointMapper;
    private final FacTvMonitorMapper tvMonitorMapper;
    /** 重大危险源（与 GET /hazards 同源）：用于校正总览卡片的「重大危险源」数量。 */
    private final FacMajorHazardMapper majorHazardMapper;

    /**
     * 工业电视首屏聚合短 TTL 缓存：大屏高频轮询入口，聚合读 fac_tv_stat_item + fac_tv_operation_stat。
     * 只读无写入口，TTL 即最终一致窗口。
     */
    private final Cache<String, TvOverview> overviewCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(1).build();

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

    /** 测试隔离用：清空首屏聚合缓存，避免跨用例污染。 */
    void clearCaches() {
        overviewCache.invalidateAll();
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
