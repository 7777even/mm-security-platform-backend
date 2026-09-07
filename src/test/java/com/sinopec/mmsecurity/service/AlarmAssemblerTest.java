package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.AlarmItem;
import com.sinopec.mmsecurity.entity.FacAlarm;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AlarmAssembler（纯单元，不启动 Spring）：status int→string 枚举映射、字段别名。
 */
class AlarmAssemblerTest {

    private final AlarmAssembler assembler = new AlarmAssembler();

    @Test
    void status_intMappedToStringEnum() {
        FacAlarm a = new FacAlarm();
        a.setStatus(2);
        assertEquals("DISPATCHED", assembler.toItem(a).getStatus());
    }

    @Test
    void status_nullDefaultsActive() {
        FacAlarm a = new FacAlarm();
        a.setStatus(null);
        assertEquals("ACTIVE", assembler.toItem(a).getStatus());
    }

    @Test
    void fieldsAliased() {
        FacAlarm a = new FacAlarm();
        a.setAlarmId("AE-2026-001");
        a.setDeviceCode("FAC2026FIREA00000001");
        a.setContent("desc");
        LocalDateTime ts = LocalDateTime.of(2026, 9, 1, 8, 12);
        a.setOccurredAt(ts);
        AlarmItem item = assembler.toItem(a);
        assertEquals("AE-2026-001", item.getAlarmId());
        assertEquals("desc", item.getDescription());
        assertEquals("FAC2026FIREA00000001", item.getDeviceCode());
        assertEquals(ts, item.getTs());
    }
}
