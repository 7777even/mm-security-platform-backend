package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 事故案例库列表，与前端 {@code emergency.openapi.json#/EmergencyCaseList} 对齐。
 */
@Data
public class EmergencyCaseList implements Serializable {

    /** 案例条目列表 */
    private List<EmergencyCaseItem> items;
}
