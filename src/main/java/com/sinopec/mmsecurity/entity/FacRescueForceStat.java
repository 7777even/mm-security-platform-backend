package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 消防救援力量分项统计（真实数据源，替代大屏硬编码 rescueStats）。 */
@Data
@TableName("fac_rescue_force_stat")
public class FacRescueForceStat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String label;
    private Integer statCount;
    private String unit;
    private String iconType;
    private Integer sortNo;
}
