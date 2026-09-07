package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.AlarmTrendPoint;
import com.sinopec.mmsecurity.dto.DashboardOverview;
import com.sinopec.mmsecurity.dto.Workstation;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.entity.FacWorkstation;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.FacDeviceMapper;
import com.sinopec.mmsecurity.mapper.FacWorkstationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** 综合风险指数权重（活动报警权重更高，离线设备次之），结果四舍五入到 2 位小数 */
    private static final double WEIGHT_ACTIVE_ALARM = 0.7;
    private static final double WEIGHT_OFFLINE_DEVICE = 0.3;

    public DashboardOverview overview() {
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

    /**
     * 报警趋势：近 24 小时、按小时分 24 桶的报警计数序列。
     * 窗口 [now 截断到小时 -23h, now 截断到小时 +1h)。单条 selectList 取窗口内 fac_alarm(deleted=0)，
     * 在 Java 侧按小时起点分桶（避免 SQL DATE_FORMAT 方言差异，见 Change design）；无报警的小时 count=0。
     *
     * @param now 当前时间（由调用方传入，便于单测注入固定时钟）
     */
    public List<AlarmTrendPoint> trend24h(LocalDateTime now) {
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
}
