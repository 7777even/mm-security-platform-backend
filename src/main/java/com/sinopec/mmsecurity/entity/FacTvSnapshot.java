package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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

    /**
     * 关联告警 id（跨域联动：生产告警详情内嵌关联抓拍）。
     * 为空表示未关联任何告警（如纯巡检抓拍）。
     */
    private Long alarmId;

    /**
     * 关联告警类型：PRODUCTION 生产 / FIRE 消防 / PERIMETER 周界；空表示未关联。
     * 与 alarm_id 配套，便于多告警域共用同一张快照表时区分来源。
     */
    private String alarmType;

    /**
     * 防区编码（关联 sys_zone.zone_code，V86 建立防区维度）。
     * 落库时由监控点位 zone_code 回填（已存在截图）或采集时回查（新截图），与点位防区保持一致。
     */
    @TableField("zone_code")
    private String zoneCode;
}
