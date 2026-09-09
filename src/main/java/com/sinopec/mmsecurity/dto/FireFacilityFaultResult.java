package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防设施监测 - 故障端点响应（故障工单列表）。 */
@Data
public class FireFacilityFaultResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<FireFacilityFaultItem> items;
}
