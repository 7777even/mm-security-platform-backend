package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消防设施监测 - 维护保养记录实体（对应 H2 表 fac_fire_facility_maintenance）。
 * 挂在台账下；report_file 为维保报告附件名，可为空。
 */
@Data
@TableName(value = "fac_fire_facility_maintenance")
public class FacFireFacilityMaintenance implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ledgerId;

    private String recordDate;

    private String contentText;

    private String reportFile;

    private Integer sortNo;
}
