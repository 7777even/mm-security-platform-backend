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

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final FacDeviceMapper deviceMapper;

    /**
     * 分页查询。兼容前端的 level/status/zone/deviceCode 筛选。
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
}
