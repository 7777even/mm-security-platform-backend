package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("fac_monitoring_point")
public class FacMonitoringPoint {
    private String id;
    private String name;
    private String category;
    private String status;
    private String lastTime;
    private String org;
    private Double longitude;
    private Double latitude;

    /** 乐观锁版本（V105 加列，默认 0），供 @Version 与实时广播使用。 */
    @com.baomidou.mybatisplus.annotation.Version
    private Long version;
}
