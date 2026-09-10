package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 指引上报链路步骤，与前端 {@code NodeGuidance.reportingChain[]} 对齐（契约 #/NodeGuidanceReportingStep）。 */
@Data
public class NodeGuidanceReportingStep implements Serializable {

    /** 步骤序号 */
    private Integer step;

    /** 上报方（角色·姓名） */
    private String fromRole;

    /** 接收方（角色·姓名） */
    private String toRole;

    /** 上报方式（对讲机频道 / 应急热线 / 广播 …） */
    private String method;

    /** 上报要点 */
    private String notice;
}
