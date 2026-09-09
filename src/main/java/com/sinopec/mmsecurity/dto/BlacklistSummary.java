package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 安防黑名单聚合：车辆黑名单 + 人员黑名单。 */
@Data
public class BlacklistSummary {
    private List<BlacklistVehicleItem> vehicles;
    private List<BlacklistPersonItem> persons;
}
