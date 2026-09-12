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
        overview.setOverviewItems(items.stream()
                .filter(i -> CAT_OVERVIEW.equals(i.getItemCategory()))
                .map(i -> {
                    TvOverviewItem item = new TvOverviewItem();
                    item.setId(i.getId());
                    item.setLabel(i.getLabel());
                    item.setValue(i.getItemCount());
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

        FacTvOperationStat stat = operationStatMapper.selectList(null).stream().findFirst().orElse(null);
        TvOperationStats stats = new TvOperationStats();
        if (stat != null) {
            stats.setTotal(stat.getTotalCount());
            stats.setOffline(stat.getOfflineCount());
            stats.setFault(stat.getFaultCount());
            stats.setIntegrityRate(stat.getIntegrityRate());
            stats.setOnlineRate(stat.getOnlineRate());
            stats.setEventTotal(stat.getEventTotal());
        }
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
