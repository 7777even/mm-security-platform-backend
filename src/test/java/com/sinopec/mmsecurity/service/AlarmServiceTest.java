package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AlarmService（纯 Mockito，不启动 Spring 上下文）：
 * 分页委派、空库返回空页、deviceCode 非 20 位抛 BusinessException。
 */
class AlarmServiceTest {

    private final AlarmMapper mapper = mock(AlarmMapper.class);
    private final AlarmService service = new AlarmService(mapper);

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
}
