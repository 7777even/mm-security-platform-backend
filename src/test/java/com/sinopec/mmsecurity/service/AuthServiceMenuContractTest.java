package com.sinopec.mmsecurity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sinopec.mmsecurity.dto.MenuVO;
import com.sinopec.mmsecurity.entity.SysMenu;
import com.sinopec.mmsecurity.mapper.SysMenuMapper;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.security.LoginUser;
import com.sinopec.mmsecurity.security.UserContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 契约锁：GET /auth/menus 必须仅返回顶部导航的 5 个 fm-* 主模块字符串 id，
 * 与前端 MENU_ROUTE_SPECS 顶部项对齐，绝不能回到早期的数字 id(1..5) + 后端资源路径。
 * 其余子应用（fm-rescue 等）走前端 SECONDARY_ROUTES 二级路由，不进顶部菜单，故不在此返回。
 * 菜单按当前登录角色做 RBAC 过滤（allowed_roles 含当前角色才放行）。
 * 纯 Mockito，不起 Spring 上下文。
 */
class AuthServiceMenuContractTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
    private final SysMenuMapper sysMenuMapper = mock(SysMenuMapper.class);
    private final AuthService authService = new AuthService(userMapper, jwtUtil, encoder, sysMenuMapper);

    private static final Set<String> EXPECTED_IDS = Set.of(
            "fm-emergency", "fm-fire", "fm-security", "fm-tv", "fm-production");

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void menus_returnsOnlyTopNavFmSubappsWithStringRouteKeys() {
        // 以 ADMIN 身份访问（与默认 admin 账号角色一致），5 个顶部菜单均允许 ADMIN,USER
        UserContext.set(new LoginUser(null, "admin", "ADMIN"));
        when(sysMenuMapper.selectList(null)).thenReturn(List.of(
                menu("fm-emergency", "应急指挥", "/emergency", 1, "ADMIN,USER"),
                menu("fm-fire", "消防报警", "/fire", 2, "ADMIN,USER"),
                menu("fm-security", "治安防恐", "/security", 3, "ADMIN,USER"),
                menu("fm-tv", "工业电视", "/tv", 4, "ADMIN,USER"),
                menu("fm-production", "生产应急", "/production", 5, "ADMIN,USER")));

        List<MenuVO> menus = authService.menus();

        assertEquals(5, menus.size(), "后端菜单应仅返回顶部导航的 5 个 fm-* 主模块");
        assertEquals(EXPECTED_IDS, menus.stream().map(MenuVO::id).collect(Collectors.toSet()),
                "菜单 id 集合必须与前端 MENU_ROUTE_SPECS 顶部项 key 完全一致");

        for (MenuVO m : menus) {
            assertNotNull(m.name(), "菜单 name 不可为空: " + m.id());
            assertNotNull(m.path(), "菜单 path 不可为空: " + m.id());
            assertFalse(m.id().matches("\\d+"),
                    "菜单 id 必须是字符串路由 key，禁止数字 id（早期契约漂移）: " + m.id());
            assertTrue(m.path().startsWith("/"), "菜单 path 必须是前端路由路径: " + m.path());
        }
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
