package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备台账。物理主键 = 20 位 MDM device_code，不自增。
 */
@Data
@TableName("fac_device")
public class FacDevice {

    /** 20 位 MDM 编码，物理主键 */
    @TableId(type = IdType.INPUT)
    private String deviceCode;

    private String deviceName;
    private String deviceType;
    private String zone;
    private Integer status;
    private Double lat;
    private Double lon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
