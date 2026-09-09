package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.CurrentWeather;
import com.sinopec.mmsecurity.dto.DailyWeatherItem;
import com.sinopec.mmsecurity.dto.HourlyWeatherItem;
import com.sinopec.mmsecurity.dto.WeatherOverview;
import com.sinopec.mmsecurity.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 天气接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class WeatherControllerTest {

    @Mock
    private WeatherService service;

    @InjectMocks
    private WeatherController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void overview_returnsCurrentHourlyAndDaily() throws Exception {
        CurrentWeather current = new CurrentWeather();
        current.setTemperature(29);
        current.setCondition("多云");
        current.setAirQuality(35);
        current.setAirQualityLevel("优");
        current.setWindDirection("东南风");
        current.setWindSpeed("2.4m/s");
        current.setWindLevel("2级");
        current.setHumidity("76%");
        current.setPressure("1004hPa");
        current.setVisibility("18km");
        current.setRainfall("0.0mm");
        current.setUpdatedAt("08-25 10:30");
        WeatherOverview overview = new WeatherOverview();
        overview.setCurrent(current);
        overview.setHourly(List.of(hourly()));
        overview.setDaily(List.of(daily()));
        when(service.overview()).thenReturn(overview);

        mvc().perform(get("/api/v1/weather/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.current.temperature").value(29))
                .andExpect(jsonPath("$.data.current.windSpeed").value("2.4m/s"))
                .andExpect(jsonPath("$.data.current.pressure").value("1004hPa"))
                .andExpect(jsonPath("$.data.hourly[0].time").value("当前"))
                .andExpect(jsonPath("$.data.hourly[0].rain").value(0.0))
                .andExpect(jsonPath("$.data.daily[0].day").value("今天"))
                .andExpect(jsonPath("$.data.daily[0].icon").value("⛅"))
                .andExpect(jsonPath("$.data.daily[0].wind").value("东南风 2级"));
    }

    @Test
    void overview_handlesEmptyBlocks() throws Exception {
        WeatherOverview overview = new WeatherOverview();
        overview.setCurrent(null);
        overview.setHourly(List.of());
        overview.setDaily(List.of());
        when(service.overview()).thenReturn(overview);

        mvc().perform(get("/api/v1/weather/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.current").doesNotExist())
                .andExpect(jsonPath("$.data.hourly.length()").value(0))
                .andExpect(jsonPath("$.data.daily.length()").value(0));
    }

    private static HourlyWeatherItem hourly() {
        HourlyWeatherItem item = new HourlyWeatherItem();
        item.setTime("当前");
        item.setRain(0.0);
        item.setWind(2.4);
        item.setTemperature(29);
        item.setPressure(1004.0);
        item.setHumidity(76);
        return item;
    }

    private static DailyWeatherItem daily() {
        DailyWeatherItem item = new DailyWeatherItem();
        item.setDay("今天");
        item.setDate("08-25");
        item.setCondition("多云");
        item.setIcon("⛅");
        item.setHigh(32);
        item.setLow(26);
        item.setWind("东南风 2级");
        item.setHumidity(76);
        item.setRain(0.0);
        return item;
    }
}
