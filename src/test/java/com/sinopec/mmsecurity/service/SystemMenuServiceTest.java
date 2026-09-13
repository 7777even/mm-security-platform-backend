package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.cache.IdNameCacheService;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.PermissionCodeItem;
import com.sinopec.mmsecurity.dto.SystemMenuNode;
import com.sinopec.mmsecurity.dto.SystemMenuSaveRequest;
import com.sinopec.mmsecurity.entity.SysMenu;
import com.sinopec.mmsecurity.entity.SysRoleMenu;
import com.sinopec.mmsecurity.mapper.SysMenuMapper;
import com.sinopec.mmsecurity.mapper.SysRoleMenuMapper;
import com.sinopec.mmsecurity.security.RoleAuthorityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemMenuServiceTest {

    @Mock
    private SysMenuMapper menuMapper;
    @Mock
    private SysRoleMenuMapper roleMenuMapper;
    @Mock
    private IdNameCacheService idNameCache;
    @Mock
    private RoleAuthorityService roleAuthorityService;
    @Mock
    private SystemAuditHelper audit;

    @InjectMocks
    private SystemMenuService service;

    private static SysMenu menu(Long id, Long parentId, String code, String type, String perm) {
        SysMenu m = new SysMenu();
        m.setId(id);
        m.setParentId(parentId);
        m.setMenuKey(code);
        m.setMenuType(type);
        m.setName("节点" + code);
        m.setPermCode(perm);
        m.setSort(10);
        m.setStatus(1);
        return m;
    }

    private static SystemMenuSaveRequest req(String code, String type) {
        SystemMenuSaveRequest r = new SystemMenuSaveRequest();
        r.setCode(code);
        r.setMenuType(type);
        r.setParentId(0L);
        r.setName("名称");
        return r;
    }

    @Test
    void tree_buildsParentChild() {
        SysMenu root = menu(1L, 0L, "ROOT", "DIR", null);
        SysMenu child = menu(2L, 1L, "SUB", "MENU", "sys:view");
        when(menuMapper.selectList(any())).thenReturn(new ArrayList<>(List.of(root, child)));

        List<SystemMenuNode> roots = service.tree();
        assertEquals(1, roots.size());
        assertEquals("ROOT", roots.get(0).getCode());
        assertNotNull(roots.get(0).getChildren());
        assertEquals(1, roots.get(0).getChildren().size());
        assertEquals("SUB", roots.get(0).getChildren().get(0).getCode());
    }

    @Test
    void permissions_aggregatesNonBlankCodes() {
        SysMenu a = menu(1L, 0L, "A", "MENU", "a:view");
        SysMenu b = menu(2L, 0L, "B", "BUTTON", null); // 无 perm_code，跳过
        when(menuMapper.selectList(any())).thenReturn(List.of(a, b));

        List<PermissionCodeItem> perms = service.permissions();
        assertEquals(1, perms.size());
        assertEquals("a:view", perms.get(0).getPermCode());
    }

    @Test
    void create_success() {
        when(menuMapper.selectCount(any())).thenReturn(0L);
        when(menuMapper.insert(any())).thenReturn(1);
        SystemMenuNode node = service.create(req("NEW", "MENU"));
        assertNotNull(node);
        assertEquals("NEW", node.getCode());
        verify(roleAuthorityService).reloadRolePerms();
    }

    @Test
    void create_duplicateCode_throwsConflict() {
        when(menuMapper.selectCount(any())).thenReturn(1L);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req("NEW", "MENU")));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void create_invalidMenuType_throws() {
        when(menuMapper.selectCount(any())).thenReturn(0L);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req("NEW", "XX")));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void update_success() {
        when(menuMapper.selectById(1L)).thenReturn(menu(1L, 0L, "OLD", "MENU", null));
        when(menuMapper.updateById(any())).thenReturn(1);
        SystemMenuNode node = service.update(1L, req("OLD", "MENU"));
        assertNotNull(node);
        verify(roleAuthorityService).reloadRolePerms();
    }

    @Test
    void update_duplicateCode_throwsConflict() {
        when(menuMapper.selectById(1L)).thenReturn(menu(1L, 0L, "OLD", "MENU", null));
        when(menuMapper.selectCount(any())).thenReturn(1L);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, req("OTHER", "MENU")));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void update_parentIsSelf_throws() {
        when(menuMapper.selectById(1L)).thenReturn(menu(1L, 0L, "OLD", "MENU", null));
        SystemMenuSaveRequest r = req("OLD", "MENU");
        r.setParentId(1L);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, r));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void delete_hasChildren_conflict() {
        when(menuMapper.selectById(2L)).thenReturn(menu(2L, 1L, "SUB", "MENU", null));
        when(menuMapper.selectCount(any())).thenReturn(3L);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(2L));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void delete_granted_conflict() {
        when(menuMapper.selectById(2L)).thenReturn(menu(2L, 1L, "SUB", "MENU", null));
        when(menuMapper.selectCount(any())).thenReturn(0L); // 子节点 0
        when(roleMenuMapper.selectCount(any())).thenReturn(2L); // 已授权 2
        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(2L));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void delete_normal_success() {
        when(menuMapper.selectById(2L)).thenReturn(menu(2L, 1L, "SUB", "MENU", null));
        when(menuMapper.selectCount(any())).thenReturn(0L);
        when(roleMenuMapper.selectCount(any())).thenReturn(0L);
        when(menuMapper.deleteById(2L)).thenReturn(1);
        DeleteResult d = service.delete(2L);
        assertTrue(d.getOk());
        verify(roleAuthorityService).reloadRolePerms();
    }
}
