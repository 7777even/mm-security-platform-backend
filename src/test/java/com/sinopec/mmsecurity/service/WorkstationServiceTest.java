package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.Workstation;
import com.sinopec.mmsecurity.entity.FacWorkstation;
import com.sinopec.mmsecurity.mapper.FacWorkstationMapper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * WorkstationService（纯 Mockito，不启动 Spring 上下文）：
 * 分页 + zone/online 筛选 + data_scope 行级 ABAC 防区过滤。
 * 镜像 DeviceServiceTest 的断言风格；resolveZones() 三态（null/空/非空）全程覆盖。
 */
@ExtendWith(MockitoExtension.class)
class WorkstationServiceTest {

    @Mock
    private FacWorkstationMapper workstationMapper;

    @Mock
    private DataScopeResolver dataScopeResolver;

    @InjectMocks
    private WorkstationService service;

    private FacWorkstation ws(String id, String name, String zone, boolean online) {
        FacWorkstation w = new FacWorkstation();
        w.setWorkstationId(id);
        w.setName(name);
        w.setZone(zone);
        w.setOnline(online);
        return w;
    }

    private Page<FacWorkstation> rawPage(List<FacWorkstation> records, long total) {
        Page<FacWorkstation> p = new Page<>();
        p.setRecords(records);
        p.setTotal(total);
        p.setCurrent(1);
        p.setSize(20);
        return p;
    }

    @Test
    void page_allZones_whenResolverReturnsNull() {
        when(dataScopeResolver.resolveZones()).thenReturn(null);
        when(workstationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(rawPage(List.of(
                        ws("WS-01", "中控室工位-01", "罐区A", true),
                        ws("WS-02", "罐区值班室工位", "化工区", false)), 2L));

        Page<Workstation> p = service.page(1, 20, null, null);
        assertEquals(2, p.getRecords().size());
        assertEquals("WS-01", p.getRecords().get(0).getId());
        assertEquals(true, p.getRecords().get(0).isOnline());
        assertEquals(2, p.getTotal());
    }

    @Test
    void page_scoped_onlyReturnsZoneSubset() {
        when(dataScopeResolver.resolveZones()).thenReturn(Set.of("罐区A"));
        when(workstationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(rawPage(List.of(ws("WS-01", "中控室工位-01", "罐区A", true)), 1L));

        Page<Workstation> p = service.page(1, 20, null, null);
        assertEquals(1, p.getRecords().size());
        assertEquals("罐区A", p.getRecords().get(0).getZone());
        assertEquals(1, p.getTotal());
    }

    @Test
    void page_emptyZoneScope_appliesOneEqualsZero() {
        when(dataScopeResolver.resolveZones()).thenReturn(Set.of());
        ArgumentCaptor<LambdaQueryWrapper<FacWorkstation>> cap =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        when(workstationMapper.selectPage(any(Page.class), cap.capture()))
                .thenReturn(rawPage(List.of(), 0L));

        Page<Workstation> p = service.page(1, 20, null, null);
        assertEquals(0, p.getRecords().size());
        assertTrue(cap.getValue().getCustomSqlSegment().contains("1=0"),
                "空防区集合应注入 1=0 最小权限兜底");
    }

    @Test
    void page_zoneLike_and_onlineFilter_applied() {
        when(dataScopeResolver.resolveZones()).thenReturn(null);
        ArgumentCaptor<LambdaQueryWrapper<FacWorkstation>> cap =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        when(workstationMapper.selectPage(any(Page.class), cap.capture()))
                .thenReturn(rawPage(List.of(ws("WS-01", "A", "罐区A", true)), 1L));

        Page<Workstation> p = service.page(1, 20, "罐区", "true");
        assertEquals(1, p.getRecords().size());
        String sql = cap.getValue().getCustomSqlSegment();
        assertTrue(sql.contains("zone"), "应注入 zone like 条件: " + sql);
        assertTrue(sql.contains("online"), "应注入 online 条件: " + sql);
    }

    @Test
    void page_onlineFalse_filtersOffline() {
        when(dataScopeResolver.resolveZones()).thenReturn(null);
        ArgumentCaptor<LambdaQueryWrapper<FacWorkstation>> cap =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        when(workstationMapper.selectPage(any(Page.class), cap.capture()))
                .thenReturn(rawPage(List.of(ws("WS-02", "B", "化工区", false)), 1L));

        Page<Workstation> p = service.page(1, 20, null, "false");
        assertEquals(1, p.getRecords().size());
        assertTrue(cap.getValue().getCustomSqlSegment().contains("online"),
                "online=false 应注入 online 条件");
    }
}
