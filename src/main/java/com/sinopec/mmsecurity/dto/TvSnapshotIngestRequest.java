package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 工业电视录像截图采集入库请求（设备/采集端上报）。 */
@Data
public class TvSnapshotIngestRequest {

    /** 监控点位编码（关联 fac_tv_monitor.monitor_code）。 */
    @NotBlank
    private String monitorCode;

    /** 点位名称（可选；缺省时后端按 monitor_code 回查 fac_tv_monitor）。 */
    private String monitorName;

    /** 采集时刻（设备上报，字符串避免时区/方言差异）；缺省用服务端入库时刻。 */
    private String captureTime;

    /** 事件类型：人员闯入/烟火检测/区域入侵/手动抓拍；缺省=设备自动。 */
    private String eventType;

    /** base64 JPEG（可带 data:image/jpeg;base64, 前缀，后端自动剥离）。 */
    @NotBlank
    private String imageBase64;

    /** 来源：DEVICE 设备采集 / MANUAL 手工；缺省 DEVICE。 */
    private String source;

    /**
     * 关联告警 id（可选，跨域联动：将该抓拍绑定到具体告警）。
     * 由采集端/调用方在已知告警上下文时上送，使生产告警详情可精准内嵌关联抓拍。
     */
    private Long alarmId;

    /**
     * 关联告警类型（可选）：PRODUCTION 生产 / FIRE 消防 / PERIMETER 周界。
     * 与 alarm_id 配套，区分多告警域来源。
     */
    private String alarmType;
}
