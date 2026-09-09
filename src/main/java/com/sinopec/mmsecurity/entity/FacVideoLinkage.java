package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 视频墙 - 视频联动配置实体（对应 H2 表 fac_video_linkage）。
 * config_code 为前端字符串编码（lk-001...），即契约里的 id。
 */
@Data
@TableName(value = "fac_video_linkage")
public class FacVideoLinkage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configCode;

    private String name;

    private String code;

    private String category;

    private Integer linkageCount;

    private String businessObjects;

    private Integer sortNo;
}
