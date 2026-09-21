package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急救援资源 - 救援人员（对应表 fac_rescue_personnel）。
 * <p>救援力量唯一真源：人员以本表为准（V62 起「消防队伍详情」亦由本表按中队归组）。
 * V62 补充 personGroup / phone / dutyStatus 三列（原仅存在于已退役的 fac_brigade_person）。
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

    /** 分组：指挥 / 战斗 / 驾驶 / 通信 / 保障（消防队伍详情分组用）。 */
    private String personGroup;

    private String phone;

    /** 在岗 / 备勤 / 休假。 */
    private String dutyStatus;

    private Integer sortNo;
}
