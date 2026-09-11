package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 台风应急响应板气象预警横幅实体（对应 H2 表 fac_typhoon_alert_banner，V41）。
 * 取代前端 TyphoonLeftPanel 硬编码的 weatherAlertBanners。
 */
@Data
@TableName(value = "fac_typhoon_alert_banner")
public class FacTyphoonAlertBanner implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 预警级别：橙色预警 / 黄色预警 / 蓝色预警 */
    private String warnLevel;

    private String title;

    private String detail;

    /** 展示色调：orange / yellow / blue */
    private String tone;

    private Integer sortNo;
}
