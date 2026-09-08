package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("fac_monitoring_alarm")
public class FacMonitoringAlarm {
    private String id;
    private String title;
    private String detail;
    private String area;
    private String time;
    private String level;
}
