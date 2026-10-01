package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.FireAlarmCreateRequest;
import com.sinopec.mmsecurity.dto.FireAlarmItem;
import com.sinopec.mmsecurity.dto.FireAlarmPageResult;
import com.sinopec.mmsecurity.dto.FireAlarmUpdateRequest;
import com.sinopec.mmsecurity.entity.FacFireAlarm;
import com.sinopec.mmsecurity.mapper.FacFireAlarmMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    /* ==================== 写回 update ==================== */

    private static FacFireAlarm existing(String alarmId) {
        FacFireAlarm e = new FacFireAlarm();
        e.setAlarmId(alarmId);
        e.setStatus("ACTIVE");
        e.setFalseAlarm("未核实");
        e.setTitle("蜡油加氢装置火灾");
        e.setVersion(0L);
        return e;
    }

    @Test
    void update_statusOnly_persistsAndReturnsItem() {
        when(mapper.selectById("FA-1")).thenReturn(existing("FA-1"));
        FireAlarmUpdateRequest req = new FireAlarmUpdateRequest();
        req.setStatus("ACKED");

        FireAlarmItem item = service.update("FA-1", req);

        ArgumentCaptor<FacFireAlarm> captor = ArgumentCaptor.forClass(FacFireAlarm.class);
        verify(mapper).updateById(captor.capture());
        assertEquals("ACKED", captor.getValue().getStatus());
        assertEquals("未核实", captor.getValue().getFalseAlarm());
        assertEquals("ACKED", item.getStatus());
    }

    @Test
    void update_falseAlarmOnly_keepsStatus() {
        when(mapper.selectById("FA-2")).thenReturn(existing("FA-2"));
        FireAlarmUpdateRequest req = new FireAlarmUpdateRequest();
        req.setFalseAlarm("是");

        FireAlarmItem item = service.update("FA-2", req);

        ArgumentCaptor<FacFireAlarm> captor = ArgumentCaptor.forClass(FacFireAlarm.class);
        verify(mapper).updateById(captor.capture());
        assertEquals("ACTIVE", captor.getValue().getStatus());
        assertEquals("是", captor.getValue().getFalseAlarm());
        assertEquals("是", item.getFalseAlarm());
    }

    @Test
    void update_invalidStatus_throwsParamInvalid() {
        when(mapper.selectById("FA-3")).thenReturn(existing("FA-3"));
        FireAlarmUpdateRequest req = new FireAlarmUpdateRequest();
        req.setStatus("NOPE");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update("FA-3", req));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
        verify(mapper, never()).updateById(any());
    }

    @Test
    void update_notFound_throwsNotFound() {
        when(mapper.selectById("FA-X")).thenReturn(null);
        FireAlarmUpdateRequest req = new FireAlarmUpdateRequest();
        req.setStatus("ACKED");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update("FA-X", req));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(mapper, never()).updateById(any());
    }

    @Test
    void update_disposalFields_persistsAllFour() {
        when(mapper.selectById("FA-4")).thenReturn(existing("FA-4"));
        FireAlarmUpdateRequest req = new FireAlarmUpdateRequest();
        req.setHandleResult("已现场核实现场无明火，持续观察");
        req.setHandleTime("2026-08-20 10:30:00");
        req.setDispatchPersonnel("张三,李四");
        req.setNotifyMethod("APP,SMS");

        FireAlarmItem item = service.update("FA-4", req);

        ArgumentCaptor<FacFireAlarm> captor = ArgumentCaptor.forClass(FacFireAlarm.class);
        verify(mapper).updateById(captor.capture());
        FacFireAlarm saved = captor.getValue();
        assertEquals("已现场核实现场无明火，持续观察", saved.getHandleResult());
        assertEquals("2026-08-20 10:30:00", saved.getHandleTime());
        assertEquals("张三,李四", saved.getDispatchPersonnel());
        assertEquals("APP,SMS", saved.getNotifyMethod());
        assertEquals("已现场核实现场无明火，持续观察", item.getHandleResult());
        assertEquals("APP,SMS", item.getNotifyMethod());
    }

    @Test
    void update_disposalFields_nullSkipsUpdate() {
        FacFireAlarm e = existing("FA-5");
        e.setHandleResult("既有处置文本");
        when(mapper.selectById("FA-5")).thenReturn(e);
        FireAlarmUpdateRequest req = new FireAlarmUpdateRequest();
        req.setStatus("ACKED");

        service.update("FA-5", req);

        ArgumentCaptor<FacFireAlarm> captor = ArgumentCaptor.forClass(FacFireAlarm.class);
        verify(mapper).updateById(captor.capture());
        // 未传处置字段时不应覆盖既有值
        assertEquals("既有处置文本", captor.getValue().getHandleResult());
        assertEquals("ACKED", captor.getValue().getStatus());
    }

    /* ==================== 新增 create ==================== */

    @Test
    void create_roundTrip_echoesTitleTimeAndDefaultsActive() {
        FireAlarmCreateRequest req = new FireAlarmCreateRequest();
        req.setTitle("联动测试报警");
        req.setTime("2026-10-01 21:00:00");
        req.setTypeLabel("火灾报警");
        req.setTypeTone("fire");
        req.setLocation("化工区-测试");
        // status 不传 → 期望默认 ACTIVE

        FireAlarmItem item = service.create(req);

        ArgumentCaptor<FacFireAlarm> captor = ArgumentCaptor.forClass(FacFireAlarm.class);
        verify(mapper).insert(captor.capture());
        FacFireAlarm saved = captor.getValue();
        assertNotNull(saved.getAlarmId());
        assertTrue(saved.getAlarmId().startsWith("FA-"), "alarmId 应以 FA- 前缀");
        assertEquals("ACTIVE", saved.getStatus());
        assertEquals(0L, saved.getVersion());
        // 回读逐字段一致
        assertEquals("联动测试报警", item.getTitle());
        assertEquals("2026-10-01 21:00:00", item.getTime());
        assertEquals("ACTIVE", item.getStatus());
        assertEquals(saved.getAlarmId(), item.getAlarmId());
    }

    @Test
    void create_invalidStatus_throwsParamInvalid() {
        FireAlarmCreateRequest req = new FireAlarmCreateRequest();
        req.setTitle("联动测试报警");
        req.setTime("2026-10-01 21:00:00");
        req.setStatus("BOGUS");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
        verify(mapper, never()).insert(any());
    }

    /* ==================== 删除 delete ==================== */

    @Test
    void delete_missing_throwsNotFound() {
        when(mapper.selectById("FA-GONE")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete("FA-GONE"));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(mapper, never()).deleteById(any(Serializable.class));
    }

    @Test
    void delete_existing_succeeds() {
        when(mapper.selectById("FA-DEL")).thenReturn(existing("FA-DEL"));

        service.delete("FA-DEL");

        verify(mapper).deleteById((Serializable) "FA-DEL");
    }

    /* ==================== 全字段 update 回读 ==================== */

    @Test
    void update_fullFieldsRoundTrip() {
        when(mapper.selectById("FA-FULL")).thenReturn(existing("FA-FULL"));
        FireAlarmUpdateRequest req = new FireAlarmUpdateRequest();
        req.setTypeLabel("烟雾报警");
        req.setTypeTone("smoke");
        req.setSource("消防主机");
        req.setObjectType("储罐");
        req.setObjectName("汽油储罐T-01");
        req.setLevel("高");
        req.setDescription("烟雾浓度超标");
        req.setLocation("罐区-测试");
        req.setTime("2026-10-01 21:05:00");
        req.setFalseAlarm("否");
        req.setStatus("ACKED");
        req.setRescueEventId("RE-1");
        req.setMonitorId("M-1");
        req.setMonitorLabel("监控点A");
        req.setOnsiteMonitorId("OM-1");
        req.setOnsiteMonitorLabel("现场监控B");
        req.setTitle("全字段回读");
        req.setHandleResult("已核实现场无明火");
        req.setHandleTime("2026-10-01 21:30:00");
        req.setDispatchPersonnel("张三,李四");
        req.setNotifyMethod("APP,SMS");

        FireAlarmItem item = service.update("FA-FULL", req);

        ArgumentCaptor<FacFireAlarm> captor = ArgumentCaptor.forClass(FacFireAlarm.class);
        verify(mapper).updateById(captor.capture());
        FacFireAlarm saved = captor.getValue();
        assertEquals("烟雾报警", saved.getTypeLabel());
        assertEquals("smoke", saved.getTypeTone());
        assertEquals("消防主机", saved.getSource());
        assertEquals("储罐", saved.getObjectType());
        assertEquals("汽油储罐T-01", saved.getObjectName());
        assertEquals("高", saved.getLevel());
        assertEquals("烟雾浓度超标", saved.getDescription());
        assertEquals("罐区-测试", saved.getLocation());
        assertEquals("2026-10-01 21:05:00", saved.getTime());
        assertEquals("否", saved.getFalseAlarm());
        assertEquals("ACKED", saved.getStatus());
        assertEquals("RE-1", saved.getRescueEventId());
        assertEquals("M-1", saved.getMonitorId());
        assertEquals("监控点A", saved.getMonitorLabel());
        assertEquals("OM-1", saved.getOnsiteMonitorId());
        assertEquals("现场监控B", saved.getOnsiteMonitorLabel());
        assertEquals("全字段回读", saved.getTitle());
        assertEquals("已核实现场无明火", saved.getHandleResult());
        assertEquals("2026-10-01 21:30:00", saved.getHandleTime());
        assertEquals("张三,李四", saved.getDispatchPersonnel());
        assertEquals("APP,SMS", saved.getNotifyMethod());
        // 回读 item 与落库实体逐字段一致
        assertEquals(saved.getTypeLabel(), item.getTypeLabel());
        assertEquals(saved.getTypeTone(), item.getTypeTone());
        assertEquals(saved.getSource(), item.getSource());
        assertEquals(saved.getObjectType(), item.getObjectType());
        assertEquals(saved.getObjectName(), item.getObjectName());
        assertEquals(saved.getLevel(), item.getLevel());
        assertEquals(saved.getDescription(), item.getDescription());
        assertEquals(saved.getLocation(), item.getLocation());
        assertEquals(saved.getTime(), item.getTime());
        assertEquals(saved.getFalseAlarm(), item.getFalseAlarm());
        assertEquals(saved.getStatus(), item.getStatus());
        assertEquals(saved.getRescueEventId(), item.getRescueEventId());
        assertEquals(saved.getMonitorId(), item.getMonitorId());
        assertEquals(saved.getMonitorLabel(), item.getMonitorLabel());
        assertEquals(saved.getOnsiteMonitorId(), item.getOnsiteMonitorId());
        assertEquals(saved.getOnsiteMonitorLabel(), item.getOnsiteMonitorLabel());
        assertEquals(saved.getTitle(), item.getTitle());
        assertEquals(saved.getHandleResult(), item.getHandleResult());
        assertEquals(saved.getHandleTime(), item.getHandleTime());
        assertEquals(saved.getDispatchPersonnel(), item.getDispatchPersonnel());
        assertEquals(saved.getNotifyMethod(), item.getNotifyMethod());
    }
}
