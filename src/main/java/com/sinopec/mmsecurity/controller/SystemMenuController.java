package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.PermissionCodeItem;
import com.sinopec.mmsecurity.dto.SystemMenuNode;
import com.sinopec.mmsecurity.dto.SystemMenuSaveRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.SystemMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单 / 权限节点管理接口（仅 ADMIN）。
 *
 * <p>权限码即菜单树中 perm_code 非空的节点（ADR-2）；{@code GET /system/permissions}
 * 提供权限码字典，供角色授权界面与前后端权限码核对。</p>
 */
@RestController
@RequestMapping("/api/v1/system")
@RequireAuth(role = "ADMIN")
@RequiredArgsConstructor
public class SystemMenuController {

    private final SystemMenuService systemMenuService;

    /** 菜单权限树（含 DIR/MENU/BUTTON 与停用节点，供管理界面维护）。 */
    @GetMapping("/menus")
    public Result<List<SystemMenuNode>> tree() {
        return Result.ok(systemMenuService.tree());
    }

    /** 新增菜单/权限节点。 */
    @PostMapping("/menus")
    public Result<SystemMenuNode> create(@Valid @RequestBody SystemMenuSaveRequest payload) {
        return Result.ok(systemMenuService.create(payload));
    }

    /** 修改菜单/权限节点。 */
    @PutMapping("/menus/{id}")
    public Result<SystemMenuNode> update(@PathVariable Long id, @Valid @RequestBody SystemMenuSaveRequest payload) {
        return Result.ok(systemMenuService.update(id, payload));
    }

    /** 删除节点（有子节点或已被角色授权时拒绝）。 */
    @DeleteMapping("/menus/{id}")
    public Result<DeleteResult> delete(@PathVariable Long id) {
        return Result.ok(systemMenuService.delete(id));
    }

    /** 权限码字典（聚合所有 perm_code 非空节点）。 */
    @GetMapping("/permissions")
    public Result<List<PermissionCodeItem>> permissions() {
        return Result.ok(systemMenuService.permissions());
    }
}
