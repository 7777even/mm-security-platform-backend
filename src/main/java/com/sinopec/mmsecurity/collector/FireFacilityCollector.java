package com.sinopec.mmsecurity.collector;

import com.sinopec.mmsecurity.config.FireFacilityCollectorProperties;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorReportItem;
import com.sinopec.mmsecurity.dto.FireFacilityMonitorReportRequest;
import com.sinopec.mmsecurity.service.FireFacilityService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消防监测采集器：按配置周期从 {@link FireFacilitySourceAdapter} 拉取真实设备快照，
 * 复用 {@code FireFacilityService.reportMonitors} 落库并触发大屏实时刷新。
 *
 * <p>默认关闭（{@code fire-facility.collector.enabled=false}）；需真实采集时在 application.yml
 * 开启并配置 {@code upstream}，或实现 {@link FireFacilitySourceAdapter} 对接真实源。</p>
 *
 * <p>容错：适配器读取异常、返回空、或上报落库异常均被捕获并记录日志，绝不中断调度或向监测表写入假数据。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fire-facility.collector.enabled", havingValue = "true")
public class FireFacilityCollector {

    private final FireFacilityCollectorProperties props;
    private final FireFacilitySourceAdapter adapter;
    private final FireFacilityService fireFacilityService;

    @PostConstruct
    public void banner() {
        log.info("[fire-facility-collector] 采集器已启用：cron={}，mode={}", props.getCron(), props.getMode());
    }

    @Scheduled(cron = "${fire-facility.collector.cron:0 0/1 * * * ?}")
    public void collect() {
        List<FireFacilityMonitorReportItem> items;
        try {
            items = adapter.fetchSnapshot();
        } catch (Exception e) {
            log.error("[fire-facility-collector] 采集源读取失败，跳过本次：{}", e.getMessage(), e);
            return;
        }
        if (items == null || items.isEmpty()) {
            log.debug("[fire-facility-collector] 采集源无数据，跳过本次上报");
            return;
        }
        FireFacilityMonitorReportRequest req = new FireFacilityMonitorReportRequest();
        req.setItems(items);
        try {
            fireFacilityService.reportMonitors(req);
            log.info("[fire-facility-collector] 采集上报 {} 项 → fac_fire_facility_monitor", items.size());
        } catch (Exception e) {
            log.error("[fire-facility-collector] 采集上报落库失败：{}", e.getMessage(), e);
        }
    }
}
