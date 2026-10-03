package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.DeviceWriteRequest;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.mapper.FacDeviceMapper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DeviceService：分页委托 Mapper、按 code 查询的校验与缺失处理。
 */
class DeviceServiceTest {

    private final FacDeviceMapper mapper = mock(FacDeviceMapper.class);
    private final DeviceService service = new DeviceService(mapper, mock(DataScopeResolver.class));

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

    // ---- 写端点（device）----
    // 三条主线：① 物理主键 deviceCode 由客户端给定、不可走 LedgerIdSupport，重复返回 409；
    // ② 删除必须是软删（置 deleted=1）——读端点 page() 恒带 deleted=0 过滤，物理删除会造成读写口径不一致；
    // ③ findActiveByCode 要求 20 位编码，非法/已软删一律视为未命中。

    private static final String CODE = "FAC2026FIREA00000001";

    private static DeviceWriteRequest writeRequest(String code) {
        DeviceWriteRequest in = new DeviceWriteRequest();
        in.setDeviceCode(code);
        in.setDeviceName("1#消防泵");
        in.setDeviceType("消防泵");
        in.setZone("A装置区");
        in.setStatus(1);
        in.setLat(21.671);
        in.setLon(110.881);
        return in;
    }

    private static FacDevice deviceEntity(Integer deleted) {
        FacDevice device = new FacDevice();
        device.setDeviceCode(CODE);
        device.setDeviceName("1#消防泵");
        device.setDeviceType("消防泵");
        device.setZone("A装置区");
        device.setStatus(1);
        device.setDeleted(deleted);
        return device;
    }

    @Test
    void createDevice_duplicateCode_throwsConflict() {
        when(mapper.selectById(CODE)).thenReturn(deviceEntity(0));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createDevice(writeRequest(CODE)));

        assertEquals(ResultCode.CONFLICT, ex.getCode());
        verify(mapper, never()).insert(any(FacDevice.class));
    }

    @Test
    void createDevice_initializesDeletedZeroAndVersionZero() {
        when(mapper.selectById(CODE)).thenReturn(null);

        service.createDevice(writeRequest(CODE));

        ArgumentCaptor<FacDevice> captor = ArgumentCaptor.forClass(FacDevice.class);
        verify(mapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getDeleted());
        assertEquals(0L, captor.getValue().getVersion());
        assertEquals("A装置区", captor.getValue().getZone());
    }

    @Test
    void updateDevice_appliesFieldsWhenActive() {
        FacDevice existing = deviceEntity(0);
        when(mapper.selectById(CODE)).thenReturn(existing);
        DeviceWriteRequest in = writeRequest(CODE);
        in.setDeviceName("改名后消防泵");
        in.setZone("B装置区");

        FacDevice updated = service.updateDevice(CODE, in);

        assertEquals("改名后消防泵", updated.getDeviceName());
        assertEquals("B装置区", updated.getZone());
        verify(mapper).updateById(existing);
    }

    @Test
    void updateDevice_alreadySoftDeleted_returnsNull() {
        when(mapper.selectById(CODE)).thenReturn(deviceEntity(1));

        assertNull(service.updateDevice(CODE, writeRequest(CODE)));
    }

    @Test
    void updateDevice_invalidCodeLength_returnsNull() {
        assertNull(service.updateDevice("short", writeRequest("short")));
    }

    @Test
    void deleteDevice_marksDeletedInsteadOfPhysicalDelete() {
        // 读端点 page() 恒带 deleted=0，物理删除会让「写口径」与「读口径」错位。
        FacDevice existing = deviceEntity(0);
        when(mapper.selectById(CODE)).thenReturn(existing);
        when(mapper.updateById(existing)).thenReturn(1);

        DeleteResult result = service.deleteDevice(CODE);

        assertTrue(result.getOk());
        assertEquals(1, existing.getDeleted());
        verify(mapper).updateById(existing);
        verify(mapper, never()).deleteById(anyString());
    }

    @Test
    void deleteDevice_absent_returnsOkFalse() {
        when(mapper.selectById(CODE)).thenReturn(null);

        assertFalse(service.deleteDevice(CODE).getOk());
    }
}
