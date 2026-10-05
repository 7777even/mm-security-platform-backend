package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

/** 人员识别检索详情（检索结果字段 + 访客/作业扩展），契约源：前端 PersonSearchDetail。 */
@Data
public class PersonSearchDetail implements ZoneAware {

    private Long id;
    private String name;
    private String gate;
    private String status;
    private String date;
    private String gender;
    private String phone;
    private String company;
    private String idNumber;
    private String appointmentNo;
    private String appointmentTime;
    private String visitPurpose;
    private String specialOperation;
    private String operationArea;

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 operationArea（人员识别作业区域），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.operationArea;
    }
}
