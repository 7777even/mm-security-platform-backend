package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 防恐柱写请求。
 * 字段白名单：仅台账字段 name/zone/longitude/latitude；
 * status 为设备实时状态，仅读不写（零下行控制红线），不在请求体内接受。
 */
@Data
public class BollardWriteRequest {

    @NotBlank(message = "防恐柱名称不能为空")
    private String name;
    private String zone;
    private Double longitude;
    private Double latitude;
}
