package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 常驻视频监控分组（高空AR / 重点关注区域）。 */
@Data
public class ImportantVideoGroup {
    /** 分组编码 */
    private String id;

    /** 分组标签 */
    private String label;

    /** 分组下通道 */
    private List<ImportantVideoFeed> feeds;
}
