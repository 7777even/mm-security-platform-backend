package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防设施监测 - 报警端点响应（报警条目列表）。 */
@Data
public class FireFacilityAlarmResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<FireFacilityAlarmItem> items;
}
