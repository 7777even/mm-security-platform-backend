package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.entity.SysMenu;
import com.sinopec.mmsecurity.entity.SysRole;
import com.sinopec.mmsecurity.entity.SysRoleMenu;
import com.sinopec.mmsecurity.mapper.SysMenuMapper;
import com.sinopec.mmsecurity.mapper.SysRoleMapper;
import com.sinopec.mmsecurity.mapper.SysRoleMenuMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RoleAuthorityService（纯 Mockito）：角色 → 授权解析、最小权限口径、缓存与写时失效。
 */
class RoleAuthorityServiceTest {

    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final SysRoleMenuMapper roleMenuMapper = mock(SysRoleMenuMapper.class);
    private final SysMenuMapper menuMapper = mock(SysMenuMapper.class);
    private final RoleAuthorityService service = new RoleAuthorityService(roleMapper, roleMenuMapper, menuMapper);

    @BeforeEach
    void setUp() {
        service.init();
    }

    @Test
    void unknownRole_yieldsEmptyGrant() {
        when(roleMapper.selectOne(any())).thenReturn(null);
        assertTrue(service.permsOf("NOPE").isEmpty());
        assertTrue(service.menuIdsOf("NOPE").isEmpty());
        assertFalse(service.hasPerm("NOPE", "system:user:create"));
    }

    @Test
    void disabledRole_yieldsEmptyGrant() {
        SysRole disabled = role(1L, "ADMIN", 0);
        when(roleMapper.selectOne(any())).thenReturn(disabled);

        assertTrue(service.permsOf("ADMIN").isEmpty(), "停用角色应等价于回收全部权限");
    }

    @Test
    void blankRoleCode_yieldsEmptyGrant() {
        assertTrue(service.permsOf(null).isEmpty());
        assertTrue(service.permsOf("  ").isEmpty());
    }

    @Test
    void enabledRole_aggregatesPermsAndMenuIds_excludingDisabledAndBlank() {
        SysRole admin = role(1L, "ADMIN", 1);
        when(roleMapper.selectOne(any())).thenReturn(admin);
        when(roleMenuMapper.selectList(any())).thenReturn(List.of(
                link(1L, 10L), link(1L, 11L), link(1L, 12L), link(1L, 13L)));
        when(menuMapper.selectBatchIds(anyCollection())).thenReturn(List.of(
                menu(10L, "DIR", null, 1),
                menu(11L, "MENU", "system:user:view", 1),
                menu(12L, "BUTTON", "system:user:create", 1),
                menu(13L, "BUTTON", "system:user:delete", 0)));

        Set<String> perms = service.permsOf("admin");
        assertEquals(Set.of("system:user:view", "system:user:create"), perms,
                "仅聚合启用节点且 perm_code 非空，停用节点与 DIR 空权限码应被剔除");
        assertEquals(Set.of(10L, 11L, 12L), service.menuIdsOf("ADMIN"), "停用节点不应进入可见菜单集合");

        assertTrue(service.hasPerm("ADMIN", "system:user:create"));
        assertFalse(service.hasPerm("ADMIN", "system:user:delete"));
        assertTrue(service.hasPerm("ADMIN", ""), "空权限码视为不校验");
    }

    @Test
    void roleWithoutGrants_yieldsEmptyGrant() {
        when(roleMapper.selectOne(any())).thenReturn(role(2L, "OUTER_OPER", 1));
        when(roleMenuMapper.selectList(any())).thenReturn(List.of());

        assertTrue(service.permsOf("OUTER_OPER").isEmpty());
    }

    @Test
    void grantIsCached_andReloadInvalidates() {
        when(roleMapper.selectOne(any())).thenReturn(role(1L, "ADMIN", 1));
        when(roleMenuMapper.selectList(any())).thenReturn(List.of(link(1L, 11L)));
        when(menuMapper.selectBatchIds(anyCollection())).thenReturn(List.of(menu(11L, "MENU", "system:user:view", 1)));

        service.permsOf("ADMIN");
        service.permsOf("ADMIN");
        verify(roleMapper, times(1)).selectOne(any());

        service.reloadRolePerms();
        service.permsOf("ADMIN");
        verify(roleMapper, times(2)).selectOne(any());
    }

    private static SysRole role(Long id, String code, int status) {
        SysRole r = new SysRole();
        r.setId(id);
        r.setRoleCode(code);
        r.setStatus(status);
        return r;
    }

    private static SysRoleMenu link(Long roleId, Long menuId) {
        SysRoleMenu l = new SysRoleMenu();
        l.setRoleId(roleId);
        l.setMenuId(menuId);
        return l;
    }

    private static SysMenu menu(Long id, String type, String permCode, int status) {
        SysMenu m = new SysMenu();
        m.setId(id);
        m.setMenuType(type);
        m.setPermCode(permCode);
        m.setStatus(status);
        return m;
    }
}
