package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.sinopec.mmsecurity.dto.NotificationItem;
import com.sinopec.mmsecurity.dto.NotificationPageResult;
import com.sinopec.mmsecurity.dto.NotificationSaveRequest;
import com.sinopec.mmsecurity.dto.NotificationTarget;
import com.sinopec.mmsecurity.entity.SysNotification;
import com.sinopec.mmsecurity.mapper.SysNotificationMapper;
import com.sinopec.mmsecurity.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 系统通知服务（消息中心收件箱）。
 *
 * <p>收件范围 = 本人专属（recipient=当前用户）+ 全员广播（recipient 为 null）；
 * read_flag 标记已读，软删除。结构变更由 {@code @RealtimeSync(domain="system.notification")} 广播。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<String> CATEGORIES = List.of("alarm", "event", "task", "system");

    private final SysNotificationMapper mapper;
    private final SystemAuditHelper audit;

    /** 当前用户收件范围：本人专属 + 全员广播。 */
    private LambdaQueryWrapper<SysNotification> inboxScope(String me) {
        return new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getDeleted, 0)
                .and(w -> w.eq(SysNotification::getRecipient, me).or().isNull(SysNotification::getRecipient));
    }

    public NotificationPageResult list(Long page, Long size, String category, Integer read) {
        String me = UserContext.username();
        long p = page == null || page < 1 ? 1 : page;
        long s = size == null || size < 1 ? 20 : size;

        LambdaQueryWrapper<SysNotification> q = inboxScope(me);
        if (category != null && !category.isBlank()) {
            q.eq(SysNotification::getCategory, category);
        }
        if (read != null) {
            q.eq(SysNotification::getReadFlag, read);
        }
        q.orderByDesc(SysNotification::getCreatedAt);

        Page<SysNotification> res = mapper.selectPage(new Page<>(p, s), q);
        List<NotificationItem> items = res.getRecords().stream().map(this::toItem).toList();
        long unread = mapper.selectCount(inboxScope(me).eq(SysNotification::getReadFlag, 0));

        NotificationPageResult r = new NotificationPageResult();
        r.setList(items);
        r.setTotal(res.getTotal());
        r.setPage(p);
        r.setSize(s);
        r.setUnreadCount(unread);
        return r;
    }

    public void markRead(Long id) {
        SysNotification n = requireOwned(id);
        n.setReadFlag(1);
        mapper.updateById(n);
    }

    public void markAllRead() {
        String me = UserContext.username();
        SysNotification patch = new SysNotification();
        patch.setReadFlag(1);
        mapper.update(patch, inboxScope(me).eq(SysNotification::getReadFlag, 0));
    }

    @Transactional
    @RealtimeSync(domain = "system.notification")
    public NotificationItem create(NotificationSaveRequest req) {
        String cat = req.getCategory() == null ? "" : req.getCategory().trim().toLowerCase();
        if (!CATEGORIES.contains(cat)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "category 只能为 alarm/event/task/system");
        }
        SysNotification n = new SysNotification();
        n.setCategory(cat);
        n.setTitle(req.getTitle());
        n.setSummary(req.getSummary());
        n.setTargetType(req.getTargetType());
        n.setTargetId(req.getTargetId());
        n.setRecipient(req.getRecipient() == null ? null : req.getRecipient().trim());
        n.setReadFlag(0);
        n.setDeleted(0);
        mapper.insert(n);
        audit.record("notification.create", Map.of("category", cat));
        return toItem(n);
    }

    @Transactional
    @RealtimeSync(domain = "system.notification")
    public void delete(Long id) {
        SysNotification n = requireOwnedOrAdmin(id);
        n.setDeleted(1);
        mapper.updateById(n);
        audit.record("notification.delete", Map.of("id", String.valueOf(id)));
    }

    // ---------------------------------------------------------------- 内部工具

    private SysNotification requireOwned(Long id) {
        SysNotification n = mapper.selectById(id);
        if (n == null || (n.getDeleted() != null && n.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }
        String me = UserContext.username();
        if (n.getRecipient() != null && !n.getRecipient().equals(me)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作他人通知");
        }
        return n;
    }

    private SysNotification requireOwnedOrAdmin(Long id) {
        SysNotification n = mapper.selectById(id);
        if (n == null || (n.getDeleted() != null && n.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }
        String me = UserContext.username();
        boolean owned = n.getRecipient() == null || n.getRecipient().equals(me);
        if (!owned && !UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该通知");
        }
        return n;
    }

    private NotificationItem toItem(SysNotification n) {
        NotificationItem it = new NotificationItem();
        it.setId(n.getId());
        it.setCategory(n.getCategory());
        it.setTitle(n.getTitle());
        it.setSummary(n.getSummary());
        it.setRead(n.getReadFlag() != null && n.getReadFlag() == 1);
        it.setCreatedAt(n.getCreatedAt() == null ? null : n.getCreatedAt().format(FMT));
        if (n.getTargetType() != null || n.getTargetId() != null) {
            NotificationTarget t = new NotificationTarget();
            t.setType(n.getTargetType());
            t.setId(n.getTargetId());
            it.setTarget(t);
        }
        return it;
    }
}
