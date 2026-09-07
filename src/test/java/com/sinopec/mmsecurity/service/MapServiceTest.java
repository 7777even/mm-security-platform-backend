package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.GeoJsonFeature;
import com.sinopec.mmsecurity.dto.GeoJsonFeatureCollection;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.mapper.FacDeviceMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MapService（纯 Mockito）：报警点位按 device_code 关联坐标；设备点位用自身坐标、status 映射。
 * 无坐标的点位不渲染。
 */
class MapServiceTest {

    private final AlarmMapper alarmMapper = mock(AlarmMapper.class);
    private final FacDeviceMapper deviceMapper = mock(FacDeviceMapper.class);
    private final MapService service = new MapService(alarmMapper, deviceMapper);

    @Test
    void alarmPoints_joinsDeviceCoordinates() {
        FacDevice d = new FacDevice();
        d.setDeviceCode("DC-01");
        d.setLat(21.5);
        d.setLon(110.4);
        when(deviceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(d));

        FacAlarm withDevice = new FacAlarm();
        withDevice.setAlarmId("AE-1");
        withDevice.setDeviceCode("DC-01");
        withDevice.setStatus(0);
        withDevice.setTitle("罐区报警");
        withDevice.setType("FIRE");
        withDevice.setLevel(2);
        FacAlarm withoutDevice = new FacAlarm(); // 无 deviceCode → 跳过
        withoutDevice.setDeviceCode(null);
        when(alarmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(withDevice, withoutDevice));

        GeoJsonFeatureCollection fc = service.alarmPoints();
        assertEquals(1, fc.getFeatures().size());
        GeoJsonFeature f = fc.getFeatures().get(0);
        assertEquals("FeatureCollection", fc.getType());
        assertEquals(List.of(110.4, 21.5), f.getGeometry().getCoordinates());
        assertEquals("AE-1", f.getProperties().get("alarmId"));
        assertEquals("ACTIVE", f.getProperties().get("status"));
    }

    @Test
    void devicePoints_mapsStatusAndSkipsMissingCoords() {
        FacDevice withCoords = new FacDevice();
        withCoords.setDeviceCode("DC-01");
        withCoords.setDeviceName("探头");
        withCoords.setLat(21.5);
        withCoords.setLon(110.4);
        withCoords.setStatus(1);
        FacDevice noCoords = new FacDevice();
        noCoords.setDeviceCode("DC-02");
        noCoords.setLat(null);
        noCoords.setLon(null);
        when(deviceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(withCoords, noCoords));

        GeoJsonFeatureCollection fc = service.devicePoints();
        assertEquals(1, fc.getFeatures().size());
        GeoJsonFeature f = fc.getFeatures().get(0);
        assertEquals("DC-01", f.getProperties().get("deviceCode"));
        assertEquals("ONLINE", f.getProperties().get("status"));
        assertEquals(List.of(110.4, 21.5), f.getGeometry().getCoordinates());
    }

    @Test
    void emptySources_returnEmptyCollection() {
        when(deviceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(alarmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        assertTrue(service.alarmPoints().getFeatures().isEmpty());
        assertTrue(service.devicePoints().getFeatures().isEmpty());
    }
}
