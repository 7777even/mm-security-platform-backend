package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急预案 - 作战力量/资源实体（对应 H2 表 fac_plan_resource）。
 * expected_count / actual_count 以 VARCHAR 落地（前端 mock 为 number | string，输出统一转字符串）。
 */
@Data
@TableName(value = "fac_plan_resource")
public class FacPlanResource implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long instanceId;

    private String resourceCode;

    private String resourceName;

    private String expectedCount;

    private String actualCount;

    private String leaderName;

    private String contactPhone;

    private String duties;

    private Double longitude;

    private Double latitude;

    private Integer sortNo;
}
