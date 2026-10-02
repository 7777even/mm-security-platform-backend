package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 事故案例库条目写请求（新增 / 编辑共用）。字段名对齐只读 DTO {@link EmergencyCaseItem}，
 * 前端弹窗可直接 {@code {...row}} 灌入表单，零字段映射。
 */
@Data
public class EmergencyCaseWriteRequest implements Serializable {

    /** 事故名称（必填）。 */
    private String title;

    /** 事故类型（可选）。 */
    private String accidentType;

    /** 事故地点（可选）。 */
    private String location;

    /** 发生时间（可选，yyyy-MM-dd HH:mm:ss）。 */
    private String occurredAt;

    /** 案例摘要（可选）。 */
    private String summary;

    /** 经验教训 / 启示（可选）。 */
    private String lessons;
}
