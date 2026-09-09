package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 特殊作业 - 作业票气体检测点实体（对应 H2 表 fac_special_operation_gas）。
 * value_text 列 + valueText 属性：H2 中 value 为保留字（与 FacProductionStat 同规避）。
 */
@Data
@TableName(value = "fac_special_operation_gas")
public class FacSpecialOperationGas implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ticketId;

    private String name;

    @TableField("value_text")
    private String valueText;

    private String statusName;
}
