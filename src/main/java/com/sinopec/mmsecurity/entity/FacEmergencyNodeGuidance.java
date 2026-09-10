package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 节点处置指引实体（对应 H2 表 fac_emergency_node_guidance），数据源 V31。
 *
 * <p>reportingChain / roleTasks 等嵌套结构序列化存于 detail_json。</p>
 */
@Data
@TableName(value = "fac_emergency_node_guidance")
public class FacEmergencyNodeGuidance implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 节点 id（'1'…'9'） */
    private String nodeId;

    /** 指引完整结构 JSON */
    private String detailJson;

    private Integer sortNo;
}
