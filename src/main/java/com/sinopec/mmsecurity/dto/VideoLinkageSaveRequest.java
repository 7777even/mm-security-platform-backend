package com.sinopec.mmsecurity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 视频联动配置保存入参（新建与更新共用），与前端 {@code video.openapi.json#/VideoLinkageSaveRequest} 对齐。
 *
 * <p>name/code/category 为 fac_video_linkage 非空列，必填；rules 可空（视为清空联动规则）。
 * linkageCount 与 businessObjects 由服务端按 rules 推导，不接受前端传入以免不一致。</p>
 */
@Data
public class VideoLinkageSaveRequest {

    /** 摄像头名称 */
    @NotBlank(message = "name 必填")
    private String name;

    /** 设备编码 */
    @NotBlank(message = "code 必填")
    private String code;

    /** 设备类型（枪机/球机/高空AR） */
    @NotBlank(message = "category 必填")
    private String category;

    /** 联动规则行；可空，空数组表示该配置无联动规则 */
    @Valid
    private List<VideoLinkageRuleInput> rules;
}
