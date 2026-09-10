package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 视频联动配置弹窗的四组下拉选项，与前端 {@code video.openapi.json#/VideoLinkageOptions} 对齐。
 * 相机名与相机类型由 fac_video_camera 派生；预置点与业务对象读 fac_video_linkage_option。
 */
@Data
public class VideoLinkageOptions implements Serializable {

    /** 监控器名称（派生自 fac_video_camera.name） */
    private List<String> monitorNames;
    /** 预置点（fac_video_linkage_option，PRESET_POINT） */
    private List<String> presetPoints;
    /** 业务对象分类（派生自 fac_video_camera.camera_type 去重） */
    private List<String> businessObjectCategories;
    /** 业务对象（fac_video_linkage_option，BUSINESS_OBJECT） */
    private List<String> businessObjects;
}
