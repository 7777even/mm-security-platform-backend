package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.NotificationItem;
import com.sinopec.mmsecurity.dto.NotificationPageResult;
import com.sinopec.mmsecurity.dto.NotificationSaveRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统通知（消息中心）接口。
 *
 * <p>除创建（ADMIN 广播/定向）外，其余端点登录即可调用，按当前登录态收件范围过滤。</p>
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequireAuth
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** 通知列表（分页 + 分类/已读过滤）+ 未读数。 */
    @GetMapping
    public Result<NotificationPageResult> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer read) {
        return Result.ok(notificationService.list(page, size, category, read));
    }

    /** 标记单条已读。 */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.ok();
    }

    /** 全部标记已读。 */
    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead();
        return Result.ok();
    }

    /** 新增系统通知（ADMIN 广播或定向推送）。 */
    @PostMapping
    @RequireAuth(role = "ADMIN")
    public Result<NotificationItem> create(@Valid @RequestBody NotificationSaveRequest req) {
        return Result.ok(notificationService.create(req));
    }

    /** 删除通知（本人或 ADMIN）。 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return Result.ok();
    }
}
