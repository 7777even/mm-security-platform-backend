package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.SysConfigNode;
import com.sinopec.mmsecurity.dto.SysConfigSaveRequest;
import com.sinopec.mmsecurity.entity.SysConfig;
import com.sinopec.mmsecurity.mapper.SysConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemConfigServiceTest {

    @Mock
    private SysConfigMapper configMapper;
    @Mock
    private SystemAuditHelper audit;

    @InjectMocks
    private SystemConfigService service;

    private static SysConfigSaveRequest req(String key, String type) {
        SysConfigSaveRequest r = new SysConfigSaveRequest();
        r.setConfigKey(key);
        r.setConfigValue("v");
        r.setConfigName("名称");
        r.setConfigGroup("system");
        r.setConfigType(type);
        r.setSortOrder(0);
        r.setStatus(1);
        return r;
    }

    private static SysConfig entity(Long id, String key, int builtIn, int deleted) {
        SysConfig c = new SysConfig();
        c.setId(id);
        c.setConfigKey(key);
        c.setBuiltIn(builtIn);
        c.setDeleted(deleted);
        return c;
    }

    @Test
    void list_noGroup_returnsAllNonDeleted() {
        SysConfig a = entity(1L, "a", 0, 0);
        a.setSortOrder(2);
        SysConfig b = entity(2L, "b", 0, 0);
        b.setSortOrder(1);
        when(configMapper.selectList(any())).thenReturn(List.of(a, b));

        List<SysConfigNode> nodes = service.list(null);
        assertEquals(2, nodes.size());
        assertTrue(nodes.stream().anyMatch(n -> "a".equals(n.getConfigKey())));
        assertTrue(nodes.stream().anyMatch(n -> "b".equals(n.getConfigKey())));
    }

    @Test
    void list_withGroup_filtersByGroup() {
        SysConfig a = entity(1L, "a", 0, 0);
        a.setConfigGroup("sys");
        a.setSortOrder(1);
        when(configMapper.selectList(any())).thenReturn(List.of(a));

        List<SysConfigNode> nodes = service.list("sys");
        assertEquals(1, nodes.size());
        assertEquals("a", nodes.get(0).getConfigKey());
    }

    @Test
    void create_success_setsKeyAndBuiltInAndAudits() {
        when(configMapper.selectCount(any())).thenReturn(0L);
        when(configMapper.insert(any())).thenAnswer(inv -> {
            SysConfig arg = inv.getArgument(0);
            arg.setId(10L);
            return 1;
        });

        SysConfigNode node = service.create(req("sys.key", "STRING"));
        assertEquals("sys.key", node.getConfigKey());
        assertEquals("STRING", node.getConfigType());
        assertEquals(0, node.getBuiltIn());
        verify(audit).record(eq("system.config.create"), any());
    }

    @Test
    void create_duplicateKey_throwsConflict() {
        when(configMapper.selectCount(any())).thenReturn(1L);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.create(req("sys.key", "STRING")));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void create_invalidType_throwsParamInvalid() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.create(req("sys.key", "BADTYPE")));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void update_success_keepsKeyAndAudits() {
        when(configMapper.selectById(1L)).thenReturn(entity(1L, "sys.key", 0, 0));
        when(configMapper.updateById(any())).thenReturn(1);

        SysConfigNode node = service.update(1L, req("sys.key", "NUMBER"));
        assertEquals("sys.key", node.getConfigKey());
        assertEquals("NUMBER", node.getConfigType());
        verify(audit).record(eq("system.config.update"), any());
    }

    @Test
    void update_keyChangeToExisting_throwsConflict() {
        when(configMapper.selectById(1L)).thenReturn(entity(1L, "sys.key", 0, 0));
        when(configMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.update(1L, req("sys.other", "STRING")));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void update_notFound_throwsNotFound() {
        when(configMapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.update(99L, req("sys.key", "STRING")));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
    }

    @Test
    void delete_success_auditsAndReturnsOk() {
        when(configMapper.selectById(1L)).thenReturn(entity(1L, "sys.key", 0, 0));
        when(configMapper.deleteById(1L)).thenReturn(1);

        DeleteResult d = service.delete(1L);
        assertTrue(d.getOk());
        verify(audit).record(eq("system.config.delete"), any());
    }

    @Test
    void delete_builtIn_throwsConflict() {
        when(configMapper.selectById(1L)).thenReturn(entity(1L, "sys.key", 1, 0));
        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(configMapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(99L));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
    }
}
