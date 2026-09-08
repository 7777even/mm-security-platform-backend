package com.sinopec.mmsecurity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.sinopec.mmsecurity.dto.MenuVO;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.JwtUtil;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 契约锁：GET /auth/menus 必须返回与前端 MENU_ROUTE_SPECS 对齐的字符串 id（fm-*），
 * 绝不能回到早期的数字 id(1..5) + 后端资源路径。否则前端 buildDynamicRoutes 全部跳过、
 * 只能降级 DEFAULT_MENUS。纯 Mockito，不起 Spring 上下文。
 */
class AuthServiceMenuContractTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
    private final AuthService authService = new AuthService(userMapper, jwtUtil, encoder);

    private static final Set<String> EXPECTED_IDS = Set.of(
            "fm-emergency", "fm-fire", "fm-security", "fm-tv", "fm-production",
            "fm-rescue", "fm-typhoon", "fm-production-area", "fm-major-hazard",
            "fm-communication", "fm-video-control", "fm-video-wall");

    @Test
    void menus_returnsAllFmSubappsWithStringRouteKeys() {
        List<MenuVO> menus = authService.menus();

        assertEquals(12, menus.size(), "后端菜单应覆盖全部 12 个 fm-* 子应用");
        assertEquals(EXPECTED_IDS, menus.stream().map(MenuVO::id).collect(Collectors.toSet()),
                "菜单 id 集合必须与前端 MENU_ROUTE_SPECS 的字符串 key 完全一致");

        for (MenuVO m : menus) {
            assertNotNull(m.name(), "菜单 name 不可为空: " + m.id());
            assertNotNull(m.path(), "菜单 path 不可为空: " + m.id());
            assertFalse(m.id().matches("\\d+"),
                    "菜单 id 必须是字符串路由 key，禁止数字 id（早期契约漂移）: " + m.id());
            assertTrue(m.path().startsWith("/"), "菜单 path 必须是前端路由路径: " + m.path());
        }
    }
}
