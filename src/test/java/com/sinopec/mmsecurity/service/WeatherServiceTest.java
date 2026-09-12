package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.WeatherOverview;
import com.sinopec.mmsecurity.entity.FacWeatherCurrent;
import com.sinopec.mmsecurity.entity.FacWeatherDaily;
import com.sinopec.mmsecurity.entity.FacWeatherHourly;
import com.sinopec.mmsecurity.mapper.FacWeatherCurrentMapper;
import com.sinopec.mmsecurity.mapper.FacWeatherDailyMapper;
import com.sinopec.mmsecurity.mapper.FacWeatherHourlyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** 天气服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private FacWeatherCurrentMapper currentMapper;
    @Mock
    private FacWeatherHourlyMapper hourlyMapper;
    @Mock
    private FacWeatherDailyMapper dailyMapper;

    @InjectMocks
    private WeatherService service;

    @BeforeEach
    void resetCaches() {
        service.clearCaches();
    }

    @Test
    void overview_mapsCurrentHourlyAndDaily() {
        FacWeatherCurrent current = new FacWeatherCurrent();
        current.setTemperature(29);
        current.setConditionText("多云");
        current.setAirQuality(35);
        current.setAirQualityLevel("优");
        current.setWindDirection("东南风");
        current.setWindSpeed("2.4m/s");
        current.setWindLevel("2级");
        current.setHumidityText("76%");
        current.setPressureText("1004hPa");
        current.setVisibilityText("18km");
        current.setRainfallText("0.0mm");
        current.setUpdatedAt("08-25 10:30");
        when(currentMapper.selectList(any())).thenReturn(List.of(current));

        FacWeatherHourly hour = new FacWeatherHourly();
        hour.setTimeLabel("当前");
        hour.setRainValue(0.0);
        hour.setWindValue(2.4);
        hour.setTemperature(29);
        hour.setPressureValue(1004.0);
        hour.setHumidityValue(76);
        hour.setSortNo(1);
        when(hourlyMapper.selectList(any())).thenReturn(List.of(hour));

        FacWeatherDaily day = new FacWeatherDaily();
        day.setDayLabel("今天");
        day.setDateLabel("08-25");
        day.setConditionText("多云");
        day.setIconText("⛅");
        day.setHighTemp(32);
        day.setLowTemp(26);
        day.setWindText("东南风 2级");
        day.setHumidityValue(76);
        day.setRainValue(0.0);
        day.setSortNo(1);
        when(dailyMapper.selectList(any())).thenReturn(List.of(day));

        WeatherOverview overview = service.overview();

        assertEquals(Integer.valueOf(29), overview.getCurrent().getTemperature());
        assertEquals("多云", overview.getCurrent().getCondition());
        assertEquals("2.4m/s", overview.getCurrent().getWindSpeed());
        assertEquals("76%", overview.getCurrent().getHumidity());
        assertEquals("1004hPa", overview.getCurrent().getPressure());
        assertEquals("08-25 10:30", overview.getCurrent().getUpdatedAt());
        assertEquals(1, overview.getHourly().size());
        assertEquals("当前", overview.getHourly().get(0).getTime());
        assertEquals(Double.valueOf(2.4), overview.getHourly().get(0).getWind());
        assertEquals(1004, overview.getHourly().get(0).getPressure().intValue());
        assertEquals(1, overview.getDaily().size());
        assertEquals("今天", overview.getDaily().get(0).getDay());
        assertEquals("⛅", overview.getDaily().get(0).getIcon());
        assertEquals(Integer.valueOf(32), overview.getDaily().get(0).getHigh());
        assertEquals(Integer.valueOf(26), overview.getDaily().get(0).getLow());
        assertEquals("东南风 2级", overview.getDaily().get(0).getWind());
    }

    @Test
    void overview_handlesMissingRows() {
        when(currentMapper.selectList(any())).thenReturn(List.of());
        when(hourlyMapper.selectList(any())).thenReturn(List.of());
        when(dailyMapper.selectList(any())).thenReturn(List.of());

        WeatherOverview overview = service.overview();

        assertNull(overview.getCurrent());
        assertEquals(0, overview.getHourly().size());
        assertEquals(0, overview.getDaily().size());
    }
}
