package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.mapper.FacDeviceMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DeviceService：分页委托 Mapper、按 code 查询的校验与缺失处理。
 */
class DeviceServiceTest {

    private final FacDeviceMapper mapper = mock(FacDeviceMapper.class);
    private final DeviceService service = new DeviceService(mapper);

    @Test
    void page_delegatesToMapper() {
        Page<FacDevice> page = new Page<>(1, 5);
        when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<FacDevice> result = service.page(1, 5, null, null, null);

        assertSame(page, result);
        verify(mapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void byCode_invalidLength_throws() {
        BusinessException ex = assertThrows(BusinessException.class, () -> service.byCode("short"));
        assertEquals(ResultCode.DEVICE_CODE_INVALID, ex.getCode());
    }

    @Test
    void byCode_notFound_throws() {
        when(mapper.selectById("FAC2026FIREA00000001")).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.byCode("FAC2026FIREA00000001"));
        assertEquals(ResultCode.DEVICE_NOT_FOUND, ex.getCode());
    }
}
