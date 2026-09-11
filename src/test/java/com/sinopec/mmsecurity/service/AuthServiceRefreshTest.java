package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.cache.IdNameCacheService;
import com.sinopec.mmsecurity.dto.TokenResponse;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.security.TokenVersionService;
import com.sinopec.mmsecurity.security.RoleAuthorityService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 安全回归：AuthService.refresh 不得再硬编码 ADMIN 角色。
 *
 * <p>历史缺陷：换发 access 令牌时角色写死 {@code "ADMIN"}，在引入多角色后即构成提权。
 * 本测试锁定「新令牌角色 = 库中真实角色」，并覆盖「账号不存在 / 已停用 / 类型不符」三种拒绝路径。</p>
 */
class AuthServiceRefreshTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
    private final IdNameCacheService idNameCache = mock(IdNameCacheService.class);
    private final RoleAuthorityService roleAuthority = mock(RoleAuthorityService.class);
    private final TokenVersionService tokenVersionService = mock(TokenVersionService.class);
    private final AuthService authService = new AuthService(userMapper, jwtUtil, encoder, idNameCache, roleAuthority, tokenVersionService);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTtl", 7200L);
        when(jwtUtil.issueAccess(any(), any(), anyInt())).thenReturn("new-access");
    }

    @Test
    void refresh_usesRealRoleFromDatabase_notHardcodedAdmin() {
        Claims claims = claims("zhang.san", "refresh");
        when(jwtUtil.parse("rt")).thenReturn(claims);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user("zhang.san", "OUTER_OPER", 1));

        TokenResponse t = authService.refresh("rt");

        assertEquals("new-access", t.getAccessToken());
        verify(jwtUtil).issueAccess(eq("zhang.san"), eq("OUTER_OPER"), anyInt());
    }

    @Test
    void refresh_unknownUser_rejected() {
        Claims claims = claims("ghost", "refresh");
        when(jwtUtil.parse("rt")).thenReturn(claims);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refresh("rt"));
        assertEquals(ResultCode.TOKEN_INVALID, ex.getCode());
    }

    @Test
    void refresh_disabledUser_rejected() {
        Claims claims = claims("zhang.san", "refresh");
        when(jwtUtil.parse("rt")).thenReturn(claims);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user("zhang.san", "OUTER_OPER", 0));

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refresh("rt"));
        assertEquals(ResultCode.TOKEN_INVALID, ex.getCode());
    }

    @Test
    void refresh_accessTokenType_rejected() {
        Claims claims = claims("zhang.san", "access");
        when(jwtUtil.parse("at")).thenReturn(claims);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refresh("at"));
        assertEquals(ResultCode.TOKEN_INVALID, ex.getCode());
    }

    private static Claims claims(String subject, String type) {
        Claims c = mock(Claims.class);
        when(c.getSubject()).thenReturn(subject);
        when(c.get("type", String.class)).thenReturn(type);
        return c;
    }

    private static SysUser user(String username, String role, int status) {
        SysUser u = new SysUser();
        u.setUsername(username);
        u.setRole(role);
        u.setStatus(status);
        return u;
    }
}
