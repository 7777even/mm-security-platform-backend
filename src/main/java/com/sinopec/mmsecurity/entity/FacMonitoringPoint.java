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
}
