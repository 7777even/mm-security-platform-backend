package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 台风现场监控视频点位。 */
@Data
@TableName("fac_typhoon_live_video")
public class FacTyphoonLiveVideo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属事件 id */
    private Long incidentId;
    /** 点位编码 */
    private String videoCode;
    /** 点位名称 */
    private String videoLabel;
    /** 关联场景序号 */
    private Integer sceneIndex;
    /** 机位角度 */
    private String angle;
    /** 在线状态 */
    private String statusName;
    /** 设备编码 */
    private String deviceCode;
    /** 排序号 */
    private Integer sortNo;}
