package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 视频墙 - 视频联动规则实体（对应 H2 表 fac_video_linkage_rule），按 config_code 归属联动配置。 */
@Data
@TableName(value = "fac_video_linkage_rule")
public class FacVideoLinkageRule implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configCode;

    private String presetPoint;

    private String objectCategory;

    private String objectName;

    private Integer sortNo;
}
