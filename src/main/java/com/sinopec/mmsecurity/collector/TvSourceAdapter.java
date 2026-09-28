package com.sinopec.mmsecurity.collector;

import com.sinopec.mmsecurity.dto.TvSnapshotIngestRequest;

import java.util.List;

/**
 * 工业电视数据源适配器。采集器（{@link TvCollector}）按周期调用 {@link #fetchSnapshots()}
 * 获取真实设备录像截图帧，再复用上报写链路落库。
 *
 * <p><b>接入真实采集源时：</b>新增一个实现本接口、标注 {@code @Component} 的类（并移除/替换
 * {@link NoOpTvSourceAdapter}，或为本实现加 {@code @Primary}），在 {@code fetchSnapshots()}
 * 中对接真实视频平台 / NVR / 流媒体网关（HTTP、RTSP 取帧、SDK 等），把上游数据映射为
 * {@link TvSnapshotIngestRequest}（monitorCode 须对应 fac_tv_monitor.monitor_code，
 * imageBase64 为 JPEG 帧；两项任一为空将被采集器跳过）。</p>
 *
 * <p><b>本接口不负责造假数据</b>：无真实源时 {@code fetchSnapshots()} 返回空列表，采集器据此
 * 跳过本次上报，截图表保持种子 / 设备自助上报值。</p>
 */
public interface TvSourceAdapter {

    /**
     * 拉取一次工业电视录像截图快照。
     *
     * @return 上报请求列表；无数据（未配置真实源 / 上游暂无可上报项）返回空列表
     */
    List<TvSnapshotIngestRequest> fetchSnapshots();
}
