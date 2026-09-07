package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.DeviceCode;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class AlarmService {

    private final AlarmMapper alarmMapper;
    private final Random rnd = new Random();

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
                throw new com.sinopec.mmsecurity.common.BusinessException(ResultCode.DEVICE_CODE_INVALID, "deviceCode 必须为 20 位");
            }
            qw.eq(FacAlarm::getDeviceCode, deviceCode);
        }
        qw.orderByDesc(FacAlarm::getOccurredAt);
        return alarmMapper.selectPage(new Page<>(page, size), qw);
    }

    public List<FacAlarm> devFallbackList(int n) {
        String[] titles = {"烟感异常", "气体泄漏", "液位超限", "非法入侵", "温度越限"};
        String[] types = {"FIRE", "GAS", "FLOOD", "INTRUSION", "TEMP"};
        java.util.List<FacAlarm> list = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            FacAlarm a = new FacAlarm();
            a.setDeviceCode(randomCode());
            a.setLevel(1 + rnd.nextInt(4));
            a.setType(types[rnd.nextInt(types.length)]);
            a.setTitle(titles[rnd.nextInt(titles.length)]);
            a.setStatus(0);
            a.setOccurredAt(LocalDateTime.now().minusMinutes(rnd.nextInt(120)));
            a.setCreatedAt(LocalDateTime.now());
            a.setDeleted(0);
            list.add(a);
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
