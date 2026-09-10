package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急流程 - 节点联动配置实体（对应 H2 表 fac_node_phase_config）。
 *
 * <p>列表型字段（camera_anchors / right_hidden_tabs / left_hidden_panels）以逗号连接的字符串落库，
 * custom_center 以 "lon,lat" 文本存储；服务层负责与 DTO 的数组结构互转。</p>
 */
@Data
@TableName(value = "fac_node_phase_config")
public class FacNodePhaseConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nodeId;

    private String nodeName;

    /** 镜头中心锚点优先级，逗号连接（如 event_device,alarm_phone_zone,factory_center） */
    private String cameraAnchors;

    /** 自定义镜头中心 "lon,lat"；空表示未配置 */
    private String customCenter;

    private Integer bufferRadiusMeters;

    /** 右侧面板隐藏页签，逗号连接 */
    private String rightHiddenTabs;

    /** 左侧面板隐藏面板，逗号连接 */
    private String leftHiddenPanels;

    private Boolean dutyAutoRoster;

    private Integer sortNo;
}
