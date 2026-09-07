package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.ClosedCase;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.EmergencyStrength;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * EmergencyService（纯 Mockito）：
 * strength/phones/knowledge/duty 静态参考配置返回；closedCases 来自 fac_alarm(status=3) 真实聚合。
 */
class EmergencyServiceTest {

    private final AlarmMapper alarmMapper = mock(AlarmMapper.class);
    private final EmergencyService service = new EmergencyService(alarmMapper);

    @Test
    void strength_returnsReferenceResources() {
        EmergencyStrength s = service.strength();
        assertEquals(8, s.getResources().size());
        assertEquals("应急专家", s.getResources().get(0).getKind());
        assertEquals(47, s.getResources().get(0).getCount());
    }

    @Test
    void closedCases_aggregatesFromAlarm() {
        FacAlarm closed = new FacAlarm();
        closed.setAlarmId("AE-2026-005");
        closed.setTitle("A装置反应釜温度异常");
        closed.setLocation("装置区 03 单元");
        closed.setOccurredAt(LocalDateTime.of(2026, 9, 4, 7, 45));
        closed.setStatus(3);
        when(alarmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(closed));

        ClosedCaseList list = service.closedCases();
        assertEquals(1, list.getCases().size());
        ClosedCase c = list.getCases().get(0);
        assertEquals("AE-2026-005", c.getCaseId());
        assertEquals("A装置反应釜温度异常", c.getTitle());
        assertEquals("系统归档", c.getHandler());
    }

    @Test
    void phones_returnsPhoneBook() {
        assertEquals(5, service.phones().getEntries().size());
        assertEquals("119", service.phones().getEntries().get(0).getNumber());
    }

    @Test
    void knowledge_returnsItems() {
        assertEquals(3, service.knowledge().getItems().size());
    }

    @Test
    void duty_returnsRoster() {
        assertEquals("白班", service.duty().getShift());
        assertEquals(2, service.duty().getMembers().size());
    }
}
