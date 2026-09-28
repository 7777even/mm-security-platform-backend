package com.sinopec.mmsecurity.collector;

import com.sinopec.mmsecurity.dto.TvSnapshotIngestRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 工业电视数据源空适配器（默认）。未接入真实采集源时返回空列表，
 * 采集器据此跳过上报，绝不向截图表写入假数据。
 *
 * <p>接入真实源时新增 {@code @Primary} 实现并替换本类即可（详见 {@link TvSourceAdapter}）。</p>
 */
@Component
public class NoOpTvSourceAdapter implements TvSourceAdapter {

    @Override
    public List<TvSnapshotIngestRequest> fetchSnapshots() {
        return Collections.emptyList();
    }
}
