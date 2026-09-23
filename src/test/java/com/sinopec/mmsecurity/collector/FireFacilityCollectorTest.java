package com.sinopec.mmsecurity.collector;

import com.sinopec.mmsecurity.config.FireFacilityCollectorProperties;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorReportItem;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorReportRequest;
import com.sinopec.mmsecurity.service.FireFacilityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** FireFacilityCollector 采集调度行为（纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class FireFacilityCollectorTest {

    @Mock
    FireFacilityCollectorProperties props;
    @Mock
    FireFacilitySourceAdapter adapter;
    @Mock
    FireFacilityService fireFacilityService;

    @InjectMocks
    FireFacilityCollector collector;

    @Test
    void collect_whenAdapterEmpty_doesNotCallReport() {
        when(adapter.fetchSnapshot()).thenReturn(Collections.emptyList());

        collector.collect();

        verify(fireFacilityService, never()).reportMonitors(any());
    }

    @Test
    void collect_whenAdapterNull_doesNotCallReport() {
        when(adapter.fetchSnapshot()).thenReturn(null);

        collector.collect();

        verify(fireFacilityService, never()).reportMonitors(any());
    }

    @Test
    void collect_whenAdapterReturnsItems_callsReportOnce() {
        FireFacilityMonitorReportItem item = new FireFacilityMonitorReportItem();
        item.setKey("fas");
        item.setTotal(100);
        item.setOnline(95);
        item.setOffline(5);
        item.setStatus("正常");
        when(adapter.fetchSnapshot()).thenReturn(List.of(item));

        collector.collect();

        verify(fireFacilityService, times(1)).reportMonitors(any(FireFacilityMonitorReportRequest.class));
    }

    @Test
    void collect_whenAdapterThrows_skipsReport() {
        when(adapter.fetchSnapshot()).thenThrow(new RuntimeException("upstream down"));

        collector.collect();

        verify(fireFacilityService, never()).reportMonitors(any());
    }
}
