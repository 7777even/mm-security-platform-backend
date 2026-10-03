package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 道闸（门禁卡口）实体。数据来自真实表 fac_gate_control。
 */
@Data
@TableName("fac_gate_control")
public class FacGateControl {
    private Long id;
    private String name;
    private String location;
    private String status;
    private Double longitude;
    private Double latitude;

    /** V101 乐观锁列；status 为设备实时状态，仅读不写（零下行控制红线）。 */
    @Version
    private Long version;
}
