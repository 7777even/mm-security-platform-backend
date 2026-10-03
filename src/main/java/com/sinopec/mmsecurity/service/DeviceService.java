package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.DeviceCode;
import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.DeviceWriteRequest;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.mapper.FacDeviceMapper;
import com.sinopec.mmsecurity.security.DataScopeHelper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final FacDeviceMapper deviceMapper;
    private final DataScopeResolver dataScopeResolver;

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
        // data_scope 行级 ABAC：与救援队伍域(brigades)同一约定——zone_codes 存中文 zone_name，
        // 直接 qw.in(zone, zones) 命中。resolveZones() 返回 null=不过滤(ALL/匿名)，
        // 空集=1=0(最小权限)，非空=IN(zones)。测试上下文 UserContext 为空→返回 null→零影响。
        Set<String> zones = dataScopeResolver.resolveZones();
        DataScopeHelper.apply(qw, FacDevice::getZone, zones);
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

    /** 新建设备台账；deviceCode 为 20 位 MDM 编码，由请求给定。广播 device 实时通道。 */
    @RealtimeSync(domain = "device")
    @Transactional
    public FacDevice createDevice(DeviceWriteRequest in) {
        if (deviceMapper.selectById(in.getDeviceCode()) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "deviceCode 已存在");
        }
        FacDevice device = new FacDevice();
        device.setDeviceCode(in.getDeviceCode());
        applyDeviceFields(device, in);
        device.setDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        device.setCreatedAt(now);
        device.setUpdatedAt(now);
        device.setVersion(0L);
        deviceMapper.insert(device);
        return device;
    }

    /** 更新设备台账（按 deviceCode）；未命中或已软删返回 null。广播 device 实时通道。 */
    @RealtimeSync(domain = "device")
    @Transactional
    public FacDevice updateDevice(String code, DeviceWriteRequest in) {
        FacDevice device = findActiveByCode(code);
        if (device == null) {
            return null;
        }
        applyDeviceFields(device, in);
        device.setUpdatedAt(LocalDateTime.now());
        deviceMapper.updateById(device);
        return device;
    }

    /**
     * 删除设备台账（按 deviceCode）：走软删除置 deleted=1，与读端点 page() 的 deleted=0 过滤保持一致。
     * 未命中 ok=false。广播 device 实时通道。
     */
    @RealtimeSync(domain = "device")
    @Transactional
    public DeleteResult deleteDevice(String code) {
        DeleteResult result = new DeleteResult();
        FacDevice device = findActiveByCode(code);
        if (device == null) {
            result.setOk(false);
            return result;
        }
        device.setDeleted(1);
        device.setUpdatedAt(LocalDateTime.now());
        result.setOk(deviceMapper.updateById(device) > 0);
        return result;
    }

    /** 按编码取未软删的设备；编码非法、未命中或已软删一律返回 null（写路径不抛异常，交由上层返回空 / ok=false）。 */
    private FacDevice findActiveByCode(String code) {
        if (code == null || code.length() != 20) {
            return null;
        }
        FacDevice device = deviceMapper.selectById(code);
        if (device == null || device.getDeleted() == null || device.getDeleted() != 0) {
            return null;
        }
        return device;
    }

    private void applyDeviceFields(FacDevice device, DeviceWriteRequest in) {
        device.setDeviceName(in.getDeviceName());
        device.setDeviceType(in.getDeviceType());
        device.setZone(in.getZone());
        device.setStatus(in.getStatus());
        device.setLat(in.getLat());
        device.setLon(in.getLon());
    }
}
