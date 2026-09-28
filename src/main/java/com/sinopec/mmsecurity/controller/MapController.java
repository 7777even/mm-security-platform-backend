package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.entity.FacMapZoneSign;
import com.sinopec.mmsecurity.entity.FacMonitoringPoint;
import com.sinopec.mmsecurity.mapper.FacMapZoneSignMapper;
import com.sinopec.mmsecurity.mapper.FacMonitoringPointMapper;
import com.sinopec.mmsecurity.security.RequireAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 一张图点位接口：报警点位 / 设备点位，均返回 GeoJSON FeatureCollection（WGS84 经纬度）。
 *
 * <p>真源为 {@code fac_monitoring_point}（安全监测点位表，含经纬度与实时状态）。按状态拆分：
 * <ul>
 *   <li>报警点位（/map/alarms）：状态为 alarm / warning 的监测点，level 映射 alarm→3、warning→2；</li>
 *   <li>设备点位（/map/devices）：状态为 normal 的监测点，status=ONLINE。</li>
 * </ul>
 * 与前端 {@code map.openapi.json#/map/alarms}、{@code /map/devices} 字节级对齐（要素 properties 含
 * alarmId/level/name/status/type 或 deviceCode/name/status，geometry.coordinates=[lng,lat]）。
 * 另提供 {@code /map/zone-signs}：3D 厂区装置区信息牌（popups/tealTags），与 V42 表
 * {@code fac_map_zone_sign} 及前端 {@code map.openapi.json#/map/zone-signs} 对齐。
 */
@RestController
@RequestMapping("/api/v1")
@RequireAuth
@RequiredArgsConstructor
public class MapController {

    private final FacMonitoringPointMapper monitoringPointMapper;
    private final FacMapZoneSignMapper zoneSignMapper;

    @GetMapping("/map/alarms")
    public Result<Map<String, Object>> getAlarmPoints() {
        List<FacMonitoringPoint> points = monitoringPointMapper.selectList(null);
        List<Map<String, Object>> features = points.stream()
                .filter(p -> isAlarming(p.getStatus()))
                .map(this::toAlarmFeature)
                .collect(Collectors.toList());
        return Result.ok(featureCollection(features));
    }

    @GetMapping("/map/devices")
    public Result<Map<String, Object>> getDevicePoints() {
        List<FacMonitoringPoint> points = monitoringPointMapper.selectList(null);
        List<Map<String, Object>> features = points.stream()
                .filter(p -> "normal".equalsIgnoreCase(p.getStatus()))
                .map(this::toDeviceFeature)
                .collect(Collectors.toList());
        return Result.ok(featureCollection(features));
    }

    /**
     * 3D 厂区装置区信息牌（V42）：从 {@code fac_map_zone_sign} 读取，按 sign_kind 拆分——
     * ALERT→popups（红色区块信息牌），TEAL→tealTags（青色信息牌），均按 sort_no 升序。
     * 取代前端 MaomingPetroCesiumMap 内硬编码的 plantZonePopups / plantZoneTealTags；
     * 渲染主题（颜色/高度/景深）与地图标注几何保留前端。
     */
    @GetMapping("/map/zone-signs")
    public Result<Map<String, Object>> getZoneSigns() {
        List<FacMapZoneSign> rows = zoneSignMapper.selectList(null);
        Comparator<FacMapZoneSign> bySort = Comparator.comparingInt(
                r -> r.getSortNo() != null ? r.getSortNo() : Integer.MAX_VALUE);

        List<Map<String, Object>> popups = rows.stream()
                .filter(r -> "ALERT".equalsIgnoreCase(r.getSignKind()))
                .sorted(bySort)
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("title", r.getTitle());
                    m.put("location", r.getLocation());
                    m.put("status", r.getStatusText());
                    m.put("statusLevel", r.getStatusLevel());
                    return m;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> tealTags = rows.stream()
                .filter(r -> "TEAL".equalsIgnoreCase(r.getSignKind()))
                .sorted(bySort)
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("title", r.getTitle());
                    m.put("status", r.getStatusText());
                    m.put("value", r.getStatValue());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("popups", popups);
        data.put("tealTags", tealTags);
        return Result.ok(data);
    }

    private boolean isAlarming(String status) {
        if (status == null) return false;
        String s = status.trim().toUpperCase();
        return s.equals("ALARM") || s.equals("WARNING");
    }

    private Map<String, Object> toAlarmFeature(FacMonitoringPoint p) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("alarmId", p.getId());
        props.put("level", "alarm".equalsIgnoreCase(p.getStatus()) ? 3 : 2);
        props.put("name", p.getName());
        props.put("status", "ACTIVE");
        props.put("type", p.getCategory());
        props.put("category", p.getCategory());
        props.put("org", p.getOrg());
        props.put("lastTime", p.getLastTime());
        return feature(p.getLongitude(), p.getLatitude(), props);
    }

    private Map<String, Object> toDeviceFeature(FacMonitoringPoint p) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("deviceCode", p.getId());
        props.put("name", p.getName());
        props.put("status", "ONLINE");
        props.put("category", p.getCategory());
        props.put("org", p.getOrg());
        props.put("lastTime", p.getLastTime());
        return feature(p.getLongitude(), p.getLatitude(), props);
    }

    private Map<String, Object> feature(Double lng, Double lat, Map<String, Object> props) {
        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "Point");
        List<Double> coords = new ArrayList<>();
        coords.add(lng);
        coords.add(lat);
        geometry.put("coordinates", coords);
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("type", "Feature");
        f.put("properties", props);
        f.put("geometry", geometry);
        return f;
    }

    private Map<String, Object> featureCollection(List<Map<String, Object>> features) {
        Map<String, Object> fc = new LinkedHashMap<>();
        fc.put("type", "FeatureCollection");
        fc.put("features", features);
        return fc;
    }
}
