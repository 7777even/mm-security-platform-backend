package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.AuditEvent;
import com.sinopec.mmsecurity.dto.AuditEventBatch;
import com.sinopec.mmsecurity.dto.FieldReportItem;
import com.sinopec.mmsecurity.entity.FacAuditLog;
import com.sinopec.mmsecurity.entity.FacFieldReport;
import com.sinopec.mmsecurity.mapper.AuditLogMapper;
import com.sinopec.mmsecurity.mapper.FacFieldReportMapper;
import com.sinopec.mmsecurity.security.AuthorizationService;
import com.sinopec.mmsecurity.security.LoginUser;
import com.sinopec.mmsecurity.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UplinkService（纯 Mockito）：审计批量尽力落库（每条 insert）；空/非法批次不落库；
 * 现场回传受理不抛错，且服务端用当前登录态覆盖 reporter（防水平越权/身份冒用）。
 */
class UplinkServiceTest {

    private final AuditLogMapper mapper = mock(AuditLogMapper.class);
    private final AuthorizationService authz = mock(AuthorizationService.class);
    private final FacFieldReportMapper facFieldReportMapper = mock(FacFieldReportMapper.class);
    private final UplinkService service = new UplinkService(mapper, authz, facFieldReportMapper);

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void reportAudit_persistsEachEvent() {
        AuditEventBatch batch = new AuditEventBatch();
        AuditEvent e1 = new AuditEvent();
        e1.setAction("route.view");
        AuditEvent e2 = new AuditEvent();
        e2.setAction("alarm.view");
        batch.setEvents(List.of(e1, e2));
        when(mapper.insert(any(FacAuditLog.class))).thenReturn(1);

        service.reportAudit(batch);
        verify(mapper, times(2)).insert(any(FacAuditLog.class));
    }

    @Test
    void reportAudit_nullBatch_doesNotInsert() {
        service.reportAudit(null);
        verify(mapper, never()).insert(any());
    }

    @Test
    void submitFieldReport_doesNotThrow() {
        FieldReportItem item = new FieldReportItem();
        item.setId("r1");
        item.setKind("field-report");
        item.setTitle("A2 区火情处置");
        item.setStatus("done");
        // reporter 不声明（null）→ assertSelfOrAdmin 放行，由服务端绑定当前登录态
        assertDoesNotThrow(() -> service.submitFieldReport(item));
        verify(facFieldReportMapper, times(1)).insert(any(FacFieldReport.class));
    }

    @Test
    void submitFieldReport_overridesReporterWithCurrentUser() {
        UserContext.set(new LoginUser(42L, "zhang.san", "VIEWER"));
        FieldReportItem item = new FieldReportItem();
        item.setId("r2");
        item.setKind("field-report");
        item.setTitle("B3 区泄漏处置");
        item.setStatus("done");
        // 客户端声称是 li.si，服务端必须覆盖为当前登录用户，杜绝身份冒用
        item.setReporter("li.si");
        service.submitFieldReport(item);
        assertEquals("zhang.san", item.getReporter(), "reporter 须被服务端重写为当前登录用户");
        verify(authz, times(1)).assertSelfOrAdmin("li.si");
        verify(facFieldReportMapper, times(1)).insert(any(FacFieldReport.class));
    }
}
