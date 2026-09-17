package com.sinopec.mmsecurity.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * DataScopeResolver 防区解析测试：覆盖 resolveZonesFor 三态——ALL→null、
 * SELF+有防区→防区集合、SELF+无防区→空集、空用户→null。
 */
class DataScopeResolverTest {

    private SysUserMapper userMapper;
    private RoleAuthorityService roleAuthorityService;
    private DataScopeResolver resolver;

    @BeforeEach
    void setUp() {
        userMapper = mock(SysUserMapper.class);
        roleAuthorityService = mock(RoleAuthorityService.class);
        resolver = new DataScopeResolver(roleAuthorityService, userMapper);
        resolver.init(); // 触发 @PostConstruct 构建 Caffeine 缓存（单测无 Spring 容器）
    }

    private void stubUser(String zoneCodes) {
        SysUser u = new SysUser();
        u.setZoneCodes(zoneCodes);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(u);
    }

    @Test
    @DisplayName("data_scope=ALL → 返回 null（不加过滤）")
    void allScope_returnsNull() {
        when(roleAuthorityService.dataScopeOf("ADMIN")).thenReturn("ALL");
        assertNull(resolver.resolveZonesFor(new LoginUser(null, "admin", "ADMIN")));
    }

    @Test
    @DisplayName("SELF + 有防区 → 返回防区集合")
    void selfScope_withZones_returnsZoneSet() {
        when(roleAuthorityService.dataScopeOf("SCHEDULER")).thenReturn("SELF");
        stubUser("炼油区,罐区");
        Set<String> zones = resolver.resolveZonesFor(new LoginUser(null, "u1", "SCHEDULER"));
        assertEquals(Set.of("炼油区", "罐区"), zones);
    }

    @Test
    @DisplayName("SELF + 无防区 → 返回空集（最小权限）")
    void selfScope_noZones_returnsEmpty() {
        when(roleAuthorityService.dataScopeOf("SCHEDULER")).thenReturn("SELF");
        stubUser("");
        Set<String> zones = resolver.resolveZonesFor(new LoginUser(null, "u1", "SCHEDULER"));
        assertTrue(zones != null && zones.isEmpty());
    }

    @Test
    @DisplayName("空用户 → 返回 null（匿名/公开场景）")
    void nullUser_returnsNull() {
        assertNull(resolver.resolveZonesFor(null));
    }
}
