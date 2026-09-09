package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防设施监测 - 维修工单端点响应（工单列表）。 */
@Data
public class FireFacilityWorkOrderResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<FireFacilityWorkOrderItem> items;
}
