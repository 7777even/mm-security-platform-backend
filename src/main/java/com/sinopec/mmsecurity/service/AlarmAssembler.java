package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.AlarmItem;
import com.sinopec.mmsecurity.entity.FacAlarm;
import org.springframework.stereotype.Component;

/**
 * FacAlarm 实体 → AlarmItem DTO 的纯转换。
 *
 * 负责契约对齐的字段映射：
 * - status(int) → string 枚举（0=ACTIVE / 1=ACKED / 2=DISPATCHED / 3=CLOSED）
 * - content → description
 * - occurredAt → ts
 * - 其余字段同名透传（alarmId / deviceCode / level / type / location / category / warned / title / planId）
 */
@Component
public class AlarmAssembler {

    public AlarmItem toItem(FacAlarm a) {
        if (a == null) return null;
        AlarmItem item = new AlarmItem();
        item.setAlarmId(a.getAlarmId());
        item.setLevel(a.getLevel());
        item.setType(a.getType());
        item.setStatus(mapStatus(a.getStatus()));
        item.setDeviceCode(a.getDeviceCode());
        item.setLocation(a.getLocation());
        item.setTs(a.getOccurredAt());
        item.setDescription(a.getContent());
        item.setCategory(a.getCategory());
        item.setWarned(a.getWarned());
        item.setTitle(a.getTitle());
        item.setPlanId(a.getPlanId());
        return item;
    }

    private String mapStatus(Integer status) {
        return switch (status == null ? 0 : status) {
            case 1 -> "ACKED";
            case 2 -> "DISPATCHED";
            case 3 -> "CLOSED";
            default -> "ACTIVE";
        };
    }
}
