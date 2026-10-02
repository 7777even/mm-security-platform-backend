package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 事故案例库条目，与前端 {@code emergency.openapi.json#/EmergencyCaseItem} 对齐。
 */
@Data
public class EmergencyCaseItem implements Serializable {

    /** 案例 ID */
    private String id;

    /** 事故名称 */
    private String title;

    /** 事故类型 */
    private String accidentType;

    /** 事故地点 */
    private String location;

    /** 发生时间（yyyy-MM-dd HH:mm:ss） */
    private String occurredAt;

    /** 案例摘要 */
    private String summary;

    /** 经验教训 / 启示 */
    private String lessons;
}
