package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.SystemRoleItem;
import com.sinopec.mmsecurity.dto.SystemRoleMenuAssign;
import com.sinopec.mmsecurity.dto.SystemRoleSaveRequest;
import com.sinopec.mmsecurity.entity.SysRole;
import com.sinopec.mmsecurity.entity.SysRoleMenu;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysRoleMapper;
import com.sinopec.mmsecurity.mapper.SysRoleMenuMapper;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.RoleAuthorityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 角色管理服务（RBAC 授权主体 + 角色-菜单授权）。
 *
 * <p>硬防护：内置角色（{@code built_in=1}）禁删除、禁改 role_code、禁停用；
 * 仍被用户引用的角色禁删除（返回 409，避免产生「无角色用户」）。</p>
 *
 * <p>授权变更（新增/修改/删除角色、调整授权）后调用 {@link RoleAuthorityService#reloadRolePerms()}
 * 使权限解析立即生效。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserMapper userMapper;
    private final RoleAuthorityService roleAuthorityService;
    private final SystemAuditHelper audit;

    /** 全部角色（按 sort_order、id 排序），供列表与用户表单下拉共用。 */
    public List<SystemRoleItem> list(String keyword) {
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim();
            qw.and(w -> w.like(SysRole::getRoleCode, k).or().like(SysRole::getRoleName, k));
        }
        qw.orderByAsc(SysRole::getSortOrder).orderByAsc(SysRole::getId);
        Map<String, Long> counts = userCountByRole();
        return roleMapper.selectList(qw).stream().map(r -> toItem(r, counts)).toList();
    }

    public SystemRoleItem get(Long id) {
        return toItem(require(id), userCountByRole());
    }

    @Transactional
    public SystemRoleItem create(SystemRoleSaveRequest req) {
        String code = normalizeCode(req.getRoleCode());
        // 含逻辑删除行判重（唯一索引 + 逻辑删除的语义鸿沟，见 SysUserMapper#countUsernameIncludingDeleted）
        if (roleMapper.countRoleCodeIncludingDeleted(code, -1L) > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "角色标识已存在（不可复用，含历史已删除角色）：" + code);
        }
        SysRole r = new SysRole();
        r.setRoleCode(code);
        r.setRoleName(req.getRoleName());
        r.setDescription(req.getDescription());
        r.setDataScope(normalizeScope(req.getDataScope()));
        r.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        r.setBuiltIn(0);
        r.setSortOrder(req.getSortOrder() == null ? 100 : req.getSortOrder());
        r.setDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        r.setCreatedAt(now);
        r.setUpdatedAt(now);
        roleMapper.insert(r);

        roleAuthorityService.reloadRolePerms();
        audit.record("system.role.create", Map.of("roleCode", code));
        return toItem(r, userCountByRole());
    }

    @Transactional
    public SystemRoleItem update(Long id, SystemRoleSaveRequest req) {
        SysRole r = require(id);
        String code = normalizeCode(req.getRoleCode());
        boolean builtIn = isBuiltIn(r);

        if (builtIn && !code.equalsIgnoreCase(r.getRoleCode())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "内置角色的标识不可修改");
        }
        if (!code.equalsIgnoreCase(r.getRoleCode())) {
            if (roleMapper.countRoleCodeIncludingDeleted(code, id) > 0) {
                throw new BusinessException(ResultCode.CONFLICT, "角色标识已存在（不可复用，含历史已删除角色）：" + code);
            }
        }
        if (builtIn && req.getStatus() != null && req.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "内置角色不可停用");
        }

        r.setRoleCode(code);
        r.setRoleName(req.getRoleName());
        r.setDescription(req.getDescription());
        r.setDataScope(normalizeScope(req.getDataScope()));
        if (req.getStatus() != null) {
            r.setStatus(req.getStatus());
        }
        if (req.getSortOrder() != null) {
            r.setSortOrder(req.getSortOrder());
        }
        r.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(r);

        roleAuthorityService.reloadRolePerms();
        audit.record("system.role.update", Map.of("roleCode", r.getRoleCode()));
        return toItem(r, userCountByRole());
    }

    @Transactional
    public SystemRoleItem updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "status 只能为 0（停用）或 1（启用）");
        }
        SysRole r = require(id);
        if (isBuiltIn(r)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "内置角色不可停用");
        }
        r.setStatus(status);
        r.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(r);

        roleAuthorityService.reloadRolePerms();
        audit.record("system.role.status", Map.of("roleCode", r.getRoleCode(), "status", status));
        return toItem(r, userCountByRole());
    }

    @Transactional
    public DeleteResult delete(Long id) {
        SysRole r = require(id);
        if (isBuiltIn(r)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "内置角色不可删除");
        }
        Long users = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, r.getRoleCode()));
        if (users != null && users > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "该角色下仍有 " + users + " 个用户，不可删除");
        }
        // 先清授权（关联表硬删），再逻辑删除角色
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        roleMapper.deleteById(id);

        roleAuthorityService.reloadRolePerms();
        audit.record("system.role.delete", Map.of("roleCode", r.getRoleCode()));

        DeleteResult d = new DeleteResult();
        d.setOk(true);
        return d;
    }

    /** 该角色已授权的菜单/权限节点 id 集合。 */
    public List<Long> menus(Long id) {
        require(id);
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, id)).stream()
                .map(SysRoleMenu::getMenuId)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * 整体覆盖角色授权（空数组 = 回收全部授权）。
     *
     * <p>MIN 保护：不允许把 ADMIN 角色的授权清空，否则会立即造成「管理员无任何权限」的自锁
     * （即使账号仍启用）。</p>
     */
    @Transactional
    public List<Long> assignMenus(Long id, SystemRoleMenuAssign req) {
        SysRole r = require(id);
        Set<Long> menuIds = new LinkedHashSet<>(req.getMenuIds() == null ? List.of() : req.getMenuIds());
        menuIds.remove(null);

        if ("ADMIN".equalsIgnoreCase(r.getRoleCode()) && menuIds.isEmpty()) {
            throw new BusinessException(ResultCode.CONFLICT, "ADMIN 角色不可清空授权（会立即造成管理员无权限）");
        }

        // 整表替换：先硬删旧授权，再插入新授权
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        LocalDateTime now = LocalDateTime.now();
        for (Long menuId : menuIds) {
            SysRoleMenu link = new SysRoleMenu();
            link.setRoleId(id);
            link.setMenuId(menuId);
            link.setCreatedAt(now);
            roleMenuMapper.insert(link);
        }

        roleAuthorityService.reloadRolePerms();
        audit.record("system.role.grant", Map.of("roleCode", r.getRoleCode(), "menuCount", menuIds.size()));
        return List.copyOf(menuIds);
    }

    // ---------------------------------------------------------------- 内部工具

    private SysRole require(Long id) {
        SysRole r = roleMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        return r;
    }

    private boolean isBuiltIn(SysRole r) {
        return r.getBuiltIn() != null && r.getBuiltIn() == 1;
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeScope(String scope) {
        if (scope == null || scope.isBlank()) {
            return "SELF";
        }
        String s = scope.trim().toUpperCase(Locale.ROOT);
        return switch (s) {
            case "ALL", "DEPT", "SELF" -> s;
            default -> throw new BusinessException(ResultCode.PARAM_INVALID, "dataScope 只能为 ALL/DEPT/SELF");
        };
    }

    /** 各角色下的用户数（用于列表展示与删除保护提示）。 */
    private Map<String, Long> userCountByRole() {
        Map<String, Long> counts = new HashMap<>();
        for (SysUser u : userMapper.selectList(null)) {
            if (u.getRole() == null) {
                continue;
            }
            counts.merge(u.getRole().toUpperCase(Locale.ROOT), 1L, Long::sum);
        }
        return counts;
    }

    private SystemRoleItem toItem(SysRole r, Map<String, Long> counts) {
        SystemRoleItem item = new SystemRoleItem();
        item.setId(r.getId());
        item.setRoleCode(r.getRoleCode());
        item.setRoleName(r.getRoleName());
        item.setDescription(r.getDescription());
        item.setDataScope(r.getDataScope());
        item.setStatus(r.getStatus());
        item.setBuiltIn(isBuiltIn(r));
        item.setSortOrder(r.getSortOrder());
        item.setUserCount(r.getRoleCode() == null ? 0L
                : counts.getOrDefault(r.getRoleCode().toUpperCase(Locale.ROOT), 0L));
        item.setCreatedAt(r.getCreatedAt());
        return item;
    }
}
