package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 巡更/通行轨迹时间轴（真实数据源，替代前端 securityTrackMock 的 timeline）。 */
@Data
@TableName("fac_security_track")
public class FacSecurityTrack {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String trackMode;
    private Long entityId;
    private Integer seqNo;
    private String location;
    private String status;
    private String statusTone;
    private String trackTime;
    private String captureHint;
}
