package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 应急力量明细参考配置（应急场所 / 医疗机构等无真实台账类别的运营可维护名单）。
 *
 * <p>与 {@link SysEmergencyStrength} 同族，属"运营可维护参考数据"；仅服务
 * {@code GET /emergency/strength} 的明细预览，不参与救援资源台账（{@code /rescue-resources/*}）。
 */
@Data
@TableName("sys_emergency_strength_item")
public class SysEmergencyStrengthItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 资源类别（应急场所 / 医疗机构）。 */
    private String kind;

    /** 明细名称（集结点 / 疏散点 / 医务室 / 协议医院）。 */
    private String name;

    /** 辅助说明（位置 / 类型 / 容量等）。 */
    private String meta;

    private Integer sortNo;
}
