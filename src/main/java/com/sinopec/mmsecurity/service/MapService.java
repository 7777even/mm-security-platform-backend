package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.GeoJsonFeature;
import com.sinopec.mmsecurity.dto.GeoJsonFeatureCollection;
import com.sinopec.mmsecurity.dto.GeoJsonGeometry;
import com.sinopec.mmsecurity.dto.MapZoneSignPopup;
import com.sinopec.mmsecurity.dto.MapZoneSignTealTag;
import com.sinopec.mmsecurity.dto.MapZoneSigns;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.entity.FacMapZoneSign;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.FacDeviceMapper;
import com.sinopec.mmsecurity.mapper.FacMapZoneSignMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 地图点位服务：返回报警 / 设备 GeoJSON FeatureCollection（WGS84）。
 *
 * <ul>
 *   <li>报警点位坐标经 {@code device_code} 关联 fac_device(lat, lon) 取得；无坐标的报警不渲染（保证几何有效）。</li>
 *   <li>设备点位直接用自身 lat/lon；status 映射 ONLINE/OFFLINE/ALARM。</li>
 * </ul>
 * 全部为真实聚合，DB 无关（Java 侧组装 GeoJSON）。
 */
@Service
@RequiredArgsConstructor
public class MapService {

    private final AlarmMapper alarmMapper;
    private final FacDeviceMapper deviceMapper;
    private final FacMapZoneSignMapper zoneSignMapper;

    /** 3D 地图装置区信息牌（V42 fac_map_zone_sign）：红色区块信息牌 + 青色信息牌。 */
    public MapZoneSigns zoneSigns() {
        List<FacMapZoneSign> rows = zoneSignMapper.selectList(
                new LambdaQueryWrapper<FacMapZoneSign>()
                        .orderByAsc(FacMapZoneSign::getSortNo));
        MapZoneSigns signs = new MapZoneSigns();
        signs.setPopups(rows.stream()
                .filter(r -> "ALERT".equals(r.getSignKind()))
                .map(r -> {
                    MapZoneSignPopup p = new MapZoneSignPopup();
                    p.setTitle(r.getTitle());
                    p.setLocation(r.getLocation());
                    p.setStatus(r.getStatusText());
                    p.setStatusLevel(r.getStatusLevel());
                    return p;
                }).collect(java.util.stream.Collectors.toList()));
        signs.setTealTags(rows.stream()
                .filter(r -> "TEAL".equals(r.getSignKind()))
                .map(r -> {
                    MapZoneSignTealTag t = new MapZoneSignTealTag();
                    t.setTitle(r.getTitle());
                    t.setStatus(r.getStatusText());
                    t.setValue(r.getStatValue());
                    return t;
                }).collect(java.util.stream.Collectors.toList()));
        return signs;
    }

    public GeoJsonFeatureCollection alarmPoints() {
        Map<String, double[]> coords = deviceCoords();
        List<FacAlarm> alarms = alarmMapper.selectList(
                new LambdaQueryWrapper<FacAlarm>().eq(FacAlarm::getDeleted, 0));
        List<GeoJsonFeature> features = new ArrayList<>();
        for (FacAlarm a : alarms) {
            double[] c = a.getDeviceCode() == null ? null : coords.get(a.getDeviceCode());
            if (c == null) continue;
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("alarmId", a.getAlarmId());
            p.put("level", a.getLevel());
            p.put("name", a.getTitle());
            p.put("status", mapAlarmStatus(a.getStatus()));
            p.put("type", a.getType());
            features.add(feature(c, p));
        }
        return collection(features);
    }

    public GeoJsonFeatureCollection devicePoints() {
        List<FacDevice> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<FacDevice>().eq(FacDevice::getDeleted, 0));
        List<GeoJsonFeature> features = new ArrayList<>();
        for (FacDevice d : devices) {
            if (d.getLat() == null || d.getLon() == null) continue;
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("deviceCode", d.getDeviceCode());
            p.put("name", d.getDeviceName());
            p.put("status", mapDeviceStatus(d.getStatus()));
            features.add(feature(new double[]{d.getLon(), d.getLat()}, p));
        }
        return collection(features);
    }

    private Map<String, double[]> deviceCoords() {
        Map<String, double[]> coords = new HashMap<>();
        for (FacDevice d : deviceMapper.selectList(
                new LambdaQueryWrapper<FacDevice>().eq(FacDevice::getDeleted, 0))) {
            if (d.getDeviceCode() != null && d.getLat() != null && d.getLon() != null) {
                coords.put(d.getDeviceCode(), new double[]{d.getLon(), d.getLat()});
            }
        }
        return coords;
    }

    private GeoJsonFeature feature(double[] c, Map<String, Object> properties) {
        GeoJsonFeature f = new GeoJsonFeature();
        GeoJsonGeometry g = new GeoJsonGeometry();
        g.setType("Point");
        g.setCoordinates(List.of(c[0], c[1]));
        f.setGeometry(g);
        f.setProperties(properties);
        return f;
    }

    private GeoJsonFeatureCollection collection(List<GeoJsonFeature> features) {
        GeoJsonFeatureCollection fc = new GeoJsonFeatureCollection();
        fc.setFeatures(features);
        return fc;
    }

    private String mapAlarmStatus(Integer status) {
        return switch (status == null ? 0 : status) {
            case 1 -> "ACKED";
            case 2 -> "DISPATCHED";
            case 3 -> "CLOSED";
            default -> "ACTIVE";
        };
    }

    private String mapDeviceStatus(Integer status) {
        return switch (status == null ? 0 : status) {
            case 1 -> "ONLINE";
            case 2 -> "ALARM";
            default -> "OFFLINE";
        };
    }
}
