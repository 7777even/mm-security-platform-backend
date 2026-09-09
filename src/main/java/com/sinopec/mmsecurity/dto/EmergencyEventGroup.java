package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 应急事件分组（与前端 EmergencyEventGroup 契约一致）。 */
@Data
public class EmergencyEventGroup {

    /** 分组标识，取表内 group_code（如 phone / tank / video）。 */
    private String id;

    private String label;

    private List<EmergencyEventItem> events;
}
