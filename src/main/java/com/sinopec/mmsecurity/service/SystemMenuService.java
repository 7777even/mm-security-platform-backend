package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.cache.IdNameCacheService;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.PermissionCodeItem;
import com.sinopec.mmsecurity.dto.SystemMenuNode;
import com.sinopec.mmsecurity.dto.SystemMenuSaveRequest;
import com.sinopec.mmsecurity.entity.SysMenu;
import com.sinopec.mmsecurity.entity.SysRoleMenu;
import com.sinopec.mmsecurity.mapper.SysMenuMapper;
import com.sinopec.mmsecurity.mapper.SysRoleMenuMapper;
import com.sinopec.mmsecurity.security.RoleAuthorityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 菜单 / 权限节点管理服务（统一授权轴，ADR-2）。
 *
 * <p>节点类型 {@code DIR/MENU/BUTTON}；BUTTON 不参与导航装配，仅贡献权限码。
 * 删除采用<b>拒绝式</b>保护（Q5 裁决）：存在子节点或已被任一角色授权时拒绝删除，
 * 要求先解绑 / 先删子节点，避免静默产生「悬空授权」与孤儿权限码。</p>
 *
 * <p>任何结构变更后须同时失效两层缓存：菜单列表缓存（{@code reloadMenus}）与
 * 角色权限解析缓存（{@code reloadRolePerms}）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMenuService {

    private static final Set<String> MENU_TYPES = Set.of("DIR", "MENU", "BUTTON");

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final IdNameCacheService idNameCache;
    private final RoleAuthorityService roleAuthorityService;
    private final SystemAuditHelper audit;

    /** 全量菜单权限树（含停用节点，供管理界面维护）。 */
    public List<SystemMenuNode> tree() {
        List<SysMenu> all = menuMapper.selectList(null);
        all.sort(Comparator.comparingInt(m -> m.getSort() == null ? 0 : m.getSort()));

        Map<Long, SystemMenuNode> byId = new LinkedHashMap<>();
        for (SysMenu m : all) {
            byId.put(m.getId(), toNode(m));
        }
        List<SystemMenuNode> roots = new ArrayList<>();
        for (SysMenu m : all) {
            SystemMenuNode node = byId.get(m.getId());
            Long parentId = m.getParentId();
            if (parentId == null || parentId == 0 || !byId.containsKey(parentId)) {
                roots.add(node);
            } else {
                SystemMenuNode parent = byId.get(parentId);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    /** 权限码字典：聚合所有非空 perm_code 节点（去重，按权限码排序）。 */
    public List<PermissionCodeItem> permissions() {
        List<PermissionCodeItem> items = new ArrayList<>();
        for (SysMenu m : menuMapper.selectList(null)) {
            if (m.getPermCode() == null || m.getPermCode().isBlank()) {
                continue;
            }
            PermissionCodeItem item = new PermissionCodeItem();
            item.setPermCode(m.getPermCode().trim());
            item.setName(m.getName());
            item.setMenuId(m.getId());
            items.add(item);
        }
        items.sort(Comparator.comparing(PermissionCodeItem::getPermCode));
        return items;
    }

    @Transactional
    public SystemMenuNode create(SystemMenuSaveRequest req) {
        String code = req.getCode().trim();
        Long dup = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getMenuKey, code));
        if (dup != null && dup > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "节点编码已存在：" + code);
        }
        SysMenu m = new SysMenu();
        applyRequest(m, req);
        m.setMenuKey(code);
        m.setDeleted(0);
        menuMapper.insert(m);

        refreshCaches();
        audit.record("system.menu.create", Map.of("code", code, "menuType", m.getMenuType()));
        return toNode(m);
    }

    @Transactional
    public SystemMenuNode update(Long id, SystemMenuSaveRequest req) {
        SysMenu m = require(id);
        String code = req.getCode().trim();
        if (!code.equalsIgnoreCase(m.getMenuKey())) {
            Long dup = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getMenuKey, code).ne(SysMenu::getId, id));
            if (dup != null && dup > 0) {
                throw new BusinessException(ResultCode.CONFLICT, "节点编码已存在：" + code);
            }
        }
        if (id.equals(req.getParentId())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "父节点不能是自己");
        }
        applyRequest(m, req);
        m.setMenuKey(code);
        menuMapper.updateById(m);

        refreshCaches();
        audit.record("system.menu.update", Map.of("code", code));
        return toNode(m);
    }

    /** 删除节点：有子节点或已被角色授权时拒绝。 */
    @Transactional
    public DeleteResult delete(Long id) {
        SysMenu m = require(id);
        Long children = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (children != null && children > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "该节点下仍有 " + children + " 个子节点，请先删除子节点");
        }
        Long granted = roleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, id));
        if (granted != null && granted > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "该节点已被 " + granted + " 个角色授权，请先解除授权");
        }
        menuMapper.deleteById(id);

        refreshCaches();
        audit.record("system.menu.delete", Map.of("code", String.valueOf(m.getMenuKey())));

        DeleteResult d = new DeleteResult();
        d.setOk(true);
        return d;
    }

    // ---------------------------------------------------------------- 内部工具

    private void applyRequest(SysMenu m, SystemMenuSaveRequest req) {
        String type = req.getMenuType() == null ? "" : req.getMenuType().trim().toUpperCase(Locale.ROOT);
        if (!MENU_TYPES.contains(type)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "menuType 只能为 DIR/MENU/BUTTON");
        }
        m.setParentId(req.getParentId() == null ? 0L : req.getParentId());
        m.setName(req.getName());
        m.setPath(req.getPath());
        m.setIcon(req.getIcon());
        m.setSort(req.getSortOrder() == null ? 0 : req.getSortOrder());
        m.setMenuType(type);
        m.setPermCode(req.getPermCode());
        m.setVisible(req.getVisible() == null ? 1 : req.getVisible());
        m.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    }

    private SysMenu require(Long id) {
        SysMenu m = menuMapper.selectById(id);
        if (m == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "菜单节点不存在");
        }
        return m;
    }

    /** 菜单结构变更后，菜单列表缓存与角色权限解析缓存均须失效。 */
    private void refreshCaches() {
        idNameCache.reloadMenus();
        roleAuthorityService.reloadRolePerms();
    }

    private SystemMenuNode toNode(SysMenu m) {
        SystemMenuNode n = new SystemMenuNode();
        n.setId(m.getId());
        n.setParentId(m.getParentId());
        n.setName(m.getName());
        n.setCode(m.getMenuKey());
        n.setPath(m.getPath());
        n.setIcon(m.getIcon());
        n.setSortOrder(m.getSort());
        n.setMenuType(m.getMenuType());
        n.setPermCode(m.getPermCode());
        n.setVisible(m.getVisible());
        n.setStatus(m.getStatus());
        n.setChildren(null);
        return n;
    }
}
