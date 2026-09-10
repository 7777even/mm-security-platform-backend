package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.cache.IdNameCacheService;
import com.sinopec.mmsecurity.common.cache.PasswordStateCache;
import com.sinopec.mmsecurity.dto.PasswordResetResult;
import com.sinopec.mmsecurity.dto.SystemUserCreate;
import com.sinopec.mmsecurity.dto.SystemUserItem;
import com.sinopec.mmsecurity.dto.SystemUserUpdate;
import com.sinopec.mmsecurity.entity.SysRole;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysRoleMapper;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import com.sinopec.mmsecurity.security.LoginUser;
import com.sinopec.mmsecurity.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SystemUserService（纯 Mockito）：三条硬防护的负例覆盖。
 * <ul>
 *   <li>禁操作自己（删除 / 改角色 / 停用）→ 403；</li>
 *   <li>保护最后一个启用 ADMIN → 409；</li>
 *   <li>用户名重复 → 409、角色不存在或停用 → 100；</li>
 *   <li>重置口令返回一次性临时口令并置强制改密。</li>
 * </ul>
 */
class SystemUserServiceTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);
    private final IdNameCacheService idNameCache = mock(IdNameCacheService.class);
    private final PasswordStateCache passwordStateCache = mock(PasswordStateCache.class);
    private final SystemAuditHelper audit = mock(SystemAuditHelper.class);
    private final PasswordPolicy passwordPolicy = new PasswordPolicy();
    private final DataScopeResolver dataScopeResolver = mock(DataScopeResolver.class);

    private final SystemUserService service = new SystemUserService(
            userMapper, roleMapper, encoder, passwordPolicy, idNameCache, passwordStateCache, audit, dataScopeResolver);

    @BeforeEach
    void setUp() {
        // 策略关停 → 测试聚焦于防护逻辑本身；复杂度由 PasswordPolicyTest 覆盖
        ReflectionTestUtils.setField(passwordPolicy, "enabled", false);
        when(encoder.encode(any())).thenReturn("$2a$10$hash");
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void deleteSelf_rejected403() {
        when(userMapper.selectById(9L)).thenReturn(user(9L, "admin", "ADMIN", 1));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(9L));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
        verify(userMapper, never()).deleteById(ArgumentMatchers.<java.io.Serializable>any());
    }

    @Test
    void deleteLastEnabledAdmin_rejected409() {
        // 目标是他人且为唯一启用 ADMIN
        when(userMapper.selectById(7L)).thenReturn(user(7L, "boss", "ADMIN", 1));
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(7L));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
        verify(userMapper, never()).deleteById(ArgumentMatchers.<java.io.Serializable>any());
    }

    @Test
    void deleteNormalUser_succeeds() {
        when(userMapper.selectById(5L)).thenReturn(user(5L, "zhang.san", "OUTER_OPER", 1));

        assertTrue(service.delete(5L).getOk());
        verify(userMapper).deleteById(ArgumentMatchers.<java.io.Serializable>eq(5L));
    }

    @Test
    void changeOwnRole_rejected403() {
        when(userMapper.selectById(1L)).thenReturn(user(1L, "admin", "ADMIN", 1));
        SystemUserUpdate req = new SystemUserUpdate();
        req.setRoleCode("OUTER_OPER");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, req));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void demoteLastEnabledAdmin_rejected409() {
        when(userMapper.selectById(7L)).thenReturn(user(7L, "boss", "ADMIN", 1));
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        SystemUserUpdate req = new SystemUserUpdate();
        req.setRoleCode("SCHEDULER");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(7L, req));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void disableSelf_rejected403() {
        when(userMapper.selectById(1L)).thenReturn(user(1L, "admin", "ADMIN", 1));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateStatus(1L, 0));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void create_duplicateUsername_rejected409() {
        // 判重走「含逻辑删除行」的原生计数（唯一索引 + 逻辑删除的语义鸿沟）
        when(userMapper.countUsernameIncludingDeleted("zhang.san")).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(createReq("zhang.san", "ADMIN")));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void create_unknownOrDisabledRole_rejected100() {
        when(userMapper.countUsernameIncludingDeleted("new.user")).thenReturn(0L);
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(createReq("new.user", "GHOST")));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void create_success_setsMustChangePwdAndRoleName() {
        when(userMapper.countUsernameIncludingDeleted("new.user")).thenReturn(0L);
        SysRole role = new SysRole();
        role.setId(1L);
        role.setRoleCode("OUTER_OPER");
        role.setRoleName("外操");
        role.setStatus(1);
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(role);
        when(roleMapper.selectList(any())).thenReturn(java.util.List.of(role));

        SystemUserItem item = service.create(createReq("new.user", "outer_oper"));

        assertEquals("OUTER_OPER", item.getRoleCode(), "角色码应统一大写");
        assertEquals("外操", item.getRoleName());
        assertTrue(item.getMustChangePwd(), "新建用户须强制首登改密");
        verify(audit).record(ArgumentMatchers.eq("system.user.create"), any());
    }

    @Test
    void resetPassword_returnsTemporaryPasswordAndForcesChange() {
        when(userMapper.selectById(5L)).thenReturn(user(5L, "zhang.san", "OUTER_OPER", 1));

        PasswordResetResult r = service.resetPassword(5L);

        assertNotNull(r.getTemporaryPassword());
        assertTrue(r.getTemporaryPassword().length() >= 8);
        assertTrue(r.getMustChangePwd());
        verify(passwordStateCache).evict("zhang.san");
        verify(audit).record(ArgumentMatchers.eq("system.user.reset-password"), any());
    }

    private static SystemUserCreate createReq(String username, String roleCode) {
        SystemUserCreate req = new SystemUserCreate();
        req.setUsername(username);
        req.setPassword("Init@12345");
        req.setRealName("新用户");
        req.setRoleCode(roleCode);
        return req;
    }

    private static SysUser user(Long id, String username, String role, int status) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setUsername(username);
        u.setRole(role);
        u.setStatus(status);
        u.setMustChangePwd(0);
        return u;
    }
}
