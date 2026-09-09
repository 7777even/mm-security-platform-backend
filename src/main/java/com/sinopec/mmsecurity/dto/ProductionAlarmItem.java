package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 生产报警项（对应前端 ProductionAlarmItem）。
 * 大屏报警面板与装置区二级页共用；装置区场景下 location / description 由服务端按设施名重写。
 */
@Data
public class ProductionAlarmItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String titleColor;
    private String location;
    private String time;
    private String description;
    private String status;
    private Integer iconIndex;
    private String thumb;
}
