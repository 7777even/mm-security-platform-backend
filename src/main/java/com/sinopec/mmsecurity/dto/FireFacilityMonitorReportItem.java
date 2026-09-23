package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消防设施监测运行数据上报 - 单分项条目。
 *
 * <p>key 为分项唯一键（对应 fac_fire_facility_monitor.key_code）。
 * 计数/状态/最近上报时间/参数均为可选：命中已有 key 时仅覆盖传入字段（read-modify-write），
 * 未命中则按 facilityType 新建。params 传入即整体替换该分项参数。</p>
 */
@Data
public class FireFacilityMonitorReportItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 分项唯一键（key_code）。必填。 */
    private String key;

    /** 设施类型。新建分项时必填。 */
    private String facilityType;

    /** 设施总数。 */
    private Integer total;

    /** 在线数。 */
    private Integer online;

    /** 离线数。 */
    private Integer offline;

    /** 故障数。 */
    private Integer fault;

    /** 监测状态：正常/告警/离线/在线。 */
    private String status;

    /** 最近上报时间（yyyy-MM-dd HH:mm:ss）。不传则用上报时刻。 */
    private String lastReportTime;

    /** 监控参数明细（传入即整体替换）。 */
    private List<FireFacilityMonitorReportParam> params;
}
