package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 视频联动规则行入参，与前端 {@code video.openapi.json#/VideoLinkageRuleInput} 对齐。
 *
 * <p>三字段均为 fac_video_linkage_rule 非空列，故必填。</p>
 */
@Data
public class VideoLinkageRuleInput {

    /** 预置位名称 */
    @NotBlank(message = "presetPoint 必填")
    private String presetPoint;

    /** 联动对象分类（重大危险源/生产装置/储罐/库区/摄像头） */
    @NotBlank(message = "objectCategory 必填")
    private String objectCategory;

    /** 联动对象名称 */
    @NotBlank(message = "objectName 必填")
    private String objectName;
}
