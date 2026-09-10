package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 视频联动配置下拉选项（对应 H2 表 fac_video_linkage_option，V35 建表）。
 * 只存无法从既有实体派生的两类：PRESET_POINT 预置点 / BUSINESS_OBJECT 业务对象。
 * 相机名与相机类型由 fac_video_camera 派生，不落此表（避免冗余数据）。
 */
@Data
@TableName(value = "fac_video_linkage_option")
public class FacVideoLinkageOption implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** PRESET_POINT / BUSINESS_OBJECT */
    private String optionType;

    /** 显示文本 */
    private String optionLabel;

    private Integer sortNo;
}
