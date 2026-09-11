package com.sinopec.mmsecurity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/** 视频墙 - 导航树节点（目标分类→目标 / 厂区分区→摄像头通道，叶子节点 children 为 null 不输出）。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoWallGroupNode {
    private String id;
    private String label;
    private List<VideoWallGroupNode> children;
}
