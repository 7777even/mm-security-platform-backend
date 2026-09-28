package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.entity.FacMonitoringPoint;
import com.sinopec.mmsecurity.mapper.FacMonitoringPointMapper;
import com.sinopec.mmsecurity.security.RequireAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
 */
@RestController
@RequestMapping("/api/v1")
@RequireAuth
@RequiredArgsConstructor
public class MapController {

    private final FacMonitoringPointMapper monitoringPointMapper;

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
