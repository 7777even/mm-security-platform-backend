package com.sinopec.mmsecurity.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * DataScopeResolver 解析逻辑（纯 Mockito + UserContext 线程局部）：
 * ALL→null（不过滤）；SELF/DEPT + zoneCodes→解析集合；空 zoneCodes→空集（最小权限）；匿名→null。
 */
@ExtendWith(MockitoExtension.class)
class DataScopeResolverTest {

    @Mock
    private RoleAuthorityService roleAuthorityService;

    @Mock
    private SysUserMapper userMapper;

    private DataScopeResolver resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new DataScopeResolver(roleAuthorityService, userMapper);
        // 触发 @PostConstruct 构建 Caffeine 缓存（测试不启 Spring 上下文）
        Method init = DataScopeResolver.class.getDeclaredMethod("init");
        init.setAccessible(true);
        init.invoke(resolver);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void allScope_returnsNull_noFilter() {
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
        when(roleAuthorityService.dataScopeOf("ADMIN")).thenReturn("ALL");

        assertThat(resolver.resolveZones()).isNull();
    }

    @Test
    void selfScope_withZoneCodes_returnsParsedSet() {
        UserContext.set(new LoginUser(1L, "alice", "TEAM_LEADER"));
        when(roleAuthorityService.dataScopeOf("TEAM_LEADER")).thenReturn("SELF");
        SysUser u = new SysUser();
        u.setZoneCodes("炼油区, 罐区 ,仓储区");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(u);

        assertThat(resolver.resolveZones()).containsExactlyInAnyOrder("炼油区", "罐区", "仓储区");
    }

    @Test
    void selfScope_emptyZoneCodes_returnsEmptySet() {
        UserContext.set(new LoginUser(1L, "bob", "TEAM_LEADER"));
        when(roleAuthorityService.dataScopeOf("TEAM_LEADER")).thenReturn("SELF");
        SysUser u = new SysUser();
        u.setZoneCodes("");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(u);

        assertThat(resolver.resolveZones()).isEmpty();
    }

    @Test
    void anonymous_returnsNull() {
        UserContext.clear();

        assertThat(resolver.resolveZones()).isNull();
    }
}
