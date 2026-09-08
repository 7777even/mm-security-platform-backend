package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.MenuVO;
import com.sinopec.mmsecurity.entity.SysMenu;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysMenuMapper;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.security.LoginUser;
import com.sinopec.mmsecurity.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AuthService.me() / menus() 真实数据源 + RBAC（纯 Mockito，不起 Spring 上下文）。
 * <ul>
 *   <li>me() 从 sys_user 读取真实身份（realName/role），用户不存在时回退为登录态。</li>
 *   <li>menus() 按当前登录角色过滤 sys_menu（allowed_roles 含当前角色才放行）。</li>
 * </ul>
 */
class AuthServiceTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
    private final SysMenuMapper sysMenuMapper = mock(SysMenuMapper.class);
    private final AuthService authService = new AuthService(userMapper, jwtUtil, encoder, sysMenuMapper);

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void me_readsRealIdentityFromSysUser() {
        UserContext.set(new LoginUser(1L, "zhang.san", "USER"));
        SysUser u = new SysUser();
        u.setRealName("张三");
        u.setRole("USER");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(u);

        Map<String, Object> me = authService.me();
        assertEquals("zhang.san", me.get("username"));
        assertEquals("张三", me.get("realName"));
        assertEquals("USER", me.get("role"));
    }

    @Test
    void me_fallsBackToContextWhenUserNotFound() {
        UserContext.set(new LoginUser(99L, "ghost", "VIEWER"));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Map<String, Object> me = authService.me();
        assertEquals("ghost", me.get("username"));
        assertEquals("ghost", me.get("realName"));
        assertEquals("VIEWER", me.get("role"));
    }

    @Test
    void menus_filtersByCurrentRole() {
        UserContext.set(new LoginUser(null, "viewer", "USER"));
        when(sysMenuMapper.selectList(null)).thenReturn(List.of(
                menu("fm-emergency", "应急指挥", "/emergency", 1, "ADMIN,USER"),
                menu("fm-admin", "后台管理", "/admin", 9, "ADMIN"),
                menu("fm-production", "生产应急", "/production", 5, "ADMIN,USER")));

        List<MenuVO> menus = authService.menus();
        assertEquals(2, menus.size());
        assertTrue(menus.stream().allMatch(m -> List.of("fm-emergency", "fm-production").contains(m.id())));
    }

    @Test
    void menus_emptyForAnonymousRole() {
        // UserContext 未注入 → 角色 ANONYMOUS，无任何菜单放行
        when(sysMenuMapper.selectList(null)).thenReturn(List.of(
                menu("fm-emergency", "应急指挥", "/emergency", 1, "ADMIN,USER")));

        List<MenuVO> menus = authService.menus();
        assertTrue(menus.isEmpty());
    }

    private static SysMenu menu(String key, String name, String path, int sort, String allowed) {
        SysMenu m = new SysMenu();
        m.setMenuKey(key);
        m.setName(name);
        m.setPath(path);
        m.setSort(sort);
        m.setAllowedRoles(allowed);
        return m;
    }
}
