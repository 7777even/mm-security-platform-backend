package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.dto.AlarmItem;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.EmergencyEventPayload;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AlarmService（纯 Mockito，不启动 Spring 上下文）：
 * 分页委派、空库返回空页、deviceCode 非 20 位抛 BusinessException；
 * create 生成 AE-{yyyy}-NNN 且默认 ACTIVE；update 不存在返回 null；delete 逻辑删除返回 ok。
 */
class AlarmServiceTest {

    private final AlarmMapper mapper = mock(AlarmMapper.class);
    private final AlarmService service = new AlarmService(mapper, new AlarmAssembler());

    @Test
    void page_delegatesToMapperAndWrapsResult() {
        Page<FacAlarm> page = new Page<>(2, 10);
        page.setTotal(35);
        when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<FacAlarm> r = service.page(2L, 10L, null, null, null);
        assertEquals(35, r.getTotal());
        assertEquals(2, r.getCurrent());
    }

    @Test
    void page_invalidDeviceCode_throws() {
        assertThrows(BusinessException.class, () -> service.page(1L, 20L, null, null, "SHORT"));
    }

    @Test
    void page_nullDeviceCode_passesThrough() {
        Page<FacAlarm> page = new Page<>(1, 20);
        when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        Page<FacAlarm> r = service.page(1L, 20L, null, null, null);
        assertEquals(0, r.getTotal());
    }

    @Test
    void create_generatesAlarmIdAndDefaultsActive() {
        when(mapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(java.util.List.of());
        when(mapper.insert(any(FacAlarm.class))).thenReturn(1);

        EmergencyEventPayload p = new EmergencyEventPayload();
        p.setLevel(2);
        p.setType("FIRE");
        p.setDeviceCode("FAC2026FIREA00000001");
        p.setLocation("罐区A");
        p.setDescription("A装置温度越限");

        AlarmItem item = service.create(p);
        assertEquals("AE-2026-001", item.getAlarmId());
        assertEquals("ACTIVE", item.getStatus());
        assertEquals("A装置温度越限", item.getTitle());
        assertEquals("OTHER", item.getCategory());
    }

    @Test
    void create_invalidDeviceCode_throws() {
        EmergencyEventPayload p = new EmergencyEventPayload();
        p.setLevel(1);
        p.setType("SOS");
        p.setDeviceCode("SHORT");
        p.setLocation("x");
        p.setDescription("y");
        assertThrows(BusinessException.class, () -> service.create(p));
    }

    @Test
    void update_notFound_returnsNull() {
        when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        EmergencyEventPayload p = new EmergencyEventPayload();
        p.setLevel(1);
        p.setType("SOS");
        p.setDeviceCode("FAC2026FIREA00000001");
        p.setLocation("x");
        p.setDescription("y");
        assertNull(service.update("AE-2099-999", p));
    }

    @Test
    void update_found_appliesFields() {
        FacAlarm existing = new FacAlarm();
        existing.setAlarmId("AE-2026-001");
        existing.setStatus(0);
        when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(mapper.updateById(any(FacAlarm.class))).thenReturn(1);

        EmergencyEventPayload p = new EmergencyEventPayload();
        p.setLevel(3);
        p.setType("FIRE");
        p.setStatus("ACKED");
        p.setDeviceCode("FAC2026FIREA00000001");
        p.setLocation("罐区A");
        p.setDescription("已确认");

        AlarmItem item = service.update("AE-2026-001", p);
        assertEquals("AE-2026-001", item.getAlarmId());
        assertEquals("ACKED", item.getStatus());
    }

    @Test
    void delete_logicalDeleteReturnsOk() {
        when(mapper.update(isNull(), any())).thenReturn(1);
        DeleteResult r = service.delete("AE-2026-001");
        assertTrue(r.getOk());
    }

    @Test
    void delete_noRowReturnsFalse() {
        when(mapper.update(isNull(), any())).thenReturn(0);
        DeleteResult r = service.delete("AE-2099-999");
        assertEquals(Boolean.FALSE, r.getOk());
    }
}
