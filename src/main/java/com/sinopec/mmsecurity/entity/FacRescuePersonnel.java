package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急救援资源 - 救援人员（对应 H2 表 fac_rescue_personnel）。
 * 来源：rescuePersonnelMock.ts 的 buildItem 生成结果，人员总量 375 人另取常量。
 */
@Data
@TableName(value = "fac_rescue_personnel")
public class FacRescuePersonnel implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String personName;

    private String squadron;

    private String personRole;

    private Integer sortNo;
}
