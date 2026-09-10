package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 轨迹模式配置（起止点标签），真实数据源，替代前端 securityTrackMock 的 start/end label 常量。 */
@Data
@TableName("fac_security_track_meta")
public class FacSecurityTrackMeta {

    @TableId(value = "track_mode")
    private String trackMode;

    private String startLabel;
    private String endLabel;
}
