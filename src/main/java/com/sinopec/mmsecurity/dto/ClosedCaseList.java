package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 近期已结案列表，与前端 {@code emergency.openapi.json#/ClosedCaseList} 对齐。
 * 数据来自 fac_alarm(status=3 CLOSED) 真实聚合。
 */
@Data
public class ClosedCaseList implements Serializable {

    /** 已结案事件列表 */
    private List<ClosedCase> cases;
}
