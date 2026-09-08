package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
}
