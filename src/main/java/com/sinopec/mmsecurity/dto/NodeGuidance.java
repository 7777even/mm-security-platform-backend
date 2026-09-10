package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 节点处置指引，与前端 {@code NodeGuidance} 对齐（契约 emergency.openapi.json#/NodeGuidance）。 */
@Data
public class NodeGuidance implements Serializable {

    /** 节点 id（'1'…'9'） */
    private String nodeId;

    /** 节点名称（节点 1：接警研判 …） */
    private String nodeName;

    /** 上报链路 */
    private List<NodeGuidanceReportingStep> reportingChain;

    /** 岗位任务分工 */
    private List<NodeGuidanceRoleTask> roleTasks;

    /** 通用注意事项 */
    private String generalNotice;
}
