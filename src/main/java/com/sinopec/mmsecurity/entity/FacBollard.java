package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 防恐柱实体。数据来自真实表 fac_bollard。
 */
@Data
@TableName("fac_bollard")
public class FacBollard {
    private Long id;
    private String name;
    private String zone;
    private String status;
    private Double longitude;
    private Double latitude;

    /** V101 乐观锁列；status 为设备实时状态，仅读不写（零下行控制红线）。 */
    @Version
    private Long version;
}
