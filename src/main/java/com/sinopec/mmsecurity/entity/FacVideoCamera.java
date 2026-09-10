package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 视频控制 - 摄像头台账实体（对应 H2 表 fac_video_camera）。
 * status_name 列：H2 保留字规避（status 不可作列名）。
 */
@Data
@TableName(value = "fac_video_camera")
public class FacVideoCamera implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String cameraType;

    private String location;

    /** live / loading / ai */
    @com.baomidou.mybatisplus.annotation.TableField("status_name")
    private String statusName;

    private Boolean hd;

    private Integer thumbIndex;

    private Integer sortNo;

    /** 静态截图字节（演示占位图，dev seeder 生成；后续接真流时替换为媒体网关转发的流地址/截图）。 */
    @com.baomidou.mybatisplus.annotation.TableField("snapshot_bytes")
    private byte[] snapshotBytes;
}
