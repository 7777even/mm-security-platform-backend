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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    /** 首屏聚合：实况 + 逐小时序列 + 七日预报。 */
    public WeatherOverview overview() {
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
