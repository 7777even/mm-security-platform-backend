package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 常驻视频监控通道（前端 ImportantVideoPanel 的 VideoFeed）。
 * imageKey 指向前端静态图资，图资本身非业务数据。
 */
@Data
public class ImportantVideoFeed {
    /** 通道编码 */
    private String id;

    /** 通道标签 */
    private String label;

    /** 静态图资 key：highAr / tanks / reactor / pipes */
    private String imageKey;

    /** 画面定位（CSS object-position），可空 */
    private String position;

    /** 是否在线 */
    private Boolean online;
}
