package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 防火巡查标准检查项定义（14 项标准检查表）。 */
@Data
@TableName("fac_fire_patrol_item_def")
public class FacFirePatrolItemDef {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String itemCode;
    private String category;
    private String content;
    private Integer sortNo;
}
