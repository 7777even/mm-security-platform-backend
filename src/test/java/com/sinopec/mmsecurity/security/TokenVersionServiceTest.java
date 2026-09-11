package com.sinopec.mmsecurity.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TokenVersionService（纯 Mockito，不起 Spring 上下文）。
 *
 * <p>覆盖令牌失效版本号的解析与递增：这是「登出后旧令牌立即不可用」的支撑，
 * 版本读错会导致大面积误踢，版本不递增则登出形同虚设。</p>
 */
class TokenVersionServiceTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final TokenVersionService service = new TokenVersionService(userMapper);

    private static SysUser user(Long id, String name, Integer version) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setUsername(name);
        u.setTokenVersion(version);
        return u;
    }

    @Test
    void current_returnsUserVersion() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user(1L, "admin", 3));

        assertEquals(3, service.current("admin"));
    }

    @Test
    void current_returnsZero_whenUserMissing() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 用户不存在时按 0 处理：与「老令牌无 ver claim」的兜底语义一致，避免误杀
        assertEquals(0, service.current("ghost"));
    }

    @Test
    void current_returnsZero_whenVersionNull() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user(1L, "admin", null));

        assertEquals(0, service.current("admin"));
    }

    @Test
    void current_returnsZero_whenUsernameBlank() {
        assertEquals(0, service.current(null));
        assertEquals(0, service.current("   "));
    }

    @Test
    void bump_incrementsAndPersists() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user(7L, "admin", 2));

        assertEquals(3, service.bump("admin"));

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals(7L, captor.getValue().getId(), "必须带 id，否则 updateById 无法定位行");
        assertEquals(3, captor.getValue().getTokenVersion());
    }

    @Test
    void bump_treatsNullVersionAsZero() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user(7L, "admin", null));

        assertEquals(1, service.bump("admin"));
    }

    @Test
    void bump_returnsZero_whenUserMissing() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertEquals(0, service.bump("ghost"), "用户不存在时无可吊销对象，返回 0 且不报错");
    }

    @Test
    void current_reflectsBumpImmediately() {
        // 第一次读到 1，递增到 2 后应立刻读到 2（缓存已被 bump 主动失效）
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(user(1L, "admin", 1))
                .thenReturn(user(1L, "admin", 1))
                .thenReturn(user(1L, "admin", 2));

        assertEquals(1, service.current("admin"));
        assertEquals(2, service.bump("admin"));
        assertEquals(2, service.current("admin"), "登出后版本号必须立即可见，否则旧令牌仍能用");
    }
}
