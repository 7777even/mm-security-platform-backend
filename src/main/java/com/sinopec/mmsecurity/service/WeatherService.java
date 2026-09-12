package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.CurrentWeather;
import com.sinopec.mmsecurity.dto.DailyWeatherItem;
import com.sinopec.mmsecurity.dto.HourlyWeatherItem;
import com.sinopec.mmsecurity.dto.WeatherOverview;
import com.sinopec.mmsecurity.entity.FacWeatherCurrent;
import com.sinopec.mmsecurity.entity.FacWeatherDaily;
import com.sinopec.mmsecurity.entity.FacWeatherHourly;
import com.sinopec.mmsecurity.mapper.FacWeatherCurrentMapper;
import com.sinopec.mmsecurity.mapper.FacWeatherDailyMapper;
import com.sinopec.mmsecurity.mapper.FacWeatherHourlyMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.stream.Collectors;

/**
 * 天气观测与预报服务。
 *
 * <p>数据来源为 V22 落地的 fac_weather_* 真实表，取代前端硬编码的 weatherMock。
 * 带量纲的展示字符串（风速/湿度/气压/能见度/雨量）原样透传，不做单位换算。
 */
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final FacWeatherCurrentMapper currentMapper;
    private final FacWeatherHourlyMapper hourlyMapper;
    private final FacWeatherDailyMapper dailyMapper;

    /**
     * 天气首屏聚合短 TTL 缓存：大屏高频轮询场景下避免每次刷新读 3 张表。
     * 观测数据可容忍 30s 滞后；TTL 即最终一致窗口（只读 fac_weather_*，无写入口）。
     */
    private final Cache<String, WeatherOverview> overviewCache =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(30)).maximumSize(1).build();

    /** 首屏聚合：实况 + 逐小时序列 + 七日预报（带短 TTL 缓存）。 */
    public WeatherOverview overview() {
        return overviewCache.get("OVERVIEW", k -> computeOverview());
    }

    private WeatherOverview computeOverview() {
        WeatherOverview overview = new WeatherOverview();
        overview.setCurrent(currentMapper.selectList(null).stream()
                .findFirst().map(this::toCurrent).orElse(null));
        overview.setHourly(hourlyMapper.selectList(
                        new LambdaQueryWrapper<FacWeatherHourly>().orderByAsc(FacWeatherHourly::getSortNo))
                .stream().map(this::toHourly).collect(Collectors.toList()));
        overview.setDaily(dailyMapper.selectList(
                        new LambdaQueryWrapper<FacWeatherDaily>().orderByAsc(FacWeatherDaily::getSortNo))
                .stream().map(this::toDaily).collect(Collectors.toList()));
        return overview;
    }

    /** 测试隔离用：清空聚合缓存，避免跨用例污染。 */
    void clearCaches() {
        overviewCache.invalidateAll();
    }

    private CurrentWeather toCurrent(FacWeatherCurrent entity) {
        CurrentWeather dto = new CurrentWeather();
        dto.setTemperature(entity.getTemperature());
        dto.setCondition(entity.getConditionText());
        dto.setAirQuality(entity.getAirQuality());
        dto.setAirQualityLevel(entity.getAirQualityLevel());
        dto.setWindDirection(entity.getWindDirection());
        dto.setWindSpeed(entity.getWindSpeed());
        dto.setWindLevel(entity.getWindLevel());
        dto.setHumidity(entity.getHumidityText());
        dto.setPressure(entity.getPressureText());
        dto.setVisibility(entity.getVisibilityText());
        dto.setRainfall(entity.getRainfallText());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private HourlyWeatherItem toHourly(FacWeatherHourly entity) {
        HourlyWeatherItem dto = new HourlyWeatherItem();
        dto.setTime(entity.getTimeLabel());
        dto.setRain(entity.getRainValue());
        dto.setWind(entity.getWindValue());
        dto.setTemperature(entity.getTemperature());
        dto.setPressure(entity.getPressureValue());
        dto.setHumidity(entity.getHumidityValue());
        return dto;
    }

    private DailyWeatherItem toDaily(FacWeatherDaily entity) {
        DailyWeatherItem dto = new DailyWeatherItem();
        dto.setDay(entity.getDayLabel());
        dto.setDate(entity.getDateLabel());
        dto.setCondition(entity.getConditionText());
        dto.setIcon(entity.getIconText());
        dto.setHigh(entity.getHighTemp());
        dto.setLow(entity.getLowTemp());
        dto.setWind(entity.getWindText());
        dto.setHumidity(entity.getHumidityValue());
        dto.setRain(entity.getRainValue());
        return dto;
    }
}
