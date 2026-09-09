package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 通讯设备实体（对应 H2 表 fac_comm_device）。
 *
 * <p>device_type：broadcast=广播 / phone=电话 / intercom=对讲；
 * group_key/group_label 为该类型下的分组（按装置区/公共区划分），同一分组的行 sort_no 连续。
 * 列名统一回避 H2 保留字（不用 value/command），展示型时间字段以 VARCHAR 原样存放。
 */
@Data
@TableName(value = "fac_comm_device")
public class FacCommDevice implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String deviceCode;

    private String deviceType;

    private String groupKey;

    private String groupLabel;

    private String deviceName;

    private String areaName;

    private String locationName;

    private String deviceStatus;

    private Double longitude;

    private Double latitude;

    private String categoryName;

    private String installTime;

    private String ownerName;

    private String ipAddress;

    private String lastCheckTime;

    private Integer sortNo;
}
