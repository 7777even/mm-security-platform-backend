package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import java.time.LocalDateTime;
import java.util.List;

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

    private Workstation toWorkstation(FacWorkstation w) {
        Workstation ws = new Workstation();
        ws.setId(w.getWorkstationId());
        ws.setName(w.getName());
        ws.setZone(w.getZone());
        ws.setOnline(Boolean.TRUE.equals(w.getOnline()));
        return ws;
    }
}
