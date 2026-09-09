package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急救援资源 - 消防队伍装备（对应 H2 表 fac_brigade_equipment），按 team_id 关联队伍。
 * 数量列为 item_count（count 为 SQL 聚合函数，不直接作列名）。
 */
@Data
@TableName(value = "fac_brigade_equipment")
public class FacBrigadeEquipment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private String equipName;

    private String category;

    private Integer itemCount;

    private String unit;

    private String equipStatus;

    private String storageLocation;

    private Integer sortNo;
}
