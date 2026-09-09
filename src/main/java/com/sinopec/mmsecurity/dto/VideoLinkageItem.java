package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 视频墙 - 视频联动配置项（对应前端 VideoLinkageConfig）。 */
@Data
public class VideoLinkageItem {
    private String id;
    private String name;
    private String code;
    private String category;
    private Integer linkageCount;
    private String businessObjects;
}
