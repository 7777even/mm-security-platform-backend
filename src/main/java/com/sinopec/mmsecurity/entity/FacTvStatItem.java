package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 工业电视 - 统计项实体（对应 H2 表 fac_tv_stat_item）。
 * item_category：OVERVIEW=视频概览卡片 / MAINTENANCE=维保工单 / EVENT=事件分析。
 */
@Data
@TableName(value = "fac_tv_stat_item")
public class FacTvStatItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String itemCategory;

    private String label;

    private Integer itemCount;

    private String color;

    private String tone;

    private Integer iconIndex;

    private Integer sortNo;
}
