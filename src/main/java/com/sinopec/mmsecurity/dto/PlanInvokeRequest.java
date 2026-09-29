package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 一键调用预案入参（激活+广播+留痕，不触达物理设备）。 */
@Data
public class PlanInvokeRequest {
    /** 调用备注（可选，如调用场景/指挥员批示）。 */
    private String note;
}
