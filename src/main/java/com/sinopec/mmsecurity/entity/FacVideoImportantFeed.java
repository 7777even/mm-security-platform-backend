package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 常驻视频监控通道实体（对应 H2 表 fac_video_important_feed，V40）。
 *
 * <p>image_key 指向前端静态图资（highAr/tanks/reactor/pipes），图资本身非业务数据，由前端按 key 映射。</p>
 */
@Data
@TableName(value = "fac_video_important_feed")
public class FacVideoImportantFeed implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 通道编码：ar-park-1 / tank-1 / boundary-4 ... */
    private String feedId;

    /** 所属分组编码（关联 fac_video_important_group.group_code） */
    private String groupCode;

    /** 通道标签 */
    private String feedLabel;

    /** 静态图资 key：highAr / tanks / reactor / pipes */
    private String imageKey;

    /** 画面定位（CSS object-position），可空 */
    private String position;

    /** 是否在线（0/1） */
    private Boolean online;

    private Integer sortNo;
}
