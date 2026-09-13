package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.cache.IdNameCacheService;
import com.sinopec.mmsecurity.common.cache.PasswordStateCache;
import com.sinopec.mmsecurity.dto.MeResult;
import com.sinopec.mmsecurity.dto.PasswordChangeRequest;
import com.sinopec.mmsecurity.dto.ProfileUpdateRequest;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.LoginUser;
import com.sinopec.mmsecurity.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AccountService 纯单元测试（18.7% → 覆盖两个公众方法 + requireCurrentUser 边界）。
 * 用 ThreadLocal 注入 UserContext，不依赖 Spring；验证越权边界（只动当前登录用户）与审计旁路。
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    SysUserMapper userMapper;
    @Mock
    BCryptPasswordEncoder encoder;
    @Mock
    PasswordPolicy passwordPolicy;
    @Mock
    IdNameCacheService idNameCache;
    @Mock
    SystemAuditHelper audit;
    @Mock
    AuthService authService;
    @Mock
    PasswordStateCache passwordStateCache;
    @InjectMocks
    AccountService service;

    private LoginUser loginUser;
    private SysUser user;

    @BeforeEach
    void setUp() {
        loginUser = mock(LoginUser.class);
        when(loginUser.getUsername()).thenReturn("alice");
        UserContext.set(loginUser);

        user = new SysUser();
        user.setId(1L);
        user.setUsername("alice");
        user.setPasswordHash("oldhash");
        when(userMapper.selectOne(any())).thenReturn(user);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void changePassword_wrongOld_throwsAndNoUpdate() {
        PasswordChangeRequest req = new PasswordChangeRequest();
        req.setOldPassword("wrong");
        req.setNewPassword("NewPass1!");
        when(encoder.matches("wrong", "oldhash")).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.changePassword(req));
        verify(userMapper, never()).updateById(any());
        verify(audit, never()).record(anyString(), any());
    }

    @Test
    void changePassword_success_updatesHashEvictsCacheAndAudits() {
        PasswordChangeRequest req = new PasswordChangeRequest();
        req.setOldPassword("old");
        req.setNewPassword("NewPass1!");
        when(encoder.matches("old", "oldhash")).thenReturn(true);
        when(encoder.encode("NewPass1!")).thenReturn("newhash");

        service.changePassword(req);

        ArgumentCaptor<SysUser> cap = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateById(cap.capture());
        assertEquals("newhash", cap.getValue().getPasswordHash());
        assertEquals(0, cap.getValue().getMustChangePwd());
        verify(passwordStateCache).evict("alice");
        verify(audit).record(anyString(), any());
    }

    @Test
    void changePassword_policyRejected_throwsAndNoUpdate() {
        PasswordChangeRequest req = new PasswordChangeRequest();
        req.setOldPassword("old");
        req.setNewPassword("weak");
        when(encoder.matches("old", "oldhash")).thenReturn(true);
        org.mockito.Mockito.doThrow(new BusinessException(com.sinopec.mmsecurity.common.ResultCode.PARAM_INVALID, "弱口令"))
                .when(passwordPolicy).validate("alice", "weak", "old");

        assertThrows(BusinessException.class, () -> service.changePassword(req));
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void updateProfile_success_evictsIdNameCacheAndAudits() {
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setRealName("新名");
        MeResult me = mock(MeResult.class);
        when(authService.me()).thenReturn(me);

        MeResult r = service.updateProfile(req);

        assertEquals(me, r);
        ArgumentCaptor<SysUser> cap = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateById(cap.capture());
        assertEquals("新名", cap.getValue().getRealName());
        verify(idNameCache).evictUser(1L);
        verify(audit).record(anyString(), any());
    }
}
