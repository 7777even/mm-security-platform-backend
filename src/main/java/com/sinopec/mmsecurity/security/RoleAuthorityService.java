package com.sinopec.mmsecurity.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sinopec.mmsecurity.entity.SysMenu;
import com.sinopec.mmsecurity.entity.SysRole;
import com.sinopec.mmsecurity.entity.SysRoleMenu;
import com.sinopec.mmsecurity.mapper.SysMenuMapper;
import com.sinopec.mmsecurity.mapper.SysRoleMapper;
import com.sinopec.mmsecurity.mapper.SysRoleMenuMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 角色 → 授权（菜单 id 集合 + 权限码集合）解析服务。
 *
 * <p><b>设计（ADR-3）</b>：权限码<b>不写入 access 令牌</b>（避免令牌膨胀与 2h 滞后），
 * 而在请求期由本服务按 {@code sys_role_menu} 解析。采用 Caffeine 读穿缓存
 * （TTL 5min 兜底）+ <b>写时主动失效</b> {@link #reloadRolePerms()}，
 * 复用 {@code IdNameCacheService} 既有范式：角色/菜单授权变更后**立即生效**，
 * 无需等令牌过期、无需踢下线。</p>
 *
 * <p><b>最小权限原则</b>：未登记的角色、已停用的角色、无授权行的角色，一律返回空授权
 * （而非回退到某种默认权限）；停用角色即等价于回收全部权限。</p>
 *
 * <p><b>信任边界</b>：缓存仅用于性能，权限判定的语义真源是库表；缓存失效窗口（TTL 5min）
 * 是最终一致窗口，写路径必须调用 {@link #reloadRolePerms()} 覆盖。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleAuthorityService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    /** 角色授权快照：启用菜单 id 集合 + 权限码集合 + 数据范围（data_scope） */
    public record RoleGrant(Set<Long> menuIds, Set<String> perms, String dataScope) {
        public static final RoleGrant EMPTY = new RoleGrant(Set.of(), Set.of(), "SELF");
    }

    private Cache<String, RoleGrant> cache;

    @PostConstruct
    void init() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(256)
                .build();
    }

    /** 取某角色的完整授权（读穿缓存）。roleCode 为空/未知角色 → 空授权。 */
    public RoleGrant grantOf(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return RoleGrant.EMPTY;
        }
        return cache.get(roleCode.trim().toUpperCase(Locale.ROOT), this::load);
    }

    /** 某角色的权限码集合（不可变）。 */
    public Set<String> permsOf(String roleCode) {
        return grantOf(roleCode).perms();
    }

    /** 某角色是否持有指定权限码。perm 为空视为不校验（true）。 */
    public boolean hasPerm(String roleCode, String perm) {
        if (perm == null || perm.isBlank()) {
            return true;
        }
        return grantOf(roleCode).perms().contains(perm);
    }

    /** 某角色可见的菜单 id 集合（已剔除停用节点）。 */
    public Set<Long> menuIdsOf(String roleCode) {
        return grantOf(roleCode).menuIds();
    }

    /** 某角色的 data_scope（ALL/DEPT/SELF）。角色未知/停用 → 默认 SELF（最小权限）。 */
    public String dataScopeOf(String roleCode) {
        String scope = grantOf(roleCode).dataScope();
        return (scope == null || scope.isBlank()) ? "SELF" : scope.trim().toUpperCase(Locale.ROOT);
    }

    /** 角色授权变更（角色-菜单授权、菜单权限码/状态变更）后调用，整表失效。 */
    public void reloadRolePerms() {
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    /** 读库加载：角色（须启用）→ sys_role_menu → sys_menu（须启用、权限码非空）。 */
    private RoleGrant load(String upperCode) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .apply("UPPER(role_code) = {0}", upperCode));
        if (role == null || role.getStatus() == null || role.getStatus() != 1) {
            return RoleGrant.EMPTY;
        }
        List<SysRoleMenu> links = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getId()));
        if (links.isEmpty()) {
            return RoleGrant.EMPTY;
        }

        Set<Long> linkedMenuIds = new LinkedHashSet<>();
        for (SysRoleMenu link : links) {
            if (link.getMenuId() != null) {
                linkedMenuIds.add(link.getMenuId());
            }
        }

        Set<Long> enabledMenuIds = new LinkedHashSet<>();
        Set<String> perms = new LinkedHashSet<>();
        for (SysMenu menu : menuMapper.selectBatchIds(linkedMenuIds)) {
            if (menu == null || (menu.getStatus() != null && menu.getStatus() != 1)) {
                continue;
            }
            enabledMenuIds.add(menu.getId());
            if (menu.getPermCode() != null && !menu.getPermCode().isBlank()) {
                perms.add(menu.getPermCode().trim());
            }
        }
        return new RoleGrant(Set.copyOf(enabledMenuIds), Set.copyOf(perms),
                role.getDataScope() == null ? "SELF" : role.getDataScope());
    }
}
