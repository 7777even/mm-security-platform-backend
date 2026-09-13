package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.entity.FacAuditLog;
import com.sinopec.mmsecurity.mapper.AuditLogMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * SystemAuditHelper 纯单元测试（17.6% → 全覆盖分支）。
 * 验证：module 路由 / 默认 system / blank module 兜底 / 空 detail 落 null / 落库失败不冒泡。
 */
@ExtendWith(MockitoExtension.class)
class SystemAuditHelperTest {

    @Mock
    AuditLogMapper auditLogMapper;
    @InjectMocks
    SystemAuditHelper helper;

    @Test
    void record_withModule_persistsMappedFieldsAndJsonDetail() {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("username", "alice");
        detail.put("orderNo", "ORD-1");
        helper.record("emergency", "emergency.command.create", detail);

        ArgumentCaptor<FacAuditLog> cap = ArgumentCaptor.forClass(FacAuditLog.class);
        verify(auditLogMapper).insert(cap.capture());
        FacAuditLog e = cap.getValue();
        assertEquals("emergency", e.getModule());
        assertEquals("emergency.command.create", e.getAction());
        assertNotNull(e.getDetailJson());
        assertTrue(e.getDetailJson().contains("\"username\"") && e.getDetailJson().contains("alice"));
        assertTrue(e.getEventAt() > 0, "eventAt 应已填当前时间戳");
    }

    @Test
    void record_twoArgOverload_defaultsModuleToSystem() {
        helper.record("system.user.create", Map.of("username", "alice"));
        ArgumentCaptor<FacAuditLog> cap = ArgumentCaptor.forClass(FacAuditLog.class);
        verify(auditLogMapper).insert(cap.capture());
        assertEquals("system", cap.getValue().getModule());
    }

    @Test
    void record_blankModule_fallsBackToSystem() {
        helper.record("  ", "x.y", Map.of("k", "v"));
        ArgumentCaptor<FacAuditLog> cap = ArgumentCaptor.forClass(FacAuditLog.class);
        verify(auditLogMapper).insert(cap.capture());
        assertEquals("system", cap.getValue().getModule());
    }

    @Test
    void record_emptyDetail_storesNullJson() {
        helper.record("system", "a.b", Map.of());
        ArgumentCaptor<FacAuditLog> cap = ArgumentCaptor.forClass(FacAuditLog.class);
        verify(auditLogMapper).insert(cap.capture());
        assertNull(cap.getValue().getDetailJson());
    }

    @Test
    void record_chineseDetail_serializesToJson() {
        helper.record("system", "a.b", Map.of("真实姓名", "张三"));
        ArgumentCaptor<FacAuditLog> cap = ArgumentCaptor.forClass(FacAuditLog.class);
        verify(auditLogMapper).insert(cap.capture());
        assertTrue(cap.getValue().getDetailJson().contains("真实姓名"));
    }

    @Test
    void record_insertFailure_doesNotPropagate() {
        doThrow(new RuntimeException("db down")).when(auditLogMapper).insert(any());
        // 审计是旁路：落库失败仅 warn，绝不阻断主流程
        helper.record("system", "a.b", Map.of("k", "v"));
        verify(auditLogMapper).insert(any());
    }
}
