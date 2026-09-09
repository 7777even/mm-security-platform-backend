package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防设施监测 - 台账端点响应（设施类型下拉 + 台账条目列表）。 */
@Data
public class FireFacilityLedgerResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> typeOptions;
    private List<FireFacilityLedgerItem> items;
}
