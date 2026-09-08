package com.sinopec.mmsecurity.common.mask;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 脱敏策略纯单测（无 Spring 上下文）。
 */
class MaskUtilTest {

    @Test
    void maskName_keepsFirstChar() {
        assertEquals("张*", MaskUtil.maskName("张三"));
        assertEquals("欧*", MaskUtil.maskName("欧阳娜娜"));
        assertEquals("李", MaskUtil.maskName("李"));
        assertEquals(null, MaskUtil.maskName(null));
    }

    @Test
    void maskPhone_keepsHead3Tail4() {
        assertEquals("138****5678", MaskUtil.maskPhone("13812345678"));
        // 长度不足 7 原样返回
        assertEquals("123", MaskUtil.maskPhone("123"));
    }

    @Test
    void maskIdCard_keepsHead3Tail4() {
        assertEquals("110***********1234", MaskUtil.maskIdCard("110101199001011234"));
    }

    @Test
    void maskDeviceCode_keepsPrefix() {
        assertEquals("MDM-****", MaskUtil.maskDeviceCode("MDM-2024010100001234"));
        assertEquals("ABC-****", MaskUtil.maskDeviceCode("ABC-123"));
    }

    @Test
    void mask_byType_dispatches() {
        assertEquals("张*", MaskUtil.mask(MaskType.NAME, "张三"));
        assertEquals("138****5678", MaskUtil.mask(MaskType.PHONE, "13812345678"));
        // NONE / null 原样
        assertEquals("张三", MaskUtil.mask(MaskType.NONE, "张三"));
        assertEquals("张三", MaskUtil.mask(null, "张三"));
        assertNull(MaskUtil.mask(MaskType.NAME, null));
    }

    @Test
    void maskForLog_mapsSensitiveKeys() {
        assertEquals("张*", MaskUtil.maskForLog("username", "张三"));
        assertEquals("138****5678", MaskUtil.maskForLog("phone", "13812345678"));
        // 未知 key 原样
        assertEquals("xyz", MaskUtil.maskForLog("traceId", "xyz"));
    }
}
