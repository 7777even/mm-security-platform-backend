package com.sinopec.mmsecurity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 视频控制 - 分组树节点（对应前端 VideoControlTreeNode，叶子节点 children 为 null 不输出）。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoGroupNode {
    private String id;
    private String label;
    private List<VideoGroupNode> children;
}
