package com.sinopec.mmsecurity.collector;

import com.sinopec.mmsecurity.dto.FireFacilityMonitorReportItem;

import java.util.List;

/**
 * 消防监测数据源适配器。采集器（{@link FireFacilityCollector}）按周期调用 {@link #fetchSnapshot()}
 * 获取真实设备状态快照，再复用上报写链路落库。
 *
 * <p><b>接入真实采集源时：</b>新增一个实现本接口、标注 {@code @Component} 的类（并移除/替换
 * {@link NoOpFireFacilitySourceAdapter}，或为本实现加 {@code @Primary}），在 {@code fetchSnapshot()}
 * 中对接真实消防主机 / 物联网平台 / SCADA（HTTP、OPC-UA、Modbus 等），把上游数据映射为
 * {@link FireFacilityMonitorReportItem}（key 须为 fac_fire_facility_monitor.key_code）。
 * 采集器会按 {@code fire-facility.collector.cron} 周期触发，并复用
 * {@code FireFacilityService.reportMonitors} 落库 + 触发大屏实时刷新。</p>
 *
 * <p><b>本接口不负责造假数据</b>：无真实源时 {@code fetchSnapshot()} 返回空列表（或 null），
 * 采集器据此跳过本次上报，监测表保持种子 / 上次真实上报值。</p>
 */
public interface FireFacilitySourceAdapter {

    /**
     * 拉取一次消防监测快照。
     *
     * @return 上报分项列表；无数据（未配置真实源 / 上游暂无可上报项）返回空列表或 null，采集器将跳过
     */
    List<FireFacilityMonitorReportItem> fetchSnapshot();
}
