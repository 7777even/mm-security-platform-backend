package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.WeatherOverview;
import com.sinopec.mmsecurity.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 天气观测与预报只读接口，数据源为 V22 fac_weather_* 真实表。
 *
 * <p>本组仅提供 GET：实况、逐小时序列、七日预报在一次聚合接口中返回。
 */
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    /** 天气首屏聚合：实况 + 逐小时序列 + 七日预报，数值单位与前端展示文案保持一致。 */
    @GetMapping("/overview")
    public Result<WeatherOverview> overview() {
        return Result.ok(weatherService.overview());
    }
}
