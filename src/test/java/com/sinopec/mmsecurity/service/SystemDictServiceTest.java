package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.DictItemItem;
import com.sinopec.mmsecurity.dto.DictItemPageResult;
import com.sinopec.mmsecurity.dto.DictItemSaveRequest;
import com.sinopec.mmsecurity.dto.DictTypeItem;
import com.sinopec.mmsecurity.dto.DictTypePageResult;
import com.sinopec.mmsecurity.dto.DictTypeSaveRequest;
import com.sinopec.mmsecurity.entity.SysDictItem;
import com.sinopec.mmsecurity.entity.SysDictType;
import com.sinopec.mmsecurity.mapper.SysDictItemMapper;
import com.sinopec.mmsecurity.mapper.SysDictTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemDictServiceTest {

    @Mock
    private SysDictTypeMapper dictTypeMapper;
    @Mock
    private SysDictItemMapper dictItemMapper;
    @Mock
    private SystemAuditHelper audit;

    @InjectMocks
    private SystemDictService service;

    @BeforeEach
    void setUp() {
        // @PostConstruct init() 在纯 Mockito 下不触发，手动注入 Caffeine 缓存
        service.init();
    }

    private static DictTypeSaveRequest typeReq(String code) {
        DictTypeSaveRequest r = new DictTypeSaveRequest();
        r.setDictCode(code);
        r.setDictName("名称");
        r.setStatus(1);
        return r;
    }

    private static DictItemSaveRequest itemReq(String code, String value) {
        DictItemSaveRequest r = new DictItemSaveRequest();
        r.setDictCode(code);
        r.setItemValue(value);
        r.setItemLabel("标签");
        r.setSortOrder(0);
        r.setStatus(1);
        return r;
    }

    @Test
    void typePage_returnsPagedTypes() {
        SysDictType t = new SysDictType();
        t.setId(1L);
        t.setDictCode("YESNO");
        Page<SysDictType> p = new Page<>(1, 10);
        p.setRecords(List.of(t));
        p.setTotal(1);
        when(dictTypeMapper.selectPage(any(), any())).thenReturn(p);

        DictTypePageResult r = service.typePage(1, 10, null);
        assertEquals(1, r.getList().size());
        assertEquals("YESNO", r.getList().get(0).getDictCode());
    }

    @Test
    void createType_success() {
        when(dictTypeMapper.countDictCodeIncludingDeleted(anyString(), anyLong())).thenReturn(0L);
        when(dictTypeMapper.insert(any())).thenReturn(1);
        DictTypeItem item = service.createType(typeReq("YESNO"));
        assertNotNull(item);
        assertEquals("yesno", item.getDictCode());
    }

    @Test
    void createType_duplicateCode_throwsConflict() {
        when(dictTypeMapper.countDictCodeIncludingDeleted(anyString(), anyLong())).thenReturn(1L);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.createType(typeReq("YESNO")));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void updateType_builtInCodeChange_forbidden() {
        SysDictType t = new SysDictType();
        t.setId(1L);
        t.setDictCode("YESNO");
        t.setBuiltIn(1);
        when(dictTypeMapper.selectById(1L)).thenReturn(t);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateType(1L, typeReq("OTHER")));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void updateType_codeChange_migratesItems() {
        SysDictType t = new SysDictType();
        t.setId(1L);
        t.setDictCode("YESNO");
        t.setBuiltIn(0);
        when(dictTypeMapper.selectById(1L)).thenReturn(t);
        SysDictItem child = new SysDictItem();
        child.setId(2L);
        when(dictItemMapper.selectList(any())).thenReturn(List.of(child));
        when(dictItemMapper.updateById(any())).thenReturn(1);
        DictTypeItem item = service.updateType(1L, typeReq("NEWP"));
        assertNotNull(item);
        verify(dictItemMapper).updateById(any());
    }

    @Test
    void deleteType_builtIn_forbidden() {
        SysDictType t = new SysDictType();
        t.setId(1L);
        t.setDictCode("YESNO");
        t.setBuiltIn(1);
        when(dictTypeMapper.selectById(1L)).thenReturn(t);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteType(1L));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void deleteType_hasItems_conflict() {
        SysDictType t = new SysDictType();
        t.setId(1L);
        t.setDictCode("YESNO");
        t.setBuiltIn(0);
        when(dictTypeMapper.selectById(1L)).thenReturn(t);
        when(dictItemMapper.selectCount(any())).thenReturn(2L);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteType(1L));
        assertEquals(ResultCode.CONFLICT, ex.getCode());
    }

    @Test
    void itemPage_emptyDictCode_throws() {
        BusinessException ex = assertThrows(BusinessException.class, () -> service.itemPage(1, 10, "  "));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void itemPage_normal_returns() {
        SysDictItem i = new SysDictItem();
        i.setId(1L);
        i.setDictCode("YESNO");
        Page<SysDictItem> p = new Page<>(1, 10);
        p.setRecords(List.of(i));
        p.setTotal(1);
        when(dictItemMapper.selectPage(any(), any())).thenReturn(p);
        DictItemPageResult r = service.itemPage(1, 10, "YESNO");
        assertEquals(1, r.getList().size());
    }

    @Test
    void createItem_success() {
        when(dictTypeMapper.selectCount(any())).thenReturn(1L);
        when(dictItemMapper.insert(any())).thenReturn(1);
        DictItemItem item = service.createItem(itemReq("YESNO", "1"));
        assertNotNull(item);
        assertEquals("1", item.getItemValue());
    }

    @Test
    void createItem_typeMissing_throws() {
        when(dictTypeMapper.selectCount(any())).thenReturn(0L);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.createItem(itemReq("MISS", "1")));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void updateItem_notFound_throws() {
        when(dictItemMapper.selectById(1L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateItem(1L, itemReq("YESNO", "1")));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
    }

    @Test
    void deleteItem_normal_success() {
        SysDictItem i = new SysDictItem();
        i.setId(1L);
        i.setDictCode("YESNO");
        when(dictItemMapper.selectById(1L)).thenReturn(i);
        when(dictItemMapper.deleteById(1L)).thenReturn(1);
        DeleteResult d = service.deleteItem(1L);
        assertTrue(d.getOk());
    }

    @Test
    void options_returnsEnabledItemsFromCache() {
        SysDictItem i = new SysDictItem();
        i.setId(1L);
        i.setDictCode("YESNO");
        i.setItemValue("1");
        i.setStatus(1);
        when(dictItemMapper.selectList(any())).thenReturn(List.of(i));
        List<DictItemItem> opts = service.options("YESNO");
        assertEquals(1, opts.size());
        assertEquals("1", opts.get(0).getItemValue());
    }
}
