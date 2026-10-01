package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.FireAlarmCreateRequest;
import com.sinopec.mmsecurity.dto.FireAlarmItem;
import com.sinopec.mmsecurity.dto.FireAlarmPageResult;
import com.sinopec.mmsecurity.dto.FireAlarmUpdateRequest;
import com.sinopec.mmsecurity.entity.FacFireAlarm;
import com.sinopec.mmsecurity.mapper.FacFireAlarmMapper;
import java.util.Set;
import java.util.UUID;
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
        // 其余业务事实字段：自由文本，无枚举约束，仅在传入非空时覆盖（read-modify-write 局部更新）。
        if (req.getTypeLabel() != null) {
            e.setTypeLabel(req.getTypeLabel());
        }
        if (req.getTypeTone() != null) {
            e.setTypeTone(req.getTypeTone());
        }
        if (req.getSource() != null) {
            e.setSource(req.getSource());
        }
        if (req.getObjectType() != null) {
            e.setObjectType(req.getObjectType());
        }
        if (req.getObjectName() != null) {
            e.setObjectName(req.getObjectName());
        }
        if (req.getLevel() != null) {
            e.setLevel(req.getLevel());
        }
        if (req.getDescription() != null) {
            e.setDescription(req.getDescription());
        }
        if (req.getLocation() != null) {
            e.setLocation(req.getLocation());
        }
        if (req.getTime() != null) {
            e.setTime(req.getTime());
        }
        if (req.getRescueEventId() != null) {
            e.setRescueEventId(req.getRescueEventId());
        }
        if (req.getMonitorId() != null) {
            e.setMonitorId(req.getMonitorId());
        }
        if (req.getMonitorLabel() != null) {
            e.setMonitorLabel(req.getMonitorLabel());
        }
        if (req.getOnsiteMonitorId() != null) {
            e.setOnsiteMonitorId(req.getOnsiteMonitorId());
        }
        if (req.getOnsiteMonitorLabel() != null) {
            e.setOnsiteMonitorLabel(req.getOnsiteMonitorLabel());
        }
        if (req.getTitle() != null) {
            e.setTitle(req.getTitle());
        }
        fireAlarmMapper.updateById(e);
        return toItem(e);
    }

    /**
     * 新增消防报警：生成业务主键 alarmId（FA- + UUID 前 18 位大写），version 置 0 开启乐观锁。
     * status 不传默认 ACTIVE，并校验枚举合法性；其余字段按请求原样落库。
     * 成功返回新建的 FireAlarmItem 供前端即时回填。
     */
    @RealtimeSync(domain = "fire-alarm.alarm")
    public FireAlarmItem create(FireAlarmCreateRequest req) {
        FacFireAlarm e = new FacFireAlarm();
        e.setAlarmId("FA-" + UUID.randomUUID().toString().replace("-", "").substring(0, 18).toUpperCase());
        e.setVersion(0L);
        e.setTitle(req.getTitle());
        e.setTime(req.getTime());
        e.setTypeLabel(req.getTypeLabel());
        e.setTypeTone(req.getTypeTone());
        e.setSource(req.getSource());
        e.setObjectType(req.getObjectType());
        e.setObjectName(req.getObjectName());
        e.setLevel(req.getLevel());
        e.setDescription(req.getDescription());
        e.setLocation(req.getLocation());
        e.setFalseAlarm(req.getFalseAlarm());
        e.setStatus(req.getStatus() == null ? "ACTIVE" : req.getStatus());
        if (e.getStatus() != null && !VALID_STATUS.contains(e.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法处置状态：" + e.getStatus());
        }
        e.setRescueEventId(req.getRescueEventId());
        e.setMonitorId(req.getMonitorId());
        e.setMonitorLabel(req.getMonitorLabel());
        e.setOnsiteMonitorId(req.getOnsiteMonitorId());
        e.setOnsiteMonitorLabel(req.getOnsiteMonitorLabel());
        e.setHandleResult(req.getHandleResult());
        e.setHandleTime(req.getHandleTime());
        e.setDispatchPersonnel(req.getDispatchPersonnel());
        e.setNotifyMethod(req.getNotifyMethod());
        fireAlarmMapper.insert(e);
        return toItem(e);
    }

    /**
     * 删除消防报警：记录不存在返回 B3 NOT_FOUND；存在则物理删除。
     */
    @RealtimeSync(domain = "fire-alarm.alarm")
    public void delete(String alarmId) {
        FacFireAlarm e = fireAlarmMapper.selectById(alarmId);
        if (e == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消防报警不存在：" + alarmId);
        }
        fireAlarmMapper.deleteById(alarmId);
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
