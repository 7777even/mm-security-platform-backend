package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 台风应急响应板指令实体（对应 H2 表 fac_typhoon_command，V41）。
 * 取代前端 TyphoonLeftPanel 硬编码的 weatherCommands / temporaryCommands；
 * command_kind 区分 plan（预案指令）与 temporary（临时指令）。
 */
@Data
@TableName(value = "fac_typhoon_command")
public class FacTyphoonCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 指令编码（前端 key）：w1..w5 / t1..t2 */
    private String cmdCode;

    /** plan / temporary */
    private String commandKind;

    private String groupLabel;

    private String name;

    private String target;

    /** 已完成 / 执行中 / 待执行 */
    private String cmdStatus;

    private String cmdTime;

    private String detail;

    private Integer sortNo;
}
