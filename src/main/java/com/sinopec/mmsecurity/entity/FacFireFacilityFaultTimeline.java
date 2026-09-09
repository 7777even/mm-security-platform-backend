package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消防设施监测 - 故障时间线实体（对应 H2 表 fac_fire_facility_fault_timeline）。
 * 挂在故障工单下：发现故障 / 确认故障 / 生成工单并派发 / 开始维修 / 提交验收 / 验收合格。
 */
@Data
@TableName(value = "fac_fire_facility_fault_timeline")
public class FacFireFacilityFaultTimeline implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long faultId;

    private String eventTime;

    private String operatorName;

    private String actionName;

    private String detailText;

    private Integer sortNo;
}
