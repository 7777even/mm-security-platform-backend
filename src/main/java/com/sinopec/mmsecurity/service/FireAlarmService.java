package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.FireAlarmItem;
import com.sinopec.mmsecurity.dto.FireAlarmPageResult;
import com.sinopec.mmsecurity.entity.FacFireAlarm;
import com.sinopec.mmsecurity.mapper.FacFireAlarmMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消防报警业务服务：分页查询消防报警（火灾/烟雾/GDS/设备故障），数据来自真实表 fac_fire_alarm。
 * 返回强类型 FireAlarmPageResult（list/total/page/size），与前端 PageResult 同构。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FireAlarmService {

    private final FacFireAlarmMapper fireAlarmMapper;

    public FireAlarmPageResult page(long page, long size) {
        Page<FacFireAlarm> p = fireAlarmMapper.selectPage(new Page<>(page, size), null);
        FireAlarmPageResult result = new FireAlarmPageResult();
        result.setList(p.getRecords().stream().map(this::toItem).toList());
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return result;
    }

    private FireAlarmItem toItem(FacFireAlarm e) {
        FireAlarmItem d = new FireAlarmItem();
        d.setAlarmId(e.getAlarmId());
        d.setTypeLabel(e.getTypeLabel());
        d.setTypeTone(e.getTypeTone());
        d.setSource(e.getSource());
        d.setObjectType(e.getObjectType());
        d.setObjectName(e.getObjectName());
        d.setLevel(e.getLevel());
        d.setDescription(e.getDescription());
        d.setLocation(e.getLocation());
        d.setTime(e.getTime());
        d.setFalseAlarm(e.getFalseAlarm());
        d.setStatus(e.getStatus());
        d.setRescueEventId(e.getRescueEventId());
        d.setMonitorId(e.getMonitorId());
        d.setMonitorLabel(e.getMonitorLabel());
        d.setOnsiteMonitorId(e.getOnsiteMonitorId());
        d.setOnsiteMonitorLabel(e.getOnsiteMonitorLabel());
        d.setTitle(e.getTitle());
        return d;
    }
}
