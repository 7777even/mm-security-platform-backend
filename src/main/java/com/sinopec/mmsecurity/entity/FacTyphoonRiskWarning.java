package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 台风风险预警条目。 */
@Data
@TableName("fac_typhoon_risk_warning")
public class FacTyphoonRiskWarning {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属事件 id */
    private Long incidentId;
    /** 预警编码 */
    private String warnCode;
    /** 发布时间 */
    private String warnTime;
    /** 预警类型 */
    private String warnType;
    /** 预警内容 */
    private String content;
    /** 排序号 */
    private Integer sortNo;}
