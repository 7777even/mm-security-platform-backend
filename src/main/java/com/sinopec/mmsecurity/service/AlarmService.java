package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.AlarmItem;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.EmergencyEventPayload;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AlarmService {

    private final AlarmMapper alarmMapper;
    private final AlarmAssembler assembler;

    public AlarmService(AlarmMapper alarmMapper, AlarmAssembler assembler) {
        this.alarmMapper = alarmMapper;
        this.assembler = assembler;
    }

    public Page<FacAlarm> page(long page, long size, String level, String status, String deviceCode) {
        LambdaQueryWrapper<FacAlarm> qw = new LambdaQueryWrapper<>();
        qw.eq(FacAlarm::getDeleted, 0);
        if (level != null && !level.isEmpty()) qw.eq(FacAlarm::getLevel, Integer.parseInt(level));
        if (status != null && !status.isEmpty()) qw.eq(FacAlarm::getStatus, Integer.parseInt(status));
        if (deviceCode != null && !deviceCode.isEmpty()) {
            validateDeviceCode(deviceCode);
            qw.eq(FacAlarm::getDeviceCode, deviceCode);
        }
        qw.orderByDesc(FacAlarm::getOccurredAt);
        return alarmMapper.selectPage(new Page<>(page, size), qw);
    }

    /**
     * 创建应急事件：生成业务 ID {@code AE-{yyyy}-{seq}}、默认 ACTIVE、逻辑未删；返回对外 AlarmItem。
     * deviceCode 强制 20 位 MDM 校验。
     */
    public AlarmItem create(EmergencyEventPayload payload) {
        validateDeviceCode(payload.getDeviceCode());
        FacAlarm a = new FacAlarm();
        a.setAlarmId(nextAlarmId());
        a.setLevel(payload.getLevel());
        a.setType(payload.getType());
        a.setStatus(mapStatus(payload.getStatus()));
        a.setDeviceCode(payload.getDeviceCode());
        a.setLocation(payload.getLocation());
        a.setContent(payload.getDescription());
        a.setTitle(payload.getDescription());
        a.setCategory("OTHER");
        a.setWarned(false);
        LocalDateTime now = LocalDateTime.now();
        a.setOccurredAt(now);
        a.setCreatedAt(now);
        a.setDeleted(0);
        alarmMapper.insert(a);
        return assembler.toItem(a);
    }

    /**
     * 更新应急事件：按 alarmId 寻址；不存在返回 null（前端契约：更新不存在返回 data=null）。
     */
    public AlarmItem update(String alarmId, EmergencyEventPayload payload) {
        FacAlarm existing = selectByAlarmId(alarmId);
        if (existing == null) return null;
        validateDeviceCode(payload.getDeviceCode());
        existing.setLevel(payload.getLevel());
        existing.setType(payload.getType());
        if (payload.getStatus() != null && !payload.getStatus().isEmpty()) {
            existing.setStatus(mapStatus(payload.getStatus()));
        }
        existing.setDeviceCode(payload.getDeviceCode());
        existing.setLocation(payload.getLocation());
        existing.setContent(payload.getDescription());
        existing.setTitle(payload.getDescription());
        // 乐观锁：updateById 携带 loaded version，版本不匹配时影响 0 行 → 抛 409 冲突。
        // 不静默覆盖、不重试到成功（避免活锁）。
        int rows = alarmMapper.updateById(existing);
        if (rows == 0) {
            throw new BusinessException(ResultCode.CONFLICT, "数据已被他人修改，请刷新后重试");
        }
        return assembler.toItem(existing);
    }

    /**
     * 逻辑删除应急事件：deleted 置 1；返回是否实际命中并删除了行。
     */
    public DeleteResult delete(String alarmId) {
        int rows = alarmMapper.update(null, new UpdateWrapper<FacAlarm>()
                .eq("alarm_id", alarmId)
                .eq("deleted", 0)
                .set("deleted", 1));
        DeleteResult r = new DeleteResult();
        r.setOk(rows > 0);
        return r;
    }

    private FacAlarm selectByAlarmId(String alarmId) {
        return alarmMapper.selectOne(new LambdaQueryWrapper<FacAlarm>()
                .eq(FacAlarm::getAlarmId, alarmId)
                .eq(FacAlarm::getDeleted, 0));
    }

    /**
     * 生成 {@code AE-{yyyy}-{seq}}：seq = 当年最大后缀 + 1；Java 侧解析存量 ID，避免 SQL 方言，DB 无关。
     */
    private String nextAlarmId() {
        int year = LocalDateTime.now().getYear();
        int max = 0;
        Pattern p = Pattern.compile("^AE-" + year + "-(\\d+)$");
        for (FacAlarm a : alarmMapper.selectList(new LambdaQueryWrapper<FacAlarm>().eq(FacAlarm::getDeleted, 0))) {
            if (a.getAlarmId() == null) continue;
            Matcher m = p.matcher(a.getAlarmId());
            if (m.matches()) max = Math.max(max, Integer.parseInt(m.group(1)));
        }
        return String.format("AE-%d-%03d", year, max + 1);
    }

    private void validateDeviceCode(String deviceCode) {
        if (deviceCode == null || deviceCode.length() != 20) {
            throw new BusinessException(ResultCode.DEVICE_CODE_INVALID, "deviceCode 必须为 20 位 MDM 编码");
        }
    }

    /** 前端 string 枚举 → 后端 int（缺省 ACTIVE=0） */
    private int mapStatus(String status) {
        if (status == null) return 0;
        return switch (status) {
            case "ACKED" -> 1;
            case "DISPATCHED" -> 2;
            case "CLOSED" -> 3;
            default -> 0;
        };
    }
}
