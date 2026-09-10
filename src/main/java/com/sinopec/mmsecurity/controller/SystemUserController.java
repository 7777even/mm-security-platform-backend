package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.PasswordResetResult;
import com.sinopec.mmsecurity.dto.SystemUserCreate;
import com.sinopec.mmsecurity.dto.SystemUserItem;
import com.sinopec.mmsecurity.dto.SystemUserPageResult;
import com.sinopec.mmsecurity.dto.SystemUserUpdate;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.SystemUserService;
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
 * 系统用户管理接口。
 *
 * <p>类级 {@code @RequireAuth(role = "ADMIN")} 作为兜底（防止方法级漏标 perm 时降级开放）；
 * 方法级 {@code @RequireAuth(perm = "system:user:*")} 为 ADR-5 第二步细粒度判定。
 * 拦截器语义为「方法级整体覆盖类级」，故方法级 perm 生效、类级 role 仅作兜底。</p>
 *
 * <p>自锁与提权防护、最后管理员保护均由 {@link SystemUserService} 服务端强制
 * （不依赖前端禁用按钮）。所有写操作落服务端审计。</p>
 */
@RestController
@RequestMapping("/api/v1/system/users")
@RequireAuth(role = "ADMIN")
@RequiredArgsConstructor
public class SystemUserController {

    private final SystemUserService systemUserService;

    /** 用户分页查询（keyword 模糊匹配用户名/姓名）。 */
    @RequireAuth(perm = "system:user:view")
    @GetMapping
    public Result<SystemUserPageResult> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String roleCode) {
        return Result.ok(systemUserService.page(page, size, keyword, status, roleCode));
    }

    /** 用户详情。 */
    @RequireAuth(perm = "system:user:view")
    @GetMapping("/{id}")
    public Result<SystemUserItem> get(@PathVariable Long id) {
        return Result.ok(systemUserService.get(id));
    }

    /** 新增用户（用户名唯一、角色须存在且启用、初始口令须过策略）。 */
    @RequireAuth(perm = "system:user:create")
    @PostMapping
    public Result<SystemUserItem> create(@Valid @RequestBody SystemUserCreate payload) {
        return Result.ok(systemUserService.create(payload));
    }

    /** 修改用户（姓名 / 角色 / 状态）。 */
    @RequireAuth(perm = "system:user:edit")
    @PutMapping("/{id}")
    public Result<SystemUserItem> update(@PathVariable Long id, @Valid @RequestBody SystemUserUpdate payload) {
        return Result.ok(systemUserService.update(id, payload));
    }

    /** 删除用户（逻辑删除；禁删自己与最后一个启用管理员）。 */
    @RequireAuth(perm = "system:user:delete")
    @DeleteMapping("/{id}")
    public Result<DeleteResult> delete(@PathVariable Long id) {
        return Result.ok(systemUserService.delete(id));
    }

    /** 启用 / 停用（status=0 停用、1 启用）。 */
    @RequireAuth(perm = "system:user:edit")
    @PutMapping("/{id}/status")
    public Result<SystemUserItem> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.ok(systemUserService.updateStatus(id, status));
    }

    /** 分配角色（请求体只需 roleCode）。 */
    @RequireAuth(perm = "system:user:assign-role")
    @PutMapping("/{id}/role")
    public Result<SystemUserItem> assignRole(@PathVariable Long id, @Valid @RequestBody SystemUserUpdate payload) {
        return Result.ok(systemUserService.assignRole(id, payload.getRoleCode()));
    }

    /** 重置口令：返回一次性临时口令，用户下次登录须强制改密。 */
    @RequireAuth(perm = "system:user:reset-pwd")
    @PostMapping("/{id}/password/reset")
    public Result<PasswordResetResult> resetPassword(@PathVariable Long id) {
        return Result.ok(systemUserService.resetPassword(id));
    }
}
