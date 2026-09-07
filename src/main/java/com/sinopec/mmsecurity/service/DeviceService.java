package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.DeviceCode;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.mapper.FacDeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final FacDeviceMapper deviceMapper;
    private final Random rnd = new Random();

    /**
     * 分页查询。兼容前端的 level/status/zone/deviceCode 筛选（参照 mock 的分页助手）。
     */
    public Page<FacDevice> page(long page, long size, String status, String zone, String deviceCode) {
        LambdaQueryWrapper<FacDevice> qw = new LambdaQueryWrapper<>();
        qw.eq(FacDevice::getDeleted, 0);
        if (status != null && !status.isEmpty()) qw.eq(FacDevice::getStatus, Integer.parseInt(status));
        if (zone != null && !zone.isEmpty()) qw.like(FacDevice::getZone, zone);
        if (deviceCode != null && !deviceCode.isEmpty()) {
            if (deviceCode.length() != 20) {
                throw new BusinessException(ResultCode.DEVICE_CODE_INVALID, "deviceCode 必须为 20 位");
            }
            qw.eq(FacDevice::getDeviceCode, deviceCode);
        }
        qw.orderByDesc(FacDevice::getUpdatedAt);
        return deviceMapper.selectPage(new Page<>(page, size), qw);
    }

    public FacDevice byCode(String code) {
        if (code == null || code.length() != 20) {
            throw new BusinessException(ResultCode.DEVICE_CODE_INVALID, "deviceCode 必须为 20 位");
        }
        FacDevice d = deviceMapper.selectById(code);
        if (d == null) {
            throw new BusinessException(ResultCode.DEVICE_NOT_FOUND, "设备不存在");
        }
        return d;
    }

    /**
     * dev 兜底：DB 无设备时生成模拟数据列表（便于大屏/后台联调）
     */
    public List<FacDevice> devFallbackList(int n) {
        String[] zones = {"罐区A", "罐区B", "装置C", "装卸区", "危化仓库"};
        String[] types = {"FIRE", "GAS", "FLOOD", "CCTV"};
        java.util.List<FacDevice> list = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            FacDevice d = new FacDevice();
            d.setDeviceCode(randomCode());
            d.setDeviceName(types[rnd.nextInt(types.length)] + "-" + (1000 + i));
            d.setDeviceType(types[rnd.nextInt(types.length)]);
            d.setZone(zones[rnd.nextInt(zones.length)]);
            d.setStatus(rnd.nextInt(3));
            d.setLat(21.0 + rnd.nextDouble());
            d.setLon(110.0 + rnd.nextDouble());
            d.setDeleted(0);
            d.setCreatedAt(LocalDateTime.now());
            list.add(d);
        }
        return list;
    }

    private String randomCode() {
        char[] cs = new char[20];
        for (int i = 0; i < 20; i++) {
            if (rnd.nextBoolean()) cs[i] = (char) ('0' + rnd.nextInt(10));
            else cs[i] = (char) ('A' + rnd.nextInt(26));
        }
        return new String(cs);
    }
}
