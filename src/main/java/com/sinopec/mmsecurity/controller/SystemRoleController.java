package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.SystemRoleItem;
import com.sinopec.mmsecurity.dto.SystemRoleMenuAssign;
import com.sinopec.mmsecurity.dto.SystemRoleSaveRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.SystemRoleService;
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

import java.util.List;

/**
 * 角色管理与角色-菜单授权接口。
 *
 * <p>类级 {@code @RequireAuth(role = "ADMIN")} 兜底；方法级 {@code @RequireAuth(perm = "system:role:*")}
 * 为 ADR-5 第二步细粒度判定。授权变更即时生效（服务端缓存写时失效）。</p>
 */
@RestController
@RequestMapping("/api/v1/system/roles")
@RequireAuth(role = "ADMIN")
@RequiredArgsConstructor
public class SystemRoleController {

    private final SystemRoleService systemRoleService;

    /** 角色列表（含各角色用户数）。 */
    @RequireAuth(perm = "system:role:view")
    @GetMapping
    public Result<List<SystemRoleItem>> list(@RequestParam(required = false) String keyword) {
        return Result.ok(systemRoleService.list(keyword));
    }

    /** 角色详情。 */
    @RequireAuth(perm = "system:role:view")
    @GetMapping("/{id}")
    public Result<SystemRoleItem> get(@PathVariable Long id) {
        return Result.ok(systemRoleService.get(id));
    }

    /** 新增角色。 */
    @RequireAuth(perm = "system:role:create")
    @PostMapping
    public Result<SystemRoleItem> create(@Valid @RequestBody SystemRoleSaveRequest payload) {
        return Result.ok(systemRoleService.create(payload));
    }

    /** 修改角色（内置角色禁改标识、禁停用）。 */
    @RequireAuth(perm = "system:role:edit")
    @PutMapping("/{id}")
    public Result<SystemRoleItem> update(@PathVariable Long id, @Valid @RequestBody SystemRoleSaveRequest payload) {
        return Result.ok(systemRoleService.update(id, payload));
    }

    /** 删除角色（内置禁删、被用户引用禁删）。 */
    @RequireAuth(perm = "system:role:delete")
    @DeleteMapping("/{id}")
    public Result<DeleteResult> delete(@PathVariable Long id) {
        return Result.ok(systemRoleService.delete(id));
    }

    /** 启用 / 停用角色。 */
    @RequireAuth(perm = "system:role:edit")
    @PutMapping("/{id}/status")
    public Result<SystemRoleItem> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.ok(systemRoleService.updateStatus(id, status));
    }

    /** 该角色已授权的菜单/权限节点 id 集合。 */
    @RequireAuth(perm = "system:role:view")
    @GetMapping("/{id}/menus")
    public Result<List<Long>> menus(@PathVariable Long id) {
        return Result.ok(systemRoleService.menus(id));
    }

    /** 整体覆盖角色授权（空数组表示回收全部；ADMIN 不可清空）。 */
    @RequireAuth(perm = "system:role:grant")
    @PutMapping("/{id}/menus")
    public Result<List<Long>> assignMenus(@PathVariable Long id, @RequestBody SystemRoleMenuAssign payload) {
        return Result.ok(systemRoleService.assignMenus(id, payload));
    }
}
