package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.FireAlarmItem;
import com.sinopec.mmsecurity.dto.FireAlarmPageResult;
import com.sinopec.mmsecurity.dto.FireAlarmUpdateRequest;
import com.sinopec.mmsecurity.entity.FacFireAlarm;
import com.sinopec.mmsecurity.mapper.FacFireAlarmMapper;
import java.util.Set;
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

    private static final Set<String> VALID_STATUS =
            Set.of("ACTIVE", "ACKED", "DISPATCHED", "CLOSED");
    private static final Set<String> VALID_FALSE_ALARM = Set.of("是", "否", "未核实");

    /**
     * 消防报警写回：确认/派单/闭环状态流转 + 误报标记。
     * read-modify-write：先按 alarmId 取当前记录（实体 @Version 乐观锁），仅在传入字段非空时覆盖，
     * updateById 自动携带 version 做并发防护；记录不存在返回 B3 NOT_FOUND。
     * 成功返回更新后的 FireAlarmItem 供前端即时回填。
     */
    @RealtimeSync(domain = "fire-alarm.alarm")
    public FireAlarmItem update(String alarmId, FireAlarmUpdateRequest req) {
        FacFireAlarm e = fireAlarmMapper.selectById(alarmId);
        if (e == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消防报警不存在：" + alarmId);
        }
        if (req.getStatus() != null) {
            if (!VALID_STATUS.contains(req.getStatus())) {
                throw new BusinessException(
                        ResultCode.PARAM_INVALID, "非法处置状态：" + req.getStatus());
            }
            e.setStatus(req.getStatus());
        }
        if (req.getFalseAlarm() != null) {
            if (!VALID_FALSE_ALARM.contains(req.getFalseAlarm())) {
                throw new BusinessException(
                        ResultCode.PARAM_INVALID, "非法误报标记：" + req.getFalseAlarm());
            }
            e.setFalseAlarm(req.getFalseAlarm());
        }
        // 处置字段：自由文本，无枚举约束，仅在传入非空时覆盖（read-modify-write 局部更新）。
        if (req.getHandleResult() != null) {
            e.setHandleResult(req.getHandleResult());
        }
        if (req.getHandleTime() != null) {
            e.setHandleTime(req.getHandleTime());
        }
        if (req.getDispatchPersonnel() != null) {
            e.setDispatchPersonnel(req.getDispatchPersonnel());
        }
        if (req.getNotifyMethod() != null) {
            e.setNotifyMethod(req.getNotifyMethod());
        }
        fireAlarmMapper.updateById(e);
        return toItem(e);
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
        d.setHandleResult(e.getHandleResult());
        d.setHandleTime(e.getHandleTime());
        d.setDispatchPersonnel(e.getDispatchPersonnel());
        d.setNotifyMethod(e.getNotifyMethod());
        return d;
    }
}
