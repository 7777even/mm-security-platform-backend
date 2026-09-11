package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.GeoJsonFeatureCollection;
import com.sinopec.mmsecurity.dto.MapZoneSigns;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/map")
@RequireAuth
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping("/alarms")
    public Result<GeoJsonFeatureCollection> alarmPoints() {
        return Result.ok(mapService.alarmPoints());
    }

    @GetMapping("/devices")
    public Result<GeoJsonFeatureCollection> devicePoints() {
        return Result.ok(mapService.devicePoints());
    }

    /** 3D 地图装置区信息牌（V42 fac_map_zone_sign），取代前端 MAP_THEME 硬编码文案。 */
    @GetMapping("/zone-signs")
    public Result<MapZoneSigns> zoneSigns() {
        return Result.ok(mapService.zoneSigns());
    }
}
