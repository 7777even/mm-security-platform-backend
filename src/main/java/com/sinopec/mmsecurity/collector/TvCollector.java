package com.sinopec.mmsecurity.collector;

import com.sinopec.mmsecurity.config.TvCollectorProperties;
import com.sinopec.mmsecurity.dto.TvSnapshotIngestRequest;
import com.sinopec.mmsecurity.service.TvService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 工业电视采集器：按配置周期从 {@link TvSourceAdapter} 拉取真实设备录像截图帧，
 * 复用 {@code TvService.submitSnapshots} 批量落库 fac_tv_snapshot 并经 {@code @RealtimeSync}
 * 广播 tv.snapshot.changed，触发大屏实时刷新上屏。
 *
 * <p>默认关闭（{@code tv.collector.enabled=false}）；需真实采集时在 application.yml
 * 开启并配置 {@code upstream}，或实现 {@link TvSourceAdapter} 对接真实源。</p>
 *
 * <p>容错：适配器读取异常、返回空、或上报落库异常均被捕获并记录日志，绝不中断调度或向截图表写入假数据；
 * monitorCode / imageBase64 任一项为空的无效项被跳过（避免单条脏数据中断整批）。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "tv.collector.enabled", havingValue = "true")
public class TvCollector {

    private final TvCollectorProperties props;
    private final TvSourceAdapter adapter;
    private final TvService tvService;

    @PostConstruct
    public void banner() {
        log.info("[tv-collector] 采集器已启用：cron={}，mode={}", props.getCron(), props.getMode());
    }

    @Scheduled(cron = "${tv.collector.cron:0 0/1 * * * ?}")
    public void collect() {
        List<TvSnapshotIngestRequest> items;
        try {
            items = adapter.fetchSnapshots();
        } catch (Exception e) {
            log.error("[tv-collector] 采集源读取失败，跳过本次：{}", e.getMessage(), e);
            return;
        }
        if (items == null || items.isEmpty()) {
            log.debug("[tv-collector] 采集源无数据，跳过本次上报");
            return;
        }
        List<TvSnapshotIngestRequest> valid = items.stream()
                .filter(i -> i != null
                        && i.getMonitorCode() != null && !i.getMonitorCode().isBlank()
                        && i.getImageBase64() != null && !i.getImageBase64().isBlank())
                .toList();
        int skipped = items.size() - valid.size();
        if (skipped > 0) {
            log.warn("[tv-collector] 跳过 {} 条缺 monitorCode/imageBase64 的无效上报", skipped);
        }
        if (valid.isEmpty()) {
            return;
        }
        try {
            int n = tvService.submitSnapshots(valid);
            log.info("[tv-collector] 采集上报 {} 项 → fac_tv_snapshot（广播 tv.snapshot.changed）", n);
        } catch (Exception e) {
            log.error("[tv-collector] 采集上报落库失败：{}", e.getMessage(), e);
        }
    }
}
