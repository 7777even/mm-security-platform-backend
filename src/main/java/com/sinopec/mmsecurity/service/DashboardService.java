package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.AlarmTrendPoint;
import com.sinopec.mmsecurity.dto.DashboardOverview;
import com.sinopec.mmsecurity.dto.RiskHeatItem;
import com.sinopec.mmsecurity.dto.SystemMessageItem;
import com.sinopec.mmsecurity.dto.Workstation;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.entity.FacWorkstation;
import com.sinopec.mmsecurity.entity.FacSystemMessage;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.FacDeviceMapper;
import com.sinopec.mmsecurity.mapper.FacWorkstationMapper;
import com.sinopec.mmsecurity.mapper.FacSystemMessageMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 态势总览服务。
 *
 * 数据全部来自真实聚合（fac_device / fac_alarm / fac_workstation 计数与查询），
 * 不再返回硬编码或随机值。字段与前端 {@code DashboardOverview} / {@code Workstation} 字节级对齐。
 *
 * 设备在线口径：fac_device.status == 1 视为在线（契约 0=离线 1=在线 2=告警）。
 * 活动报警口径：fac_alarm.status == 0（ACTIVE）视为活动未处置。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FacDeviceMapper deviceMapper;
    private final AlarmMapper alarmMapper;
    private final FacWorkstationMapper workstationMapper;
    private final FacSystemMessageMapper systemMessageMapper;

    /**
     * 大屏聚合短 TTL 缓存：轮询场景下避免每次刷新全表物化 + Java 侧聚合。
     * 仪表盘数据可容忍 10s 滞后；TTL 即最终一致窗口，无需写时失效（聚合只读 fac_device/fac_alarm，无写入口）。
     * 库无关（不引入方言 SQL），复用项目已有的 Caffeine 基础设施。
     */
    private final Cache<String, DashboardOverview> overviewCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(10)).maximumSize(1).build();
    private final Cache<String, List<RiskHeatItem>> riskHeatmapCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(10)).maximumSize(1).build();
    private final Cache<LocalDateTime, List<AlarmTrendPoint>> trendCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(10)).maximumSize(48).build();

    /** 综合风险指数权重（活动报警权重更高，离线设备次之），结果四舍五入到 2 位小数 */
    private static final double WEIGHT_ACTIVE_ALARM = 0.7;
    private static final double WEIGHT_OFFLINE_DEVICE = 0.3;

    public DashboardOverview overview() {
        return overviewCache.get("OVERVIEW", k -> computeOverview());
    }

    private DashboardOverview computeOverview() {
        long deviceTotal = deviceMapper.selectCount(new LambdaQueryWrapper<FacDevice>().eq(FacDevice::getDeleted, 0));
        long deviceOnline = deviceMapper.selectCount(new LambdaQueryWrapper<FacDevice>()
                .eq(FacDevice::getDeleted, 0).eq(FacDevice::getStatus, 1));
        long activeAlarm = alarmMapper.selectCount(new LambdaQueryWrapper<FacAlarm>()
                .eq(FacAlarm::getDeleted, 0).eq(FacAlarm::getStatus, 0));
        long offlineDevice = Math.max(0, deviceTotal - deviceOnline);

        List<Workstation> stations = workstations();
        int onlineWorkstation = (int) stations.stream().filter(Workstation::isOnline).count();

        double rawRisk = activeAlarm * WEIGHT_ACTIVE_ALARM + offlineDevice * WEIGHT_OFFLINE_DEVICE;
        double riskIndex = Math.round(rawRisk * 100.0) / 100.0;

        DashboardOverview ov = new DashboardOverview();
        ov.setActiveAlarm(activeAlarm);
        ov.setDeviceOnline(deviceOnline);
        ov.setDeviceTotal(deviceTotal);
        ov.setRiskIndex(riskIndex);
        ov.setOnlineWorkstation(onlineWorkstation);
        ov.setTs(LocalDateTime.now());
        return ov;
    }

    public List<Workstation> workstations() {
        List<FacWorkstation> rows = workstationMapper.selectList(
                new LambdaQueryWrapper<FacWorkstation>().eq(FacWorkstation::getDeleted, 0));
        return rows.stream().map(this::toWorkstation).toList();
    }

    /** 大屏底部滚动系统消息（危险/预警两类），来自 V24 fac_system_message 真实表。 */
    public List<SystemMessageItem> systemMessages() {
        List<FacSystemMessage> rows = systemMessageMapper.selectList(
                new LambdaQueryWrapper<FacSystemMessage>().orderByAsc(FacSystemMessage::getSortNo));
        return rows.stream().map(r -> {
            SystemMessageItem item = new SystemMessageItem();
            item.setId(r.getId());
            item.setType(r.getMsgType());
            item.setTitle(r.getTitle());
            item.setContent(r.getContent());
            item.setTime(r.getOccurredAt());
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 报警趋势：近 24 小时、按小时分 24 桶的报警计数序列。
     * 窗口 [now 截断到小时 -23h, now 截断到小时 +1h)。单条 selectList 取窗口内 fac_alarm(deleted=0)，
     * 在 Java 侧按小时起点分桶（避免 SQL DATE_FORMAT 方言差异，见 Change design）；无报警的小时 count=0。
     *
     * @param now 当前时间（由调用方传入，便于单测注入固定时钟）
     */
    public List<AlarmTrendPoint> trend24h(LocalDateTime now) {
        return trendCache.get(now.truncatedTo(ChronoUnit.HOURS), k -> computeTrend24h(now));
    }

    private List<AlarmTrendPoint> computeTrend24h(LocalDateTime now) {
        LocalDateTime endHour = now.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime startHour = endHour.minusHours(23);
        LocalDateTime windowEnd = endHour.plusHours(1);

        List<FacAlarm> alarms = alarmMapper.selectList(new LambdaQueryWrapper<FacAlarm>()
                .eq(FacAlarm::getDeleted, 0)
                .ge(FacAlarm::getOccurredAt, startHour)
                .lt(FacAlarm::getOccurredAt, windowEnd));

        Map<Integer, Integer> counts = new HashMap<>();
        for (FacAlarm a : alarms) {
            LocalDateTime occ = a.getOccurredAt();
            if (occ == null) continue;
            long bucket = Duration.between(startHour, occ.truncatedTo(ChronoUnit.HOURS)).toHours();
            if (bucket >= 0 && bucket <= 23) {
                counts.merge((int) bucket, 1, Integer::sum);
            }
        }

        List<AlarmTrendPoint> points = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            AlarmTrendPoint p = new AlarmTrendPoint();
            p.setHour(String.format("%02d:00", startHour.plusHours(i).getHour()));
            p.setCount(counts.getOrDefault(i, 0));
            points.add(p);
        }
        return points;
    }

    private Workstation toWorkstation(FacWorkstation w) {
        Workstation ws = new Workstation();
        ws.setId(w.getWorkstationId());
        ws.setName(w.getName());
        ws.setZone(w.getZone());
        ws.setOnline(Boolean.TRUE.equals(w.getOnline()));
        return ws;
    }

    /**
     * 风险热力图：按设备主数据的 zone 分区，基于真实数据聚合风险评分。
     * <ul>
     *   <li>设备离线数（status=0）× 0.5</li>
     *   <li>设备告警数（status=2）× 1.5</li>
     *   <li>该 zone 内活动报警数（fac_alarm status=0，经 device_code 关联 zone）× 1.0</li>
     * </ul>
     * 评分四舍五入 1 位小数，按分值降序。无随机、无硬编码，DB 无关（Java 侧聚合）。
     *
     * @return 各分区风险评分列表
     */
    public List<RiskHeatItem> riskHeatmap() {
        return riskHeatmapCache.get("RISK", k -> computeRiskHeatmap());
    }

    private List<RiskHeatItem> computeRiskHeatmap() {
        List<FacDevice> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<FacDevice>().eq(FacDevice::getDeleted, 0));

        Map<String, ZoneStat> zoneStat = new HashMap<>();
        Map<String, String> deviceZone = new HashMap<>();
        for (FacDevice d : devices) {
            String zone = d.getZone() == null || d.getZone().isEmpty() ? "未知" : d.getZone();
            ZoneStat s = zoneStat.computeIfAbsent(zone, k -> new ZoneStat());
            s.total++;
            Integer st = d.getStatus();
            if (st != null) {
                if (st == 0) s.offline++;
                else if (st == 2) s.alarm++;
            }
            if (d.getDeviceCode() != null) deviceZone.put(d.getDeviceCode(), zone);
        }

        List<FacAlarm> activeAlarms = alarmMapper.selectList(new LambdaQueryWrapper<FacAlarm>()
                .eq(FacAlarm::getDeleted, 0).eq(FacAlarm::getStatus, 0));
        Map<String, Integer> alarmByZone = new HashMap<>();
        for (FacAlarm a : activeAlarms) {
            if (a.getDeviceCode() == null) continue;
            String zone = deviceZone.get(a.getDeviceCode());
            if (zone != null) alarmByZone.merge(zone, 1, Integer::sum);
        }

        List<RiskHeatItem> items = new ArrayList<>();
        for (Map.Entry<String, ZoneStat> e : zoneStat.entrySet()) {
            ZoneStat s = e.getValue();
            double raw = s.offline * 0.5 + s.alarm * 1.5 + alarmByZone.getOrDefault(e.getKey(), 0) * 1.0;
            RiskHeatItem it = new RiskHeatItem();
            it.setZone(e.getKey());
            it.setScore(Math.round(raw * 10.0) / 10.0);
            items.add(it);
        }
        items.sort((a, b) -> Double.compare(b.getScore() == null ? 0 : b.getScore(),
                a.getScore() == null ? 0 : a.getScore()));
        return items;
    }

    /** 分区聚合临时统计（不落库） */
    private static final class ZoneStat {
        int total;
        int offline;
        int alarm;
    }

    /** 测试隔离用：清空聚合缓存，避免 DashboardService 单实例跨测试方法串味。 */
    void clearCaches() {
        overviewCache.invalidateAll();
        riskHeatmapCache.invalidateAll();
        trendCache.invalidateAll();
    }
}
