package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 安防黑名单 - 车辆条目（字段与前端 blacklistMock.vehicleBlacklist 对齐）。 */
@Data
public class BlacklistVehicleItem {
    private Long id;
    private String plate;
    private String reason;
    private String time;
    private String status;
}
