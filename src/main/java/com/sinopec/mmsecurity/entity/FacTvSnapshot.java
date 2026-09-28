package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 工业电视录像截图采集入库记录（V78）。
 * 支撑「设备采集 → 录像截图入库 → 实时广播 → 大屏上屏」闭环链路。
 */
@Data
@TableName("fac_tv_snapshot")
public class FacTvSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 监控点位编码（关联 fac_tv_monitor.monitor_code）。 */
    private String monitorCode;
    /** 点位名称（落库时按 monitor_code 回查或取上报值）。 */
    private String monitorName;
    /** 采集时刻（设备上报，字符串避免时区/方言差异）。 */
    private String captureTime;
    /** 事件类型：人员闯入/烟火检测/区域入侵/手动抓拍；缺省=设备自动。 */
    private String eventType;
    /** 复核状态：PENDING 待确认 / ACKED 已确认。 */
    private String reviewStatus;
    /** 来源：DEVICE 设备采集 / MANUAL 手工。 */
    private String source;
    /** 截图 JPEG 字节。 */
    private byte[] snapshotBytes;
    /** 入库时刻（服务端生成，yyyy-MM-dd HH:mm:ss）。 */
    private String createdAt;
    private Integer sortNo;
}
