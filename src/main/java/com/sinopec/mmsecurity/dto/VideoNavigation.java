package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 视频控制 - 左侧导航聚合：顶部分类（扁平）+ 分组树（应急演练/事件/巡检/重点监控）。 */
@Data
public class VideoNavigation {
    private List<VideoCategoryItem> categories;
    private List<VideoGroupNode> tree;
}
