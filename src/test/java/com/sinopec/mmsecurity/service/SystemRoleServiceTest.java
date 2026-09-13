package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.SystemRoleItem;
import com.sinopec.mmsecurity.dto.SystemRoleMenuAssign;
import com.sinopec.mmsecurity.dto.SystemRoleSaveRequest;
import com.sinopec.mmsecurity.entity.SysRole;
import com.sinopec.mmsecurity.entity.SysRoleMenu;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysRoleMapper;
import com.sinopec.mmsecurity.mapper.SysRoleMenuMapper;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import com.sinopec.mmsecurity.security.RoleAuthorityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemRoleServiceTest {

    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysRoleMenuMapper roleMenuMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private RoleAuthorityService roleAuthorityService;
    @Mock
    private SystemAuditHelper audit;

    @InjectMocks
    private SystemRoleService service;

    private static SysRole role(Long id, String code, int builtIn) {
        SysRole r = new SysRole();
        r.setId(id);
        r.setRoleCode(code);
        r.setRoleName("角色" + code);
        r.setBuiltIn(builtIn);
        r.setSortOrder(10);
        r.setStatus(1);
        return r;
    }

    private static SystemRoleSaveRequest req(String code) {
        SystemRoleSaveRequest r = new SystemRoleSaveRequest();
        r.setRoleCode(code);
        r.setRoleName("名称");
        r.setDataScope("ALL");
        r.setStatus(1);
        return r;
    }

    @Test
    void list_returnsItemsWithUserCount() {
        when(roleMapper.selectList(any())).thenReturn(List.of(role(1L, "OP", 0)));
        SysUser u = new SysUser();
        u.setRole("OP");
        when(userMapper.selectList(any())).thenReturn(List.of(u));

        List<SystemRoleItem> items = service.list(null);
        assertEquals(1, items.size());
        assertEquals("OP", items.get(0).getRoleCode());
        assertEquals(1L, items.get(0).getUserCount());
    }

    @Test
    void list_keywordTrimsAndSearches() {
        when(roleMapper.selectList(any())).thenReturn(List.of(role(2L, "ADMIN", 1)));
        when(userMapper.selectList(any())).thenReturn(List.of());
        List<SystemRoleItem> items = service.list("  admin  ");
        assertEquals(1, items.size());
        assertTrue(items.get(0).getBuiltIn());
    }

    @Test
    void get_notFound_throws() {
        when(roleMapper.selectById(anyLong())).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.get(9L));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
    }

    @Test
    void create_success_reloadsPermsAndAudits() {
        when(roleMapper.countRoleCodeIncludingDeleted(anyString(), anyLong())).thenReturn(0L);
        when(roleMapper.insert(any())).thenReturn(1);
        when(userMapper.selectList(any())).thenReturn(List.of());

        SystemRoleItem item = service.create(req("NEW"));
        assertNotNull(item);
        verify(roleMapper).insert(any());
        verify(roleAuthorityService).reloadRolePerms();
        verify(audit).record(eq("system.role.create"), any());
    }

    @Test
    void create_duplicateCode_throwsConflict() {
        when(roleMapper.countRoleCodeIncludingDeleted(anyString(), anyLong())).thenReturn(1L);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req("NEW")));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void create_normalizesScopeToUpper() {
        when(roleMapper.countRoleCodeIncludingDeleted(anyString(), anyLong())).thenReturn(0L);
        when(roleMapper.insert(any())).thenReturn(1);
        when(userMapper.selectList(any())).thenReturn(List.of());
        SystemRoleSaveRequest r = req("S");
        r.setDataScope("self");
        assertEquals("SELF", service.create(r).getDataScope());
    }

    @Test
    void create_invalidScope_throws() {
        when(roleMapper.countRoleCodeIncludingDeleted(anyString(), anyLong())).thenReturn(0L);
        SystemRoleSaveRequest r = req("S");
        r.setDataScope("XX");
        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(r));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void update_builtInCodeChange_forbidden() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L, "ADMIN", 1));
        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, req("OTHER")));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void update_builtInDisable_forbidden() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L, "ADMIN", 1));
        SystemRoleSaveRequest r = req("ADMIN");
        r.setStatus(0);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, r));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void update_duplicateCode_throwsConflict() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L, "OP", 0));
        when(roleMapper.countRoleCodeIncludingDeleted(anyString(), anyLong())).thenReturn(1L);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, req("OP2")));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void update_normal_success() {
        when(roleMapper.selectById(3L)).thenReturn(role(3L, "OP", 0));
        when(roleMapper.updateById(any())).thenReturn(1);
        when(userMapper.selectList(any())).thenReturn(List.of());
        SystemRoleItem item = service.update(3L, req("OP"));
        assertNotNull(item);
        verify(roleAuthorityService).reloadRolePerms();
    }

    @Test
    void updateStatus_invalid_throws() {
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateStatus(1L, 2));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void updateStatus_builtIn_forbidden() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L, "ADMIN", 1));
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateStatus(1L, 0));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void updateStatus_normal_success() {
        when(roleMapper.selectById(4L)).thenReturn(role(4L, "OP", 0));
        when(roleMapper.updateById(any())).thenReturn(1);
        when(userMapper.selectList(any())).thenReturn(List.of());
        SystemRoleItem item = service.updateStatus(4L, 0);
        assertEquals(0, item.getStatus());
        verify(roleAuthorityService).reloadRolePerms();
    }

    @Test
    void delete_builtIn_forbidden() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L, "ADMIN", 1));
        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void delete_hasUsers_conflict() {
        when(roleMapper.selectById(2L)).thenReturn(role(2L, "OP", 0));
        when(userMapper.selectCount(any())).thenReturn(3L);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(2L));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void delete_normal_success() {
        when(roleMapper.selectById(2L)).thenReturn(role(2L, "OP", 0));
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(roleMenuMapper.delete(any())).thenReturn(1);
        when(roleMapper.deleteById(2L)).thenReturn(1);
        DeleteResult d = service.delete(2L);
        assertTrue(d.getOk());
        verify(roleAuthorityService).reloadRolePerms();
        verify(audit).record(eq("system.role.delete"), any());
    }

    @Test
    void menus_returnsLinkedMenuIds() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L, "OP", 0));
        SysRoleMenu link = new SysRoleMenu();
        link.setMenuId(11L);
        when(roleMenuMapper.selectList(any())).thenReturn(List.of(link));
        assertEquals(List.of(11L), service.menus(1L));
    }

    @Test
    void assignMenus_normal_replacesAndReturns() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L, "OP", 0));
        when(roleMenuMapper.delete(any())).thenReturn(1);
        when(roleMenuMapper.insert(any())).thenReturn(1);
        SystemRoleMenuAssign assign = new SystemRoleMenuAssign();
        assign.setMenuIds(Arrays.asList(11L, 12L, null));
        assertEquals(List.of(11L, 12L), service.assignMenus(1L, assign));
        verify(roleAuthorityService).reloadRolePerms();
    }

    @Test
    void assignMenus_adminClear_forbidden() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L, "ADMIN", 1));
        SystemRoleMenuAssign assign = new SystemRoleMenuAssign();
        assign.setMenuIds(List.of());
        BusinessException ex = assertThrows(BusinessException.class, () -> service.assignMenus(1L, assign));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }
}
