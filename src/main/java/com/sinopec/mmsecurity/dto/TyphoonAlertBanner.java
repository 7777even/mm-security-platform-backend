package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 台风响应板气象预警横幅（前端 TyphoonLeftPanel 的 weatherAlertBanners 项，V41）。 */
@Data
public class TyphoonAlertBanner {
    /** 预警级别：橙色预警 / 黄色预警 / 蓝色预警 */
    private String level;

    private String title;

    private String detail;

    /** 展示色调：orange / yellow / blue */
    private String tone;
}
