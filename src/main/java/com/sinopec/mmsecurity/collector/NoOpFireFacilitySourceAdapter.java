package com.sinopec.mmsecurity.collector;

import com.sinopec.mmsecurity.dto.FireFacilityMonitorReportItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 默认采集源适配器：未配置真实采集源时使用。{@link #fetchSnapshot()} 始终返回空列表，
 * 使采集器保持「无副作用」——不写库、不造假。
 *
 * <p>接入真实采集源时移除本类（或让真实实现 {@code @Primary}），见 {@link FireFacilitySourceAdapter}。</p>
 */
@Slf4j
@Component
public class NoOpFireFacilitySourceAdapter implements FireFacilitySourceAdapter {

    private boolean warned = false;

    @Override
    public List<FireFacilityMonitorReportItem> fetchSnapshot() {
        if (!warned) {
            log.info("[fire-facility-collector] 未配置真实采集源（NoOpFireFacilitySourceAdapter），" +
                    "采集器将以空快照运行、不写库。如需真实设备状态自动持久化，请实现 FireFacilitySourceAdapter 并对接上游。");
            warned = true;
        }
        return Collections.emptyList();
    }
}
