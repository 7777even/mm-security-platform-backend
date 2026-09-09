package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 应急预案 - 预案实例实体（对应 H2 表 fac_plan_instance），子表以 instance_id 关联。 */
@Data
@TableName(value = "fac_plan_instance")
public class FacPlanInstance implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String planCode;

    private String title;

    private String description;

    private Integer sortNo;
}
