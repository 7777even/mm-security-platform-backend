package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 3D 地图装置区信息牌实体（对应 H2 表 fac_map_zone_sign，V42）。
 * 取代前端 MaomingPetroCesiumMap 硬编码的 plantZonePopups / plantZoneTealTags；
 * sign_kind 区分 ALERT（红色区块信息牌）与 TEAL（青色信息牌）。
 */
@Data
@TableName(value = "fac_map_zone_sign")
public class FacMapZoneSign implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** ALERT / TEAL */
    private String signKind;

    private String title;

    /** 位置说明（仅 ALERT 使用），可空 */
    private String location;

    /** 状态文案 */
    @TableField("status_text")
    private String statusText;

    /** 状态强调级别：alert / normal，可空 */
    private String statusLevel;

    /** 数值文案（仅 TEAL 使用），可空 */
    private String statValue;

    private Integer sortNo;
}
