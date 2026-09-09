package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 疏散人员名册实体（对应 H2 表 fac_evacuation_person）。
 *
 * <p>route_progress 为疏散路线上的归一化进度（0..1），前端据此把人员沿路线摆放；
 * 演示用的经纬度抖动不落库（与 V15 前端静态几何不建表同一先例）。
 */
@Data
@TableName(value = "fac_evacuation_person")
public class FacEvacuationPerson implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String personName;

    private String orgName;

    private String jobTitle;

    private Double routeProgress;

    private Integer sortNo;
}
