package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.NotificationItem;
import com.sinopec.mmsecurity.dto.NotificationPageResult;
import com.sinopec.mmsecurity.dto.NotificationSaveRequest;
import com.sinopec.mmsecurity.entity.SysNotification;
import com.sinopec.mmsecurity.mapper.SysNotificationMapper;
import com.sinopec.mmsecurity.security.LoginUser;
import com.sinopec.mmsecurity.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SysNotificationMapper mapper;
    @Mock
    private SystemAuditHelper audit;

    @InjectMocks
    private NotificationService service;

    @BeforeEach
    void setUp() {
        UserContext.set(new LoginUser(1L, "me", "ADMIN"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private static SysNotification n(Long id, String cat, String recipient, int read) {
        SysNotification x = new SysNotification();
        x.setId(id);
        x.setCategory(cat);
        x.setRecipient(recipient);
        x.setReadFlag(read);
        x.setDeleted(0);
        x.setTitle("t");
        x.setCreatedAt(LocalDateTime.now());
        return x;
    }

    private static SysNotification n(Long id, String cat, String recipient, int read, int deleted) {
        SysNotification x = n(id, cat, recipient, read);
        x.setDeleted(deleted);
        return x;
    }

    private static NotificationSaveRequest req(String cat) {
        NotificationSaveRequest r = new NotificationSaveRequest();
        r.setCategory(cat);
        r.setTitle("标题");
        r.setSummary("摘要");
        r.setTargetType("alarm");
        r.setTargetId("a1");
        return r;
    }

    @Test
    void list_returnsInboxItemsAndUnreadCount() {
        when(mapper.selectPage(any(), any())).thenAnswer(inv -> {
            Page<SysNotification> p = inv.getArgument(0);
            p.setRecords(List.of(n(1L, "system", "me", 0), n(2L, "system", null, 1)));
            p.setTotal(2);
            return p;
        });
        when(mapper.selectCount(any())).thenReturn(1L);

        NotificationPageResult r = service.list(1L, 20L, null, null);
        assertEquals(2, r.getList().size());
        assertEquals(2, r.getTotal());
        assertEquals(1, r.getUnreadCount());
    }

    @Test
    void markRead_setsReadFlagAndUpdates() {
        when(mapper.selectById(1L)).thenReturn(n(1L, "system", "me", 0));
        when(mapper.updateById(any())).thenReturn(1);

        service.markRead(1L);
        verify(mapper).updateById(argThat(n -> n.getReadFlag() == 1));
    }

    @Test
    void markRead_othersNotification_throwsForbidden() {
        when(mapper.selectById(1L)).thenReturn(n(1L, "system", "other", 0));
        BusinessException ex = assertThrows(BusinessException.class, () -> service.markRead(1L));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void markAllRead_updatesUnread() {
        when(mapper.update(any(), any())).thenReturn(2);
        service.markAllRead();
        verify(mapper).update(any(), any());
    }

    @Test
    void create_success_lowercasesCategoryAndAudits() {
        when(mapper.insert(any())).thenAnswer(inv -> {
            SysNotification arg = inv.getArgument(0);
            arg.setId(10L);
            return 1;
        });
        NotificationItem item = service.create(req("SYSTEM"));
        assertEquals("system", item.getCategory());
        assertNotNull(item.getTarget());
        verify(audit).record(eq("notification.create"), any());
    }

    @Test
    void create_invalidCategory_throwsParamInvalid() {
        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req("BAD")));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(mapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(99L));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
    }

    @Test
    void delete_othersNotification_asNonAdmin_throwsForbidden() {
        UserContext.set(new LoginUser(2L, "you", "USER"));
        when(mapper.selectById(1L)).thenReturn(n(1L, "system", "me", 0));
        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }
}
