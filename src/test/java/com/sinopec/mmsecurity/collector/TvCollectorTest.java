package com.sinopec.mmsecurity.collector;

import com.sinopec.mmsecurity.config.TvCollectorProperties;
import com.sinopec.mmsecurity.dto.TvSnapshotIngestRequest;
import com.sinopec.mmsecurity.service.TvService;
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

/** 工业电视采集器逻辑校验（纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class TvCollectorTest {

    @Mock
    private TvCollectorProperties props;
    @Mock
    private TvSourceAdapter adapter;
    @Mock
    private TvService tvService;

    @InjectMocks
    private TvCollector collector;

    @Test
    void collect_skipsWhenAdapterReturnsEmpty() {
        when(adapter.fetchSnapshots()).thenReturn(Collections.emptyList());
        collector.collect();
        verify(tvService, never()).submitSnapshots(any());
    }

    @Test
    void collect_submitsValidItems() {
        TvSnapshotIngestRequest r = new TvSnapshotIngestRequest();
        r.setMonitorCode("ar-01");
        r.setImageBase64("data:image/jpeg;base64,/9j/4AAQSkZJRg==");
        when(adapter.fetchSnapshots()).thenReturn(List.of(r));
        when(tvService.submitSnapshots(any())).thenReturn(1);

        collector.collect();

        verify(tvService, times(1)).submitSnapshots(any());
    }

    @Test
    void collect_skipsInvalidItems() {
        TvSnapshotIngestRequest bad = new TvSnapshotIngestRequest();
        bad.setMonitorCode("ar-02");
        bad.setImageBase64(""); // 缺图，应被跳过
        when(adapter.fetchSnapshots()).thenReturn(List.of(bad));

        collector.collect();

        verify(tvService, never()).submitSnapshots(any());
    }
}
