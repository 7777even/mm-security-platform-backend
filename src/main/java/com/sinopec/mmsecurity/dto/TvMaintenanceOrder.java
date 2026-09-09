package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 工业电视 - 维保工单统计项（对应前端 maintenanceOrders，tone: grey/blue/red）。 */
@Data
public class TvMaintenanceOrder {
    private String label;
    private Integer value;
    private String tone;
}
