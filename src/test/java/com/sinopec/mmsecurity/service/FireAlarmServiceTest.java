package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.FireAlarmItem;
import com.sinopec.mmsecurity.dto.FireAlarmPageResult;
import com.sinopec.mmsecurity.entity.FacFireAlarm;
import com.sinopec.mmsecurity.mapper.FacFireAlarmMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * FireAlarmService（纯 Mockito，不启动 Spring 上下文）：
 * 分页委托 Mapper、字段映射、PageResult 的 list/total/page/size 组装。
 */
class FireAlarmServiceTest {

    private final FacFireAlarmMapper mapper = mock(FacFireAlarmMapper.class);
    private final FireAlarmService service = new FireAlarmService(mapper);

    @Test
    void page_assemblesPageResultAndMapsFields() {
        FacFireAlarm e = new FacFireAlarm();
        e.setAlarmId("FA-20260907-001");
        e.setTypeLabel("火灾报警");
        e.setTypeTone("fire");
        e.setObjectType("装置");
        e.setObjectName("蜡油加氢装置");
        e.setStatus("ACTIVE");
        e.setFalseAlarm("未核实");
        e.setTitle("蜡油加氢装置火灾");

        Page<FacFireAlarm> p = new Page<>(1, 10);
        p.setRecords(List.of(e));
        p.setTotal(16);
        p.setCurrent(1);
        p.setSize(10);
        when(mapper.selectPage(any(Page.class), isNull())).thenReturn(p);

        FireAlarmPageResult result = service.page(1, 10);

        assertEquals(16, result.getTotal());
        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getList().size());
        FireAlarmItem d = result.getList().get(0);
        assertEquals("FA-20260907-001", d.getAlarmId());
        assertEquals("火灾报警", d.getTypeLabel());
        assertEquals("ACTIVE", d.getStatus());
        assertEquals("蜡油加氢装置火灾", d.getTitle());
        assertEquals("未核实", d.getFalseAlarm());
    }

    @Test
    void page_emptyDb_returnsZeroTotal() {
        Page<FacFireAlarm> p = new Page<>(2, 10);
        p.setRecords(List.of());
        p.setTotal(0);
        p.setCurrent(2);
        p.setSize(10);
        when(mapper.selectPage(any(Page.class), isNull())).thenReturn(p);

        FireAlarmPageResult result = service.page(2, 10);
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getList().size());
    }
}
