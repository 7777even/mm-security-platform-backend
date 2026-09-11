package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 常驻视频监控分组实体（对应 H2 表 fac_video_important_group，V40）。
 *
 * <p>取代前端 ImportantVideoPanel 硬编码的 高空AR / 重点关注区域 分组。group_type 区分
 * highAr（高空AR）与 focus（重点关注区域）。</p>
 */
@Data
@TableName(value = "fac_video_important_group")
public class FacVideoImportantGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分组编码：park / refinery / chemical / tank / device / boundary */
    private String groupCode;

    /** 分组标签：园区全景组 / 炼油区高点组 / ... */
    private String groupLabel;

    /** 分组类型：highAr / focus */
    private String groupType;

    private Integer sortNo;
}
