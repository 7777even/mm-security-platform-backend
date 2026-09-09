package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急救援资源 - 筛选项（对应 H2 表 fac_rescue_option）。
 * option_kind：SQUADRON=中队 / PERSONNEL_ROLE=岗位 / VEHICLE_TYPE=车辆类型 / BRIGADE_AREA=区域。
 * 含“全部中队”“全部岗位”等首项，与前端下拉选项完全一致。
 */
@Data
@TableName(value = "fac_rescue_option")
public class FacRescueOption implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String optionKind;

    private String optionLabel;

    private Integer sortNo;
}
