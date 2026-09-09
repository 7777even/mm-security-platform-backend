package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急救援资源 - 消防队伍人员（对应 H2 表 fac_brigade_person），按 team_id 关联队伍。
 *
 * <p>注意：分组列名为 person_group、Java 属性名为 personGroup —— group 在 SQL 中为分组关键字，
 * 不直接作为列名；DTO 侧使用 group 字段输出（DTO 不参与 SQL，故安全）。
 */
@Data
@TableName(value = "fac_brigade_person")
public class FacBrigadePerson implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private String personName;

    private String personRole;

    private String personGroup;

    private String phone;

    private String dutyStatus;

    private Integer sortNo;
}
