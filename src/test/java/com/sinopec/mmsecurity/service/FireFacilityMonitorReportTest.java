package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorReportItem;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorReportParam;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorReportRequest;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorResult;
import com.sinopec.mmsecurity.entity.FacFireFacilityMonitor;
import com.sinopec.mmsecurity.entity.FacFireFacilityParam;
import com.sinopec.mmsecurity.mapper.FacFireFacilityFaultMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityFaultTimelineMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityLedgerMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityMaintenanceMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityOptionMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityParamMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** FireFacilityService.reportMonitors upsert 逻辑（纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class FireFacilityMonitorReportTest {

    @Mock
    private FacFireFacilityMonitorMapper monitorMapper;
    @Mock
    private FacFireFacilityParamMapper paramMapper;
    @Mock
    private FacFireFacilityLedgerMapper ledgerMapper;
    @Mock
    private FacFireFacilityMaintenanceMapper maintenanceMapper;
    @Mock
    private FacFireFacilityFaultMapper faultMapper;
    @Mock
    private FacFireFacilityFaultTimelineMapper timelineMapper;
    @Mock
    private FacFireFacilityOptionMapper optionMapper;

    @InjectMocks
    private FireFacilityService service;

    private FireFacilityMonitorReportRequest singleItemRequest(
            FireFacilityMonitorReportItem item) {
        FireFacilityMonitorReportRequest req = new FireFacilityMonitorReportRequest();
        req.setItems(List.of(item));
        return req;
    }

    @Test
    void reportMonitors_updatesExistingAndReplacesParams() {
        FacFireFacilityMonitor row = new FacFireFacilityMonitor();
        row.setId(2L);
        row.setKeyCode("water");
        row.setFacilityType("消防水源");
        row.setTotalCount(46);
        row.setOnlineCount(44);
        row.setOfflineCount(1);
        row.setFaultCount(1);
        row.setMonitorStatus("告警");
        row.setSortNo(2);

        // 同一实例既用于 selectOne（命中）又用于 selectList（monitors 返回），
        // 因此内存中的字段更新会直接反映到返回的 summary 上。
        when(monitorMapper.selectOne(any())).thenReturn(row);
        when(monitorMapper.selectList(any())).thenReturn(List.of(row));
        when(paramMapper.selectList(any())).thenReturn(List.of());
        when(optionMapper.selectList(any())).thenReturn(List.of());
        when(monitorMapper.updateById(any())).thenReturn(1);
        when(paramMapper.delete(any())).thenReturn(1);
        when(paramMapper.insert(any())).thenReturn(1);

        FireFacilityMonitorReportItem item = new FireFacilityMonitorReportItem();
        item.setKey("water");
        item.setTotal(50);
        item.setStatus("正常");
        FireFacilityMonitorReportParam p = new FireFacilityMonitorReportParam();
        p.setLabel("水位");
        p.setValue("40%");
        p.setTone("normal");
        item.setParams(List.of(p));

        FireFacilityMonitorResult result = service.reportMonitors(singleItemRequest(item));

        verify(monitorMapper).updateById(row);
        verify(paramMapper).delete(any());
        verify(paramMapper, times(1)).insert(any());
        assertEquals(50, row.getTotalCount());
        assertEquals("正常", row.getMonitorStatus());
        assertNotNull(row.getLastReportTime());
        assertEquals(50, result.getItems().get(0).getTotal());
    }

    @Test
    void reportMonitors_insertsNewWhenKeyMissing() {
        when(monitorMapper.selectOne(any())).thenReturn(null);
        when(monitorMapper.selectList(any())).thenReturn(List.of());
        when(paramMapper.selectList(any())).thenReturn(List.of());
        when(optionMapper.selectList(any())).thenReturn(List.of());
        when(monitorMapper.insert(any())).thenReturn(1);

        FireFacilityMonitorReportItem item = new FireFacilityMonitorReportItem();
        item.setKey("newtype");
        item.setFacilityType("消防电源");
        item.setTotal(10);

        FireFacilityMonitorResult result = service.reportMonitors(singleItemRequest(item));

        verify(monitorMapper).insert(any());
        assertNotNull(result);
    }

    @Test
    void reportMonitors_rejectsBlankKey() {
        FireFacilityMonitorReportItem item = new FireFacilityMonitorReportItem();
        item.setKey("  ");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.reportMonitors(singleItemRequest(item)));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void reportMonitors_rejectsIllegalStatus() {
        FireFacilityMonitorReportItem item = new FireFacilityMonitorReportItem();
        item.setKey("newtype");
        item.setFacilityType("消防电源");
        item.setStatus("爆炸"); // 非法状态

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.reportMonitors(singleItemRequest(item)));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
    }
}
