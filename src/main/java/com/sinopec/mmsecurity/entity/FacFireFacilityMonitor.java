package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消防设施监测 - 监控卡片实体（对应 H2 表 fac_fire_facility_monitor）。
 * key_code：卡片标识（fas/water/hydrant...），monitor_status：正常/告警/离线。
 */
@Data
@TableName(value = "fac_fire_facility_monitor")
public class FacFireFacilityMonitor implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String keyCode;

    private String facilityType;

    private Integer totalCount;

    private Integer onlineCount;

    private Integer offlineCount;

    private Integer faultCount;

    private String monitorStatus;

    private String lastReportTime;

    private Integer sortNo;
}
