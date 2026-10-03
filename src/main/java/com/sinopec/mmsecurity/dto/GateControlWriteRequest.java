package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 道闸（门禁卡口）写请求。
 * 字段白名单：仅台账字段 name/location/longitude/latitude；
 * status 为设备实时状态，仅读不写（零下行控制红线），不在请求体内接受。
 */
@Data
public class GateControlWriteRequest {

    @NotBlank(message = "道闸名称不能为空")
    private String name;
    private String location;
    private Double longitude;
    private Double latitude;
}
