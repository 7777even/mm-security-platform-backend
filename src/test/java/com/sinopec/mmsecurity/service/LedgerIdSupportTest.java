package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.entity.FacRescuePersonnel;
import com.sinopec.mmsecurity.mapper.FacRescuePersonnelMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 台账主键 / 排序号分配器校验（纯 Mockito）。
 *
 * <p>覆盖「显式插 id 让自增序列滞后」这一坑的两条兜底路径：
 * 有历史行时 max+1、空表（或 id 为 null）时从 1 起。
 */
@ExtendWith(MockitoExtension.class)
class LedgerIdSupportTest {

    @Mock
    private FacRescuePersonnelMapper mapper;

    @Test
    void nextId_returnsMaxPlusOne() {
        FacRescuePersonnel last = new FacRescuePersonnel();
        last.setId(9L);
        when(mapper.selectOne(any())).thenReturn(last);

        assertEquals(10L,
                LedgerIdSupport.nextId(mapper, FacRescuePersonnel::getId, FacRescuePersonnel::getId));
    }

    @Test
    void nextId_emptyTableStartsFromOne() {
        when(mapper.selectOne(any())).thenReturn(null);

        assertEquals(1L,
                LedgerIdSupport.nextId(mapper, FacRescuePersonnel::getId, FacRescuePersonnel::getId));
    }

    @Test
    void nextId_nullIdTreatedAsEmptyTable() {
        when(mapper.selectOne(any())).thenReturn(new FacRescuePersonnel());

        assertEquals(1L,
                LedgerIdSupport.nextId(mapper, FacRescuePersonnel::getId, FacRescuePersonnel::getId));
    }

    @Test
    void nextSortNo_returnsMaxPlusOne() {
        FacRescuePersonnel last = new FacRescuePersonnel();
        last.setSortNo(7);
        when(mapper.selectOne(any())).thenReturn(last);

        assertEquals(8,
                LedgerIdSupport.nextSortNo(mapper, FacRescuePersonnel::getSortNo, FacRescuePersonnel::getSortNo));
    }

    @Test
    void nextSortNo_emptyTableStartsFromOne() {
        when(mapper.selectOne(any())).thenReturn(null);

        assertEquals(1,
                LedgerIdSupport.nextSortNo(mapper, FacRescuePersonnel::getSortNo, FacRescuePersonnel::getSortNo));
    }
}
