package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.Workstation;
import com.sinopec.mmsecurity.entity.FacWorkstation;
import com.sinopec.mmsecurity.mapper.FacWorkstationMapper;
import com.sinopec.mmsecurity.security.DataScopeHelper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工作站/工位域：分页列表（防区过滤）。
 *
 * <p>镜像 {@link DeviceService#page} —— 复用 data_scope 行级 ABAC，zone_codes 存中文 zone_name，
 * 直接 {@code qw.in(zone, zones)} 命中。resolveZones() 三态：null=看全(ALL/匿名) / 空=1=0(最小权限) / 非空=IN(zones)。</p>
 *
 * <p>服务返回已映射的 {@link Workstation} DTO 分页（不暴露实体），映射逻辑与
 * {@code DashboardService#toWorkstation} 同源（见 design ADR-5）。</p>
 */
@Service
@RequiredArgsConstructor
public class WorkstationService {

    private final FacWorkstationMapper workstationMapper;
    private final DataScopeResolver dataScopeResolver;

    public Page<Workstation> page(long page, long size, String zone, String online) {
        LambdaQueryWrapper<FacWorkstation> qw = new LambdaQueryWrapper<>();
        qw.eq(FacWorkstation::getDeleted, 0);
        if (zone != null && !zone.isEmpty()) qw.like(FacWorkstation::getZone, zone);
        Boolean on = parseOnline(online);
        if (on != null) qw.eq(FacWorkstation::getOnline, on);
        qw.orderByDesc(FacWorkstation::getUpdatedAt);
        // data_scope 行级 ABAC：与设备域(DeviceService.page)同一约定
        DataScopeHelper.apply(qw, FacWorkstation::getZone, dataScopeResolver.resolveZones());
        Page<FacWorkstation> raw = workstationMapper.selectPage(new Page<>(page, size), qw);
        Page<Workstation> out = new Page<>();
        out.setCurrent(raw.getCurrent());
        out.setSize(raw.getSize());
        out.setTotal(raw.getTotal());
        out.setRecords(raw.getRecords().stream().map(this::toWorkstation).collect(Collectors.toList()));
        return out;
    }

    private static Boolean parseOnline(String online) {
        if (online == null || online.isEmpty()) return null;
        if ("1".equals(online) || "true".equalsIgnoreCase(online)) return Boolean.TRUE;
        if ("0".equals(online) || "false".equalsIgnoreCase(online)) return Boolean.FALSE;
        return null;
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
