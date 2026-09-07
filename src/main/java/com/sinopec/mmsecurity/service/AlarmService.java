package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AlarmService {

    private final AlarmMapper alarmMapper;

    public AlarmService(AlarmMapper alarmMapper) {
        this.alarmMapper = alarmMapper;
    }

    public Page<FacAlarm> page(long page, long size, String level, String status, String deviceCode) {
        LambdaQueryWrapper<FacAlarm> qw = new LambdaQueryWrapper<>();
        qw.eq(FacAlarm::getDeleted, 0);
        if (level != null && !level.isEmpty()) qw.eq(FacAlarm::getLevel, Integer.parseInt(level));
        if (status != null && !status.isEmpty()) qw.eq(FacAlarm::getStatus, Integer.parseInt(status));
        if (deviceCode != null && !deviceCode.isEmpty()) {
            if (deviceCode.length() != 20) {
                throw new BusinessException(ResultCode.DEVICE_CODE_INVALID, "deviceCode 必须为 20 位");
            }
            qw.eq(FacAlarm::getDeviceCode, deviceCode);
        }
        qw.orderByDesc(FacAlarm::getOccurredAt);
        return alarmMapper.selectPage(new Page<>(page, size), qw);
    }
}
