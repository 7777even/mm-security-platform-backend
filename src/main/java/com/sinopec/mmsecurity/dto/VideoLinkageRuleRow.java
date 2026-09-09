package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 视频墙 - 联动规则行（对应前端 LinkageRuleRow，id 为行内序号 r1/r2...）。 */
@Data
public class VideoLinkageRuleRow {
    private String id;
    private String presetPoint;
    private String objectCategory;
    private String objectName;
}
