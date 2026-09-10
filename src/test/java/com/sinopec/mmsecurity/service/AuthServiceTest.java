package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.cache.IdNameCacheService;
import com.sinopec.mmsecurity.dto.MeResult;
import com.sinopec.mmsecurity.dto.MenuVO;
import com.sinopec.mmsecurity.entity.SysMenu;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.security.LoginUser;
import com.sinopec.mmsecurity.security.RoleAuthorityService;
import com.sinopec.mmsecurity.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AuthService.me() / menus()（纯 Mockito，不起 Spring 上下文）。
 * <ul>
 *   <li>me() 从 sys_user 读取真实身份，并下发 roles / perms（前端权限唯一来源）。</li>
 *   <li>menus() 按当前登录角色经 sys_role_menu 授权过滤，且仅返回 fm-* 顶层非 BUTTON 节点。</li>
 * </ul>
 */
class AuthServiceTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
    private final IdNameCacheService idNameCache = mock(IdNameCacheService.class);
    private final RoleAuthorityService roleAuthority = mock(RoleAuthorityService.class);
    private final AuthService authService = new AuthService(userMapper, jwtUtil, encoder, idNameCache, roleAuthority);

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void me_readsRealIdentityAndInjectsPerms() {
        UserContext.set(new LoginUser(1L, "zhang.san", "USER"));
        SysUser u = new SysUser();
        u.setRealName("张三");
        u.setRole("USER");
        u.setMustChangePwd(0);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(u);
        when(roleAuthority.permsOf("USER")).thenReturn(Set.of("fire-alarm:ack", "security:view"));

        MeResult me = authService.me();
        assertEquals("zhang.san", me.getUsername());
        assertEquals("张三", me.getRealName());
        assertEquals("USER", me.getRole());
        assertEquals(List.of("USER"), me.getRoles());
        assertEquals(List.of("fire-alarm:ack", "security:view"), me.getPerms());
        assertFalse(me.getMustChangePwd());
    }

    @Test
    void me_fallsBackToContextWhenUserNotFound() {
        UserContext.set(new LoginUser(99L, "ghost", "VIEWER"));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(roleAuthority.permsOf(anyString())).thenReturn(Set.of());

        MeResult me = authService.me();
        assertEquals("ghost", me.getUsername());
        assertEquals("ghost", me.getRealName());
        assertEquals("VIEWER", me.getRole());
        assertTrue(me.getPerms().isEmpty());
    }

    @Test
    void me_flagsMustChangePwd() {
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
        SysUser u = new SysUser();
        u.setRealName("系统管理员");
        u.setRole("ADMIN");
        u.setMustChangePwd(1);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(u);
        when(roleAuthority.permsOf(anyString())).thenReturn(Set.of());

        assertTrue(authService.me().getMustChangePwd());
    }

    @Test
    void menus_filtersByRoleAuthority() {
        UserContext.set(new LoginUser(null, "viewer", "USER"));
        when(idNameCache.allMenus()).thenReturn(List.of(
                menu(1L, "fm-emergency", "应急指挥", "/emergency", 1),
                menu(2L, "fm-admin", "后台管理", "/admin", 9),
                menu(3L, "fm-production", "生产应急", "/production", 5)));
        when(roleAuthority.menuIdsOf("USER")).thenReturn(Set.of(1L, 3L));

        List<MenuVO> menus = authService.menus();
        assertEquals(2, menus.size());
        assertTrue(menus.stream().allMatch(m -> List.of("fm-emergency", "fm-production").contains(m.id())));
    }

    @Test
    void menus_excludesNonNavigableAndNonFmNodes() {
        UserContext.set(new LoginUser(null, "admin", "ADMIN"));
        SysMenu button = menu(4L, "system-user-create", "新增用户", null, 9101);
        button.setMenuType("BUTTON");
        SysMenu systemDir = menu(5L, "system", "系统管理", "/system", 900);

        when(idNameCache.allMenus()).thenReturn(List.of(
                menu(1L, "fm-emergency", "应急指挥", "/emergency", 1),
                button, systemDir));
        when(roleAuthority.menuIdsOf("ADMIN")).thenReturn(Set.of(1L, 4L, 5L));

        List<MenuVO> menus = authService.menus();
        assertEquals(1, menus.size());
        assertEquals("fm-emergency", menus.get(0).id());
    }

    @Test
    void menus_emptyForAnonymousRole() {
        // UserContext 未注入 → 无角色 → 空菜单（且不查缓存）
        when(idNameCache.allMenus()).thenReturn(List.of(
                menu(1L, "fm-emergency", "应急指挥", "/emergency", 1)));

        assertTrue(authService.menus().isEmpty());
    }

    private static SysMenu menu(Long id, String key, String name, String path, int sort) {
        SysMenu m = new SysMenu();
        m.setId(id);
        m.setMenuKey(key);
        m.setName(name);
        m.setPath(path);
        m.setSort(sort);
        m.setMenuType("DIR");
        m.setStatus(1);
        m.setVisible(1);
        return m;
    }
}
