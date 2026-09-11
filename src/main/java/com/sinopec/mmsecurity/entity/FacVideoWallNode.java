package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 视频墙 - 导航节点实体（对应 H2 表 fac_video_wall_node，V37）。
 *
 * <p>单表承载四类节点，node_kind 区分：
 * CATEGORY=监测目标分类（targetTree 根，6 行）/ AREA=厂区视频分区（videoTree 根，5 行）/
 * HIGH_AR=默认高空AR相机（4 行）/ TARGET=监测目标（120 行，parent_code=所属分类、
 * area_code=所属厂区、cam_count=派生摄像头通道数）。</p>
 *
 * <p>摄像头通道（v-{i}-{j}，j=1..camCount）与其到目标的映射由 VideoService 按 TARGET 行
 * 确定性派生，不单独建表；接真实 MDM 设备后改为读设备表。</p>
 */
@Data
@TableName(value = "fac_video_wall_node")
public class FacVideoWallNode implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 前端字符串节点编码（cat-1 / area-2 / t-35 / high-ar-1 ...），即契约里的 id */
    private String nodeCode;

    /** CATEGORY / AREA / HIGH_AR / TARGET */
    private String nodeKind;

    private String label;

    /** TARGET 专用：所属分类的 node_code（cat-N）；其余为 null */
    private String parentCode;

    /** TARGET 专用：所属厂区的 node_code（area-N）；其余为 null */
    private String areaCode;

    /** TARGET 专用：派生摄像头通道数（4~12）；其余为 0 */
    private Integer camCount;

    private Integer sortNo;
}
